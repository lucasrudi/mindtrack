package com.mindtrack.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.ai.dto.ChatRequest;
import com.mindtrack.ai.dto.ChatResponse;
import com.mindtrack.ai.model.ConversationType;
import com.mindtrack.ai.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldReturnAiResponse() throws Exception {
        ChatResponse response = new ChatResponse(1L, 10L, "Hello! How are you?", false, 150);
        when(conversationService.chat(eq(1L), any(ChatRequest.class))).thenReturn(response);

        ChatRequest request = new ChatRequest(null, "Hi there", ConversationType.COACHING);

        mockMvc.perform(post("/api/ai/chat")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello! How are you?"))
                .andExpect(jsonPath("$.cached").value(false))
                .andExpect(jsonPath("$.tokensUsed").value(150));
    }

    @Test
    void shouldReturnCachedResponse() throws Exception {
        ChatResponse response = new ChatResponse(1L, 10L, "Cached summary", true, 1200);
        when(conversationService.chat(eq(1L), any(ChatRequest.class))).thenReturn(response);

        ChatRequest request = new ChatRequest(null, "Summarize my week", ConversationType.SESSION_SUMMARY);

        mockMvc.perform(post("/api/ai/chat")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cached").value(true));
    }

    @Test
    void shouldRejectEmptyMessage() throws Exception {
        ChatRequest request = new ChatRequest(null, "", ConversationType.COACHING);

        mockMvc.perform(post("/api/ai/chat")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListConversations() throws Exception {
        when(conversationService.listConversations(1L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/ai/conversations")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn404ForMissingConversation() throws Exception {
        when(conversationService.getConversation(eq(999L), eq(1L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/ai/conversations/999")
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401ForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/ai/conversations"))
                .andExpect(status().isUnauthorized());
    }
}
