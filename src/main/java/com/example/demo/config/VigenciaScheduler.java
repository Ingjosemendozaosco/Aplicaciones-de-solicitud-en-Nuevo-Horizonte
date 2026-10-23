package com.example.demo.config;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.RolPermisoRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class VigenciaScheduler {

    private static final Logger log = LoggerFactory.getLogger(VigenciaScheduler.class);

    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public VigenciaScheduler(RolPermisoRepository rolPermisoRepository,
                             UsuarioRolRepository usuarioRolRepository) {
        this.rolPermisoRepository = rolPermisoRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void revocarVencidos() {
        LocalDate hoy = LocalDate.now();

        var permisosVencidos = rolPermisoRepository
                .findByActivoTrueAndFechaFinIsNotNullAndFechaFinBefore(hoy);
        for (var permiso : permisosVencidos) {
            permiso.setActivo(false);
        }
        if (!permisosVencidos.isEmpty()) {
            rolPermisoRepository.saveAll(permisosVencidos);
            log.info("Revocados {} permisos temporales vencidos", permisosVencidos.size());
        }

        var rolesVencidos = usuarioRolRepository
                .findByActivoTrueAndFechaFinIsNotNullAndFechaFinBefore(hoy);
        for (var usuarioRol : rolesVencidos) {
            usuarioRol.setActivo(false);
        }
        if (!rolesVencidos.isEmpty()) {
            usuarioRolRepository.saveAll(rolesVencidos);
            log.info("Revocados {} roles temporales vencidos", rolesVencidos.size());
        }
    }
}
