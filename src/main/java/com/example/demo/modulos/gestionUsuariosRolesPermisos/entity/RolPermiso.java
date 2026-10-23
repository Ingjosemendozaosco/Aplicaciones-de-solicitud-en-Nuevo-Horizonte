package com.example.demo.modulos.gestionUsuariosRolesPermisos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "rol_permiso")
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "modulo", nullable = false, length = 60)
    private String modulo;

    @Column(name = "puede_leer", nullable = false)
    private boolean puedeLeer;

    @Column(name = "puede_crear", nullable = false)
    private boolean puedeCrear;

    @Column(name = "puede_actualizar", nullable = false)
    private boolean puedeActualizar;

    @Column(name = "puede_eliminar", nullable = false)
    private boolean puedeEliminar;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vigencia", length = 20)
    private TipoVigencia tipoVigencia = TipoVigencia.DEFINITIVO;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "origen_solicitud", length = 20)
    private String origenSolicitud;

    public boolean esVigente() {
        return activo
                && (fechaFin == null || !fechaFin.isBefore(LocalDate.now()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public boolean isPuedeLeer() {
        return puedeLeer;
    }

    public void setPuedeLeer(boolean puedeLeer) {
        this.puedeLeer = puedeLeer;
    }

    public boolean isPuedeCrear() {
        return puedeCrear;
    }

    public void setPuedeCrear(boolean puedeCrear) {
        this.puedeCrear = puedeCrear;
    }

    public boolean isPuedeActualizar() {
        return puedeActualizar;
    }

    public void setPuedeActualizar(boolean puedeActualizar) {
        this.puedeActualizar = puedeActualizar;
    }

    public boolean isPuedeEliminar() {
        return puedeEliminar;
    }

    public void setPuedeEliminar(boolean puedeEliminar) {
        this.puedeEliminar = puedeEliminar;
    }

    public TipoVigencia getTipoVigencia() {
        return tipoVigencia;
    }

    public void setTipoVigencia(TipoVigencia tipoVigencia) {
        this.tipoVigencia = tipoVigencia;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getOrigenSolicitud() {
        return origenSolicitud;
    }

    public void setOrigenSolicitud(String origenSolicitud) {
        this.origenSolicitud = origenSolicitud;
    }
}
