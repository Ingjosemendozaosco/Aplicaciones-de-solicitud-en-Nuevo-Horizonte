package com.example.demo.config;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.auditoria.service.AuditoriaService;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.UsuarioRol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.RolPermisoRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRolRepository;
import com.example.demo.modulos.notificaciones.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class VigenciaScheduler {

    private static final Logger log = LoggerFactory.getLogger(VigenciaScheduler.class);

    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    public VigenciaScheduler(RolPermisoRepository rolPermisoRepository,
                             UsuarioRolRepository usuarioRolRepository,
                             NotificacionService notificacionService,
                             AuditoriaService auditoriaService) {
        this.rolPermisoRepository = rolPermisoRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void revocarVencidos() {
        LocalDate hoy = LocalDate.now();
        Map<String, List<String>> avisosModulos = new LinkedHashMap<>();

        var permisosVencidos = rolPermisoRepository
                .findByActivoTrueAndFechaFinIsNotNullAndFechaFinLessThanEqual(hoy);
        for (RolPermiso permiso : permisosVencidos) {
            permiso.setActivo(false);
            String modulo = ModuloCatalogo.etiqueta(permiso.getModulo());
            auditoriaService.registrar("VENCIMIENTO", "Permiso", permiso.getId(),
                    "Acceso al módulo " + modulo + " venció el " + permiso.getFechaFin()
                            + " (rol " + (permiso.getRol() != null ? permiso.getRol().getNombre() : "—") + ").");
            if (permiso.getRol() != null && permiso.getRol().getId() != null) {
                for (UsuarioRol asignacion : usuarioRolRepository
                        .findByRolIdAndActivoTrue(permiso.getRol().getId())) {
                    if (asignacion.getUsuario() == null) {
                        continue;
                    }
                    avisosModulos.computeIfAbsent(asignacion.getUsuario().getUsername(),
                                    clave -> new ArrayList<>())
                            .add(modulo);
                }
            }
        }
        if (!permisosVencidos.isEmpty()) {
            rolPermisoRepository.saveAll(permisosVencidos);
            log.info("Revocados {} permisos temporales vencidos", permisosVencidos.size());
        }

        var rolesVencidos = usuarioRolRepository
                .findByActivoTrueAndFechaFinIsNotNullAndFechaFinLessThanEqual(hoy);
        for (UsuarioRol usuarioRol : rolesVencidos) {
            usuarioRol.setActivo(false);
            auditoriaService.registrar("VENCIMIENTO", "Rol", usuarioRol.getId(),
                    "Asignación del rol "
                            + (usuarioRol.getRol() != null ? usuarioRol.getRol().getNombre() : "—")
                            + " venció el " + usuarioRol.getFechaFin() + ".");
            if (usuarioRol.getUsuario() != null) {
                notificacionService.crear(usuarioRol.getUsuario().getUsername(),
                        "Rol vencido",
                        "Tu rol " + (usuarioRol.getRol() != null
                                ? usuarioRol.getRol().getNombre() : "asignado")
                                + " venció el " + usuarioRol.getFechaFin() + ".",
                        "/mis-accesos");
            }
        }
        if (!rolesVencidos.isEmpty()) {
            usuarioRolRepository.saveAll(rolesVencidos);
            log.info("Revocados {} roles temporales vencidos", rolesVencidos.size());
        }

        avisosModulos.forEach((username, modulos) -> notificacionService.crear(username,
                "Accesos vencidos",
                "Venció tu acceso a: " + String.join(", ", modulos)
                        + ". Revisa tus accesos o solicita una renovación.",
                "/mis-accesos"));
    }
}
