package com.example.demo.modulos.solicitudes.service;

import com.example.demo.config.CorreoService;
import com.example.demo.modulos.auditoria.service.AuditoriaService;
import com.example.demo.modulos.documentos.entity.Documento;
import com.example.demo.modulos.documentos.repository.DocumentoRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.TipoVigencia;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.RolPermisoRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.ColoresRol;
import com.example.demo.modulos.notificaciones.service.NotificacionService;
import com.example.demo.modulos.solicitudes.dto.AreaDisponibleDTO;
import com.example.demo.modulos.solicitudes.dto.ModuloActualDTO;
import com.example.demo.modulos.solicitudes.dto.ModuloDisponibleDTO;
import com.example.demo.modulos.solicitudes.dto.RolModuloDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudPropuestaDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudRequestDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.NivelAcceso;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import com.example.demo.modulos.solicitudes.entity.SolicitudAccesoDetalle;
import com.example.demo.modulos.solicitudes.entity.SolicitudPropuestaDetalle;
import com.example.demo.modulos.solicitudes.entity.TipoSolicitud;
import com.example.demo.modulos.solicitudes.mapper.SolicitudMapper;
import com.example.demo.modulos.solicitudes.repository.SolicitudRepository;
import com.example.demo.modulos.ModuloCatalogo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final RolPermisoRepository rolPermisoRepository;
    private final DocumentoRepository documentoRepository;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;
    private final String urlPublica;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            UsuarioRepository usuarioRepository,
                            SolicitudMapper solicitudMapper,
                            CorreoService correoService,
                            RolPermisoRepository rolPermisoRepository,
                            DocumentoRepository documentoRepository,
                            NotificacionService notificacionService,
                            AuditoriaService auditoriaService,
                            @Value("${app.public-url:http://localhost:8080}") String urlPublica) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudMapper = solicitudMapper;
        this.correoService = correoService;
        this.rolPermisoRepository = rolPermisoRepository;
        this.documentoRepository = documentoRepository;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
        this.urlPublica = urlPublica;
    }

    @Transactional
    public SolicitudResponseDTO crear(SolicitudRequestDTO dto, String username) throws IOException {
        aplicarDefaults(dto, username);
        validarPorTipo(dto);
        validarEvidenciaObligatoria(dto.getArchivo());
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
        Solicitud guardada = solicitudRepository.save(solicitud);
        auditoriaService.registrar("CREAR", "Solicitud", guardada.getId(),
                "Solicitud " + numero + " creada por " + username + ".");
        notificacionService.crearParaRol("Administración",
                "Nueva solicitud " + numero,
                dto.getNombreSolicitante() + " (" + dto.getAreaSolicitante()
                        + ") envió una solicitud de "
                        + (dto.getTipoSolicitud() == TipoSolicitud.ACCESO
                                ? "acceso a módulos." : "información."),
                "/solicitudes/" + guardada.getId());
        return solicitudMapper.toResponse(guardada);
    }

    private void validarEvidenciaObligatoria(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Adjunta evidencia que respalde la solicitud antes de enviarla.");
        }
    }

    @Transactional
    public SolicitudResponseDTO guardarBorrador(SolicitudRequestDTO dto, String username) throws IOException {
        aplicarDefaults(dto, username);
        // No validamos por tipo para permitir borradores incompletos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));
        String numero = "SOL-" + Year.now().getValue() + "-"
                + String.format("%05d", solicitudRepository.siguienteNumero());
        Solicitud solicitud = solicitudMapper.toEntity(dto, usuario, numero);
        solicitud.setEstado(EstadoSolicitud.BORRADOR);
        if (dto.isEliminarArchivo()) {
            solicitud.setNombreArchivo(null);
            solicitud.setTipoArchivo(null);
            solicitud.setContenidoArchivo(null);
        } else {
            adjuntarArchivo(solicitud, dto.getArchivo());
        }
        Solicitud guardada = solicitudRepository.save(solicitud);
        auditoriaService.registrar("CREAR", "Solicitud", guardada.getId(),
                "Borrador " + numero + " guardado por " + username + ".");
        // No enviamos notificación para borradores
        return solicitudMapper.toResponse(guardada);
    }

    private void aplicarDefaults(SolicitudRequestDTO dto, String username) {
        if (dto.getNombreSolicitante() == null || dto.getNombreSolicitante().isBlank()) {
            dto.setNombreSolicitante(username);
        }
        if (dto.getFechaSolicitud() == null) {
            dto.setFechaSolicitud(java.time.LocalDate.now());
        }
        if (dto.getResponsableAprobacion() == null || dto.getResponsableAprobacion().isBlank()) {
            dto.setResponsableAprobacion("Administración");
        }
        if (dto.getAreaSolicitante() == null || dto.getAreaSolicitante().isBlank()) {
            throw new IllegalArgumentException("Selecciona el área solicitante.");
        }
        if (dto.getTipoSolicitud() != TipoSolicitud.ACCESO
                && dto.getTipoInformacion() != null && !dto.getTipoInformacion().isBlank()) {
            String tipo = dto.getTipoInformacion().trim();
            String detalle = dto.getRecursoModulo() == null ? "" : dto.getRecursoModulo().trim();
            if ("OTRO".equalsIgnoreCase(tipo)) {
                dto.setRecursoModulo(detalle);
            } else {
                dto.setRecursoModulo(detalle.isBlank() ? tipo : tipo + " - " + detalle);
            }
        }
    }

    private void validarPorTipo(SolicitudRequestDTO dto) {
        if (dto.getTipoSolicitud() == TipoSolicitud.ACCESO) {
            if (dto.getModulosAcceso() == null || dto.getModulosAcceso().isEmpty()) {
                throw new IllegalArgumentException("Selecciona al menos un módulo al que necesitas acceso.");
            }
            if (dto.getNivelesModulo() != null) {
                boolean nivelInvalido = dto.getNivelesModulo().values().stream()
                        .anyMatch(v -> v != null
                                && !v.isBlank()
                                && !"LECTURA".equalsIgnoreCase(v)
                                && !"EDICION".equalsIgnoreCase(v));
                if (nivelInvalido) {
                    throw new IllegalArgumentException("El nivel de acceso solo puede ser Lectura o Edición.");
                }
            }
            if (dto.getFechaFinAcceso() != null
                    && !dto.getFechaFinAcceso().isAfter(java.time.LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de vigencia del acceso debe ser posterior a hoy.");
            }
        } else {
            if ((dto.getTipoInformacion() == null || dto.getTipoInformacion().isBlank())
                    && (dto.getRecursoModulo() == null || dto.getRecursoModulo().isBlank())) {
                throw new IllegalArgumentException("Indica qué información necesitas solicitar.");
            }
            if ("OTRO".equalsIgnoreCase(dto.getTipoInformacion())
                    && (dto.getRecursoModulo() == null || dto.getRecursoModulo().isBlank())) {
                throw new IllegalArgumentException("Especifica la información que necesitas.");
            }
            if (dto.getRecursoModulo() == null || dto.getRecursoModulo().isBlank()) {
                dto.setRecursoModulo(dto.getTipoInformacion());
            }
        }
    }

    private void adjuntarArchivo(Solicitud solicitud, MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return;
        }
        validarArchivoPermitido(archivo);
        solicitud.setNombreArchivo(nombreDe(archivo));
        solicitud.setTipoArchivo(tipoDe(archivo));
        solicitud.setContenidoArchivo(archivo.getBytes());
    }

    private void validarArchivoPermitido(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        String contentType = archivo.getContentType();
        String nombre = nombreDe(archivo);
        boolean tipoValido = contentType != null && TIPOS_PERMITIDOS.contains(contentType);
        boolean extensionValida = nombre.matches(EXTENSIONES_PERMITIDAS_REGEX);
        if (!tipoValido && !extensionValida) {
            throw new IllegalArgumentException("Archivo no válido. Solo se permiten imágenes (jpg, png, gif, webp) o documentos (pdf, doc, docx, xls, xlsx, txt).");
        }
    }

    private String nombreDe(MultipartFile archivo) {
        return archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "archivo";
    }

    private String tipoDe(MultipartFile archivo) {
        return archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream";
    }

    @Transactional(readOnly = true)
    public Solicitud buscarEntidadPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarTodas() {
        return solicitudRepository.findAll().stream()
                .map(solicitudMapper::toResponse)
                .sorted(Comparator.comparing(SolicitudResponseDTO::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
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
        return solicitudRepository.findByUsuarioUsername(username).stream()
                .map(solicitudMapper::toResponse)
                .sorted(Comparator.comparing(SolicitudResponseDTO::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarFiltrado(EstadoSolicitud estado, String area,
                                                     LocalDate desde, LocalDate hasta,
                                                     String username, String texto) {
        String patron = null;
        if (texto != null && !texto.isBlank()) {
            patron = "%" + texto.trim().toLowerCase() + "%";
        }
        return solicitudRepository.buscarFiltrado(estado,
                        area != null && !area.isBlank() ? area.trim() : null,
                        desde, hasta, username, patron).stream()
                .map(solicitudMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarAreas() {
        return solicitudRepository.areasDistintas();
    }

    @Transactional(readOnly = true)
    public SolicitudRequestDTO paraDuplicar(Long id, String username, boolean administracion) {
        Solicitud solicitud = buscarEntidadPorId(id);
        boolean propietario = solicitud.getUsuario() != null
                && username.equals(solicitud.getUsuario().getUsername());
        if (!propietario && !administracion) {
            throw new IllegalArgumentException("Solo el solicitante o Administración pueden duplicar esta solicitud.");
        }
        SolicitudRequestDTO dto = new SolicitudRequestDTO();
        dto.setTipoSolicitud(solicitud.getTipoSolicitud());
        dto.setNombreSolicitante(solicitud.getNombreSolicitante());
        dto.setAreaSolicitante(solicitud.getAreaSolicitante());
        dto.setMotivo(solicitud.getMotivo());
        dto.setResponsableAprobacion(solicitud.getResponsableAprobacion());
        dto.setFechaSolicitud(LocalDate.now());
        dto.setPermanente(solicitud.isPermanente());
        dto.setFechaInicioAcceso(solicitud.getFechaInicioAcceso() != null
                ? solicitud.getFechaInicioAcceso() : LocalDate.now());
        dto.setFechaFinAcceso(solicitud.getFechaFinAcceso());
        if (solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO) {
            List<String> modulos = new ArrayList<>();
            Map<String, String> niveles = new LinkedHashMap<>();
            solicitud.getDetalles().forEach(detalle -> {
                modulos.add(detalle.getModulo());
                niveles.put(detalle.getModulo(),
                        detalle.getNivel() == NivelAcceso.EDICION ? "EDICION" : "LECTURA");
            });
            dto.setModulosAcceso(modulos);
            dto.setNivelesModulo(niveles);
        } else {
            dto.setTipoInformacion("OTRO");
            dto.setRecursoModulo(solicitud.getRecursoModulo());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDTO buscarPorId(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
        return solicitudMapper.toResponse(solicitud);
    }

    @Transactional
    public void marcarVisto(Long id) {
        Solicitud solicitud = buscarEntidadPorId(id);
        if (!solicitud.isVisto()) {
            solicitud.setVisto(Boolean.TRUE);
        }
    }

    @Transactional
    public SolicitudResponseDTO decidir(Long id, EstadoSolicitud estado, String comentario,
                                        Long idDocumento) {
        if (estado == null || estado == EstadoSolicitud.PENDIENTE
                || estado == EstadoSolicitud.EN_REVISION) {
            throw new IllegalArgumentException("El estado debe ser APROBADA o RECHAZADA");
        }
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));
        if (estado == EstadoSolicitud.APROBADA
                && solicitud.getTipoSolicitud() == TipoSolicitud.INFORMACION) {
            if (idDocumento == null) {
                solicitud.setNombreInforme(null);
                solicitud.setIdDocumentoInforme(null);
                solicitud.setTipoInforme(null);
                solicitud.setContenidoInforme(null);
                if (comentario == null || comentario.isBlank()) {
                    comentario = "El documento solicitado no está disponible.";
                }
            } else {
                Documento documento = documentoRepository.findById(idDocumento)
                        .filter(Documento::isActivo)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "El documento seleccionado no está disponible."));
                solicitud.setIdDocumentoInforme(documento.getId());
                solicitud.setNombreInforme(documento.getNombreArchivo() != null
                        ? documento.getNombreArchivo() : documento.getNombre());
                solicitud.setTipoInforme(documento.getTipoArchivo() != null
                        ? documento.getTipoArchivo() : "application/octet-stream");
                solicitud.setContenidoInforme(documento.getContenido());
            }
        }
        solicitud.setEstado(estado);
        solicitud.setComentarioAdmin(comentario);
        solicitud.setFechaAprobacionRechazo(LocalDateTime.now());
        if (estado == EstadoSolicitud.APROBADA
                && solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO) {
            otorgarAccesos(solicitud);
        }
        Solicitud guardada = solicitudRepository.save(solicitud);
        auditoriaService.registrar(estado == EstadoSolicitud.APROBADA ? "APROBAR" : "RECHAZAR",
                "Solicitud", guardada.getId(),
                "Solicitud " + guardada.getNumeroSolicitud() + " marcada como " + estado + ".");
        if (estado == EstadoSolicitud.APROBADA
                && guardada.getTipoSolicitud() == TipoSolicitud.ACCESO) {
            auditoriaService.registrar("OTORGAR", "Solicitud", guardada.getId(),
                    "Accesos otorgados para la solicitud " + guardada.getNumeroSolicitud() + ".");
        }
        notificarDecision(guardada);
        if (guardada.getUsuario() != null) {
            notificacionService.crear(guardada.getUsuario().getUsername(),
                    "Solicitud " + guardada.getNumeroSolicitud() + " " + guardada.getEstado(),
                    "Tu solicitud fue "
                            + (guardada.getEstado() == EstadoSolicitud.APROBADA
                                    ? "aprobada." : "rechazada."),
                    "/solicitudes/" + guardada.getId());
        }
        return solicitudMapper.toResponse(guardada);
    }

    private void otorgarAccesos(Solicitud solicitud) {
        Usuario usuario = solicitud.getUsuario();
        if (usuario == null) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = solicitud.getFechaInicioAcceso() != null
                ? solicitud.getFechaInicioAcceso()
                : hoy;
        solicitud.setFechaInicioAcceso(fechaInicio);
        boolean permanente = solicitud.isPermanente();
        LocalDate fechaFin = permanente
                ? null
                : (solicitud.getFechaFinAcceso() != null
                        ? solicitud.getFechaFinAcceso()
                        : fechaInicio.plusDays(30));
        solicitud.setFechaFinAcceso(fechaFin);

        Set<Rol> roles = usuario.rolesVigentes();
        if (roles.isEmpty()) {
            return;
        }
        for (var detalle : solicitud.getDetalles()) {
            boolean puedeEditar = detalle.getNivel() == NivelAcceso.EDICION;
            for (Rol rol : roles) {
                RolPermiso permiso = rolPermisoRepository
                        .findByRolIdAndModulo(rol.getId(), detalle.getModulo())
                        .orElseGet(() -> {
                            RolPermiso nuevo = new RolPermiso();
                            nuevo.setRol(rol);
                            nuevo.setModulo(detalle.getModulo());
                            nuevo.setPuedeLeer(false);
                            nuevo.setPuedeCrear(false);
                            nuevo.setPuedeActualizar(false);
                            nuevo.setPuedeEliminar(false);
                            nuevo.setTipoVigencia(TipoVigencia.TEMPORAL);
                            nuevo.setFechaInicio(fechaInicio);
                            nuevo.setActivo(true);
                            nuevo.setOrigenSolicitud(solicitud.getNumeroSolicitud());
                            return nuevo;
                        });

                        boolean definitivoExistente = permiso.getTipoVigencia() == TipoVigencia.DEFINITIVO
                                && permiso.isActivo()
                                && permiso.getFechaFin() == null;

                        // Siempre se garantiza el nivel solicitado. Nunca se quita un nivel
                        // superior que el usuario ya tuviera (p. ej. Edición al pedir Lectura).
                        permiso.setPuedeLeer(true);
                        if (puedeEditar) {
                            permiso.setPuedeCrear(true);
                            permiso.setPuedeActualizar(true);
                        }
                        if (!definitivoExistente) {
                            permiso.setTipoVigencia(permanente
                                    ? TipoVigencia.DEFINITIVO
                                    : TipoVigencia.TEMPORAL);
                            permiso.setFechaInicio(fechaInicio);
                            permiso.setFechaFin(fechaFin);
                        }
                        permiso.setActivo(true);
                        permiso.setOrigenSolicitud(solicitud.getNumeroSolicitud());
                        rolPermisoRepository.save(permiso);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ModuloActualDTO> modulosActuales(Long idSolicitud) {
        Solicitud solicitud = buscarEntidadPorId(idSolicitud);
        List<ModuloActualDTO> resultado = new ArrayList<>();
        Usuario usuario = solicitud.getUsuario();
        Set<Rol> roles = usuario != null ? usuario.rolesVigentes() : Set.of();
        for (String modulo : ModuloCatalogo.todos()) {
            ModuloActualDTO dto = new ModuloActualDTO();
            dto.setModulo(modulo);
            dto.setEtiqueta(ModuloCatalogo.etiqueta(modulo));
            List<RolModuloDTO> accesos = new ArrayList<>();
            List<String> colores = new ArrayList<>();
            boolean edicion = false;
            for (Rol rol : roles) {
                RolPermiso permiso = rol.getRolPermisos().stream()
                        .filter(rp -> modulo.equals(rp.getModulo()) && rp.esVigente())
                        .filter(rp -> rp.isPuedeLeer() || rp.isPuedeCrear()
                                || rp.isPuedeActualizar() || rp.isPuedeEliminar())
                        .findFirst()
                        .orElse(null);
                if (permiso == null) {
                    continue;
                }
                boolean puedeEditar = permiso.isPuedeCrear() || permiso.isPuedeActualizar();
                RolModuloDTO acceso = new RolModuloDTO();
                acceso.setIdPermiso(permiso.getId());
                acceso.setIdRol(rol.getId());
                acceso.setNombre(rol.getNombre());
                acceso.setColor(ColoresRol.colorDe(rol));
                acceso.setNivel(puedeEditar ? "EDICION" : "LECTURA");
                acceso.setNivelLegible(puedeEditar ? "Edición" : "Lectura");
                accesos.add(acceso);
                colores.add(acceso.getColor());
                if (puedeEditar) {
                    edicion = true;
                }
            }
            dto.setRoles(accesos);
            dto.setTieneAcceso(!accesos.isEmpty());
            dto.setNivel(accesos.isEmpty() ? null : (edicion ? "EDICION" : "LECTURA"));
            dto.setNivelLegible(accesos.isEmpty() ? "Sin acceso" : (edicion ? "Edición" : "Lectura"));
            dto.setGradiente(ColoresRol.gradiente(colores));
            resultado.add(dto);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<ModuloDisponibleDTO> modulosDisponibles() {
        Map<String, ModuloDisponibleDTO> porModulo = new LinkedHashMap<>();
        for (String modulo : ModuloCatalogo.todos()) {
            ModuloDisponibleDTO dto = new ModuloDisponibleDTO();
            dto.setModulo(modulo);
            dto.setEtiqueta(ModuloCatalogo.etiqueta(modulo));
            porModulo.put(modulo, dto);
        }
        for (RolPermiso permiso : rolPermisoRepository.findAll()) {
            if (!permiso.esVigente()
                    || !(permiso.isPuedeLeer() || permiso.isPuedeCrear()
                    || permiso.isPuedeActualizar() || permiso.isPuedeEliminar())) {
                continue;
            }
            Rol rol = permiso.getRol();
            if (rol == null || !rol.isActivo()) {
                continue;
            }
            ModuloDisponibleDTO dto = porModulo.computeIfAbsent(permiso.getModulo(), modulo -> {
                ModuloDisponibleDTO nuevo = new ModuloDisponibleDTO();
                nuevo.setModulo(modulo);
                nuevo.setEtiqueta(ModuloCatalogo.etiqueta(modulo));
                return nuevo;
            });
            boolean edicion = permiso.isPuedeCrear() || permiso.isPuedeActualizar()
                    || permiso.isPuedeEliminar();
            RolModuloDTO acceso = new RolModuloDTO();
            acceso.setIdPermiso(permiso.getId());
            acceso.setIdRol(rol.getId());
            acceso.setNombre(rol.getNombre());
            acceso.setColor(ColoresRol.colorDe(rol));
            acceso.setNivel(edicion ? "EDICION" : "LECTURA");
            acceso.setNivelLegible(edicion ? "Edición" : "Lectura");
            dto.getRoles().add(acceso);
        }
        List<ModuloDisponibleDTO> resultado = new ArrayList<>();
        for (ModuloDisponibleDTO dto : porModulo.values()) {
            dto.getRoles().sort(Comparator.comparing(RolModuloDTO::getNombre, Comparator.nullsLast(Comparator.naturalOrder())));
            dto.setGradiente(ColoresRol.gradiente(
                    dto.getRoles().stream()
                            .filter(r -> r != null)
                            .map(r -> r.getColor() != null ? r.getColor() : "")
                            .toList()));
            resultado.add(dto);
        }
        resultado.sort(Comparator.comparing(ModuloDisponibleDTO::getEtiqueta, Comparator.nullsLast(Comparator.naturalOrder())));
        return resultado;
    }

    /**
     * Módulos del catálogo sobre los que el usuario no tiene ningún permiso vigente.
     * Se usa para que /solicitudes/nueva solo ofrezca lo que realmente le falta.
     */
    @Transactional(readOnly = true)
    public Map<String, String> modulosSinAcceso(String username) {
        Set<String> conAcceso = new HashSet<>();
        Usuario usuario = username == null ? null
                : usuarioRepository.findByUsername(username).orElse(null);
        if (usuario != null) {
            for (Rol rol : usuario.rolesVigentes()) {
                if (rol == null || !rol.isActivo()) {
                    continue;
                }
                for (RolPermiso permiso : rol.getRolPermisos()) {
                    if (permiso.esVigente()
                            && (permiso.isPuedeLeer() || permiso.isPuedeCrear()
                            || permiso.isPuedeActualizar() || permiso.isPuedeEliminar())) {
                        conAcceso.add(permiso.getModulo());
                    }
                }
            }
        }
        Map<String, String> resultado = new LinkedHashMap<>();
        for (String modulo : ModuloCatalogo.todos()) {
            if (!conAcceso.contains(modulo)) {
                resultado.put(modulo, ModuloCatalogo.etiqueta(modulo));
            }
        }
        return resultado;
    }

    private boolean tieneAccesoModulo(Usuario usuario, String modulo) {
        if (usuario == null || modulo == null) {
            return false;
        }
        for (Rol rol : usuario.rolesVigentes()) {
            if (rol == null || !rol.isActivo()) {
                continue;
            }
            for (RolPermiso permiso : rol.getRolPermisos()) {
                if (modulo.equals(permiso.getModulo()) && permiso.esVigente()
                        && (permiso.isPuedeLeer() || permiso.isPuedeCrear()
                        || permiso.isPuedeActualizar() || permiso.isPuedeEliminar())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Documentos activos que el usuario aún NO tiene desbloqueados (no han sido
     * aprobados en una solicitud de INFORMACIÓN). Son los que puede solicitar
     * desde el formulario. Gerencia y Administración no necesitan solicitarlos.
     */
    @Transactional(readOnly = true)
    public List<Documento> documentosSinRecibir(String username) {
        List<Documento> activos = documentoRepository.findByActivoTrueOrderByFechaSubidaDesc();
        if (username == null) {
            return activos;
        }
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (esGestorDocumentos(usuario)) {
            return List.of();
        }
        Set<Long> recibidos = documentosAprobados(
                activos, solicitudRepository.findByUsuarioUsername(username));
        List<Documento> pendientes = new ArrayList<>();
        for (Documento documento : activos) {
            if (!recibidos.contains(documento.getId())) {
                pendientes.add(documento);
            }
        }
        return pendientes;
    }

    /**
     * IDs de los documentos que el usuario puede ver/descargar: aquellos que ya
     * fueron aprobados en una solicitud de INFORMACIÓN, o todos si pertenece a
     * Gerencia o Administración.
     */
    @Transactional(readOnly = true)
    public Set<Long> documentosDesbloqueados(String username) {
        List<Documento> activos = documentoRepository.findByActivoTrueOrderByFechaSubidaDesc();
        Set<Long> desbloqueados = new HashSet<>();
        if (username == null) {
            for (Documento doc : activos) {
                desbloqueados.add(doc.getId());
            }
            return desbloqueados;
        }
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (esGestorDocumentos(usuario)) {
            for (Documento doc : activos) {
                desbloqueados.add(doc.getId());
            }
            return desbloqueados;
        }
        desbloqueados.addAll(documentosAprobados(
                activos, solicitudRepository.findByUsuarioUsername(username)));
        return desbloqueados;
    }

    private Set<Long> documentosAprobados(List<Documento> activos, List<Solicitud> solicitudes) {
        Set<Long> idsActivos = new HashSet<>();
        for (Documento documento : activos) {
            idsActivos.add(documento.getId());
        }
        Set<Long> desbloqueados = new HashSet<>();
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getEstado() != EstadoSolicitud.APROBADA
                    || solicitud.getTipoSolicitud() != TipoSolicitud.INFORMACION) {
                continue;
            }
            Long idDocumento = solicitud.getIdDocumentoInforme();
            if (idDocumento != null) {
                if (idsActivos.contains(idDocumento)) {
                    desbloqueados.add(idDocumento);
                }
                continue;
            }
            String nombreInforme = solicitud.getNombreInforme();
            byte[] contenidoInforme = solicitud.getContenidoInforme();
            if (contenidoInforme != null) {
                List<Documento> coincidenciasPorContenido = activos.stream()
                        .filter(doc -> Arrays.equals(contenidoInforme, doc.getContenido()))
                        .toList();
                if (coincidenciasPorContenido.size() == 1) {
                    desbloqueados.add(coincidenciasPorContenido.get(0).getId());
                    continue;
                }
            }
            if (nombreInforme == null || nombreInforme.isBlank()) {
                continue;
            }

            List<Documento> coincidencias = activos.stream()
                    .filter(doc -> doc.getNombreArchivo() != null
                            && normalizar(nombreInforme).equals(normalizar(doc.getNombreArchivo())))
                    .toList();
            if (coincidencias.isEmpty()) {
                coincidencias = activos.stream()
                        .filter(doc -> normalizar(nombreInforme).equals(normalizar(doc.getNombre())))
                        .toList();
            }
            if (coincidencias.size() == 1) {
                desbloqueados.add(coincidencias.get(0).getId());
            }
        }
        return desbloqueados;
    }

    @Transactional(readOnly = true)
    public boolean tieneAccesoDocumento(String username, Long idDocumento) {
        return idDocumento != null && documentosDesbloqueados(username).contains(idDocumento);
    }

    private boolean esGestorDocumentos(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return usuario.rolesVigentes().stream()
                .filter(rol -> rol != null && rol.isActivo())
                .map(Rol::getNombre)
                .anyMatch(nombre -> nombre != null
                        && (nombre.contains("Gerencia") || nombre.contains("Administración")));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }

    public List<AreaDisponibleDTO> areasDisponibles(List<ModuloDisponibleDTO> modulos) {
        Map<String, AreaDisponibleDTO> porArea = new LinkedHashMap<>();
        for (ModuloDisponibleDTO modulo : modulos) {
            if (modulo == null || modulo.getRoles() == null) {
                continue;
            }
            for (RolModuloDTO rol : modulo.getRoles()) {
                if (rol == null || rol.getNombre() == null) {
                    continue;
                }
                AreaDisponibleDTO area = porArea.computeIfAbsent(rol.getNombre(), nombre -> {
                    AreaDisponibleDTO nueva = new AreaDisponibleDTO();
                    nueva.setNombre(nombre);
                    if (rol.getColor() != null) {
                        nueva.setColor(rol.getColor());
                    }
                    return nueva;
                });
                area.getModulos().add(modulo.getEtiqueta());
            }
        }
        List<AreaDisponibleDTO> resultado = new ArrayList<>(porArea.values());
        resultado.sort(Comparator.comparing(AreaDisponibleDTO::getNombre, Comparator.nullsLast(Comparator.naturalOrder())));
        return resultado;
    }

    @Transactional
    public void cambiarNivelModulo(Long idSolicitud, Long idPermiso, String nivel) {
        RolPermiso permiso = permisoDelSolicitante(idSolicitud, idPermiso);
        boolean edicion = "EDICION".equalsIgnoreCase(nivel);
        permiso.setPuedeLeer(true);
        permiso.setPuedeCrear(edicion);
        permiso.setPuedeActualizar(edicion);
        permiso.setPuedeEliminar(false);
        permiso.setActivo(true);
        rolPermisoRepository.save(permiso);
        auditoriaService.registrar("CAMBIAR_NIVEL", "Permiso", permiso.getId(),
                "Nivel de " + permiso.getModulo() + " cambiado a "
                        + (edicion ? "EDICION" : "LECTURA") + ".");
    }

    @Transactional
    public void revocarModulo(Long idSolicitud, Long idPermiso) {
        RolPermiso permiso = permisoDelSolicitante(idSolicitud, idPermiso);
        permiso.setActivo(false);
        rolPermisoRepository.save(permiso);
        auditoriaService.registrar("REVOCAR", "Permiso", permiso.getId(),
                "Acceso al módulo " + permiso.getModulo() + " revocado.");
    }

    @Transactional
    public void agregarAcceso(Long idSolicitud, String modulo, Long idRol, String nivel) {
        Solicitud solicitud = buscarEntidadPorId(idSolicitud);
        Usuario usuario = solicitud.getUsuario();
        if (usuario == null) {
            throw new IllegalArgumentException("La solicitud no tiene un usuario asociado.");
        }
        Rol rol = usuario.rolesVigentes().stream()
                .filter(r -> r.getId() != null && r.getId().equals(idRol))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no pertenece al solicitante."));
        if (modulo == null || !ModuloCatalogo.todos().contains(modulo)) {
            throw new IllegalArgumentException("Módulo no válido.");
        }
        RolPermiso permiso = rolPermisoRepository.findByRolIdAndModulo(idRol, modulo)
                .orElseGet(() -> {
                    RolPermiso nuevo = new RolPermiso();
                    nuevo.setRol(rol);
                    nuevo.setModulo(modulo);
                    nuevo.setPuedeLeer(false);
                    nuevo.setPuedeCrear(false);
                    nuevo.setPuedeActualizar(false);
                    nuevo.setPuedeEliminar(false);
                    return nuevo;
                });
        boolean edicion = "EDICION".equalsIgnoreCase(nivel);
        permiso.setPuedeLeer(true);
        permiso.setPuedeCrear(edicion);
        permiso.setPuedeActualizar(edicion);
        permiso.setActivo(true);
        LocalDate inicio = solicitud.getFechaInicioAcceso() != null
                ? solicitud.getFechaInicioAcceso()
                : LocalDate.now();
        if (solicitud.isPermanente()) {
            permiso.setTipoVigencia(TipoVigencia.DEFINITIVO);
            permiso.setFechaInicio(inicio);
            permiso.setFechaFin(null);
        } else {
            permiso.setTipoVigencia(TipoVigencia.TEMPORAL);
            permiso.setFechaInicio(inicio);
            permiso.setFechaFin(solicitud.getFechaFinAcceso() != null
                    ? solicitud.getFechaFinAcceso()
                    : inicio.plusDays(30));
        }
        permiso.setOrigenSolicitud(solicitud.getNumeroSolicitud());
        rolPermisoRepository.save(permiso);
        auditoriaService.registrar("OTORGAR", "Permiso", permiso.getId(),
                "Acceso al módulo " + modulo + " otorgado a " + rol.getNombre() + ".");
    }

    private RolPermiso permisoDelSolicitante(Long idSolicitud, Long idPermiso) {
        Solicitud solicitud = buscarEntidadPorId(idSolicitud);
        RolPermiso permiso = rolPermisoRepository.findById(idPermiso)
                .orElseThrow(() -> new IllegalArgumentException("Permiso no encontrado."));
        boolean pertenece = solicitud.getUsuario() != null
                && solicitud.getUsuario().rolesVigentes().stream()
                        .anyMatch(r -> r.getId() != null && r.getId().equals(permiso.getRol().getId()));
        if (!pertenece) {
            throw new IllegalArgumentException("El permiso no pertenece al solicitante.");
        }
        return permiso;
    }

    @Transactional
    public void proponer(Long idSolicitud, SolicitudPropuestaDTO propuesta) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));
        if (solicitud.getTipoSolicitud() != TipoSolicitud.ACCESO) {
            throw new IllegalArgumentException("Solo las solicitudes de acceso admiten propuesta.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE
                && solicitud.getEstado() != EstadoSolicitud.EN_REVISION) {
            throw new IllegalArgumentException("La solicitud ya fue decidida.");
        }
        if (solicitud.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La solicitud no tiene módulos solicitados.");
        }
        Map<String, String> niveles = propuesta.getNiveles() != null ? propuesta.getNiveles() : Map.of();
        solicitud.getPropuestaDetalles().clear();
        List<String> etiquetas = new ArrayList<>();
        for (SolicitudAccesoDetalle detalle : solicitud.getDetalles()) {
            NivelAcceso nivel = SolicitudMapper.parsearNivel(niveles.get(detalle.getModulo()));
            solicitud.addPropuestaDetalle(new SolicitudPropuestaDetalle(solicitud, detalle.getModulo(), nivel));
            etiquetas.add(ModuloCatalogo.etiqueta(detalle.getModulo())
                    + " (" + SolicitudMapper.legible(nivel) + ")");
        }
        LocalDate inicio = propuesta.getFechaInicio() != null
                ? propuesta.getFechaInicio()
                : LocalDate.now();
        LocalDate fin = propuesta.isPermanente()
                ? null
                : (propuesta.getFechaFin() != null ? propuesta.getFechaFin() : inicio.plusDays(30));
        solicitud.setPropuestaFechaInicio(inicio);
        solicitud.setPropuestaFechaFin(fin);
        solicitud.setPropuestaPermanente(propuesta.isPermanente());
        solicitud.setNotaPropuesta(propuesta.getNota());
        solicitud.setEstado(EstadoSolicitud.EN_REVISION);
        solicitud.setFechaAprobacionRechazo(null);
        solicitud.setComentarioAdmin(null);
        solicitudRepository.save(solicitud);
        notificarPropuesta(solicitud, String.join("<br>", etiquetas));
        if (solicitud.getUsuario() != null) {
            notificacionService.crear(solicitud.getUsuario().getUsername(),
                    "Propuesta recibida",
                    "Administración envió una propuesta para tu solicitud "
                            + solicitud.getNumeroSolicitud() + ". Acéptala o recházala.",
                    "/solicitudes/" + solicitud.getId());
        }
    }

    @Transactional
    public void aceptarPropuesta(Long idSolicitud, String username) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));
        validarPropuestaDelSolicitante(solicitud, username);
        boolean permanente = Boolean.TRUE.equals(solicitud.getPropuestaPermanente());
        solicitud.setPermanente(permanente);
        solicitud.setFechaInicioAcceso(solicitud.getPropuestaFechaInicio() != null
                ? solicitud.getPropuestaFechaInicio()
                : LocalDate.now());
        solicitud.setFechaFinAcceso(permanente ? null : solicitud.getPropuestaFechaFin());
        solicitud.getDetalles().clear();
        List<String> etiquetas = new ArrayList<>();
        for (SolicitudPropuestaDetalle detalle : solicitud.getPropuestaDetalles()) {
            solicitud.addDetalle(new SolicitudAccesoDetalle(solicitud, detalle.getModulo(), detalle.getNivel()));
            etiquetas.add(ModuloCatalogo.etiqueta(detalle.getModulo())
                    + " (" + SolicitudMapper.legible(detalle.getNivel()) + ")");
        }
        solicitud.setRecursoModulo(String.join(", ", etiquetas));
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaAprobacionRechazo(LocalDateTime.now());
        solicitud.setComentarioAdmin("Propuesta aceptada por el solicitante.");
        otorgarAccesos(solicitud);
        Solicitud guardada = solicitudRepository.save(solicitud);
        auditoriaService.registrar("APROBAR", "Solicitud", guardada.getId(),
                "Propuesta aceptada para la solicitud " + guardada.getNumeroSolicitud() + ".");
        auditoriaService.registrar("OTORGAR", "Solicitud", guardada.getId(),
                "Accesos otorgados tras aceptar la propuesta " + guardada.getNumeroSolicitud() + ".");
        notificarDecision(guardada);
        notificacionService.crearParaRol("Administración",
                "Propuesta aceptada",
                "El solicitante aceptó la propuesta de la solicitud "
                        + guardada.getNumeroSolicitud() + ".",
                "/solicitudes/" + guardada.getId());
    }

    @Transactional
    public void rechazarPropuesta(Long idSolicitud, String username) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud));
        validarPropuestaDelSolicitante(solicitud, username);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaAprobacionRechazo(LocalDateTime.now());
        solicitud.setComentarioAdmin("Propuesta rechazada por el solicitante.");
        Solicitud guardada = solicitudRepository.save(solicitud);
        auditoriaService.registrar("RECHAZAR", "Solicitud", guardada.getId(),
                "Propuesta rechazada para la solicitud " + guardada.getNumeroSolicitud() + ".");
        notificacionService.crearParaRol("Administración",
                "Propuesta rechazada",
                "El solicitante rechazó la propuesta de la solicitud "
                        + guardada.getNumeroSolicitud() + ".",
                "/solicitudes/" + guardada.getId());
    }

    private void validarPropuestaDelSolicitante(Solicitud solicitud, String username) {
        if (solicitud.getEstado() != EstadoSolicitud.EN_REVISION) {
            throw new IllegalArgumentException("La solicitud no tiene una propuesta pendiente.");
        }
        if (solicitud.getUsuario() == null
                || !solicitud.getUsuario().getUsername().equals(username)) {
            throw new IllegalArgumentException("Solo el solicitante puede responder la propuesta.");
        }
    }

    private void notificarPropuesta(Solicitud solicitud, String etiquetasHtml) {
        Usuario usuario = solicitud.getUsuario();
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            return;
        }
        String vigencia = Boolean.TRUE.equals(solicitud.getPropuestaPermanente())
                ? "Permanente"
                : "Desde " + formatear(solicitud.getPropuestaFechaInicio())
                        + " hasta " + formatear(solicitud.getPropuestaFechaFin());
        String nota = (solicitud.getNotaPropuesta() == null || solicitud.getNotaPropuesta().isBlank())
                ? "Sin observaciones."
                : solicitud.getNotaPropuesta();
        String enlace = urlPublica + "/solicitudes/" + solicitud.getId();
        StringBuilder filas = new StringBuilder();
        fila(filas, "Número de solicitud", solicitud.getNumeroSolicitud());
        filaHtml(filas, "Módulos propuestos", etiquetasHtml);
        fila(filas, "Vigencia propuesta", vigencia);
        fila(filas, "Observaciones", nota);
        String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f3f4f6;padding:24px;">
                  <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;">
                    <div style="background:#d97706;padding:20px 24px;color:#ffffff;">
                      <h2 style="margin:0;font-size:18px;">Nuevo Horizonte</h2>
                      <p style="margin:4px 0 0;font-size:13px;opacity:.9;">Propuesta de acceso pendiente</p>
                    </div>
                    <div style="padding:24px;color:#374151;">
                      <p style="margin:0 0 12px;">Hola <strong>%s</strong>,</p>
                      <p style="margin:0 0 16px;">El administrador revisó tu solicitud <strong>%s</strong> y envió una propuesta.
                        Ingresa al sistema para aceptarla o rechazarla.</p>
                      <table style="width:100%%;border-collapse:collapse;font-size:14px;">%s</table>
                      <p style="margin:22px 0;">
                        <a href="%s" style="display:inline-block;background:#2563eb;color:#ffffff;text-decoration:none;padding:10px 18px;border-radius:8px;font-weight:bold;">
                          Revisar propuesta
                        </a>
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(usuario.getUsername(), solicitud.getNumeroSolicitud(), filas, enlace);
        correoService.enviarHtml(usuario.getEmail(),
                "Propuesta para tu solicitud " + solicitud.getNumeroSolicitud(), html);
    }

    private String formatear(LocalDate fecha) {
        return fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
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
        fila(filas, "Tipo de solicitud", solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO
                ? "Solicitud de acceso a módulos"
                : "Solicitud de información");
        fila(filas, "Solicitante", solicitud.getNombreSolicitante());
        fila(filas, "Usuario", usuario.getUsername());
        fila(filas, "Área solicitante", solicitud.getAreaSolicitante());
        fila(filas, "Fecha de solicitud", fechaSolicitud);
        fila(filas, solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO
                ? "Módulos solicitados"
                : "Recurso o módulo solicitado", solicitud.getRecursoModulo());
        if (solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO) {
            if (!solicitud.getDetalles().isEmpty()) {
                String detalles = solicitud.getDetalles().stream()
                        .map(d -> com.example.demo.modulos.ModuloCatalogo.etiqueta(d.getModulo())
                                + " (" + SolicitudMapper.legible(d.getNivel()) + ")")
                        .reduce((a, b) -> a + "<br>" + b)
                        .orElse("—");
                filaHtml(filas, "Niveles por módulo", detalles);
            }
            fila(filas, "Vigencia del acceso", solicitud.isPermanente()
                    ? "Permanente (sin fecha de fin)"
                    : "Desde " + formatear(solicitud.getFechaInicioAcceso())
                            + " hasta " + (solicitud.getFechaFinAcceso() != null
                                    ? formatear(solicitud.getFechaFinAcceso())
                                    : "30 días después del inicio"));
        }
        fila(filas, "Responsable de aprobación", solicitud.getResponsableAprobacion());
        fila(filas, "Motivo", solicitud.getMotivo());
        fila(filas, "Archivo adjunto", adjunto);
        if (solicitud.getNombreInforme() != null) {
            filaHtml(filas, "Informe solicitado",
                    "<a href=\"" + urlPublica + "/solicitudes/" + solicitud.getId() + "/informe\" "
                            + "style=\"color:#2563eb;font-weight:bold;\">"
                            + escaparHtml(solicitud.getNombreInforme()) + "</a>");
        }
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

    private void filaHtml(StringBuilder sb, String etiqueta, String htmlValor) {
        sb.append("<tr>")
                .append("<td style=\"padding:7px 8px 7px 0;color:#6b7280;vertical-align:top;white-space:nowrap;\">")
                .append(escaparHtml(etiqueta)).append("</td>")
                .append("<td style=\"padding:7px 0;color:#111827;\">")
                .append(htmlValor).append("</td>")
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
