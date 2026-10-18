package com.example.demo.modulos.solicitudes.mapper;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.solicitudes.dto.SolicitudRequestDTO;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class SolicitudMapper {

    public Solicitud toEntity(SolicitudRequestDTO dto, Usuario usuario, String numeroSolicitud) {
        Solicitud solicitud = new Solicitud();
        solicitud.setNumeroSolicitud(numeroSolicitud);
        solicitud.setNombreSolicitante(dto.getNombreSolicitante());
        solicitud.setFechaSolicitud(dto.getFechaSolicitud() != null ? dto.getFechaSolicitud() : LocalDate.now());
        solicitud.setRecursoModulo(dto.getRecursoModulo());
        solicitud.setMotivo(dto.getMotivo());
        solicitud.setResponsableAprobacion(dto.getResponsableAprobacion());
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setAreaSolicitante(dto.getAreaSolicitante());
        solicitud.setUsuario(usuario);
        solicitud.setFechaCreacion(LocalDateTime.now());
        return solicitud;
    }

    public SolicitudResponseDTO toResponse(Solicitud solicitud) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setIdSolicitud(solicitud.getId());
        dto.setNumeroSolicitud(solicitud.getNumeroSolicitud());
        dto.setNombreSolicitante(solicitud.getNombreSolicitante());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setRecursoModulo(solicitud.getRecursoModulo());
        dto.setMotivo(solicitud.getMotivo());
        dto.setResponsableAprobacion(solicitud.getResponsableAprobacion());
        dto.setEstado(solicitud.getEstado());
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
