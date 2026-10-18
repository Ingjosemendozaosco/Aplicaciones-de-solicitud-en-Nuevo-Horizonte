package com.example.demo.config;

import com.example.demo.modulos.ModuloCatalogo;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.RolRepository;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String PASSWORD_INICIAL = "admin123";

    private record PermisoDef(String modulo, boolean leer, boolean crear,
                              boolean actualizar, boolean eliminar) {
    }

    private static PermisoDef leer(String modulo) {
        return new PermisoDef(modulo, true, false, false, false);
    }

    private static PermisoDef lcr(String modulo) {
        return new PermisoDef(modulo, true, true, true, false);
    }

    private static PermisoDef crud(String modulo) {
        return new PermisoDef(modulo, true, true, true, true);
    }

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedDemoUsuarios;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seguridad.seed-demo-usuarios:true}") boolean seedDemoUsuarios) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedDemoUsuarios = seedDemoUsuarios;
    }

    @Override
    @Transactional
    public void run(String... args) {
        sembrarRol("Area de Producto", "Gestion del inventario y catalogo de productos",
                "#f59e0b",
                permisos(leer(ModuloCatalogo.REPORTES), leer(ModuloCatalogo.NOTIFICACIONES),
                        lcr(ModuloCatalogo.DOCUMENTOS), lcr(ModuloCatalogo.CATALOGO),
                        lcr(ModuloCatalogo.PROVEEDORES), lcr(ModuloCatalogo.TARIFAS),
                        lcr(ModuloCatalogo.PAQUETES), lcr(ModuloCatalogo.PROMOCIONES)));
        sembrarRol("Area de Ventas", "Gestion de clientes, pedidos y facturacion",
                "#10b981",
                permisos(leer(ModuloCatalogo.REPORTES), leer(ModuloCatalogo.NOTIFICACIONES),
                        lcr(ModuloCatalogo.DOCUMENTOS), lcr(ModuloCatalogo.COTIZACIONES),
                        lcr(ModuloCatalogo.VENTAS), lcr(ModuloCatalogo.RESERVAS),
                        lcr(ModuloCatalogo.PAGOS), lcr(ModuloCatalogo.GESTION_AGENCIAS),
                        leer(ModuloCatalogo.SEGUIMIENTO_COMERCIAL), lcr(ModuloCatalogo.MARKETING)));
        sembrarRol("Gerencia", "Reportes, indicadores y supervision general",
                "#6366f1",
                permisos(lcr(ModuloCatalogo.NOTIFICACIONES), lcr(ModuloCatalogo.REPORTES),
                        lcr(ModuloCatalogo.DOCUMENTOS), lcr(ModuloCatalogo.GESTION_USUARIOS_ROLES_PERMISOS),
                        lcr(ModuloCatalogo.CATALOGO), lcr(ModuloCatalogo.PROVEEDORES),
                        lcr(ModuloCatalogo.TARIFAS), lcr(ModuloCatalogo.PAQUETES),
                        lcr(ModuloCatalogo.PROMOCIONES), lcr(ModuloCatalogo.COTIZACIONES),
                        lcr(ModuloCatalogo.VENTAS), lcr(ModuloCatalogo.RESERVAS),
                        lcr(ModuloCatalogo.PAGOS), lcr(ModuloCatalogo.GESTION_AGENCIAS),
                        lcr(ModuloCatalogo.SEGUIMIENTO_COMERCIAL), lcr(ModuloCatalogo.MARKETING)));
        sembrarRol("Administración", "Acceso total a la administracion del sistema",
                "#2563eb",
                permisos(crud(ModuloCatalogo.NOTIFICACIONES), crud(ModuloCatalogo.REPORTES),
                        crud(ModuloCatalogo.DOCUMENTOS), crud(ModuloCatalogo.GESTION_USUARIOS_ROLES_PERMISOS),
                        crud(ModuloCatalogo.CATALOGO), crud(ModuloCatalogo.PROVEEDORES),
                        crud(ModuloCatalogo.TARIFAS), crud(ModuloCatalogo.PAQUETES),
                        crud(ModuloCatalogo.PROMOCIONES), crud(ModuloCatalogo.COTIZACIONES),
                        crud(ModuloCatalogo.VENTAS), crud(ModuloCatalogo.RESERVAS),
                        crud(ModuloCatalogo.PAGOS), crud(ModuloCatalogo.GESTION_AGENCIAS),
                        crud(ModuloCatalogo.SEGUIMIENTO_COMERCIAL), crud(ModuloCatalogo.MARKETING)));

        if (seedDemoUsuarios) {
            sembrarUsuario("ADM", "adm@nuevohorizonte.com", "Administración");
            sembrarUsuario("GRT", "grt@nuevohorizonte.com", "Gerencia");
            sembrarUsuario("AP", "ap@nuevohorizonte.com", "Area de Producto");
            sembrarUsuario("AV", "av@nuevohorizonte.com", "Area de Ventas");
        }
    }

    private void sembrarRol(String nombre, String descripcion, String color, Map<String, boolean[]> deseados) {
        Rol rol = rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol nuevo = new Rol();
            nuevo.setNombre(nombre);
            nuevo.setFechaCreacion(LocalDateTime.now());
            return nuevo;
        });

        rol.setDescripcion(descripcion);
        if (rol.getColor() == null || rol.getColor().isBlank() || rol.getId() == null) {
            rol.setColor(color);
        }
        rol.setEsSistema(true);
        rol.setActivo(true);
        rol.setFechaActualizacion(LocalDateTime.now());

        aplicarPermisos(rol, deseados);
        rolRepository.save(rol);
    }

    private void aplicarPermisos(Rol rol, Map<String, boolean[]> deseados) {
        Set<String> modulos = deseados.keySet();
        List<RolPermiso> aEliminar = rol.getRolPermisos().stream()
                .filter(permiso -> !modulos.contains(permiso.getModulo()))
                .toList();
        rol.getRolPermisos().removeAll(aEliminar);

        Map<String, RolPermiso> porModulo = rol.getRolPermisos().stream()
                .collect(Collectors.toMap(RolPermiso::getModulo, permiso -> permiso));

        deseados.forEach((modulo, flags) -> {
            RolPermiso permiso = porModulo.get(modulo);
            if (permiso == null) {
                permiso = new RolPermiso();
                permiso.setRol(rol);
                permiso.setModulo(modulo);
                rol.getRolPermisos().add(permiso);
            }
            permiso.setPuedeLeer(flags[0]);
            permiso.setPuedeCrear(flags[1]);
            permiso.setPuedeActualizar(flags[2]);
            permiso.setPuedeEliminar(flags[3]);
        });
    }

    private void sembrarUsuario(String username, String email, String nombreRol) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + nombreRol));
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(PASSWORD_INICIAL));
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.addRol(rol);
        usuarioRepository.save(usuario);
    }

    private Map<String, boolean[]> permisos(PermisoDef... defs) {
        return Arrays.stream(defs).collect(Collectors.toMap(
                PermisoDef::modulo,
                def -> new boolean[]{def.leer(), def.crear(), def.actualizar(), def.eliminar()}));
    }
}
