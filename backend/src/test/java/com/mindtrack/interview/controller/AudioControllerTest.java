package com.mindtrack.interview.controller;

import com.mindtrack.interview.dto.AudioUploadResponse;
import com.mindtrack.interview.service.AudioService;
import com.mindtrack.interview.service.InterviewService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterviewService interviewService;

    @MockitoBean
    private AudioService audioService;

    private static UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void shouldUploadAudioFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "recording.mp3", "audio/mpeg", new byte[1024]);
        AudioUploadResponse response = new AudioUploadResponse(
                "http://example.com/audio.mp3", null, LocalDateTime.now().plusDays(7));
        when(audioService.uploadAudio(eq(1L), eq(1L), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/interviews/1/audio")
                        .file(file)
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.audioUrl").value("http://example.com/audio.mp3"))
                .andExpect(jsonPath("$.audioExpiresAt").exists());
    }

    @Test
    void shouldGetAudioUrl() throws Exception {
        AudioUploadResponse response = new AudioUploadResponse(
                "http://example.com/audio.mp3", "Hello world",
                LocalDateTime.now().plusDays(7));
        when(audioService.getAudio(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/interviews/1/audio")
                        .with(authentication(mockAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audioUrl").value("http://example.com/audio.mp3"))
                .andExpect(jsonPath("$.transcriptionText").value("Hello world"));
    }

    @Test
    void shouldReturn404WhenNoAudioExists() throws Exception {
        when(audioService.getAudio(1L, 1L)).thenReturn(null);

        mockMvc.perform(get("/api/interviews/1/audio")
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAudio() throws Exception {
        when(audioService.deleteAudio(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/interviews/1/audio")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentAudio() throws Exception {
        when(audioService.deleteAudio(1L, 1L)).thenReturn(false);

        mockMvc.perform(delete("/api/interviews/1/audio")
                        .with(csrf())
                        .with(authentication(mockAuth())))
                .andExpect(status().isNotFound());
    }
}
