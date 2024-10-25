package xsolla;

import io.github.cdimascio.dotenv.Dotenv;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Base64;

@WebServlet("/server-side")
public class ServerSide extends HttpServlet {
    private final String projectId;
    private final String apiKey;
    private final String backendPort;
    private final String webhookSecretKey;

    public ServerSide() {
        Dotenv dotenv = Dotenv.load();
        this.projectId = dotenv.get("XSOLLA_PROJECT_ID");
        this.apiKey = dotenv.get("XSOLLA_API_KEY");
        this.backendPort = dotenv.get("BACKEND_PORT");
        this.webhookSecretKey = dotenv.get("XSOLLA_WEBHOOK_SECRET_KEY");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("xsollaProjectId", this.projectId);
        request.setAttribute("xsollaApiKey", this.apiKey);
        request.setAttribute("backendPort", this.backendPort);
        request.setAttribute("xsollaWebhookSecretKey", this.webhookSecretKey);

        request.getRequestDispatcher("/server-side.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String projectId = request.getParameter("projectId");
        String apiKey = request.getParameter("apiKey");
        String body = request.getParameter("body");

        try {
            new com.google.gson.JsonParser().parse(body);
        } catch (Exception e) {
            request.setAttribute("apiResponse", "Json in body is not valid");
            request.getRequestDispatcher("/server-side.jsp").forward(request, response);
            return;
        }

        try {
            String urlString = "https://store.xsolla.com/api/v3/project/" + projectId + "/admin/payment/token";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String auth = Base64.getEncoder().encodeToString((projectId + ":" + apiKey).getBytes());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder apiResponse = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    apiResponse.append(scanner.nextLine());
                }
            }

            String responseBody = apiResponse.toString();
            String token = extractToken(responseBody);

            boolean isSandbox = body.contains("\"sandbox\": true");
            String baseUrl = isSandbox ? "https://sandbox-secure.xsolla.com/paystation4/?token=" : "https://secure.xsolla.com/paystation4/?token=";
            String fullUrl = baseUrl + token;

            String formattedLink = "<a href=\"" + fullUrl + "\" target=\"_blank\">" + fullUrl + "</a>";
            String completeResponse = "<h3>API Response:</h3><pre>" + escapeHtml(responseBody) + "</pre>" + "<h3>Payment Link:</h3>" + formattedLink;

            request.setAttribute("apiResponse", completeResponse);
            request.setAttribute("body", body);
            request.setAttribute("xsollaProjectId", this.projectId);
            request.setAttribute("xsollaApiKey", this.apiKey);
            request.setAttribute("xsollaWebhookSecretKey", this.webhookSecretKey);

        } catch (Exception e) {
            request.setAttribute("apiResponse", "Error processing request: " + e.getMessage());
        }

        request.getRequestDispatcher("/server-side.jsp").forward(request, response);
    }

    private String extractToken(String json) {
        int tokenStart = json.indexOf("\"token\":\"") + 9;
        int tokenEnd = json.indexOf("\"", tokenStart);
        return json.substring(tokenStart, tokenEnd);
    }

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
