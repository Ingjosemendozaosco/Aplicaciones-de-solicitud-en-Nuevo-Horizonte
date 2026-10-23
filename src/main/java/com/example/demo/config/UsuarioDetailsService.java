package com.example.demo.config;

import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Rol;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.RolPermiso;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.entity.Usuario;
import com.example.demo.modulos.gestionUsuariosRolesPermisos.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .or(() -> usuarioRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Rol rol : usuario.rolesVigentes()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
            for (RolPermiso permiso : rol.getRolPermisos()) {
                if (!permiso.esVigente()) {
                    continue;
                }
                String modulo = permiso.getModulo();
                if (permiso.isPuedeLeer()) {
                    authorities.add(new SimpleGrantedAuthority("PERM_" + modulo + "_LEER"));
                }
                if (permiso.isPuedeCrear()) {
                    authorities.add(new SimpleGrantedAuthority("PERM_" + modulo + "_CREAR"));
                }
                if (permiso.isPuedeActualizar()) {
                    authorities.add(new SimpleGrantedAuthority("PERM_" + modulo + "_ACTUALIZAR"));
                }
                if (permiso.isPuedeEliminar()) {
                    authorities.add(new SimpleGrantedAuthority("PERM_" + modulo + "_ELIMINAR"));
                }
            }
        }

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .disabled(!usuario.isActivo())
                .authorities(authorities)
                .build();
    }
}
