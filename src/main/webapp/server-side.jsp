<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>

<html>
<head>
    <title>Xsolla Integration</title>
</head>

<body>
    <fieldset>
        <legend>Create token (server side)</legend>

        <form action="server-side" method="post">
            <label>
                Project id
                <input name="projectId" type="text" value="<%= request.getAttribute("xsollaProjectId") %>">
            </label>
            <br><br>

            <label>
                Api key
                <input name="apiKey" type="text" value="<%= request.getAttribute("xsollaApiKey") %>">
            </label>
            <br><br>

            <label>
                Webhook secret key
                <input name="webhookSecretKey" type="text" value="<%= request.getAttribute("xsollaWebhookSecretKey") %>">
            </label>
            <br><br>

            <label>JSON body</label>
            <textarea cols="90" rows="40" name="body"><%= request.getAttribute("body") != null ? request.getAttribute("body") :
            "{\n  \"sandbox\": true,\n  \"user\": {\n    \"id\": {\n      \"value\": \"user-id\"\n    },\n    \"country\": {\n      \"value\": \"US\"\n    }\n  },\n  \"purchase\": {\n    \"items\": [\n      {\n        \"sku\": \"mysku01\",\n        \"quantity\": 1\n      }\n    ]\n  }\n}" %></textarea>
            <br><br>

            <a href="https://developers.xsolla.com/api/igs-bb/operation/admin-create-payment-token/" target="_blank">Docs</a>
            <br><br>

            <input type="submit" value="Submit">
        </form>

        <div id="result">
            <%
                String apiResponse = (String) request.getAttribute("apiResponse");
                if (apiResponse != null) {
            %>
                <%= apiResponse %>
            <%
                }
            %>
        </div>
    </fieldset>
</body>
</html>