package com.example.demo.modulos.solicitudes.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class SolicitudRequestDTO {

    private String nombreSolicitante;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaSolicitud;

    private String recursoModulo;

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
