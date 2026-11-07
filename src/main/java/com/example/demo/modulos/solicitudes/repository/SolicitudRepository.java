package com.example.demo.modulos.solicitudes.repository;

import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstado(EstadoSolicitud estado);

    long countByEstado(EstadoSolicitud estado);

    long countByUsuarioUsernameAndEstado(String username, EstadoSolicitud estado);

    List<Solicitud> findByAreaSolicitante(String areaSolicitante);

    List<Solicitud> findByAreaSolicitanteAndEstado(String areaSolicitante, EstadoSolicitud estado);

    List<Solicitud> findByUsuarioUsername(String username);

    boolean existsByNumeroSolicitud(String numeroSolicitud);

    List<Solicitud> findByEstadoAndFechaCreacionBefore(EstadoSolicitud estado, LocalDateTime fechaLimite);

    @Query("""
            select s from Solicitud s
            where (:estado is null or s.estado = :estado)
              and (:area is null or s.areaSolicitante = :area)
              and (:desde is null or s.fechaSolicitud >= :desde)
              and (:hasta is null or s.fechaSolicitud <= :hasta)
              and (:username is null or s.usuario.username = :username)
              and (:texto is null
                   or lower(s.numeroSolicitud) like :texto
                   or lower(s.nombreSolicitante) like :texto
                   or lower(s.areaSolicitante) like :texto
                   or lower(s.recursoModulo) like :texto
                   or lower(s.motivo) like :texto)
            order by s.fechaCreacion desc
            """)
    List<Solicitud> buscarFiltrado(@Param("estado") EstadoSolicitud estado,
                                   @Param("area") String area,
                                   @Param("desde") LocalDate desde,
                                   @Param("hasta") LocalDate hasta,
                                   @Param("username") String username,
                                   @Param("texto") String texto);

    @Query("select distinct s.areaSolicitante from Solicitud s where s.areaSolicitante is not null order by s.areaSolicitante")
    List<String> areasDistintas();

    @Query(value = "SELECT nextval('seq_numero_solicitud')", nativeQuery = true)
    Long siguienteNumero();
}
