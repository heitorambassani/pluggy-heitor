package ai.pluggy.quickstart;

import java.io.IOException;
import java.util.List;

import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.auth.AuthenticationHelper;
import ai.pluggy.client.request.CreateConnectTokenRequest;
import ai.pluggy.client.response.Connector;
import ai.pluggy.exception.PluggyException;
import okhttp3.*;

public class QuickstartApplication {


    public static void main(String[] args) throws IOException, PluggyException {
        // Client configurations
        final String clientId = "f5b9a1e7-d4ff-41a9-b978-e9915f279ec9";
        final String clientSecret = "85bd5853-03bd-4083-81bf-4f0c9cb9a909";
        final String webhookUrl = "https://pluggy-java.requestcatcher.com";

        final String accessToken = getAccessToken(clientId, clientSecret, webhookUrl);

        String widgetURL = getWidgetURL(clientId, clientSecret, accessToken);
        System.out.println("Widget url: " + widgetURL);

        // https://pluggyheitor.requestcatcher.com/
        storeWebhookInfo(clientId, clientSecret, webhookUrl, accessToken);
    }

    private static String getApiKey(String clientId, String clientSecret) throws PluggyException, IOException {
        // Create API KEY
        OkHttpClient clientAPIKEY = new OkHttpClient();

        MediaType mediaTypeAPIKEY = MediaType.parse("application/json");
        String bodyAPIKEY = "{\"clientId\":\"" + clientId + "\",\"clientSecret\":\"" + clientSecret + "\"}";
        RequestBody requestBodyAPIKEY = RequestBody.create(mediaTypeAPIKEY, bodyAPIKEY);
        Request requestAPIKEY = new Request.Builder()
                .url("https://api.pluggy.ai/auth")
                .post(requestBodyAPIKEY)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .build();

        Response responseAPIKEY = clientAPIKEY.newCall(requestAPIKEY).execute();
        String apiKey = AuthenticationHelper.extractApiKeyFromResponse(responseAPIKEY);
        return apiKey;
    }

    private static void storeWebhookInfo(String clientId, String clientSecret, String webhookUrl, String accessToken) throws IOException, PluggyException {
       String apiKey = getApiKey(clientId, clientSecret);

        // Webhook
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"event\":\"all\"}");
        Request request = new Request.Builder()
                .url("https://api.pluggy.ai/webhooks")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("X-API-KEY", apiKey)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }

    private static String getWidgetURL(String clientId, String clientSecret, String accessToken) throws IOException {
        final String institutionToConnect = "Banco do Brasil";

        PluggyClient pluggyClient = PluggyClient.builder()
                .clientIdAndSecret(clientId, clientSecret)
                .build();

        // Fetch connectors
        List<Connector> connectors = pluggyClient.service()
                .getConnectors()
                .execute()
                .body()
                .getResults();

        Connector institution = connectors.stream()
                .filter(connector -> institutionToConnect.equals(connector.getName()))
                .findFirst().orElse(null);

        String timestamp = String.valueOf(System.currentTimeMillis());

        String str = "https://connect.pluggy.ai/?connect_token=" +
                accessToken +
                "&with_sandbox=true" +
                "&itemId=" +
                institution.getId() +
                "&events=OPEN%2CSUBMITTED_CONSENT%2CSELECTED_INSTITUTION" +
                "&timestamp=" + timestamp +
                "&connectorId=" +
                institution.getId() +
                "&connector_name=" +
                institution.getName().replace(' ', '+');

        return str;
    }

    private static String getAccessToken(String clientId, String clientSecret, String webhookUrl) throws IOException {
        PluggyClient pluggyClient = PluggyClient.builder()
                .clientIdAndSecret(clientId, clientSecret)
                .build();

        // Fetch access token
        CreateConnectTokenRequest createConnectTokenRequest = new CreateConnectTokenRequest(webhookUrl, clientId);
        return pluggyClient.service()
                .createConnectToken(createConnectTokenRequest)
                .execute()
                .body()
                .getAccessToken();
    }

}
