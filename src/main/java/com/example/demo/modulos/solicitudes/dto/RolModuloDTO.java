package com.example.demo.modulos.solicitudes.dto;

public class RolModuloDTO {

    private Long idPermiso;
    private Long idRol;
    private String nombre;
    private String color;
    private String nivel;
    private String nivelLegible;

    public Long getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Long idPermiso) {
        this.idPermiso = idPermiso;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getNivelLegible() {
        return nivelLegible;
    }

    public void setNivelLegible(String nivelLegible) {
        this.nivelLegible = nivelLegible;
    }
}
