package com.example.demo.model;

import java.io.Serializable;

/**
 * Archivo asociado a una solicitud (o a un reporte). El contenido se guarda
 * en Base64 dentro de la tabla DOCUMENTO (columna CONTENIDO_BASE64).
 *
 * @author Alan Esteban Zarinana Arizmendi
 * @since 2026-08-25
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

    /** Construye un documento vacío; el DAO lo llena campo por campo. */
    public Documento() {}

    /**
     * Devuelve el identificador del documento.
     *
     * @return el identificador único del documento
     */
    public int getIdDocumento() {
        return idDocumento;
    }

    /**
     * Asigna el identificador del documento.
     *
     * @param idDocumento identificador único del documento
     */
    public void setIdDocumento(int idDocumento) {
        this.idDocumento = idDocumento;
    }

    /**
     * Devuelve la solicitud a la que está ligado el documento.
     *
     * @return el id de la solicitud a la que pertenece, o {@code null} si el documento es de un reporte
     */
    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    /**
     * Liga el documento a una solicitud.
     *
     * @param idSolicitud id de la solicitud a la que pertenece, o {@code null} si el documento es de un reporte
     */
    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    /**
     * Devuelve el reporte al que está ligado el documento.
     *
     * @return el id del reporte al que pertenece, o {@code null} si el documento es de una solicitud
     */
    public Integer getIdReporte() {
        return idReporte;
    }

    /**
     * Liga el documento a un reporte.
     *
     * @param idReporte id del reporte al que pertenece, o {@code null} si el documento es de una solicitud
     */
    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    /**
     * Devuelve el tipo de documento.
     *
     * @return el id del tipo de documento (formato firmado, carta responsiva, etc.)
     */
    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    /**
     * Asigna el tipo de documento.
     *
     * @param idTipoDocumento id del tipo de documento (formato firmado, carta responsiva, etc.)
     */
    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    /**
     * Devuelve el archivo tal como se guarda en la base: texto en Base64.
     *
     * @return el contenido del archivo codificado en Base64
     */
    public String getContenidoBase64() {
        return contenidoBase64;
    }

    /**
     * Guarda el contenido del archivo en Base64.
     *
     * @param contenidoBase64 contenido del archivo codificado en Base64
     */
    public void setContenidoBase64(String contenidoBase64) {
        this.contenidoBase64 = contenidoBase64;
    }

    /**
     * Devuelve la fecha de carga del archivo.
     *
     * @return la fecha en que se subió el archivo, en formato yyyy-MM-dd HH:mm
     */
    public String getFechaCarga() {
        return fechaCarga;
    }

    /**
     * Asigna la fecha de carga del archivo.
     *
     * @param fechaCarga fecha en que se subió el archivo, en formato yyyy-MM-dd HH:mm
     */
    public void setFechaCarga(String fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    /**
     * Devuelve el nombre del tipo de documento. Campo de apoyo para la vista, no es columna de DOCUMENTO.
     *
     * @return el nombre legible del tipo de documento, para mostrarlo en la vista
     */
    public String getNombreTipo() {
        return nombreTipo;
    }

    /**
     * Asigna el nombre del tipo de documento.
     *
     * @param nombreTipo nombre legible del tipo de documento, para mostrarlo en la vista
     */
    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    /**
     * Devuelve el tamaño del archivo. Campo de apoyo para la vista, no es columna de DOCUMENTO.
     *
     * @return el tamaño del archivo en bytes
     */
    public long getTamanoBytes() {
        return tamanoBytes;
    }

    /**
     * Asigna el tamaño del archivo.
     *
     * @param tamanoBytes tamaño del archivo en bytes
     */
    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    /**
     * Tamaño legible para la vista, ej. "1.8 MB".
     *
     * @return el tamaño formateado en KB o MB, o cadena vacía si no hay dato
     */
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