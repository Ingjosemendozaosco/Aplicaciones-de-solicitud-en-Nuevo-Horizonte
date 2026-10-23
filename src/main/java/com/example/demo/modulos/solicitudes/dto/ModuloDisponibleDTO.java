package com.example.demo.modulos.solicitudes.dto;

import java.util.ArrayList;
import java.util.List;

public class ModuloDisponibleDTO {

    private String modulo;
    private String etiqueta;
    private List<RolModuloDTO> roles = new ArrayList<>();
    private String gradiente;

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public List<RolModuloDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RolModuloDTO> roles) {
        this.roles = roles;
    }

    public String getGradiente() {
        return gradiente;
    }

    public void setGradiente(String gradiente) {
        this.gradiente = gradiente;
    }
}
