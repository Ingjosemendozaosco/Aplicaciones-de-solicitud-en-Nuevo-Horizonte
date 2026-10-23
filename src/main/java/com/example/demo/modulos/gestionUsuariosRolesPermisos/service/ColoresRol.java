package com.example.demo.modulos.gestionUsuariosRolesPermisos.service;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;

import java.util.ArrayList;
import java.util.List;

public final class ColoresRol {

    private static final String[] PALETA = {
            "#2563eb", "#7c3aed", "#ea580c", "#059669",
            "#db2777", "#0891b2", "#ca8a04", "#dc2626"
    };

    private ColoresRol() {
    }

    public static String colorDe(Rol rol) {
        if (rol == null) {
            return PALETA[0];
        }
        if (rol.getColor() != null && !rol.getColor().isBlank()) {
            return rol.getColor().trim();
        }
        long base = rol.getId() != null
                ? rol.getId()
                : Math.abs((rol.getNombre() == null ? "" : rol.getNombre()).hashCode());
        return PALETA[(int) Math.floorMod(base, PALETA.length)];
    }

    public static String gradiente(List<String> colores) {
        List<String> distintos = new ArrayList<>();
        for (String color : colores) {
            if (color != null && !color.isBlank() && !distintos.contains(color)) {
                distintos.add(color);
            }
        }
        if (distintos.isEmpty()) {
            return "#e2e8f0";
        }
        if (distintos.size() == 1) {
            return distintos.get(0);
        }
        int n = distintos.size();
        StringBuilder sb = new StringBuilder("linear-gradient(90deg");
        for (int i = 0; i < n; i++) {
            int desde = i * 100 / n;
            int hasta = (i + 1) * 100 / n;
            sb.append(", ").append(distintos.get(i))
                    .append(' ').append(desde).append("% ")
                    .append(hasta).append('%');
        }
        return sb.append(')').toString();
    }
}
