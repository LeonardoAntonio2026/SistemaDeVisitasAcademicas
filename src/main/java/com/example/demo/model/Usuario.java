package com.example.demo.model;

import java.io.Serializable;

/**
 * Clase que representa la entidad de un Usuario dentro del sistema.
 * Implementa {@link Serializable} para permitir la persistencia y la transferencia de objetos en sesión.
 *
 * @author Hugo Alberto Ramirez Martinez
 * @since 25/08/2026
 */
public class Usuario implements Serializable {

    /** Identificador único del usuario. */
    private int id;

    /** Identificador del rol asignado al usuario. */
    private int idRol;

    /** Nombre completo del usuario. */
    private String nombre;

    /** Correo electrónico del usuario (utilizado como credencial de acceso). */
    private String correo;

    /**
     * Nombre del rol asignado. Campo auxiliar obtenido a través de una consulta JOIN con la tabla ROL.
     */
    private String nombreRol;

    /**
     * Contraseña del usuario. Utilizada en memoria durante los procesos de autenticación y registro
     * (en la base de datos se almacena su hash correspondiente).
     */
    private String contrasena;

    /**
     * Constructor por defecto.
     *
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public Usuario() {}

    /**
     * Constructor parametrizado con los datos principales de la entidad.
     *
     * @param id     Identificador único.
     * @param idRol  Identificador del rol.
     * @param nombre Nombre completo del usuario.
     * @param correo Correo electrónico.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public Usuario(int id, int idRol, String nombre, String correo) {
        this.id = id;
        this.idRol = idRol;
        this.nombre = nombre;
        this.correo = correo;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return {@code int} con el ID del usuario.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador del usuario.
     *
     * @param id Nuevo ID del usuario.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador del rol asociado.
     *
     * @return {@code int} con el ID del rol.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public int getIdRol() {
        return idRol;
    }

    /**
     * Establece el identificador del rol asociado.
     *
     * @param idRol Nuevo ID del rol.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    /**
     * Obtiene el nombre completo del usuario.
     *
     * @return {@link String} con el nombre del usuario.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre completo del usuario.
     *
     * @param nombre Nuevo nombre del usuario.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return {@link String} con el correo electrónico.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param correo Nuevo correo electrónico.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene el nombre descriptivo del rol del usuario.
     *
     * @return {@link String} con el nombre del rol.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public String getNombreRol() {
        return nombreRol;
    }

    /**
     * Establece el nombre descriptivo del rol del usuario.
     *
     * @param nombreRol Nuevo nombre del rol.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    /**
     * Obtiene la contraseña almacenada temporalmente en memoria.
     *
     * @return {@link String} con la contraseña.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public String getContrasena() {
        return contrasena;
    }

    /**
     * Establece la contraseña en memoria.
     *
     * @param contrasena Nueva contraseña.
     * @author Hugo Alberto Ramirez Martinez
     * @since 25/08/2026
     */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}