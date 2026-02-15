package com.mindtrack.journal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindtrack.journal.dto.JournalEntryRequest;
import com.mindtrack.journal.dto.JournalEntryResponse;
import com.mindtrack.journal.service.JournalService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JournalService journalService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldCreateEntry() throws Exception {
        JournalEntryRequest request = createRequest();
        JournalEntryResponse response = createResponse(1L);
        when(journalService.create(eq(1L), any(JournalEntryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/journal")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Good day"))
                .andExpect(jsonPath("$.mood").value(7))
                .andExpect(jsonPath("$.tags", hasSize(2)));
    }

    @Test
    void shouldRejectCreateWithoutEntryDate() throws Exception {
        JournalEntryRequest request = new JournalEntryRequest();
        request.setTitle("No date");

        mockMvc.perform(post("/api/journal")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateWithInvalidMood() throws Exception {
        JournalEntryRequest request = createRequest();
        request.setMood(11);

        mockMvc.perform(post("/api/journal")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListEntries() throws Exception {
        JournalEntryResponse r1 = createResponse(1L);
        JournalEntryResponse r2 = createResponse(2L);
        when(journalService.listByUser(1L)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/journal")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldListEntriesByDateRange() throws Exception {
        JournalEntryResponse r1 = createResponse(1L);
        when(journalService.listByUserAndDateRange(
                eq(1L), eq(LocalDate.of(2025, 1, 1)), eq(LocalDate.of(2025, 1, 31))))
                .thenReturn(List.of(r1));

        mockMvc.perform(get("/api/journal")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldGetEntryById() throws Exception {
        JournalEntryResponse response = createResponse(1L);
        when(journalService.getByIdAndUser(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/journal/1")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Good day"));
    }

    @Test
    void shouldReturn404WhenEntryNotFound() throws Exception {
        when(journalService.getByIdAndUser(999L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/journal/999")
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateEntry() throws Exception {
        JournalEntryRequest request = createRequest();
        request.setTitle("Updated");
        JournalEntryResponse response = createResponse(1L);
        response.setTitle("Updated");
        when(journalService.update(eq(1L), eq(1L), any(JournalEntryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/journal/1")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistent() throws Exception {
        when(journalService.update(eq(999L), eq(1L), any(JournalEntryRequest.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/journal/999")
                        .with(csrf())
                        .with(authentication(mockAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteEntry() throws Exception {
        when(journalService.delete(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/journal/1")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistent() throws Exception {
        when(journalService.delete(999L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/journal/999")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldToggleSharing() throws Exception {
        JournalEntryResponse response = createResponse(1L);
        response.setSharedWithTherapist(true);
        when(journalService.toggleSharing(1L, 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/journal/1/share")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sharedWithTherapist").value(true));
    }

    @Test
    void shouldReturn404WhenTogglingNonExistent() throws Exception {
        when(journalService.toggleSharing(999L, 1L)).thenReturn(null);

        mockMvc.perform(patch("/api/journal/999/share")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/journal"))
                .andExpect(status().isUnauthorized());
    }

    private JournalEntryRequest createRequest() {
        JournalEntryRequest request = new JournalEntryRequest();
        request.setEntryDate(LocalDate.of(2025, 1, 15));
        request.setTitle("Good day");
        request.setContent("Today was productive");
        request.setMood(7);
        request.setTags(List.of("gratitude", "work"));
        request.setSharedWithTherapist(false);
        return request;
    }

    private JournalEntryResponse createResponse(Long id) {
        JournalEntryResponse response = new JournalEntryResponse();
        response.setId(id);
        response.setEntryDate(LocalDate.of(2025, 1, 15));
        response.setTitle("Good day");
        response.setContent("Today was productive");
        response.setMood(7);
        response.setTags(List.of("gratitude", "work"));
        response.setSharedWithTherapist(false);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}
