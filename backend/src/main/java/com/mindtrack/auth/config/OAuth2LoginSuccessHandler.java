package com.mindtrack.auth.config;

import com.mindtrack.auth.service.JwtService;
import com.mindtrack.auth.service.UserService;
import com.mindtrack.common.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Handles successful OAuth2 login by creating/updating the user, issuing a JWT as an
 * HttpOnly cookie, and redirecting to the frontend login route.
 *
 * <p>The token is delivered via a Set-Cookie header (HttpOnly, Secure, SameSite=Strict)
 * rather than a URL query parameter to prevent token leakage in browser history,
 * server logs, and Referer headers (C-1).
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    public static final String AUTH_COOKIE_NAME = "auth_token";

    private final UserService userService;
    private final JwtService jwtService;
    private final String frontendUrl;
    private final boolean cookieSecure;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            JwtService jwtService,
            @Value("${mindtrack.auth.frontend-url:http://localhost:3000}") String frontendUrl,
            @Value("${mindtrack.auth.cookie-secure:true}") boolean cookieSecure) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.cookieSecure = cookieSecure;
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

        LOG.info("OAuth2 login success for userId={}", user.getId());

        ResponseCookie jwtCookie = ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getExpirationMs()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        response.sendRedirect(frontendUrl + "/login");
    }
}
