package com.example.demo.modulos.solicitudes.dto;

public class RolColorDTO {

    private Long idRol;
    private String nombre;
    private String color;

    public RolColorDTO() {
    }

    public RolColorDTO(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public RolColorDTO(Long idRol, String nombre, String color) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.color = color;
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
}
