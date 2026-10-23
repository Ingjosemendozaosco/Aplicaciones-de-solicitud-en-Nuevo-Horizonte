package com.example.demo.modulos.notificaciones.repository;

import com.example.demo.modulos.notificaciones.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioUsernameOrderByFechaCreacionDesc(String username);

    long countByUsuarioUsernameAndLeidaFalse(String username);

    Optional<Notificacion> findByIdAndUsuarioUsername(Long id, String username);

    @Modifying
    @Query("update Notificacion n set n.leida = true " +
            "where n.usuario.username = :username and n.leida = false")
    int marcarTodasLeidas(@Param("username") String username);
}
