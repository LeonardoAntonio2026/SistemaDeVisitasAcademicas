package com.example.demo.model;

import java.io.Serializable;

/**
 * Archivo asociado a una solicitud (o a un reporte). El contenido se guarda
 * en Base64 dentro de la tabla DOCUMENTO (columna CONTENIDO_BASE64).
 */
public class Documento implements Serializable {
    private int idDocumento;
    private Integer idSolicitud;
    private Integer idReporte;
    private int idTipoDocumento;
    private String contenidoBase64;
    private String fechaCarga;

    // Campos de apoyo para la vista (no son columnas de DOCUMENTO)
    private String nombreTipo;
    private long tamanoBytes;

    public Documento() {}

    public int getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(int idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getContenidoBase64() {
        return contenidoBase64;
    }

    public void setContenidoBase64(String contenidoBase64) {
        this.contenidoBase64 = contenidoBase64;
    }

    public String getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(String fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    /** Tamaño legible para la vista, ej. "1.8 MB". */
    public String getTamanoLegible() {
        if (tamanoBytes <= 0) {
            return "";
        }
        if (tamanoBytes < 1024 * 1024) {
            return String.format("%.0f KB", tamanoBytes / 1024.0);
        }
        return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
    }
}
