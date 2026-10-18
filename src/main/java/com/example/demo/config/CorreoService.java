package com.example.demo.config;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final Resend resend;
    private final String remitente;
    private final String overrideTo;
    private final boolean habilitado;

    public CorreoService(@Value("${app.resend.api-key:}") String apiKey,
                         @Value("${app.resend.from:onboarding@resend.dev}") String remitente,
                         @Value("${app.resend.override-to:}") String overrideTo,
                         @Value("${app.resend.enabled:false}") boolean habilitado) {
        this.remitente = remitente;
        this.overrideTo = overrideTo;
        this.habilitado = habilitado;
        this.resend = (apiKey != null && !apiKey.isBlank()) ? new Resend(apiKey) : null;
    }

    public void enviarHtml(String destinatario, String asunto, String html) {
        if (!habilitado) {
            log.info("Correo deshabilitado (app.resend.enabled=false). Destino={}, asunto={}", destinatario, asunto);
            return;
        }
        if (resend == null) {
            log.warn("No se envió el correo: falta configurar app.resend.api-key");
            return;
        }
        String destino = (overrideTo != null && !overrideTo.isBlank()) ? overrideTo : destinatario;
        if (destino == null || destino.isBlank()) {
            log.warn("No se envió el correo: destinatario vacío");
            return;
        }
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(remitente)
                    .to(destino)
                    .subject(asunto)
                    .html(html)
                    .build();
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Correo enviado a {} (id={})", destino, data.getId());
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
        }
    }
}
