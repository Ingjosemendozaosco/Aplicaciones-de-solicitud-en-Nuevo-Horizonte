package com.example.demo.modulos.gestionUsuariosRolesPermisos.dto;

import java.util.List;

public class RolResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String color;
    private boolean esSistema;
    private boolean activo;
    private int totalPermisos;
    private List<String> modulos;

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

    public boolean isEsSistema() {
        return esSistema;
    }

    public void setEsSistema(boolean esSistema) {
        this.esSistema = esSistema;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getTotalPermisos() {
        return totalPermisos;
    }

    public void setTotalPermisos(int totalPermisos) {
        this.totalPermisos = totalPermisos;
    }

    public List<String> getModulos() {
        return modulos;
    }

    public void setModulos(List<String> modulos) {
        this.modulos = modulos;
    }
}
