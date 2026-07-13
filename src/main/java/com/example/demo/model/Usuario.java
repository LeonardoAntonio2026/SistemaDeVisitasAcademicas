package com.example.demo.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private int id;
    private int idRol;
    private String nombre;
    private String correo;

    // Campos de apoyo: nombreRol viene del JOIN con ROL;
    // contrasena solo se usa en memoria para registro/login (en BD vive en CONTRASENA como hash)
    private String nombreRol;
    private String contrasena;

    public Usuario() {}

    public Usuario(int id, int idRol, String nombre, String correo) {
        this.id = id;
        this.idRol = idRol;
        this.nombre = nombre;
        this.correo = correo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
