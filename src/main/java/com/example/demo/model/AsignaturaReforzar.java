package com.example.demo.model;

import java.io.Serializable;

/**
 * Asignatura que se reforzará con la visita académica (tabla
 * ASIGNATURA_REFORZAR_SOLICITUD).
 * <p>
 * SolicitudDao trae las asignaturas como texto suelto porque el formulario las
 * guarda todas de golpe y nunca necesita el id de cada una. El panel de
 * desglose sí lo necesita: ahí cada asignatura se edita y se borra por
 * separado, así que se maneja como entidad con su llave primaria.
 * </p>
 *
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
public class AsignaturaReforzar implements Serializable {

    /** Llave primaria (ID_ASIGNATURA). Vale 0 mientras no se haya guardado. */
    private int idAsignatura;

    /** Solicitud a la que pertenece. */
    private int idSolicitud;

    /** Nombre de la asignatura, hasta 100 caracteres por la columna. */
    private String nombre;

    public AsignaturaReforzar() {}

    public AsignaturaReforzar(int idSolicitud, String nombre) {
        this.idSolicitud = idSolicitud;
        this.nombre = nombre;
    }

    public int getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(int idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
