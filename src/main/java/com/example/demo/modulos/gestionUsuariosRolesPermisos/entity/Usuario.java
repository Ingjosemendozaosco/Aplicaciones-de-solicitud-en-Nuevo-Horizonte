package com.example.demo.modulos.gestionUsuariosRolesPermisos.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UsuarioRol> usuarioRoles = new LinkedHashSet<>();

    public void addRol(Rol rol) {
        addRol(rol, TipoVigencia.DEFINITIVO, null);
    }

    public void addRol(Rol rol, TipoVigencia tipoVigencia, LocalDate fechaFin) {
        boolean yaAsignado = usuarioRoles.stream()
                .anyMatch(ur -> ur.isActivo() && ur.getRol().getId() != null
                        && ur.getRol().getId().equals(rol.getId()));
        if (yaAsignado) {
            return;
        }
        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(this);
        usuarioRol.setRol(rol);
        usuarioRol.setTipoVigencia(tipoVigencia != null ? tipoVigencia : TipoVigencia.DEFINITIVO);
        usuarioRol.setFechaInicio(LocalDate.now());
        usuarioRol.setFechaFin(tipoVigencia == TipoVigencia.TEMPORAL ? fechaFin : null);
        usuarioRol.setActivo(true);
        usuarioRoles.add(usuarioRol);
    }

    public Set<Rol> rolesVigentes() {
        return usuarioRoles.stream()
                .filter(UsuarioRol::esVigente)
                .map(UsuarioRol::getRol)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Set<UsuarioRol> getUsuarioRoles() {
        return usuarioRoles;
    }

    public void setUsuarioRoles(Set<UsuarioRol> usuarioRoles) {
        this.usuarioRoles = usuarioRoles;
    }
}

