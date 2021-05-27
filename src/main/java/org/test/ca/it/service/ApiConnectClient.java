package org.mcgill.ca.it.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.mcgill.ca.it.log.LogFileGenerator;
import org.mcgill.ca.it.main.RabbitMQConfiguration;
import org.mcgill.ca.it.security.SSLContextManager;

import javax.net.ssl.HttpsURLConnection;
import java.util.Map;

import static org.mcgill.ca.it.security.KeyCipherUtils.decrypt;

/**
 * Author: Sanjeeva
 * Description : This class used to configure with  IBM API Connect from Java application.
 * It will receive the response from the workday based on response it will delete and keep the message in the RabbitMQ
 */
public class ApiConnectClient {
    private String clientId;
    private String clientSecret;
    private String workdayUser;
    private String workdayPassword;
    private String foapalUser;
    private String foapalPassword;

    private ApiConnectClient() {
    }

    private String getClientId() {
        return clientId;
    }

    private void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private String getClientSecret() {
        return clientSecret;
    }

    private void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    private String getWorkdayUser() {
        return workdayUser;
    }

    private void setWorkdayUser(String workdayUser) {
        this.workdayUser = workdayUser;
    }

    private String getWorkdayPassword() {
        return workdayPassword;
    }

    private void setWorkdayPassword(String workdayPassword) {
        this.workdayPassword = workdayPassword;
    }

    private String getFoapalUser() {
        return foapalUser;
    }

    private void setFoapalUser(String foapalUser) {
        this.foapalUser = foapalUser;
    }

    private String getFoapalPassword() {
        return foapalPassword;
    }

    private void setFoapalPassword(String foapalPassword) {
        this.foapalPassword = foapalPassword;
    }

    public static boolean connectIbmapi(String message, RabbitMQConfiguration configuration, LogFileGenerator files, boolean isHires) throws UnableToSetConfigurationException {

        Map<String, Object> m = configuration.getConfiguration();

        ApiConnectClient client = new ApiConnectClient();

        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        switch (configuration.getEnv()) {
            case "D":
                client.setClientId(decrypt(configuration.getApiUsername(), m.get("dac").toString()));
                client.setClientSecret(decrypt(configuration.getApiUsername(), m.get("das").toString()));
                client.setWorkdayUser(decrypt(configuration.getApiPassword(), m.get("dwu").toString()));
                client.setFoapalUser(decrypt(configuration.getApiPassword(), m.get("dwp").toString()));
                client.setWorkdayPassword(decrypt(configuration.getApiPassword(), m.get("fdwu").toString()));
                client.setFoapalPassword(decrypt(configuration.getApiPassword(), m.get("fdwp").toString()));
                break;
            case "Q":
                client.setClientId(decrypt(configuration.getApiUsername(), m.get("qac").toString()));
                client.setClientSecret(decrypt(configuration.getApiUsername(), m.get("qas").toString()));
                client.setWorkdayUser(decrypt(configuration.getApiPassword(), m.get("qwu").toString()));
                client.setWorkdayPassword(decrypt(configuration.getApiPassword(), m.get("qwp").toString()));
                client.setFoapalUser(decrypt(configuration.getApiPassword(), m.get("fqwu").toString()));
                client.setFoapalPassword(decrypt(configuration.getApiPassword(), m.get("fqwp").toString()));
                break;
            case "I":
                client.setClientId(decrypt(configuration.getApiUsername(), m.get("iac").toString()));
                client.setClientSecret(decrypt(configuration.getApiUsername(), m.get("ias").toString()));
                client.setWorkdayUser(decrypt(configuration.getApiPassword(), m.get("iwu").toString()));
                client.setWorkdayPassword(decrypt(configuration.getApiPassword(), m.get("iwp").toString()));
                client.setFoapalUser(decrypt(configuration.getApiPassword(), m.get("fiwu").toString()));
                client.setFoapalPassword(decrypt(configuration.getApiPassword(), m.get("fiwp").toString()));
                break;
            case "P":
                client.setClientId(decrypt(configuration.getApiUsername(), m.get("piac").toString()));
                client.setClientSecret(decrypt(configuration.getApiUsername(), m.get("pias").toString()));
                client.setWorkdayUser(decrypt(configuration.getApiPassword(), m.get("pwu").toString()));
                client.setWorkdayPassword(decrypt(configuration.getApiPassword(), m.get("pwp").toString()));
                client.setFoapalUser(decrypt(configuration.getApiPassword(), m.get("pdwu").toString()));
                client.setFoapalPassword(decrypt(configuration.getApiPassword(), m.get("pdwp").toString()));
                break;
            default:
                throw new UnableToSetConfigurationException();
        }

        boolean check = false;

        OkHttpClient httpClient = SSLContextManager.getHttpClient(configuration.getTimeout());

        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");

        RequestBody body = RequestBody.create(mediaType, message);

        Request request;

        String CLIENT_ID = "x-ibm-client-id";

        if (isHires) {
            request = new Request.Builder()
                    .url(configuration.getApiUrl())
                    .post(body)
                    .addHeader(CLIENT_ID, client.getClientId())
                    .addHeader("x-ibm-client-secret", client.getClientSecret())
                    .addHeader("content-type", "application/json")
                    .addHeader("accept", "application/json")
                    .addHeader("wd-username", client.getWorkdayUser())
                    .addHeader("wd-userpwrd", client.getWorkdayPassword())
                    .build();
        } else {
            request = new Request.Builder()
                    .url(configuration.getApiFolpalUrl())
                    .post(body)
                    .addHeader(CLIENT_ID, client.getClientId())
                    .addHeader("x-ibm-client-secret", client.getClientSecret())
                    .addHeader("content-type", "application/json")
                    .addHeader("accept", "application/json")
                    .addHeader("username", client.getFoapalUser())
                    .addHeader("userpwrd", client.getFoapalPassword())
                    .build();
        }

        try {

            long start = System.nanoTime();

            Response response = httpClient.newCall(request).execute();
            long end = System.nanoTime();

            if (response.code() == 200) {

                assert response.body() != null;
                files.successFile(message, response.code(), response.body().string(), (end - start) / 1000000);

                check = true;

            } else if (!response.isSuccessful()) {

                assert response.body() != null;
                files.errorFile(message, response.code(), response.body().string(), (end - start) / 1000000);
                response.close();
            }

        } catch (Exception e) {
            files.debugFile(e.getMessage());
        }
        return check;
    }
}
