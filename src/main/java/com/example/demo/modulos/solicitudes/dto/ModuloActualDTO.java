package com.example.demo.modulos.solicitudes.dto;

import java.util.ArrayList;
import java.util.List;

public class ModuloActualDTO {

    private String modulo;
    private String etiqueta;
    private boolean tieneAcceso;
    private String nivel;
    private String nivelLegible;
    private String gradiente;
    private List<RolModuloDTO> roles = new ArrayList<>();

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

    public boolean isTieneAcceso() {
        return tieneAcceso;
    }

    public void setTieneAcceso(boolean tieneAcceso) {
        this.tieneAcceso = tieneAcceso;
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

    public String getGradiente() {
        return gradiente;
    }

    public void setGradiente(String gradiente) {
        this.gradiente = gradiente;
    }

    public List<RolModuloDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RolModuloDTO> roles) {
        this.roles = roles;
    }
}
