package app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class SolicitudController {

    private static class Solicitud {
        long id;
        String nombreSolicitante;
        LocalDate fechaSolicitud;
        String recurso;
        String motivo;
        String responsableAprobacion;
        String estado;
        LocalDateTime fechaAprobacionRechazo;

        Solicitud(long id, String nombre, LocalDate fecha, String recurso,
                  String motivo, String responsable, String estado,
                  LocalDateTime fechaAprobacionRechazo) {
            this.id = id;
            this.nombreSolicitante = nombre;
            this.fechaSolicitud = fecha;
            this.recurso = recurso;
            this.motivo = motivo;
            this.responsableAprobacion = responsable;
            this.estado = estado;
            this.fechaAprobacionRechazo = fechaAprobacionRechazo;
        }
    }

    private final AtomicLong idCounter = new AtomicLong(1000);
    private final Map<String, List<Solicitud>> solicitudesPorUsuario = new ConcurrentHashMap<>();

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("areas", List.of("Administracion", "Producto", "Ventas", "Gerencia"));
        return "dashboard";
    }

    @GetMapping("/formulario")
    public String formulario(Model model) {
        model.addAttribute("areasDestino", List.of("Administracion", "Producto", "Ventas", "Gerencia"));
        return "formulario";
    }

    @PostMapping("/formulario/enviar")
    public String enviarSolicitud(@RequestParam String nombreSolicitante,
                                  @RequestParam String fechaSolicitud,
                                  @RequestParam String recurso,
                                  @RequestParam String motivo,
                                  @RequestParam String responsableAprobacion,
                                  @RequestParam String areaDestino,
                                  Model model) {
        Solicitud s = new Solicitud(
            idCounter.getAndIncrement(),
            nombreSolicitante,
            LocalDate.parse(fechaSolicitud),
            recurso,
            motivo,
            responsableAprobacion,
            "PENDIENTE",
            null
        );
        solicitudesPorUsuario.computeIfAbsent(areaDestino, k -> new ArrayList<>()).add(s);

        model.addAttribute("mensajeExito", "Solicitud enviada correctamente");
        model.addAttribute("numeroSolicitud", s.id);
        return "formulario-exito";
    }

    @GetMapping("/admin/solicitudes")
    public String listarSolicitudesAdmin(Model model) {
        List<Solicitud> todas = new ArrayList<>();
        for (List<Solicitud> lista : solicitudesPorUsuario.values()) {
            todas.addAll(lista);
        }
        model.addAttribute("solicitudes", todas);
        return "admin-solicitudes";
    }

    @GetMapping("/area-producto/solicitudes")
    public String listarSolicitudesProducto(Model model) {
        model.addAttribute("solicitudes", solicitudesPorUsuario.getOrDefault("Producto", List.of()));
        return "area-solicitudes";
    }

    @GetMapping("/area-ventas/solicitudes")
    public String listarSolicitudesVentas(Model model) {
        model.addAttribute("solicitudes", solicitudesPorUsuario.getOrDefault("Ventas", List.of()));
        return "area-solicitudes";
    }

    @GetMapping("/gerencia/solicitudes")
    public String listarSolicitudesGerencia(Model model) {
        model.addAttribute("solicitudes", solicitudesPorUsuario.getOrDefault("Gerencia", List.of()));
        return "area-solicitudes";
    }

    @PostMapping("/admin/aprobar")
    public String aprobarSolicitud(@RequestParam long id,
                                   @RequestParam String estado,
                                   @RequestParam(required = false) String fechaAprobacionRechazo) {
        for (List<Solicitud> lista : solicitudesPorUsuario.values()) {
            for (Solicitud s : lista) {
                if (s.id == id) {
                    s.estado = estado;
                    s.fechaAprobacionRechazo = fechaAprobacionRechazo != null
                        ? LocalDateTime.parse(fechaAprobacionRechazo)
                        : LocalDateTime.now();
                    return "redirect:/admin/solicitudes";
                }
            }
        }
        return "redirect:/admin/solicitudes";
    }
}
