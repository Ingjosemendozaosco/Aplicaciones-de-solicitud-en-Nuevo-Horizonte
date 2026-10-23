package com.example.demo.modulos.notificaciones.service;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import com.example.demo.modulos.notificaciones.entity.Notificacion;
import com.example.demo.modulos.notificaciones.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void crear(String username, String titulo, String mensaje, String enlace) {
        if (username == null || username.isBlank()) {
            return;
        }
        usuarioRepository.findByUsername(username).ifPresent(usuario ->
                guardar(usuario, titulo, mensaje, enlace));
    }

    @Transactional
    public void crearParaRol(String nombreRol, String titulo, String mensaje, String enlace) {
        List<Usuario> destinatarios = usuarioRepository
                .findByActivoTrueAndUsuarioRolesRolNombre(nombreRol);
        for (Usuario usuario : destinatarios) {
            boolean tieneRol = usuario.rolesVigentes().stream()
                    .anyMatch(rol -> nombreRol.equals(rol.getNombre()));
            if (tieneRol) {
                guardar(usuario, titulo, mensaje, enlace);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listar(String username) {
        return notificacionRepository.findByUsuarioUsernameOrderByFechaCreacionDesc(username);
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String username) {
        return notificacionRepository.countByUsuarioUsernameAndLeidaFalse(username);
    }

    @Transactional
    public void marcarLeida(Long id, String username) {
        notificacionRepository.findByIdAndUsuarioUsername(id, username)
                .ifPresent(notificacion -> {
                    notificacion.setLeida(true);
                    notificacionRepository.save(notificacion);
                });
    }

    @Transactional
    public void marcarTodasLeidas(String username) {
        notificacionRepository.marcarTodasLeidas(username);
    }

    private void guardar(Usuario usuario, String titulo, String mensaje, String enlace) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setEnlace(enlace);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }
}
