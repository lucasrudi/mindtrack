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
class TelegramServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    @Test
    void shouldSkipSendWhenTelegramTokenIsBlank() throws Exception {
        TelegramService service = new TelegramService(propertiesWithTelegramToken("   "), httpClient);

        service.sendMessage("12345", "Hello");

        verify(httpClient, never()).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void shouldSkipSendWhenTelegramTokenIsNull() throws Exception {
        TelegramService service = new TelegramService(propertiesWithTelegramToken(null), httpClient);

        service.sendMessage("12345", "Hello");

        verify(httpClient, never()).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void shouldSendTelegramMessageWithEscapedPayload() throws Exception {
        TelegramService service = new TelegramService(propertiesWithTelegramToken("bot-token"), httpClient);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        service.sendMessage("chat-\"1", "Line 1\nTabbed\t\\\\quoted\"");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());

        HttpRequest request = requestCaptor.getValue();
        assertEquals(URI.create("https://api.telegram.org/botbot-token/sendMessage"), request.uri());
        assertEquals("application/json", request.headers().firstValue("Content-Type").orElseThrow());
        assertEquals(
                "{\"chat_id\":\"chat-\\\"1\",\"text\":\"Line 1\\nTabbed\\t\\\\\\\\quoted\\\"\",\"parse_mode\":\"Markdown\"}",
                requestBody(request));
    }

    @Test
    void shouldTreatNon200TelegramResponseAsHandled() throws Exception {
        TelegramService service = new TelegramService(propertiesWithTelegramToken("bot-token"), httpClient);
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("bad request");
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        service.sendMessage("12345", null);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
        assertEquals(
                "{\"chat_id\":\"12345\",\"text\":\"\",\"parse_mode\":\"Markdown\"}",
                requestBody(requestCaptor.getValue()));
    }

    @Test
    void shouldRestoreInterruptedFlagWhenTelegramSendIsInterrupted() throws Exception {
        TelegramService service = new TelegramService(propertiesWithTelegramToken("bot-token"), httpClient);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new InterruptedException("interrupted"));

        assertFalse(Thread.currentThread().isInterrupted());

        service.sendMessage("12345", "Hello");

        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void shouldSwallowTelegramIoExceptions() throws Exception {
        TelegramService service = new TelegramService(propertiesWithTelegramToken("bot-token"), httpClient);
        when(httpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("network down"));

        service.sendMessage("12345", "Hello");

        verify(httpClient).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    private MessagingProperties propertiesWithTelegramToken(String token) {
        MessagingProperties properties = new MessagingProperties();
        properties.getTelegram().setBotToken(token);
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
