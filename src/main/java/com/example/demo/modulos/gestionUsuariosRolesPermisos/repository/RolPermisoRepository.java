package com.example.demo.modulos.gestionUsuariosRolesPermisos.repository;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    List<RolPermiso> findByRolId(Long idRol);

    Optional<RolPermiso> findByRolIdAndModulo(Long idRol, String modulo);
}
