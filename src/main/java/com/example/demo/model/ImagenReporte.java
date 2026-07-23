package com.example.demo.model;

import java.io.Serializable;

/**
 * Imagen adjunta a un reporte de visita (RF-08). El contenido se guarda en
 * Base64 dentro de la tabla IMAGEN_REPORTE (columna CONTENIDO_BASE64), igual
 * que se hace con DOCUMENTO.
 */
public class ImagenReporte implements Serializable {
private int idImagen;
private int idReporte;
private String contenidoBase64;
private String tipoMime;
private String fechaCarga;

// Campo de apoyo para la vista (no es columna de IMAGEN_REPORTE)
private long tamanoBytes;

public ImagenReporte() {}

public int getIdImagen() {
return idImagen;
}

public void setIdImagen(int idImagen) {
this.idImagen = idImagen;
}

public int getIdReporte() {
return idReporte;
}

public void setIdReporte(int idReporte) {
this.idReporte = idReporte;
}

public String getContenidoBase64() {
return contenidoBase64;
}

public void setContenidoBase64(String contenidoBase64) {
this.contenidoBase64 = contenidoBase64;
}

public String getTipoMime() {
return tipoMime;
}

public void setTipoMime(String tipoMime) {
this.tipoMime = tipoMime;
}

public String getFechaCarga() {
return fechaCarga;
}

public void setFechaCarga(String fechaCarga) {
this.fechaCarga = fechaCarga;
}

public long getTamanoBytes() {
return tamanoBytes;
}

public void setTamanoBytes(long tamanoBytes) {
this.tamanoBytes = tamanoBytes;
}
}