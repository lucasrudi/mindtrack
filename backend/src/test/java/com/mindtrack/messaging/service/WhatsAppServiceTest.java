package com.mindtrack.messaging.service;

import com.mindtrack.messaging.config.MessagingProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    @Test
    void shouldSkipSendWhenWhatsappTokenIsBlank() throws Exception {
        WhatsAppService service = new WhatsAppService(properties("   ", "phone-id"), httpClient);

        service.sendMessage("34600111222", "Hello");

        verify(httpClient, never()).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void shouldSkipSendWhenWhatsappTokenIsNull() throws Exception {
        WhatsAppService service = new WhatsAppService(properties(null, "phone-id"), httpClient);

        service.sendMessage("34600111222", "Hello");

        verify(httpClient, never()).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void shouldSendWhatsappMessageWithEscapedPayloadAndAuthHeader() throws Exception {
        WhatsAppService service = new WhatsAppService(properties("api-token", "phone-id"), httpClient);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        service.sendMessage("34600\"111222", "Line 1\nTabbed\t\\\\quoted\"");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());

        HttpRequest request = requestCaptor.getValue();
        assertEquals(URI.create("https://graph.facebook.com/v18.0/phone-id/messages"), request.uri());
        assertEquals("application/json", request.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("Bearer api-token", request.headers().firstValue("Authorization").orElseThrow());
        assertEquals(
                "{\"messaging_product\":\"whatsapp\",\"to\":\"34600\\\"111222\","
                        + "\"type\":\"text\",\"text\":{\"body\":\"Line 1\\nTabbed\\t\\\\\\\\quoted\\\"\"}}",
                requestBody(request));
    }

    @Test
    void shouldTreatNon200WhatsappResponseAsHandled() throws Exception {
        WhatsAppService service = new WhatsAppService(properties("api-token", "phone-id"), httpClient);
        when(response.statusCode()).thenReturn(400);
        when(response.body()).thenReturn("bad request");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        service.sendMessage("34600111222", null);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        assertEquals(
                "{\"messaging_product\":\"whatsapp\",\"to\":\"34600111222\","
                        + "\"type\":\"text\",\"text\":{\"body\":\"\"}}",
                requestBody(requestCaptor.getValue()));
    }

    @Test
    void shouldRestoreInterruptedFlagWhenWhatsappSendIsInterrupted() throws Exception {
        WhatsAppService service = new WhatsAppService(properties("api-token", "phone-id"), httpClient);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new InterruptedException("interrupted"));

        assertFalse(Thread.currentThread().isInterrupted());

        service.sendMessage("34600111222", "Hello");

        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void shouldSwallowWhatsappIoExceptions() throws Exception {
        WhatsAppService service = new WhatsAppService(properties("api-token", "phone-id"), httpClient);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("network down"));

        service.sendMessage("34600111222", "Hello");

        verify(httpClient).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    private MessagingProperties properties(String token, String phoneNumberId) {
        MessagingProperties properties = new MessagingProperties();
        properties.getWhatsapp().setApiToken(token);
        properties.getWhatsapp().setPhoneNumberId(phoneNumberId);
        return properties;
    }

    private String requestBody(HttpRequest request) throws Exception {
        HttpRequest.BodyPublisher publisher = request.bodyPublisher().orElseThrow();
        BodyCollector subscriber = new BodyCollector();
        publisher.subscribe(subscriber);
        return subscriber.body();
    }

    private static final class BodyCollector implements Flow.Subscriber<ByteBuffer> {

        private final StringBuilder body = new StringBuilder();
        private final CompletableFuture<Void> done = new CompletableFuture<>();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            body.append(StandardCharsets.UTF_8.decode(item.duplicate()));
        }

        @Override
        public void onError(Throwable throwable) {
            done.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            done.complete(null);
        }

        private String body() throws Exception {
            done.get();
            return body.toString();
        }
    }
}
