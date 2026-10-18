package com.example.demo.modulos.solicitudes.dto;

import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;

public class SolicitudDecisionDTO {

    private EstadoSolicitud estado;

    private String comentario;

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
