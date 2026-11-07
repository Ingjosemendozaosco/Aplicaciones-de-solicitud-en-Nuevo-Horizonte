package com.example.demo.modulos.dashboard.controller;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.RolService;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.service.UsuarioService;
import com.example.demo.modulos.solicitudes.dto.SolicitudResponseDTO;
import com.example.demo.modulos.solicitudes.entity.EstadoSolicitud;
import com.example.demo.modulos.solicitudes.entity.TipoSolicitud;
import com.example.demo.modulos.solicitudes.service.SolicitudService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final SolicitudService solicitudService;
    private final UsuarioService usuarioService;
    private final RolService rolService;

    public DashboardController(SolicitudService solicitudService,
                               UsuarioService usuarioService,
                               RolService rolService) {
        this.solicitudService = solicitudService;
        this.usuarioService = usuarioService;
        this.rolService = rolService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        boolean administracion = tieneRol(authentication, "Administra");
        boolean gerencia = tieneRol(authentication, "Gerencia");
        boolean visionGlobal = administracion || gerencia;

        List<SolicitudResponseDTO> solicitudes = visionGlobal
                ? solicitudService.listarTodas()
                : solicitudService.listarPorUsuario(authentication.getName());

        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("pendientes", contar(solicitudes, EstadoSolicitud.PENDIENTE));
        model.addAttribute("aprobadas", contar(solicitudes, EstadoSolicitud.APROBADA));
        model.addAttribute("rechazadas", contar(solicitudes, EstadoSolicitud.RECHAZADA));
        model.addAttribute("borradores", contar(solicitudes, EstadoSolicitud.BORRADOR));
        model.addAttribute("visionGlobal", visionGlobal);
        model.addAttribute("recientes", recientes(solicitudes));
        model.addAttribute("semana", semanaStats(solicitudes));

        if (administracion) {
            model.addAttribute("totalUsuarios", usuarioService.total());
            model.addAttribute("totalRoles", rolService.total());
        }
        return "dashboard";
    }

    @GetMapping("/reportes")
    public String reportes(Authentication authentication, Model model) {
        boolean administracion = tieneRol(authentication, "Administra");
        boolean gerencia = tieneRol(authentication, "Gerencia");
        if (!administracion && !gerencia) {
            throw new org.springframework.security.access.AccessDeniedException("Sin permisos");
        }

        List<SolicitudResponseDTO> solicitudes = solicitudService.listarTodas();
        LocalDateTime ahora = LocalDateTime.now();

        long total = solicitudes.size();
        long pendientes = contar(solicitudes, EstadoSolicitud.PENDIENTE);
        long enRevision = contar(solicitudes, EstadoSolicitud.EN_REVISION);
        long aprobadas = contar(solicitudes, EstadoSolicitud.APROBADA);
        long rechazadas = contar(solicitudes, EstadoSolicitud.RECHAZADA);
        long borradores = contar(solicitudes, EstadoSolicitud.BORRADOR);
        long decididas = aprobadas + rechazadas;

        long tasaAprobacion = decididas == 0 ? 0 : Math.round(aprobadas * 100.0 / decididas);
        long tasaResolucion = total == 0 ? 0 : Math.round(decididas * 100.0 / total);

        // Tiempo medio de atención (desde la creación hasta la decisión)
        List<Long> tiemposMinutos = solicitudes.stream()
                .filter(s -> s.getFechaCreacion() != null && s.getFechaAprobacionRechazo() != null)
                .map(s -> Duration.between(s.getFechaCreacion(), s.getFechaAprobacionRechazo()).toMinutes())
                .filter(m -> m >= 0)
                .toList();

        double promedioHoras = tiemposMinutos.stream().mapToLong(Long::longValue).average().orElse(0) / 60.0;
        String tiempoPromedioValor;
        String tiempoPromedioUnidad;
        if (tiemposMinutos.isEmpty() || promedioHoras <= 0) {
            tiempoPromedioValor = "—";
            tiempoPromedioUnidad = "sin datos";
        } else if (promedioHoras < 48) {
            tiempoPromedioValor = String.format(Locale.US, "%.1f", promedioHoras);
            tiempoPromedioUnidad = "horas";
        } else {
            tiempoPromedioValor = String.format(Locale.US, "%.1f", promedioHoras / 24);
            tiempoPromedioUnidad = "días";
        }

        long dentroDe48 = tiemposMinutos.stream().filter(m -> m <= 48 * 60).count();
        long slaCumplido = tiemposMinutos.isEmpty()
                ? 0
                : Math.round(dentroDe48 * 100.0 / tiemposMinutos.size());

        // Tendencia de los últimos 7 días frente a los 7 anteriores
        LocalDateTime hace7 = ahora.minusDays(7);
        LocalDateTime hace14 = ahora.minusDays(14);
        long ultimos7 = solicitudes.stream()
                .filter(s -> s.getFechaCreacion() != null && !s.getFechaCreacion().isBefore(hace7))
                .count();
        long previos7 = solicitudes.stream()
                .filter(s -> s.getFechaCreacion() != null
                        && !s.getFechaCreacion().isBefore(hace14)
                        && s.getFechaCreacion().isBefore(hace7))
                .count();
        long variacionSemana = previos7 == 0
                ? (ultimos7 == 0 ? 0 : 100)
                : Math.round((ultimos7 - previos7) * 100.0 / previos7);
        String promedioDiario = String.format(Locale.US, "%.1f", ultimos7 / 7.0);

        // Distribución por estado + gradiente del donut
        List<Map<String, Object>> porEstado = new ArrayList<>();
        agregarEstado(porEstado, "Borradores", borradores, "#94a3b8", total);
        agregarEstado(porEstado, "En revisión", enRevision, "#6366f1", total);
        agregarEstado(porEstado, "Pendientes", pendientes, "#f59e0b", total);
        agregarEstado(porEstado, "Aprobadas", aprobadas, "#22c55e", total);
        agregarEstado(porEstado, "Rechazadas", rechazadas, "#ef4444", total);

        StringBuilder gradiente = new StringBuilder("conic-gradient(");
        if (total == 0) {
            gradiente.append("#e2e8f0 0% 100%");
        } else {
            double acumulado = 0;
            for (Map<String, Object> estado : porEstado) {
                long valor = ((Number) estado.get("total")).longValue();
                double inicio = acumulado;
                double fin = acumulado + (valor * 100.0 / total);
                gradiente.append(estado.get("color")).append(' ')
                        .append(String.format(Locale.US, "%.2f", inicio)).append("% ")
                        .append(String.format(Locale.US, "%.2f", fin)).append("% ");
                acumulado = fin;
            }
        }
        gradiente.append(')');

        // Solicitudes por área (top 6)
        Map<String, Long> agrupadoPorArea = solicitudes.stream()
                .filter(s -> s.getAreaSolicitante() != null && !s.getAreaSolicitante().isBlank())
                .collect(Collectors.groupingBy(SolicitudResponseDTO::getAreaSolicitante,
                        TreeMap::new, Collectors.counting()));

        List<Map.Entry<String, Long>> areasOrdenadas = agrupadoPorArea.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        List<Map<String, Object>> porArea = new ArrayList<>();
        for (Map.Entry<String, Long> entry : areasOrdenadas.stream().limit(6).toList()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("area", entry.getKey());
            item.put("total", entry.getValue());
            item.put("pct", total == 0 ? 0 : Math.round(entry.getValue() * 100.0 / total));
            porArea.add(item);
        }

        String areaTop = areasOrdenadas.isEmpty() ? "Sin datos" : areasOrdenadas.get(0).getKey();
        long areaTopTotal = areasOrdenadas.isEmpty() ? 0 : areasOrdenadas.get(0).getValue();

        // Distribución por tipo de solicitud
        long totalInfo = solicitudes.stream()
                .filter(s -> s.getTipoSolicitud() == TipoSolicitud.INFORMACION).count();
        long totalAcceso = solicitudes.stream()
                .filter(s -> s.getTipoSolicitud() == TipoSolicitud.ACCESO).count();

        List<Map<String, Object>> porTipo = new ArrayList<>();
        porTipo.add(tipoItem("Información", "info", totalInfo, total, "#0ea5e9"));
        porTipo.add(tipoItem("Acceso a módulos", "acceso", totalAcceso, total, "#6366f1"));

        // Serie de las últimas 8 semanas
        List<Map<String, Object>> serieSemanas = serieSemanas(solicitudes, ahora.toLocalDate());
        long maxSerie = serieSemanas.stream()
                .mapToLong(s -> ((Number) s.get("total")).longValue())
                .max().orElse(0);
        for (Map<String, Object> semana : serieSemanas) {
            long valor = ((Number) semana.get("total")).longValue();
            semana.put("pct", maxSerie == 0 ? 0 : Math.round(valor * 100.0 / maxSerie));
        }

        String fechaReporte = ahora.format(DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", new Locale("es", "ES")));

        model.addAttribute("totalSolicitudes", total);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enRevision", enRevision);
        model.addAttribute("aprobadas", aprobadas);
        model.addAttribute("rechazadas", rechazadas);
        model.addAttribute("borradores", borradores);
        model.addAttribute("decididas", decididas);
        model.addAttribute("tasaAprobacion", tasaAprobacion);
        model.addAttribute("tasaResolucion", tasaResolucion);
        model.addAttribute("slaCumplido", slaCumplido);
        model.addAttribute("tiempoPromedioValor", tiempoPromedioValor);
        model.addAttribute("tiempoPromedioUnidad", tiempoPromedioUnidad);
        model.addAttribute("ultimos7", ultimos7);
        model.addAttribute("variacionSemana", variacionSemana);
        model.addAttribute("promedioDiario", promedioDiario);
        model.addAttribute("porEstado", porEstado);
        model.addAttribute("donutEstados", gradiente.toString());
        model.addAttribute("porArea", porArea);
        model.addAttribute("areaTop", areaTop);
        model.addAttribute("areaTopTotal", areaTopTotal);
        model.addAttribute("porTipo", porTipo);
        model.addAttribute("totalInfo", totalInfo);
        model.addAttribute("totalAcceso", totalAcceso);
        model.addAttribute("serieSemanas", serieSemanas);
        model.addAttribute("maxSerie", maxSerie);
        model.addAttribute("fechaReporte", fechaReporte);
        return "reportes";
    }

    private void agregarEstado(List<Map<String, Object>> destino, String etiqueta,
                               long valor, String color, long total) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("etiqueta", etiqueta);
        item.put("total", valor);
        item.put("pct", total == 0 ? 0 : Math.round(valor * 100.0 / total));
        item.put("color", color);
        destino.add(item);
    }

    private Map<String, Object> tipoItem(String etiqueta, String clave, long valor,
                                         long total, String color) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("etiqueta", etiqueta);
        item.put("clave", clave);
        item.put("total", valor);
        item.put("pct", total == 0 ? 0 : Math.round(valor * 100.0 / total));
        item.put("color", color);
        return item;
    }

    private List<Map<String, Object>> serieSemanas(List<SolicitudResponseDTO> solicitudes, LocalDate hoy) {
        LocalDate lunesActual = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<LocalDate, Long> conteo = new LinkedHashMap<>();
        for (int i = 7; i >= 0; i--) {
            conteo.put(lunesActual.minusWeeks(i), 0L);
        }
        for (SolicitudResponseDTO s : solicitudes) {
            if (s.getFechaCreacion() == null) {
                continue;
            }
            LocalDate semana = s.getFechaCreacion().toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            if (conteo.containsKey(semana)) {
                conteo.put(semana, conteo.get(semana) + 1);
            }
        }
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM");
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : conteo.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("etiqueta", entry.getKey().format(formato));
            item.put("inicio", entry.getKey().toString());
            item.put("total", entry.getValue());
            lista.add(item);
        }
        return lista;
    }

    private long contar(List<SolicitudResponseDTO> solicitudes, EstadoSolicitud estado) {
        return solicitudes.stream().filter(s -> s.getEstado() == estado).count();
    }

    private List<SolicitudResponseDTO> recientes(List<SolicitudResponseDTO> solicitudes) {
        return solicitudes.stream()
                .sorted(Comparator.comparing(SolicitudResponseDTO::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList();
    }

    private List<Map<String, Object>> semanaStats(List<SolicitudResponseDTO> solicitudes) {
        LocalDate hoy = LocalDate.now();
        Map<LocalDate, Long> conteo = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            conteo.put(dia, 0L);
        }
        for (SolicitudResponseDTO s : solicitudes) {
            if (s.getFechaCreacion() != null) {
                LocalDate fecha = s.getFechaCreacion().toLocalDate();
                if (conteo.containsKey(fecha)) {
                    conteo.put(fecha, conteo.get(fecha) + 1);
                }
            }
        }
        List<Map<String, Object>> lista = new ArrayList<>();
        String[] diasEs = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"};
        for (Map.Entry<LocalDate, Long> entry : conteo.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            int diaSemana = entry.getKey().getDayOfWeek().getValue() - 1;
            item.put("dia", diasEs[diaSemana]);
            item.put("fecha", entry.getKey().toString());
            item.put("count", entry.getValue());
            lista.add(item);
        }
        return lista;
    }

    private boolean tieneRol(Authentication authentication, String fragmento) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains(fragmento));
    }
}
