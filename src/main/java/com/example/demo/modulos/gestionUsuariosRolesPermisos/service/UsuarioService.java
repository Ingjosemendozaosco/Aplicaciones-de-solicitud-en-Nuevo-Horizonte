package com.example.demo.modulos.gestionUsuariosRolesPermisos.service;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioResponseDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.TipoVigencia;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.mapper.UsuarioMapper;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.RolRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          UsuarioMapper usuarioMapper,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll(Sort.by("username")).stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscar(Long id) {
        return usuarioMapper.toResponse(obtener(id));
    }

    @Transactional(readOnly = true)
    public Usuario perfil(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    @Transactional(readOnly = true)
    public UsuarioRequestDTO paraEdicion(Long id) {
        return usuarioMapper.toRequest(obtener(id));
    }

    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        String username = limpiar(dto.getUsername());
        String email = limpiar(dto.getEmail());

        if (username == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (email == null) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setActivo(dto.isActivo());
        usuario.setFechaCreacion(LocalDateTime.now());
        asignarRoles(usuario, dto);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = obtener(id);
        String username = limpiar(dto.getUsername());
        String email = limpiar(dto.getEmail());

        if (username == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (email == null) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (usuarioRepository.existsByUsernameAndIdNot(username, id)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
        }
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }

        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setActivo(dto.isActivo());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            usuario.getUsuarioRoles().clear();
            asignarRoles(usuario, dto);
        }
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Usuario usuario = obtener(id);
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(obtener(id));
    }

    @Transactional(readOnly = true)
    public List<String> rolesDisponibles() {
        return rolRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(Rol::getNombre)
                .toList();
    }

    @Transactional(readOnly = true)
    public long total() {
        return usuarioRepository.count();
    }

    private Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private void asignarRoles(Usuario usuario, UsuarioRequestDTO dto) {
        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            return;
        }
        TipoVigencia tipo = "TEMPORAL".equalsIgnoreCase(dto.getTipoVigencia())
                ? TipoVigencia.TEMPORAL
                : TipoVigencia.DEFINITIVO;
        if (tipo == TipoVigencia.TEMPORAL) {
            if (dto.getFechaFinRol() == null) {
                throw new IllegalArgumentException("Indique la fecha de fin para un rol temporal");
            }
            if (!dto.getFechaFinRol().isAfter(java.time.LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de fin del rol debe ser posterior a hoy");
            }
        }
        for (String nombreRol : dto.getRoles()) {
            String nombre = limpiar(nombreRol);
            if (nombre == null) {
                continue;
            }
            Rol rol = rolRepository.findByNombre(nombre)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombre));
            usuario.addRol(rol, tipo, tipo == TipoVigencia.TEMPORAL ? dto.getFechaFinRol() : null);
        }
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
