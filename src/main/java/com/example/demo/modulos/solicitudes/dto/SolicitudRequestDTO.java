package com.example.demo.modulos.solicitudes.dto;

import com.example.demo.modulos.solicitudes.entity.TipoSolicitud;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SolicitudRequestDTO {

    private String nombreSolicitante;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaSolicitud;

    private TipoSolicitud tipoSolicitud = TipoSolicitud.INFORMACION;

    private String recursoModulo;

    private String tipoInformacion;

    private List<String> modulosAcceso;

    private Map<String, String> nivelesModulo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicioAcceso;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinAcceso;

    private boolean permanente;

    private String motivo;

    private String responsableAprobacion;

    private String areaSolicitante;

    private MultipartFile archivo;

    private boolean eliminarArchivo;


    // Getters and setters
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

    public String getRecursoModulo() {
        return recursoModulo;
    }

    public void setRecursoModulo(String recursoModulo) {
        this.recursoModulo = recursoModulo;
    }

    public String getTipoInformacion() {
        return tipoInformacion;
    }

    public void setTipoInformacion(String tipoInformacion) {
        this.tipoInformacion = tipoInformacion;
    }

    public boolean isPermanente() {
        return permanente;
    }

    public void setPermanente(boolean permanente) {
        this.permanente = permanente;
    }

    public TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(TipoSolicitud tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public List<String> getModulosAcceso() {
        return modulosAcceso;
    }

    public void setModulosAcceso(List<String> modulosAcceso) {
        this.modulosAcceso = modulosAcceso;
    }

    public Map<String, String> getNivelesModulo() {
        return nivelesModulo;
    }

    public void setNivelesModulo(Map<String, String> nivelesModulo) {
        this.nivelesModulo = nivelesModulo;
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

    public String getAreaSolicitante() {
        return areaSolicitante;
    }

    public void setAreaSolicitante(String areaSolicitante) {
        this.areaSolicitante = areaSolicitante;
    }

    public MultipartFile getArchivo() {
        return archivo;
    }

    public void setArchivo(MultipartFile archivo) {
        this.archivo = archivo;
    }

    public boolean isEliminarArchivo() {
        return eliminarArchivo;
    }

    public void setEliminarArchivo(boolean eliminarArchivo) {
        this.eliminarArchivo = eliminarArchivo;
    }
}
