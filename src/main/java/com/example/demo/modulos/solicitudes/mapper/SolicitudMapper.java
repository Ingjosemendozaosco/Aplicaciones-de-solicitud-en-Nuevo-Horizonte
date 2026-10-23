package com.example.demo.modulos.solicitudes.mapper;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.ColoresRol;
import com.example.demo.modulos.solicitudes.dto.RolColorDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudRequestDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.NivelAcceso;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import com.example.demo.modulos.solicitudes.entity.SolicitudAccesoDetalle;
import com.example.demo.modulos.solicitudes.entity.TipoSolicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SolicitudMapper {

    public Solicitud toEntity(SolicitudRequestDTO dto, Usuario usuario, String numeroSolicitud) {
        Solicitud solicitud = new Solicitud();
        solicitud.setNumeroSolicitud(numeroSolicitud);
        solicitud.setNombreSolicitante(dto.getNombreSolicitante());
        solicitud.setFechaSolicitud(dto.getFechaSolicitud() != null ? dto.getFechaSolicitud() : LocalDate.now());
        solicitud.setTipoSolicitud(dto.getTipoSolicitud() != null ? dto.getTipoSolicitud() : TipoSolicitud.INFORMACION);
        solicitud.setFechaInicioAcceso(dto.getFechaInicioAcceso() != null
                ? dto.getFechaInicioAcceso()
                : LocalDate.now());
        solicitud.setFechaFinAcceso(dto.isPermanente() ? null : dto.getFechaFinAcceso());
        solicitud.setPermanente(dto.isPermanente());
        if (solicitud.getTipoSolicitud() == TipoSolicitud.ACCESO
                && dto.getModulosAcceso() != null && !dto.getModulosAcceso().isEmpty()) {
            Map<String, String> niveles = dto.getNivelesModulo() != null ? dto.getNivelesModulo() : Map.of();
            List<String> etiquetas = new ArrayList<>();
            for (String modulo : dto.getModulosAcceso()) {
                NivelAcceso nivel = parsearNivel(niveles.get(modulo));
                solicitud.addDetalle(new SolicitudAccesoDetalle(solicitud, modulo, nivel));
                etiquetas.add(ModuloCatalogo.etiqueta(modulo) + " (" + legible(nivel) + ")");
            }
            solicitud.setRecursoModulo(String.join(", ", etiquetas));
        } else {
            solicitud.setRecursoModulo(dto.getRecursoModulo());
        }
        solicitud.setMotivo(dto.getMotivo());
        solicitud.setResponsableAprobacion(dto.getResponsableAprobacion());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setAreaSolicitante(dto.getAreaSolicitante());
        solicitud.setUsuario(usuario);
        solicitud.setFechaCreacion(LocalDateTime.now());
        return solicitud;
    }

    public static NivelAcceso parsearNivel(String valor) {
        return "EDICION".equalsIgnoreCase(valor) ? NivelAcceso.EDICION : NivelAcceso.LECTURA;
    }

    public static String legible(NivelAcceso nivel) {
        return nivel == NivelAcceso.EDICION ? "Edición" : "Lectura";
    }

    public SolicitudResponseDTO toResponse(Solicitud solicitud) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setIdSolicitud(solicitud.getId());
        dto.setNumeroSolicitud(solicitud.getNumeroSolicitud());
        dto.setNombreSolicitante(solicitud.getNombreSolicitante());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setTipoSolicitud(solicitud.getTipoSolicitud());
        dto.setFechaInicioAcceso(solicitud.getFechaInicioAcceso());
        dto.setFechaFinAcceso(solicitud.getFechaFinAcceso());
        dto.setDetallesAcceso(solicitud.getDetalles().stream()
                .map(d -> ModuloCatalogo.etiqueta(d.getModulo()) + " (" + legible(d.getNivel()) + ")")
                .toList());
        dto.setDetalles(solicitud.getDetalles().stream()
                .map(d -> new com.example.demo.modulos.solicitudes.dto.DetalleModuloDTO(
                        d.getModulo(),
                        ModuloCatalogo.etiqueta(d.getModulo()),
                        d.getNivel() != null ? d.getNivel().name() : "LECTURA",
                        legible(d.getNivel())))
                .toList());
        dto.setRolesSolicitante(solicitud.getUsuario() == null
                ? List.of()
                : solicitud.getUsuario().rolesVigentes().stream()
                        .map(r -> new RolColorDTO(r.getId(), r.getNombre(), ColoresRol.colorDe(r)))
                        .toList());
        dto.setPermanente(solicitud.isPermanente());
        dto.setPropuestaDetalles(solicitud.getPropuestaDetalles().stream()
                .map(d -> ModuloCatalogo.etiqueta(d.getModulo()) + " (" + legible(d.getNivel()) + ")")
                .toList());
        dto.setPropuestaPermanente(solicitud.getPropuestaPermanente());
        dto.setPropuestaFechaInicio(solicitud.getPropuestaFechaInicio());
        dto.setPropuestaFechaFin(solicitud.getPropuestaFechaFin());
        dto.setNotaPropuesta(solicitud.getNotaPropuesta());
        dto.setNombreInforme(solicitud.getNombreInforme());
        dto.setRecursoModulo(solicitud.getRecursoModulo());
        dto.setCargoSolicitante(solicitud.getUsuario() != null
                ? com.example.demo.modulos.gestionUsuariosRolesPermisos.service.CargoFormal
                        .de(solicitud.getUsuario().rolesVigentes())
                : null);
        dto.setMotivo(solicitud.getMotivo());
        dto.setResponsableAprobacion(solicitud.getResponsableAprobacion());
        dto.setEstado(solicitud.getEstado());
        dto.setVisto(solicitud.isVisto());
        dto.setFechaAprobacionRechazo(solicitud.getFechaAprobacionRechazo());
        dto.setComentarioAdmin(solicitud.getComentarioAdmin());
        dto.setAreaSolicitante(solicitud.getAreaSolicitante());
        dto.setNombreArchivo(solicitud.getNombreArchivo());
        dto.setTipoArchivo(solicitud.getTipoArchivo());
        dto.setContenidoArchivo(solicitud.getContenidoArchivo());
        dto.setUsernameSolicitante(solicitud.getUsuario() != null ? solicitud.getUsuario().getUsername() : null);
        dto.setFechaCreacion(solicitud.getFechaCreacion());
        return dto;
    }
}
