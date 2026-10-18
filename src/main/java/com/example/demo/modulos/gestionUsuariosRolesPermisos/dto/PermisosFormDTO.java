package com.example.demo.modulos.gestionUsuariosRolesPermisos.dto;

import java.util.ArrayList;
import java.util.List;

public class PermisosFormDTO {

    private List<PermisoItemDTO> items = new ArrayList<>();

    public List<PermisoItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PermisoItemDTO> items) {
        this.items = items;
    }
}
