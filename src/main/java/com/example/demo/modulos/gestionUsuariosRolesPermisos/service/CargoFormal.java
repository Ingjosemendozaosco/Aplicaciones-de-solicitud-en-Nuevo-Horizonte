package com.example.demo.modulos.gestionUsuariosRolesPermisos.service;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CargoFormal {

    private static final Map<String, Integer> JERARQUIA = Map.of(
            "Gerencia", 1,
            "Administración", 2,
            "Area de Ventas", 3,
            "Area de Producto", 4);

    private static final Map<String, String> POR_COMBINACION = combinaciones();

    private CargoFormal() {
    }

    public static String de(Set<Rol> roles) {
        if (roles == null || roles.isEmpty()) {
            return "Sin cargo";
        }
        if (roles.size() > 4) {
            return "Gerente General (CEO / Director Ejecutivo)";
        }
        Set<String> claves = new LinkedHashSet<>();
        roles.stream()
                .sorted(Comparator.comparingInt(r -> jerarquia(r.getNombre())))
                .forEach(r -> claves.add(normalizar(r.getNombre())));
        String clave = String.join("+", claves);
        return POR_COMBINACION.getOrDefault(clave, "Empleado");
    }

    public static String deNombres(java.util.Collection<String> nombres) {
        if (nombres == null || nombres.isEmpty()) {
            return "Sin cargo";
        }
        Set<String> claves = new LinkedHashSet<>();
        nombres.stream()
                .filter(n -> n != null && !n.isBlank())
                .sorted(Comparator.comparingInt(CargoFormal::jerarquia))
                .forEach(n -> claves.add(normalizar(n)));
        if (claves.isEmpty()) {
            return "Sin cargo";
        }
        if (claves.size() > 4) {
            return "Gerente General (CEO / Director Ejecutivo)";
        }
        return POR_COMBINACION.getOrDefault(String.join("+", claves), "Empleado");
    }

    public static List<String> ordenarPorJerarquia(List<String> nombres) {
        if (nombres == null) {
            return List.of();
        }
        return nombres.stream()
                .sorted(Comparator.comparingInt(CargoFormal::jerarquia))
                .toList();
    }

    private static String normalizar(String nombre) {
        if (nombre == null) {
            return "";
        }
        String n = nombre.trim();
        if (n.equalsIgnoreCase("Area de Producto") || n.equalsIgnoreCase("Productos")) {
            return "Area de Producto";
        }
        if (n.equalsIgnoreCase("Area de Ventas") || n.equalsIgnoreCase("Ventas")) {
            return "Area de Ventas";
        }
        if (n.equalsIgnoreCase("Administracion")) {
            return "Administración";
        }
        return n;
    }

    private static int jerarquia(String nombre) {
        return JERARQUIA.getOrDefault(normalizar(nombre), 99);
    }

    private static Map<String, String> combinaciones() {
        Map<String, String> mapa = new java.util.LinkedHashMap<>();
        for (String solo : List.of("Gerencia", "Administración", "Area de Ventas", "Area de Producto")) {
            mapa.put(solo, "Empleado");
        }
        mapa.put("Gerencia+Administración", "Gerente Administrativo");
        mapa.put("Area de Ventas+Area de Producto", "Gerente Comercial (Líder de Producto y Ventas)");
        mapa.put("Gerencia+Area de Ventas", "Director Comercial");
        mapa.put("Gerencia+Area de Producto", "Gerente de Producto (Product Manager)");
        mapa.put("Administración+Area de Ventas", "Administrador Comercial");
        mapa.put("Administración+Area de Producto", "Administrador de Operaciones y Productos");
        mapa.put("Gerencia+Administración+Area de Ventas", "Gerente Comercial y Administrativo");
        mapa.put("Gerencia+Administración+Area de Producto", "Gerente de Operaciones (COO)");
        mapa.put("Gerencia+Area de Ventas+Area de Producto", "Director de Negocio y Producto");
        mapa.put("Administración+Area de Ventas+Area de Producto", "Coordinador Operativo de Productos y Ventas");
        mapa.put("Gerencia+Administración+Area de Ventas+Area de Producto", "Gerente General (CEO / Director Ejecutivo)");
        return mapa;
    }
}
