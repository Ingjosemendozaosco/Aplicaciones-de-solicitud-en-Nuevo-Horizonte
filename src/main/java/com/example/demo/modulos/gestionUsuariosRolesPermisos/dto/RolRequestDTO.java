package com.example.demo.modulos.gestionUsuariosRolesPermisos.dto;

public class RolRequestDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String color = "#2563eb";
    private Boolean activo = true;
    private boolean esSistema;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public boolean isEsSistema() {
        return esSistema;
    }

    public void setEsSistema(boolean esSistema) {
        this.esSistema = esSistema;
    }
}
