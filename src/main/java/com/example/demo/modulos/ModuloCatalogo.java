package com.example.demo.modulos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuloCatalogo {

    public static final String NOTIFICACIONES = "NOTIFICACIONES";
    public static final String REPORTES = "REPORTES";
    public static final String DOCUMENTOS = "DOCUMENTOS";
    public static final String GESTION_USUARIOS_ROLES_PERMISOS = "GESTION_USUARIOS_ROLES_PERMISOS";
    public static final String CATALOGO = "CATALOGO";
    public static final String PROVEEDORES = "PROVEEDORES";
    public static final String TARIFAS = "TARIFAS";
    public static final String PAQUETES = "PAQUETES";
    public static final String PROMOCIONES = "PROMOCIONES";
    public static final String COTIZACIONES = "COTIZACIONES";
    public static final String VENTAS = "VENTAS";
    public static final String RESERVAS = "RESERVAS";
    public static final String PAGOS = "PAGOS";
    public static final String GESTION_AGENCIAS = "GESTION_AGENCIAS";
    public static final String SEGUIMIENTO_COMERCIAL = "SEGUIMIENTO_COMERCIAL";
    public static final String MARKETING = "MARKETING";

    private static final Map<String, String> ETIQUETAS;

    static {
        Map<String, String> etiquetas = new LinkedHashMap<>();
        etiquetas.put(NOTIFICACIONES, "Notificaciones");
        etiquetas.put(REPORTES, "Reportes");
        etiquetas.put(DOCUMENTOS, "Documentos");
        etiquetas.put(GESTION_USUARIOS_ROLES_PERMISOS, "Gestión de usuarios, roles y permisos");
        etiquetas.put(CATALOGO, "Catálogo");
        etiquetas.put(PROVEEDORES, "Proveedores");
        etiquetas.put(TARIFAS, "Tarifas");
        etiquetas.put(PAQUETES, "Paquetes");
        etiquetas.put(PROMOCIONES, "Promociones");
        etiquetas.put(COTIZACIONES, "Cotizaciones");
        etiquetas.put(VENTAS, "Ventas");
        etiquetas.put(RESERVAS, "Reservas");
        etiquetas.put(PAGOS, "Pagos");
        etiquetas.put(GESTION_AGENCIAS, "Gestión de agencias");
        etiquetas.put(SEGUIMIENTO_COMERCIAL, "Seguimiento comercial");
        etiquetas.put(MARKETING, "Marketing");
        ETIQUETAS = Collections.unmodifiableMap(etiquetas);
    }

    private ModuloCatalogo() {
    }

    public static Map<String, String> etiquetas() {
        return ETIQUETAS;
    }

    public static List<String> todos() {
        return List.copyOf(ETIQUETAS.keySet());
    }

    public static String etiqueta(String modulo) {
        return ETIQUETAS.getOrDefault(modulo, modulo);
    }
}
