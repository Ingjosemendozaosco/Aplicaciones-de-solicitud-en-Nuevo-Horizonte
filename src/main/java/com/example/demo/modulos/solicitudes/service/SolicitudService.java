package com.example.demo.modulos.solicitudes.service;

import com.example.demo.config.CorreoService;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import com.example.demo.modulos.solicitudes.dto.SolicitudRequestDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import com.example.demo.modulos.solicitudes.mapper.SolicitudMapper;
import com.example.demo.modulos.solicitudes.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class SolicitudService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain");

    private static final String EXTENSIONES_PERMITIDAS_REGEX =
            ".*\\.(jpg|jpeg|png|gif|webp|pdf|doc|docx|xls|xlsx|txt)$";

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMapper solicitudMapper;
    private final CorreoService correoService;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            UsuarioRepository usuarioRepository,
                            SolicitudMapper solicitudMapper,
                            CorreoService correoService) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudMapper = solicitudMapper;
        this.correoService = correoService;
    }

    @Transactional
    public SolicitudResponseDTO crear(SolicitudRequestDTO dto, String username) throws IOException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));
        String numero = "SOL-" + Year.now().getValue() + "-"
                + String.format("%05d", solicitudRepository.siguienteNumero());
        Solicitud solicitud = solicitudMapper.toEntity(dto, usuario, numero);
        if (dto.isEliminarArchivo()) {
            solicitud.setNombreArchivo(null);
            solicitud.setTipoArchivo(null);
            solicitud.setContenidoArchivo(null);
        } else {
            adjuntarArchivo(solicitud, dto.getArchivo());
        }
        return solicitudMapper.toResponse(solicitudRepository.save(solicitud));
    }

    private void adjuntarArchivo(Solicitud solicitud, MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return;
        }
        String contentType = archivo.getContentType();
        String nombre = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "adjunto";
        boolean tipoValido = contentType != null && TIPOS_PERMITIDOS.contains(contentType);
        boolean extensionValida = nombre.matches(EXTENSIONES_PERMITIDAS_REGEX);
        if (!tipoValido && !extensionValida) {
            throw new IllegalArgumentException("Archivo no válido. Solo se permiten imágenes (jpg, png, gif, webp) o documentos (pdf, doc, docx, xls, xlsx, txt).");
        }
        solicitud.setNombreArchivo(nombre);
        solicitud.setTipoArchivo(contentType != null ? contentType : "application/octet-stream");
        solicitud.setContenidoArchivo(archivo.getBytes());
    }

    @Transactional(readOnly = true)
    public Solicitud buscarEntidadPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarTodas() {
        return solicitudRepository.findAll().stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorArea(String area) {
        return solicitudRepository.findByAreaSolicitante(area).stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorEstado(EstadoSolicitud estado) {
        return solicitudRepository.findByEstado(estado).stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarPorUsuario(String username) {
        return solicitudRepository.findByUsuarioUsername(username).stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDTO buscarPorId(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
        return solicitudMapper.toResponse(solicitud);
    }

    @Transactional
    public SolicitudResponseDTO decidir(Long id, EstadoSolicitud estado, String comentario) {
        if (estado == null || estado == EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("El estado debe ser APROBADA o RECHAZADA");
        }
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
        solicitud.setEstado(estado);
        solicitud.setComentarioAdmin(comentario);
        solicitud.setFechaAprobacionRechazo(LocalDateTime.now());
        Solicitud guardada = solicitudRepository.save(solicitud);
        notificarDecision(guardada);
        return solicitudMapper.toResponse(guardada);
    }

    private void notificarDecision(Solicitud solicitud) {
        Usuario usuario = solicitud.getUsuario();
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return;
        }
        boolean aprobada = solicitud.getEstado() == EstadoSolicitud.APROBADA;
        String colorEstado = aprobada ? "#16a34a" : "#dc2626";
        String textoEstado = aprobada ? "APROBADA" : "RECHAZADA";
        String comentario = (solicitud.getComentarioAdmin() == null || solicitud.getComentarioAdmin().isBlank())
                ? "Sin comentarios."
                : solicitud.getComentarioAdmin();
        String fechaSolicitud = solicitud.getFechaSolicitud() != null
                ? solicitud.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "—";
        String fechaDecision = solicitud.getFechaAprobacionRechazo() != null
                ? solicitud.getFechaAprobacionRechazo().format(FORMATO_FECHA)
                : "—";
        String adjunto = (solicitud.getNombreArchivo() == null || solicitud.getNombreArchivo().isBlank())
                ? "Sin adjunto"
                : solicitud.getNombreArchivo();

        StringBuilder filas = new StringBuilder();
        fila(filas, "Número de solicitud", solicitud.getNumeroSolicitud());
        fila(filas, "Solicitante", solicitud.getNombreSolicitante());
        fila(filas, "Usuario", usuario.getUsername());
        fila(filas, "Área solicitante", solicitud.getAreaSolicitante());
        fila(filas, "Fecha de solicitud", fechaSolicitud);
        fila(filas, "Recurso o módulo solicitado", solicitud.getRecursoModulo());
        fila(filas, "Responsable de aprobación", solicitud.getResponsableAprobacion());
        fila(filas, "Motivo", solicitud.getMotivo());
        fila(filas, "Archivo adjunto", adjunto);
        fila(filas, "Fecha de decisión", fechaDecision);

        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f3f4f6;padding:24px;">
                  <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;">
                    <div style="background:#2563eb;padding:20px 24px;color:#ffffff;">
                      <h2 style="margin:0;font-size:18px;">Nuevo Horizonte</h2>
                      <p style="margin:4px 0 0;font-size:13px;opacity:.9;">Sistema de Gestión de Solicitudes</p>
                    </div>
                    <div style="padding:24px;">
                      <p style="margin:0 0 12px;color:#111827;">Hola <strong>%s</strong>,</p>
                      <p style="margin:0 0 16px;color:#374151;">
                        Tu solicitud <strong>%s</strong> ha sido evaluada por el administrador.
                      </p>
                      <p style="display:inline-block;margin:0 0 20px;padding:6px 14px;border-radius:999px;background:%s;color:#ffffff;font-weight:bold;font-size:13px;">
                        %s
                      </p>
                      <h3 style="margin:0 0 8px;font-size:14px;color:#111827;border-bottom:1px solid #e5e7eb;padding-bottom:8px;">
                        Detalle completo de la solicitud
                      </h3>
                      <table style="width:100%%;border-collapse:collapse;font-size:14px;color:#374151;">
                        %s
                      </table>
                      <p style="margin:20px 0 6px;color:#6b7280;font-size:13px;">Comentario del administrador:</p>
                      <div style="background:#f9fafb;border-left:4px solid %s;border-radius:8px;padding:12px;color:#111827;font-size:14px;">%s</div>
                    </div>
                    <div style="padding:16px 24px;background:#f9fafb;color:#9ca3af;font-size:12px;text-align:center;">
                      Este es un mensaje automático, por favor no responder.
                    </div>
                  </div>
                </div>
                """.formatted(
                usuario.getUsername(),
                solicitud.getNumeroSolicitud(),
                colorEstado,
                textoEstado,
                filas.toString(),
                colorEstado,
                escaparHtml(comentario));
        String asunto = "Solicitud " + solicitud.getNumeroSolicitud() + " " + textoEstado;
        correoService.enviarHtml(usuario.getEmail(), asunto, html);
    }

    private void fila(StringBuilder sb, String etiqueta, String valor) {
        String contenido = (valor == null || valor.isBlank()) ? "—" : valor;
        sb.append("<tr>")
                .append("<td style=\"padding:7px 8px 7px 0;color:#6b7280;vertical-align:top;white-space:nowrap;\">")
                .append(escaparHtml(etiqueta)).append("</td>")
                .append("<td style=\"padding:7px 0;color:#111827;\">")
                .append(escaparHtml(contenido)).append("</td>")
                .append("</tr>");
    }

    private String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }
}
