package com.example.demo.modulos.gestionUsuariosRolesPermisos.dto;

import java.time.LocalDate;

public class AccesoUsuarioDTO {

    private String modulo;
    private String moduloLegible;
    private String rol;
    private String color;
    private String nivel;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean permanente;
    private long diasRestantes;
    private String estado;
    private String origenSolicitud;

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getModuloLegible() {
        return moduloLegible;
    }

    public void setModuloLegible(String moduloLegible) {
        this.moduloLegible = moduloLegible;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isPermanente() {
        return permanente;
    }

    public void setPermanente(boolean permanente) {
        this.permanente = permanente;
    }

    public long getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(long diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getOrigenSolicitud() {
        return origenSolicitud;
    }

    public void setOrigenSolicitud(String origenSolicitud) {
        this.origenSolicitud = origenSolicitud;
    }
}
