package com.example.demo.modulos.solicitudes.entity;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @Column(name = "numero_solicitud", nullable = false, unique = true, length = 20)
    private String numeroSolicitud;

    @Column(name = "nombre_solicitante", nullable = false, length = 120)
    private String nombreSolicitante;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "recurso_modulo", nullable = false, length = 255)
    private String recursoModulo;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "responsable_aprobacion", nullable = false, length = 120)
    private String responsableAprobacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false, length = 20)
    private TipoSolicitud tipoSolicitud = TipoSolicitud.INFORMACION;

    @Column(name = "fecha_inicio_acceso")
    private LocalDate fechaInicioAcceso;

    @Column(name = "fecha_fin_acceso")
    private LocalDate fechaFinAcceso;

    @Column(name = "fecha_recordatorio_sla")
    private LocalDateTime fechaRecordatorioSla;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SolicitudAccesoDetalle> detalles = new ArrayList<>();

    @Column(name = "permanente")
    private Boolean permanente = Boolean.FALSE;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SolicitudPropuestaDetalle> propuestaDetalles = new LinkedHashSet<>();

    @Column(name = "propuesta_permanente")
    private Boolean propuestaPermanente;

    @Column(name = "propuesta_fecha_inicio")
    private LocalDate propuestaFechaInicio;

    @Column(name = "propuesta_fecha_fin")
    private LocalDate propuestaFechaFin;

    @Column(name = "nota_propuesta", columnDefinition = "TEXT")
    private String notaPropuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "visto")
    private Boolean visto = Boolean.FALSE;

    @Column(name = "fecha_aprobacion_rechazo")
    private LocalDateTime fechaAprobacionRechazo;

    @Column(name = "comentario_admin", columnDefinition = "TEXT")
    private String comentarioAdmin;

    @Column(name = "area_solicitante", nullable = false, length = 60)
    private String areaSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "tipo_archivo")
    private String tipoArchivo;

    @Column(name = "contenido_archivo", columnDefinition = "bytea")
    private byte[] contenidoArchivo;

    @Column(name = "nombre_informe")
    private String nombreInforme;

    @Column(name = "tipo_informe")
    private String tipoInforme;

    @Column(name = "contenido_informe", columnDefinition = "bytea")
    private byte[] contenidoInforme;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroSolicitud() {
        return numeroSolicitud;
    }

    public void setNumeroSolicitud(String numeroSolicitud) {
        this.numeroSolicitud = numeroSolicitud;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getRecursoModulo() {
        return recursoModulo;
    }

    public void setRecursoModulo(String recursoModulo) {
        this.recursoModulo = recursoModulo;
    }

    public TipoSolicitud getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(TipoSolicitud tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public LocalDate getFechaFinAcceso() {
        return fechaFinAcceso;
    }

    public void setFechaFinAcceso(LocalDate fechaFinAcceso) {
        this.fechaFinAcceso = fechaFinAcceso;
    }

    public LocalDate getFechaInicioAcceso() {
        return fechaInicioAcceso;
    }

    public void setFechaInicioAcceso(LocalDate fechaInicioAcceso) {
        this.fechaInicioAcceso = fechaInicioAcceso;
    }

    public LocalDateTime getFechaRecordatorioSla() {
        return fechaRecordatorioSla;
    }

    public void setFechaRecordatorioSla(LocalDateTime fechaRecordatorioSla) {
        this.fechaRecordatorioSla = fechaRecordatorioSla;
    }

    public List<SolicitudAccesoDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<SolicitudAccesoDetalle> detalles) {
        this.detalles = detalles;
    }

    public void addDetalle(SolicitudAccesoDetalle detalle) {
        detalle.setSolicitud(this);
        this.detalles.add(detalle);
    }

    public boolean isPermanente() {
        return Boolean.TRUE.equals(permanente);
    }

    public void setPermanente(boolean permanente) {
        this.permanente = permanente;
    }

    public Set<SolicitudPropuestaDetalle> getPropuestaDetalles() {
        return propuestaDetalles;
    }

    public void setPropuestaDetalles(Set<SolicitudPropuestaDetalle> propuestaDetalles) {
        this.propuestaDetalles = propuestaDetalles;
    }

    public void addPropuestaDetalle(SolicitudPropuestaDetalle detalle) {
        detalle.setSolicitud(this);
        this.propuestaDetalles.add(detalle);
    }

    public Boolean getPropuestaPermanente() {
        return propuestaPermanente;
    }

    public void setPropuestaPermanente(Boolean propuestaPermanente) {
        this.propuestaPermanente = propuestaPermanente;
    }

    public LocalDate getPropuestaFechaInicio() {
        return propuestaFechaInicio;
    }

    public void setPropuestaFechaInicio(LocalDate propuestaFechaInicio) {
        this.propuestaFechaInicio = propuestaFechaInicio;
    }

    public LocalDate getPropuestaFechaFin() {
        return propuestaFechaFin;
    }

    public void setPropuestaFechaFin(LocalDate propuestaFechaFin) {
        this.propuestaFechaFin = propuestaFechaFin;
    }

    public String getNotaPropuesta() {
        return notaPropuesta;
    }

    public void setNotaPropuesta(String notaPropuesta) {
        this.notaPropuesta = notaPropuesta;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getResponsableAprobacion() {
        return responsableAprobacion;
    }

    public void setResponsableAprobacion(String responsableAprobacion) {
        this.responsableAprobacion = responsableAprobacion;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public boolean isVisto() {
        return Boolean.TRUE.equals(visto);
    }

    public void setVisto(Boolean visto) {
        this.visto = visto;
    }

    public LocalDateTime getFechaAprobacionRechazo() {
        return fechaAprobacionRechazo;
    }

    public void setFechaAprobacionRechazo(LocalDateTime fechaAprobacionRechazo) {
        this.fechaAprobacionRechazo = fechaAprobacionRechazo;
    }

    public String getComentarioAdmin() {
        return comentarioAdmin;
    }

    public void setComentarioAdmin(String comentarioAdmin) {
        this.comentarioAdmin = comentarioAdmin;
    }

    public String getAreaSolicitante() {
        return areaSolicitante;
    }

    public void setAreaSolicitante(String areaSolicitante) {
        this.areaSolicitante = areaSolicitante;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public byte[] getContenidoArchivo() {
        return contenidoArchivo;
    }

    public void setContenidoArchivo(byte[] contenidoArchivo) {
        this.contenidoArchivo = contenidoArchivo;
    }

    public String getNombreInforme() {
        return nombreInforme;
    }

    public void setNombreInforme(String nombreInforme) {
        this.nombreInforme = nombreInforme;
    }

    public String getTipoInforme() {
        return tipoInforme;
    }

    public void setTipoInforme(String tipoInforme) {
        this.tipoInforme = tipoInforme;
    }

    public byte[] getContenidoInforme() {
        return contenidoInforme;
    }

    public void setContenidoInforme(byte[] contenidoInforme) {
        this.contenidoInforme = contenidoInforme;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
