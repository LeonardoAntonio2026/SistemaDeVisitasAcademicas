package com.example.demo.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class TokenRecuperacion implements Serializable {
    private int idToken;
    private int idUsuario;
    private String token;
    private Timestamp fechaExpiracion;
    private boolean usado;

    public TokenRecuperacion() {}

    public int getIdToken() {
        return idToken;
    }

    public void setIdToken(int idToken) {
        this.idToken = idToken;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Timestamp fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

    public boolean estaVencido() {
        return fechaExpiracion == null
                || fechaExpiracion.before(new Timestamp(System.currentTimeMillis()));
    }
}