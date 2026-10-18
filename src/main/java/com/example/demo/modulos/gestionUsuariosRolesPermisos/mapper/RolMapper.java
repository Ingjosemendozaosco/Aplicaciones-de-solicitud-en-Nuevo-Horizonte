package com.example.demo.modulos.gestionUsuariosRolesPermisos.mapper;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.PermisoItemDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.PermisosFormDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.RolRequestDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.RolResponseDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RolMapper {

    public RolResponseDTO toResponse(Rol rol) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setColor(rol.getColor());
        dto.setEsSistema(rol.isEsSistema());
        dto.setActivo(rol.isActivo());
        dto.setTotalPermisos(rol.getRolPermisos().size());
        dto.setModulos(rol.getRolPermisos().stream()
                .map(RolPermiso::getModulo)
                .sorted()
                .toList());
        return dto;
    }

    public RolRequestDTO toRequest(Rol rol) {
        RolRequestDTO dto = new RolRequestDTO();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setColor(rol.getColor());
        dto.setActivo(rol.isActivo());
        dto.setEsSistema(rol.isEsSistema());
        return dto;
    }

    public PermisosFormDTO toPermisosForm(Rol rol) {
        Map<String, RolPermiso> porModulo = rol.getRolPermisos().stream()
                .collect(Collectors.toMap(RolPermiso::getModulo, Function.identity()));

        List<PermisoItemDTO> items = ModuloCatalogo.todos().stream().map(modulo -> {
            PermisoItemDTO item = new PermisoItemDTO();
            item.setModulo(modulo);
            item.setEtiqueta(ModuloCatalogo.etiqueta(modulo));
            RolPermiso permiso = porModulo.get(modulo);
            if (permiso != null) {
                item.setLeer(permiso.isPuedeLeer());
                item.setCrear(permiso.isPuedeCrear());
                item.setActualizar(permiso.isPuedeActualizar());
                item.setEliminar(permiso.isPuedeEliminar());
            }
            return item;
        }).toList();

        PermisosFormDTO form = new PermisosFormDTO();
        form.setItems(items);
        return form;
    }

    public void aplicarPermisos(Rol rol, PermisosFormDTO form) {
        Set<String> recibidos = form.getItems().stream()
                .map(PermisoItemDTO::getModulo)
                .collect(Collectors.toSet());

        Map<String, RolPermiso> existentes = rol.getRolPermisos().stream()
                .collect(Collectors.toMap(RolPermiso::getModulo, Function.identity()));

        rol.getRolPermisos().removeIf(permiso -> !recibidos.contains(permiso.getModulo()));

        for (PermisoItemDTO item : form.getItems()) {
            RolPermiso permiso = existentes.get(item.getModulo());
            if (permiso == null) {
                permiso = new RolPermiso();
                permiso.setRol(rol);
                permiso.setModulo(item.getModulo());
                rol.getRolPermisos().add(permiso);
            }
            permiso.setPuedeLeer(item.isLeer());
            permiso.setPuedeCrear(item.isCrear());
            permiso.setPuedeActualizar(item.isActualizar());
            permiso.setPuedeEliminar(item.isEliminar());
        }
    }
}
