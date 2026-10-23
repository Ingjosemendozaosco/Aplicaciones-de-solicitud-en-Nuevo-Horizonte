package com.example.demo.modulos.gestionUsuariosRolesPermisos.controller;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.CargoFormal;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.ColoresRol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.UsuarioService;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.service.SolicitudService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PerfilController {

    private final UsuarioService usuarioService;
    private final SolicitudService solicitudService;

    public PerfilController(UsuarioService usuarioService,
                            SolicitudService solicitudService) {
        this.usuarioService = usuarioService;
        this.solicitudService = solicitudService;
    }

    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.perfil(authentication.getName());
        List<Rol> roles = usuario.rolesVigentes().stream()
                .sorted(Comparator.comparing(Rol::getNombre))
                .toList();
        List<Map<String, Object>> rolesView = new ArrayList<>();
        for (Rol rol : roles) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nombre", rol.getNombre());
            item.put("descripcion", rol.getDescripcion());
            item.put("color", ColoresRol.colorDe(rol));
            item.put("permisos", rol.getRolPermisos().stream()
                    .filter(p -> p.esVigente())
                    .filter(p -> p.isPuedeLeer() || p.isPuedeCrear()
                            || p.isPuedeActualizar() || p.isPuedeEliminar())
                    .sorted(Comparator.comparing(p -> ModuloCatalogo.etiqueta(p.getModulo())))
                    .map(p -> {
                        boolean edicion = p.isPuedeCrear() || p.isPuedeActualizar() || p.isPuedeEliminar();
                        Map<String, Object> permiso = new LinkedHashMap<>();
                        permiso.put("etiqueta", ModuloCatalogo.etiqueta(p.getModulo()));
                        permiso.put("nivel", edicion ? "Edición" : "Lectura");
                        permiso.put("edicion", edicion);
                        return permiso;
                    })
                    .toList());
            rolesView.add(item);
        }
        List<SolicitudResponseDTO> solicitudes = solicitudService
                .listarPorUsuario(authentication.getName()).stream()
                .sorted(Comparator.comparing(SolicitudResponseDTO::getFechaSolicitud).reversed())
                .toList();
        long totalModulos = roles.stream()
                .flatMap(rol -> rol.getRolPermisos().stream())
                .filter(p -> p.esVigente())
                .filter(p -> p.isPuedeLeer() || p.isPuedeCrear()
                        || p.isPuedeActualizar() || p.isPuedeEliminar())
                .map(p -> p.getModulo())
                .distinct()
                .count();

        model.addAttribute("perfil", usuario);
        model.addAttribute("roles", rolesView);
        model.addAttribute("cargo", CargoFormal.de(usuario.rolesVigentes()));
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("totalPendientes", solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE).count());
        model.addAttribute("totalAprobadas", solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.APROBADA).count());
        model.addAttribute("totalModulos", totalModulos);
        model.addAttribute("recientes", solicitudes.stream().limit(4).toList());
        return "perfil/index";
    }
}
