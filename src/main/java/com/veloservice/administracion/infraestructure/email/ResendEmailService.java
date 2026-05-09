package com.veloservice.administracion.infraestructure.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResendEmailService {
    private final RestClient restClient;
    private final String apiKey;
    private final String from;
    private final String resetPasswordUrl;
    private final long resetExpirationMs;

    public ResendEmailService(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.base-url:https://api.resend.com}") String baseUrl,
            @Value("${resend.from:}") String from,
            @Value("${app.reset-password-url:http://localhost:3000/reset-password}") String resetPasswordUrl,
            @Value("${jwt.reset-expiration:900000}") long resetExpirationMs) {
        this.apiKey = apiKey;
        this.from = from;
        this.resetPasswordUrl = resetPasswordUrl;
        this.resetExpirationMs = resetExpirationMs;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void sendPasswordResetEmail(String to, String nombre, String token) {
        validateConfig(to, token);

        String resetUrl = UriComponentsBuilder.fromUriString(resetPasswordUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
        long minutes = Math.max(1L, resetExpirationMs / 60000L);

        String subject = "Restablecer contrasena";
        String greeting = StringUtils.hasText(nombre) ? "Hola " + nombre + "," : "Hola,";
        String safeGreeting = escapeHtml(greeting);

        String text = greeting
                + "\n\nRecibimos una solicitud para restablecer tu contrasena."
                + "\nUsa este enlace (valido por " + minutes + " minutos):\n"
                + resetUrl
                + "\n\nSi no lo solicitaste, ignora este correo.";

        String html = "<p>" + safeGreeting + "</p>"
                + "<p>Recibimos una solicitud para restablecer tu contrasena.</p>"
                + "<p><a href=\"" + escapeHtml(resetUrl) + "\">Restablecer contrasena</a></p>"
                + "<p>Este enlace es valido por " + minutes + " minutos.</p>"
                + "<p>Si no lo solicitaste, ignora este correo.</p>";

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", from);
        payload.put("to", to);
        payload.put("subject", subject);
        payload.put("text", text);
        payload.put("html", html);

        try {
            restClient.post()
                    .uri("/emails")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw new IllegalArgumentException("Email invalido");
            }
            throw ex;
        }
    }

    private void validateConfig(String to, String token) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Resend API key no configurada");
        }
        if (!StringUtils.hasText(from)) {
            throw new IllegalStateException("Resend from no configurado");
        }
        if (!StringUtils.hasText(resetPasswordUrl)) {
            throw new IllegalStateException("Reset password URL no configurada");
        }
        if (!StringUtils.hasText(to)) {
            throw new IllegalArgumentException("Email no registrado");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token de restablecimiento no valido");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
