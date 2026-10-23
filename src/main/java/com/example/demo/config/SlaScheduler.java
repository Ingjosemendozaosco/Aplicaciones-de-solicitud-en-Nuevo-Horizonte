package com.example.demo.config;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import com.example.demo.modulos.solicitudes.repository.SolicitudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SlaScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaScheduler.class);
    private static final int DIAS_LIMITE = 3;

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final CorreoService correoService;

    public SlaScheduler(SolicitudRepository solicitudRepository,
                        UsuarioRepository usuarioRepository,
                        CorreoService correoService) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.correoService = correoService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void recordarPendientesViejas() {
        LocalDateTime limite = LocalDateTime.now().minusDays(DIAS_LIMITE);
        List<Solicitud> pendientes = solicitudRepository
                .findByEstadoAndFechaCreacionBefore(EstadoSolicitud.PENDIENTE, limite)
                .stream()
                .filter(s -> s.getFechaRecordatorioSla() == null
                        || s.getFechaRecordatorioSla().isBefore(limite))
                .toList();
        if (pendientes.isEmpty()) {
            return;
        }

        List<Usuario> administradores = usuarioRepository
                .findByActivoTrueAndUsuarioRolesRolNombre("Administración");
        if (administradores.isEmpty()) {
            log.warn("Hay {} solicitudes pendientes sin administradores para notificar", pendientes.size());
            return;
        }

        StringBuilder filas = new StringBuilder();
        for (Solicitud solicitud : pendientes) {
            filas.append("<tr>")
                    .append("<td style=\"padding:7px 8px 7px 0;color:#6b7280;\">")
                    .append(escapar(solicitud.getNumeroSolicitud())).append("</td>")
                    .append("<td style=\"padding:7px 0;color:#111827;\">")
                    .append(escapar(solicitud.getNombreSolicitante())).append("</td>")
                    .append("<td style=\"padding:7px 0;color:#111827;\">")
                    .append(escapar(solicitud.getAreaSolicitante())).append("</td>")
                    .append("<td style=\"padding:7px 0;color:#111827;\">")
                    .append(escapar(solicitud.getRecursoModulo())).append("</td>")
                    .append("<td style=\"padding:7px 0;color:#dc2626;font-weight:bold;\">")
                    .append(solicitud.getFechaCreacion().toLocalDate()).append("</td>")
                    .append("</tr>");
            solicitud.setFechaRecordatorioSla(LocalDateTime.now());
        }
        solicitudRepository.saveAll(pendientes);

        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f3f4f6;padding:24px;">
                  <div style="max-width:640px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;">
                    <div style="background:#2563eb;padding:20px 24px;color:#ffffff;">
                      <h2 style="margin:0;font-size:18px;">Nuevo Horizonte</h2>
                      <p style="margin:4px 0 0;font-size:13px;opacity:.9;">Recordatorio de solicitudes pendientes</p>
                    </div>
                    <div style="padding:24px;">
                      <p style="margin:0 0 16px;color:#374151;">
                        Hay <strong>%d</strong> solicitud(es) con más de %d días pendientes de decisión:
                      </p>
                      <table style="width:100%%;border-collapse:collapse;font-size:13.5px;color:#374151;">
                        <thead>
                          <tr style="border-bottom:2px solid #e5e7eb;">
                            <th style="padding:8px 8px 8px 0;text-align:left;color:#6b7280;">N°</th>
                            <th style="padding:8px;text-align:left;color:#6b7280;">Solicitante</th>
                            <th style="padding:8px;text-align:left;color:#6b7280;">Área</th>
                            <th style="padding:8px;text-align:left;color:#6b7280;">Detalle</th>
                            <th style="padding:8px;text-align:left;color:#6b7280;">Creada</th>
                          </tr>
                        </thead>
                        <tbody>%s</tbody>
                      </table>
                      <p style="margin:20px 0 0;color:#6b7280;font-size:13px;">
                        Ingrese al sistema para aprobar o rechazar estas solicitudes.
                      </p>
                    </div>
                    <div style="padding:16px 24px;background:#f9fafb;color:#9ca3af;font-size:12px;text-align:center;">
                      Este es un mensaje automático, por favor no responder.
                    </div>
                  </div>
                </div>
                """.formatted(pendientes.size(), DIAS_LIMITE, filas.toString());

        for (Usuario admin : administradores) {
            correoService.enviarHtml(admin.getEmail(),
                    "Recordatorio: " + pendientes.size() + " solicitud(es) pendientes hace " + DIAS_LIMITE + " días",
                    html);
        }
        log.info("Recordatorio SLA enviado por {} solicitudes pendientes", pendientes.size());
    }

    private String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
