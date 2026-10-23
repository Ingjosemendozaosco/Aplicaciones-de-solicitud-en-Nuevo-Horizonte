package com.example.demo.modulos.solicitudes.dto;

import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.TipoSolicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitudResponseDTO {

    private Long idSolicitud;
    private String numeroSolicitud;
    private String nombreSolicitante;
    private LocalDate fechaSolicitud;
    private TipoSolicitud tipoSolicitud;
    private LocalDate fechaInicioAcceso;
    private LocalDate fechaFinAcceso;
    private List<String> detallesAcceso;
    private List<DetalleModuloDTO> detalles;
    private List<RolColorDTO> rolesSolicitante;
    private boolean permanente;
    private List<String> propuestaDetalles;
    private Boolean propuestaPermanente;
    private LocalDate propuestaFechaInicio;
    private LocalDate propuestaFechaFin;
    private String notaPropuesta;
    private String nombreInforme;
    private String recursoModulo;
    private String cargoSolicitante;
    private String motivo;
    private String responsableAprobacion;
    private EstadoSolicitud estado;
    private boolean visto;
    private LocalDateTime fechaAprobacionRechazo;
    private String comentarioAdmin;
    private String areaSolicitante;
    private String nombreArchivo;
    private String tipoArchivo;
    private byte[] contenidoArchivo;
    private String usernameSolicitante;
    private LocalDateTime fechaCreacion;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getNumeroSolicitud() {
        return numeroSolicitud;
    }

    public void setNumeroSolicitud(String numeroSolicitud) {
        this.numeroSolicitud = numeroSolicitud;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(TipoSolicitud tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public LocalDate getFechaFinAcceso() {
        return fechaFinAcceso;
    }

    public void setFechaFinAcceso(LocalDate fechaFinAcceso) {
        this.fechaFinAcceso = fechaFinAcceso;
    }

    public LocalDate getFechaInicioAcceso() {
        return fechaInicioAcceso;
    }

    public void setFechaInicioAcceso(LocalDate fechaInicioAcceso) {
        this.fechaInicioAcceso = fechaInicioAcceso;
    }

    public String getCargoSolicitante() {
        return cargoSolicitante;
    }

    public void setCargoSolicitante(String cargoSolicitante) {
        this.cargoSolicitante = cargoSolicitante;
    }

    public List<String> getDetallesAcceso() {
        return detallesAcceso;
    }

    public void setDetallesAcceso(List<String> detallesAcceso) {
        this.detallesAcceso = detallesAcceso;
    }

    public List<DetalleModuloDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleModuloDTO> detalles) {
        this.detalles = detalles;
    }

    public List<RolColorDTO> getRolesSolicitante() {
        return rolesSolicitante;
    }

    public void setRolesSolicitante(List<RolColorDTO> rolesSolicitante) {
        this.rolesSolicitante = rolesSolicitante;
    }

    public boolean isPermanente() {
        return permanente;
    }

    public void setPermanente(boolean permanente) {
        this.permanente = permanente;
    }

    public List<String> getPropuestaDetalles() {
        return propuestaDetalles;
    }

    public void setPropuestaDetalles(List<String> propuestaDetalles) {
        this.propuestaDetalles = propuestaDetalles;
    }

    public Boolean getPropuestaPermanente() {
        return propuestaPermanente;
    }

    public void setPropuestaPermanente(Boolean propuestaPermanente) {
        this.propuestaPermanente = propuestaPermanente;
    }

    public LocalDate getPropuestaFechaInicio() {
        return propuestaFechaInicio;
    }

    public void setPropuestaFechaInicio(LocalDate propuestaFechaInicio) {
        this.propuestaFechaInicio = propuestaFechaInicio;
    }

    public LocalDate getPropuestaFechaFin() {
        return propuestaFechaFin;
    }

    public void setPropuestaFechaFin(LocalDate propuestaFechaFin) {
        this.propuestaFechaFin = propuestaFechaFin;
    }

    public String getNotaPropuesta() {
        return notaPropuesta;
    }

    public void setNotaPropuesta(String notaPropuesta) {
        this.notaPropuesta = notaPropuesta;
    }

    public String getNombreInforme() {
        return nombreInforme;
    }

    public void setNombreInforme(String nombreInforme) {
        this.nombreInforme = nombreInforme;
    }

    public String getTipoSolicitudLegible() {
        return tipoSolicitud == TipoSolicitud.ACCESO
                ? "Solicitud de acceso a módulos"
                : "Solicitud de información";
    }

    public String getRecursoModuloEtiqueta() {
        return tipoSolicitud == TipoSolicitud.ACCESO
                ? "Módulos solicitados"
                : "Recurso o información solicitada";
    }

    public String getRecursoModulo() {
        return recursoModulo;
    }

    public void setRecursoModulo(String recursoModulo) {
        this.recursoModulo = recursoModulo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getResponsableAprobacion() {
        return responsableAprobacion;
    }

    public void setResponsableAprobacion(String responsableAprobacion) {
        this.responsableAprobacion = responsableAprobacion;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public boolean isVisto() {
        return visto;
    }

    public void setVisto(boolean visto) {
        this.visto = visto;
    }

    public LocalDateTime getFechaAprobacionRechazo() {
        return fechaAprobacionRechazo;
    }

    public void setFechaAprobacionRechazo(LocalDateTime fechaAprobacionRechazo) {
        this.fechaAprobacionRechazo = fechaAprobacionRechazo;
    }

    public String getComentarioAdmin() {
        return comentarioAdmin;
    }

    public void setComentarioAdmin(String comentarioAdmin) {
        this.comentarioAdmin = comentarioAdmin;
    }

    public String getAreaSolicitante() {
        return areaSolicitante;
    }

    public void setAreaSolicitante(String areaSolicitante) {
        this.areaSolicitante = areaSolicitante;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public byte[] getContenidoArchivo() {
        return contenidoArchivo;
    }

    public void setContenidoArchivo(byte[] contenidoArchivo) {
        this.contenidoArchivo = contenidoArchivo;
    }

    public String getUsernameSolicitante() {
        return usernameSolicitante;
    }

    public void setUsernameSolicitante(String usernameSolicitante) {
        this.usernameSolicitante = usernameSolicitante;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
