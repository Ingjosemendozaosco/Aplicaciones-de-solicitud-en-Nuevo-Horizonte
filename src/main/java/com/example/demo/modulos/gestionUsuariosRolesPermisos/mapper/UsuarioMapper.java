package com.example.demo.modulos.gestionUsuariosRolesPermisos.mapper;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioResponseDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setRoles(usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList());
        return dto;
    }

    public UsuarioRequestDTO toRequest(Usuario usuario) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        dto.setRolNombre(usuario.getRoles().stream()
                .map(Rol::getNombre)
                .findFirst()
                .orElse(null));
        return dto;
    }
}
