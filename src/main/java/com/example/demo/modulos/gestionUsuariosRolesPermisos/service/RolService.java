package com.example.demo.modulos.gestionUsuariosRolesPermisos.service;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.PermisosFormDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.RolRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.RolResponseDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.mapper.RolMapper;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.RolRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolMapper rolMapper;

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolMapper = rolMapper;
    }

    @Transactional(readOnly = true)
    public List<RolResponseDTO> listar() {
        return rolRepository.findAll(Sort.by("nombre")).stream()
                .map(rolMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RolRequestDTO paraEdicion(Long id) {
        return rolMapper.toRequest(obtener(id));
    }

    @Transactional
    public void crear(RolRequestDTO dto) {
        String nombre = limpiar(dto.getNombre());
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        if (rolRepository.existsByNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }

        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(limpiar(dto.getDescripcion()));
        rol.setColor(colorValido(dto.getColor()));
        rol.setActivo(dto.getActivo() == null || dto.getActivo());
        rol.setEsSistema(false);
        rol.setFechaCreacion(LocalDateTime.now());
        rolRepository.save(rol);
    }

    @Transactional
    public void actualizar(Long id, RolRequestDTO dto) {
        Rol rol = obtener(id);
        if (!rol.isEsSistema()) {
            String nombre = limpiar(dto.getNombre());
            if (nombre == null) {
                throw new IllegalArgumentException("El nombre del rol es obligatorio");
            }
            if (rolRepository.existsByNombreAndIdNot(nombre, id)) {
                throw new IllegalArgumentException("Ya existe un rol con ese nombre");
            }
            rol.setNombre(nombre);
        }
        rol.setDescripcion(limpiar(dto.getDescripcion()));
        rol.setColor(colorValido(dto.getColor()));
        rol.setActivo(dto.getActivo() == null || dto.getActivo());
        rol.setFechaActualizacion(LocalDateTime.now());
        rolRepository.save(rol);
    }

    @Transactional
    public void eliminar(Long id) {
        Rol rol = obtener(id);
        if (rol.isEsSistema()) {
            throw new IllegalArgumentException("No se puede eliminar un rol del sistema");
        }
        if (usuarioRepository.countByRolesId(id) > 0) {
            throw new IllegalArgumentException("El rol tiene usuarios asignados");
        }
        rolRepository.delete(rol);
    }

    @Transactional(readOnly = true)
    public PermisosFormDTO formularioPermisos(Long id) {
        return rolMapper.toPermisosForm(obtener(id));
    }

    @Transactional
    public void actualizarPermisos(Long id, PermisosFormDTO form) {
        Rol rol = obtener(id);
        rolMapper.aplicarPermisos(rol, form);
        rol.setFechaActualizacion(LocalDateTime.now());
        rolRepository.save(rol);
    }

    @Transactional(readOnly = true)
    public long total() {
        return rolRepository.count();
    }

    @Transactional(readOnly = true)
    public List<String> modulosDisponibles() {
        return ModuloCatalogo.todos();
    }

    private Rol obtener(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
    }

    private String colorValido(String color) {
        String limpio = limpiar(color);
        return limpio == null ? "#2563eb" : limpio;
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
