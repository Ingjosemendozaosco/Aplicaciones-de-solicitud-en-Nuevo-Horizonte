package com.example.demo.modulos.documentos.service;

import com.example.demo.modulos.documentos.entity.Documento;
import com.example.demo.modulos.documentos.repository.DocumentoRepository;
import com.example.demo.modulos.notificaciones.service.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class DocumentoService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");

    private static final String EXTENSIONES_PERMITIDAS =
            "(?i).+\\.(pdf|doc|docx|xls|xlsx|txt|jpg|jpeg|png|gif|webp)$";

    private final DocumentoRepository documentoRepository;
    private final NotificacionService notificacionService;

    public DocumentoService(DocumentoRepository documentoRepository,
                            NotificacionService notificacionService) {
        this.documentoRepository = documentoRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public List<Documento> listar() {
        return documentoRepository.findByActivoTrueOrderByFechaSubidaDesc();
    }

    @Transactional(readOnly = true)
    public List<Documento> listarTodos() {
        return documentoRepository.findAllByOrderByFechaSubidaDesc();
    }

    @Transactional(readOnly = true)
    public Documento buscar(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado: " + id));
    }

    @Transactional
    public Documento subir(String nombre, String descripcion, String categoria,
                           MultipartFile archivo, String username) throws IOException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del documento es obligatorio.");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona el archivo del documento.");
        }
        validarArchivo(archivo);

        Documento documento = new Documento();
        documento.setNombre(nombre.trim());
        documento.setDescripcion(descripcion == null || descripcion.isBlank()
                ? null : descripcion.trim());
        documento.setCategoria(categoria == null || categoria.isBlank()
                ? null : categoria.trim());
        documento.setNombreArchivo(archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename() : "documento");
        documento.setTipoArchivo(archivo.getContentType() != null
                ? archivo.getContentType() : "application/octet-stream");
        documento.setContenido(archivo.getBytes());
        documento.setSubidoPor(username);
        documento.setFechaSubida(LocalDateTime.now());
        documento.setActivo(true);

        Documento guardado = documentoRepository.save(documento);
        notificacionService.crearParaRol("Administración",
                "Nuevo documento disponible",
                "Gerencia publicó el documento \"" + guardado.getNombre()
                        + "\" para las solicitudes de información.",
                "/solicitudes");
        return guardado;
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Documento documento = buscar(id);
        documento.setActivo(activo);
        documentoRepository.save(documento);
    }

    @Transactional
    public void eliminar(Long id) {
        Documento documento = buscar(id);
        documentoRepository.delete(documento);
    }

    private void validarArchivo(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        String nombre = archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename() : "";
        boolean tipoValido = contentType != null && TIPOS_PERMITIDOS.contains(contentType);
        boolean extensionValida = nombre.matches(EXTENSIONES_PERMITIDAS);
        if (!tipoValido && !extensionValida) {
            throw new IllegalArgumentException(
                    "Archivo no válido. Usa pdf, doc, docx, xls, xlsx, txt o imágenes.");
        }
    }
}
