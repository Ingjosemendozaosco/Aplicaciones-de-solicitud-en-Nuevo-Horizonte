package com.example.demo.modulos.gestionUsuariosRolesPermisos.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public class UsuarioRequestDTO {

    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean activo = true;
    private List<String> roles;
    private String tipoVigencia = "DEFINITIVO";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinRol;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getTipoVigencia() {
        return tipoVigencia;
    }

    public void setTipoVigencia(String tipoVigencia) {
        this.tipoVigencia = tipoVigencia;
    }

    public LocalDate getFechaFinRol() {
        return fechaFinRol;
    }

    public void setFechaFinRol(LocalDate fechaFinRol) {
        this.fechaFinRol = fechaFinRol;
    }
}
