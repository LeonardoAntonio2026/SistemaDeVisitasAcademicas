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

    /** Construye un token vacío; el DAO lo llena campo por campo. */
    public TokenRecuperacion() {}

    /**
     * Devuelve el identificador del token.
     *
     * @return el identificador único del token
     */
    public int getIdToken() {
        return idToken;
    }

    /**
     * Asigna el identificador del token.
     *
     * @param idToken identificador único del token
     */
    public void setIdToken(int idToken) {
        this.idToken = idToken;
    }

    /**
     * Devuelve el usuario dueño del token.
     *
     * @return el id del usuario que pidió restablecer su contraseña
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Asigna el usuario dueño del token.
     *
     * @param idUsuario id del usuario que pidió restablecer su contraseña
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Devuelve la cadena aleatoria que viaja en el enlace del correo.
     *
     * @return el token en texto, tal como aparece en la URL de restablecimiento
     */
    public String getToken() {
        return token;
    }

    /**
     * Asigna la cadena aleatoria que viaja en el enlace del correo.
     *
     * @param token el token en texto, tal como aparece en la URL de restablecimiento
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Devuelve el momento en que el token deja de servir.
     *
     * @return la fecha de expiración, 24 horas después de haberse generado
     */
    public Timestamp getFechaExpiracion() {
        return fechaExpiracion;
    }

    /**
     * Asigna el momento en que el token deja de servir.
     *
     * @param fechaExpiracion fecha de expiración del token
     */
    public void setFechaExpiracion(Timestamp fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    /**
     * Indica si el token ya se gastó. Es de un solo uso.
     *
     * @return {@code true} si ya se usó para restablecer una contraseña
     */
    public boolean isUsado() {
        return usado;
    }

    /**
     * Marca el token como gastado.
     *
     * @param usado {@code true} si ya se usó para restablecer una contraseña
     */
    public void setUsado(boolean usado) {
        this.usado = usado;
    }
    /**
     * Indica si el token ya no sirve por haber pasado su fecha de expiración.
     * Un token sin fecha (null) se considera vencido, para no dejar pasar
     * datos incompletos como si fueran válidos.
     *
     * @return {@code true} si el token ya expiró o no trae fecha
     */
    public boolean estaVencido() {
        return fechaExpiracion == null
                || fechaExpiracion.before(new Timestamp(System.currentTimeMillis()));
    }
}