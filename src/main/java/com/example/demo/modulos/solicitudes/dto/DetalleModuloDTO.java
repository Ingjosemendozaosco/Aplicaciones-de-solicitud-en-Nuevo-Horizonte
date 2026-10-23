package com.example.demo.modulos.solicitudes.dto;

public class DetalleModuloDTO {

    private String modulo;
    private String etiqueta;
    private String nivel;
    private String nivelLegible;

    public DetalleModuloDTO() {
    }

    public DetalleModuloDTO(String modulo, String etiqueta, String nivel, String nivelLegible) {
        this.modulo = modulo;
        this.etiqueta = etiqueta;
        this.nivel = nivel;
        this.nivelLegible = nivelLegible;
    }

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
