package xsolla;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@WebServlet("/webhook")
public class WebhookServlet extends HttpServlet {
    private final String secretKey;

    public WebhookServlet() {
        Dotenv dotenv = Dotenv.load();
        this.secretKey = dotenv.get("XSOLLA_WEBHOOK_SECRET_KEY");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        String signature = request.getHeader("Authorization");

        try {
            if (!verifySignature(body, signature)) {
                response.setStatus(400);
                response.getWriter().write("{\"error\": {\"code\": \"INVALID_SIGNATURE\",\"message\": \"Invalid signature\"}}");
                return;
            }

            JSONObject message = new JSONObject(body);
            JSONObject result = handleWebhook(message);

            response.setStatus(result.getInt("code"));
            response.getWriter().write(result.getString("body"));

        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"error\": {\"code\": \"INTERNAL_ERROR\",\"message\": \"" + e.getMessage() + "\"}}");
        }
    }

    private boolean verifySignature(String body, String authorizationHeader) throws Exception {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Signature ")) {
            throw new Exception("\"Authorization\" header not found or invalid in Xsolla webhook request.");
        }

        String clientSignature = authorizationHeader.substring(10); // Skip "Signature "
        String serverSignature = sha1(body + this.secretKey);

        return clientSignature.equals(serverSignature);
    }

    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm is not available", e);
        }
    }

    private JSONObject handleWebhook(JSONObject message) {
        String notificationType = message.getString("notification_type");
        JSONObject response = new JSONObject();

        switch (notificationType) {
            case "user_validation":
                JSONObject user = message.getJSONObject("user");
                if (user.getString("id").startsWith("test_xsolla")) {
                    response.put("body", "{\"error\": {\"code\": \"INVALID_USER\",\"message\": \"Invalid user\"}}");
                    response.put("code", 400);
                } else {
                    response.put("body", "");
                    response.put("code", 200);
                }
                break;
            case "payment":
                response.put("body", "{\"status\": \"Payment processed successfully\"}");
                response.put("code", 200);
                break;
            case "refund":
                response.put("body", "");
                response.put("code", 200);
                break;
            default:
                response.put("body", "");
                response.put("code", 200);
        }

        return response;
    }
}