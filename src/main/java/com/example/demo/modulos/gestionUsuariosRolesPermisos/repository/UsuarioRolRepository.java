package com.example.demo.modulos.gestionUsuariosRolesPermisos.repository;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    List<UsuarioRol> findByUsuarioId(Long idUsuario);

    List<UsuarioRol> findByActivoTrueAndFechaFinIsNotNullAndFechaFinBefore(LocalDate fecha);
}
