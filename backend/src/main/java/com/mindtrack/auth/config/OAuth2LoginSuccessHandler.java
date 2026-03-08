package com.mindtrack.auth.config;

import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Handles successful OAuth2 login by creating/updating the user and redirecting with a JWT token.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            JwtService jwtService,
            @Value("${mindtrack.auth.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User authUser = (OAuth2User) authentication.getPrincipal();

        String googleId = authUser.getAttribute("sub");
        String email = authUser.getAttribute("email");
        String name = authUser.getAttribute("name");

        User user = userService.findOrCreateFromGoogle(googleId, email, name);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().getName());

        LOG.info("OAuth2 login success for user id={}", user.getId());

        String redirectUrl = frontendUrl + "/login?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
