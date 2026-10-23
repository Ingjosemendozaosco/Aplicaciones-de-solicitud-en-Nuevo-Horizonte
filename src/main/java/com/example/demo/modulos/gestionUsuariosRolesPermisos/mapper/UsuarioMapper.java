package com.example.demo.modulos.gestionUsuariosRolesPermisos.mapper;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.UsuarioResponseDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.UsuarioRol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.CargoFormal;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        var rolesVigentes = usuario.rolesVigentes();
        dto.setRoles(rolesVigentes.stream()
                .map(rol -> rol.getNombre())
                .sorted()
                .toList());
        dto.setCargoFormal(CargoFormal.de(rolesVigentes));
        return dto;
    }

    public UsuarioRequestDTO toRequest(Usuario usuario) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setActivo(usuario.isActivo());
        dto.setRoles(usuario.getUsuarioRoles().stream()
                .filter(UsuarioRol::esVigente)
                .map(ur -> ur.getRol().getNombre())
                .toList());
        Optional<UsuarioRol> asignacion = usuario.getUsuarioRoles().stream()
                .filter(UsuarioRol::esVigente)
                .findFirst();
        asignacion.ifPresent(ur -> {
            dto.setTipoVigencia(ur.getTipoVigencia() != null ? ur.getTipoVigencia().name() : "DEFINITIVO");
            dto.setFechaFinRol(ur.getFechaFin());
        });
        return dto;
    }
}
