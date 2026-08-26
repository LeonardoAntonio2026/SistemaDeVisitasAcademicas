package com.example.demo.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Enlace de un solo uso para restablecer la contraseña de un usuario (RF-02).
 * Se genera al pedir "olvidé mi contraseña" y vive 24 horas (fijadas en el
 * INSERT de TokenRecuperacionDao.crear): pasado ese tiempo, o una vez usado,
 * deja de servir para restablecer nada.
 */
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
    /**
     * Indica si el token ya no sirve por haber pasado su fecha de expiración.
     * Un token sin fecha (null) se considera vencido, para no dejar pasar
     * datos incompletos como si fueran válidos.
     */
    public boolean estaVencido() {
        return fechaExpiracion == null
                || fechaExpiracion.before(new Timestamp(System.currentTimeMillis()));
    }
}