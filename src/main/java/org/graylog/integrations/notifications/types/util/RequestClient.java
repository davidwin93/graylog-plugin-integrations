package org.graylog.integrations.notifications.types.util;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.notifications.TemporaryEventNotificationException;
import org.graylog.integrations.notifications.types.microsoftTeams.TeamsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class RequestClient {
    private static final Logger LOG = LoggerFactory.getLogger(TeamsClient.class);
    private final OkHttpClient httpClient;


    @Inject
    public RequestClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }


    /**
     * @param message
     * @param webhookUrl
     * @throws TemporaryEventNotificationException - thrown for network or timeout type issues
     * @throws PermanentEventNotificationException - thrown with bad webhook url, authentication error type issues
     */
    public void send(String message, String webhookUrl) throws TemporaryEventNotificationException, PermanentEventNotificationException {

        final Request request = new Request.Builder()
                .url(webhookUrl)
                .post(RequestBody.create(MediaType.parse(APPLICATION_JSON), message))
                .build();

        LOG.debug("Posting to webhook url <{}> the payload is <{}>",
                webhookUrl,
                message);

        try (final Response r = httpClient.newCall(request).execute()) {
            if (!r.isSuccessful()) {
                throw new PermanentEventNotificationException(
                        "Expected successful HTTP response [2xx] but got [" + r.code() + "]. " + webhookUrl);
            }
        } catch (IOException e) {
            throw new TemporaryEventNotificationException("Unable to send the Message. " + e.getMessage());
        }
    }
}
