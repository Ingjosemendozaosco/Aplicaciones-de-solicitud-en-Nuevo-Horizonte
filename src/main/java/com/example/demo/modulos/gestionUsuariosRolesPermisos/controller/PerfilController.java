package com.example.demo.modulos.gestionUsuariosRolesPermisos.controller;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.dto.AccesoUsuarioDTO;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @GetMapping("/mis-accesos")
    public String misAccesos(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.perfil(authentication.getName());
        LocalDate hoy = LocalDate.now();
        List<AccesoUsuarioDTO> accesos = new ArrayList<>();

        for (Rol rol : usuario.rolesVigentes()) {
            for (RolPermiso permiso : rol.getRolPermisos()) {
                boolean tieneAcceso = permiso.isPuedeLeer() || permiso.isPuedeCrear()
                        || permiso.isPuedeActualizar() || permiso.isPuedeEliminar();
                if (!tieneAcceso) {
                    continue;
                }
                if (!permiso.isActivo() && permiso.getFechaFin() == null) {
                    continue;
                }
                AccesoUsuarioDTO dto = new AccesoUsuarioDTO();
                dto.setModulo(permiso.getModulo());
                dto.setModuloLegible(ModuloCatalogo.etiqueta(permiso.getModulo()));
                dto.setRol(rol.getNombre());
                dto.setColor(ColoresRol.colorDe(rol));
                boolean edicion = permiso.isPuedeCrear() || permiso.isPuedeActualizar()
                        || permiso.isPuedeEliminar();
                dto.setNivel(edicion ? "Edición" : "Lectura");
                dto.setFechaInicio(permiso.getFechaInicio());
                dto.setFechaFin(permiso.getFechaFin());
                dto.setPermanente(permiso.getFechaFin() == null);
                dto.setOrigenSolicitud(permiso.getOrigenSolicitud());

                boolean vencido = !permiso.isActivo()
                        || (permiso.getFechaFin() != null && permiso.getFechaFin().isBefore(hoy));
                if (vencido) {
                    dto.setEstado("vencido");
                    dto.setDiasRestantes(permiso.getFechaFin() != null
                            ? Math.abs(ChronoUnit.DAYS.between(hoy, permiso.getFechaFin())) : 0);
                } else if (permiso.getFechaFin() == null) {
                    dto.setEstado("vigente");
                    dto.setDiasRestantes(-1);
                } else {
                    long dias = ChronoUnit.DAYS.between(hoy, permiso.getFechaFin());
                    dto.setDiasRestantes(dias);
                    dto.setEstado(dias <= 7 ? "por_vencer" : "vigente");
                }
                accesos.add(dto);
            }
        }

        accesos.sort(Comparator
                .comparing((AccesoUsuarioDTO a) -> "vencido".equals(a.getEstado()))
                .thenComparing(a -> a.getFechaFin() != null
                        ? a.getFechaFin() : LocalDate.MAX)
                .thenComparing(AccesoUsuarioDTO::getModuloLegible));

        model.addAttribute("accesos", accesos);
        model.addAttribute("totalAccesos", accesos.size());
        model.addAttribute("totalVigentes", accesos.stream()
                .filter(a -> "vigente".equals(a.getEstado())).count());
        model.addAttribute("totalPorVencer", accesos.stream()
                .filter(a -> "por_vencer".equals(a.getEstado())).count());
        model.addAttribute("totalVencidos", accesos.stream()
                .filter(a -> "vencido".equals(a.getEstado())).count());
        model.addAttribute("hoy", hoy);
        return "mis-accesos/index";
    }
}
