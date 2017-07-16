package com.infusion.jiramigrationtool.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicRestClient {

    private static final String ZAPI_PERMISSIONS_ERROR_MSG = "Error validating logged-in users permission against Zephyr custom permissions.";

    private static final Logger logger = LoggerFactory.getLogger(BasicRestClient.class.getName());

    final String USER_PERM_ERROR_HINT = "User does not have permissions.  Zephyr permissions may not have been set up properly."
            + "Go to Manage Add-ons, Zephyr, configure.  Enable 'Enable Zephyr Permission Scheme'.  Re-index."
            + "Then assign the Zephyr QA permission to the user.";

    final String username;
    final String password;

    public BasicRestClient(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public BasicRestResponse get(final String urlString) {
        try {
            final URL url = new URL(urlString);
            final HttpURLConnection urlConnection = getConnection(url, username, password);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            final StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return new BasicRestResponse(urlConnection.getResponseCode(), sb.toString());
        } catch (final Exception e) {
            logger.info("{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public BasicRestResponse postPut(final String urlString, final String jsonString, final RestRequestMethod method) {
        try {
            final URL url = new URL(urlString);
            final HttpURLConnection urlConnection = getConnection(url, username, password);
            urlConnection.setRequestMethod(method.title());
            urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf8");

            urlConnection.setDoOutput(true);
            final DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            final String testString = jsonString;
            wr.writeBytes(testString);
            wr.flush();
            wr.close();

            final int responseCode = urlConnection.getResponseCode();
            logger.debug("Sending 'POST' request to URL : " + url);
            logger.debug("Post parameters : " + testString);
            logger.debug("Response Code : " + responseCode);

            final StringBuffer response = new StringBuffer();
            if (responseCode != 200) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                if (response.toString().contains(ZAPI_PERMISSIONS_ERROR_MSG)) {
                    logger.warn(USER_PERM_ERROR_HINT);
                }
            } else {
                final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }

            logger.info("RESPONSE:" + response.toString());
            return new BasicRestResponse(urlConnection.getResponseCode(), response.toString());
        } catch (final Exception e) {
            logger.info("{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private HttpURLConnection getConnection(final URL url, final String username, final String password) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        final String authString = username + ":" + password;
        final String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        return urlConnection;
    }

}
