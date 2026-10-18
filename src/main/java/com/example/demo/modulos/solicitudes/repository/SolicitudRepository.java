package com.example.demo.modulos.solicitudes.repository;

import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstado(EstadoSolicitud estado);

    List<Solicitud> findByAreaSolicitante(String areaSolicitante);

    List<Solicitud> findByAreaSolicitanteAndEstado(String areaSolicitante, EstadoSolicitud estado);

    List<Solicitud> findByUsuarioUsername(String username);

    boolean existsByNumeroSolicitud(String numeroSolicitud);

    @Query(value = "SELECT nextval('seq_numero_solicitud')", nativeQuery = true)
    Long siguienteNumero();
}
