package com.example.demo.model;

import java.io.Serializable;

/**
 * Modelo de datos que representa una imagen adjunta a un reporte de visita (RF-08).
 * <p>
 * El contenido binario se almacena codificado en Base64 dentro de la tabla IMAGEN
 * (columna CONTENIDO_BASE64). El tipo MIME no se guarda en la base de datos,
 * sino que se detecta dinámicamente mediante los magic bytes (JPG/PNG) al momento de servir el archivo.
 * </p>
 *
 * @author Alan Esteban Zarinana Arizmendi
 * @since 2026-08-25
 */
public class ImagenReporte implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Atributos de la tabla IMAGEN ---

    /** Identificador único de la imagen. */
    private int idImagen;

    /** Identificador del reporte al cual pertenece la imagen. */
    private int idReporte;

    /** Contenido del archivo de imagen codificado en cadena Base64. */
    private String contenidoBase64;

    /** Fecha y hora en la que se subió la imagen al sistema. */
    private String fechaCarga;

    // --- Campos de apoyo para la vista (No persisten en la tabla IMAGEN) ---

    /** Tamaño del archivo de imagen expresado en bytes. */
    private long tamanoBytes;

    /**
     * Constructor por defecto de la clase.
     */
    public ImagenReporte() {}

    // --- Getters y Setters ---

    /**
     * Obtiene el identificador único de la imagen.
     * @return Identificador de la imagen.
     */
    public int getIdImagen() {
        return idImagen;
    }

    /**
     * Establece el identificador único de la imagen.
     * @param idImagen Identificador a asignar.
     */
    public void setIdImagen(int idImagen) {
        this.idImagen = idImagen;
    }

    /**
     * Obtiene el identificador del reporte asociado.
     * @return Identificador del reporte.
     */
    public int getIdReporte() {
        return idReporte;
    }

    /**
     * Establece el identificador del reporte asociado.
     * @param idReporte Identificador del reporte a vincular.
     */
    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    /**
     * Obtiene el contenido binario de la imagen en formato Base64.
     * @return Cadena codificada en Base64.
     */
    public String getContenidoBase64() {
        return contenidoBase64;
    }

    /**
     * Establece el contenido en formato Base64 de la imagen.
     * @param contenidoBase64 Cadena codificada en Base64.
     */
    public void setContenidoBase64(String contenidoBase64) {
        this.contenidoBase64 = contenidoBase64;
    }

    /**
     * Obtiene la fecha en la que fue cargada la imagen.
     * @return Cadena con la fecha de carga.
     */
    public String getFechaCarga() {
        return fechaCarga;
    }

    /**
     * Establece la fecha de carga de la imagen.
     * @param fechaCarga Fecha de carga a asignar.
     */
    public void setFechaCarga(String fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    /**
     * Obtiene el tamaño del archivo de imagen en bytes para apoyo en interfaz visual.
     * @return Tamaño del archivo en bytes.
     */
    public long getTamanoBytes() {
        return tamanoBytes;
    }

    /**
     * Establece el tamaño del archivo de imagen en bytes.
     * @param tamanoBytes Tamaño en bytes.
     */
    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }
}