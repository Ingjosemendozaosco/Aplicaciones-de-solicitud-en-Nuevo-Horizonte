package com.example.demo.modulos.solicitudes.dto;

import java.util.ArrayList;
import java.util.List;

public class AreaDisponibleDTO {

    private String nombre;
    private String color;
    private List<String> modulos = new ArrayList<>();

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

    public List<String> getModulos() {
        return modulos;
    }

    public void setModulos(List<String> modulos) {
        this.modulos = modulos;
    }
}
