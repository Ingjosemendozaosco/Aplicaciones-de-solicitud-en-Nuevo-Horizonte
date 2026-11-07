package com.example.demo.modulos.auditoria.repository;

import com.example.demo.modulos.auditoria.entity.AuditoriaEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEvento, Long> {

    /*
     * Los filtros opcionales usan coalesce en lugar de "(:param is null or ...)".
     * Con PostgreSQL, un parametro nulo dentro de "? is null" no tiene tipo
     * inferible y la consulta falla con "could not determine data type of parameter".
     */
    @Query("""
            select e from AuditoriaEvento e
            where e.accion = coalesce(:accion, e.accion)
              and e.entidad = coalesce(:entidad, e.entidad)
              and lower(e.actor) like coalesce(:actor, lower(e.actor))
              and e.fecha >= coalesce(:desde, e.fecha)
              and e.fecha <= coalesce(:hasta, e.fecha)
              and (coalesce(:texto, '') = ''
                   or lower(e.detalle) like :texto
                   or lower(e.actor) like :texto
                   or lower(e.entidad) like :texto
                   or lower(e.entidadId) like :texto)
            """)
    Page<AuditoriaEvento> buscar(@Param("accion") String accion,
                                 @Param("entidad") String entidad,
                                 @Param("actor") String actor,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta,
                                 @Param("texto") String texto,
                                 Pageable pageable);

    @Query("select distinct e.accion from AuditoriaEvento e order by e.accion")
    java.util.List<String> accionesDistintas();

    @Query("select distinct e.entidad from AuditoriaEvento e order by e.entidad")
    java.util.List<String> entidadesDistintas();
}
