package com.example.demo.model.dao;
import java.util.List;

/**
 * Contrato común de los DAO: las cinco operaciones básicas contra una tabla.
 * <p>
 * Cada DAO concreto añade además sus propias consultas (filtros por estado,
 * por usuario, etc.); aquí solo vive lo que todos comparten.
 * </p>
 *
 * @param <T> tipo de la entidad que maneja el DAO, por ejemplo {@code Solicitud}
 * @param <K> tipo de la llave primaria de esa entidad, por ejemplo {@code Integer}
 * @author Leonardo Antonio Arroyo Rodriguez
 * @since 25/08/2026
 */
public interface Dao<T, K> {
    /**
     * Inserta una entidad nueva en la base.
     *
     * @param entidad la entidad a guardar
     * @return {@code true} si se insertó, {@code false} si la operación falló
     */
    boolean create(T entidad);

    /**
     * Trae todos los registros de la tabla.
     *
     * @return la lista de entidades, vacía si no hay registros o si falló la consulta
     */
    List<T> getAll();

    /**
     * Busca una entidad por su llave primaria.
     *
     * @param id llave primaria a buscar
     * @return la entidad encontrada, o {@code null} si no existe o si falló la consulta
     */
    T getById(K id);

    /**
     * Guarda los cambios de una entidad que ya existe.
     *
     * @param entidad la entidad con los datos actualizados y su llave primaria
     * @return {@code true} si se actualizó, {@code false} si la operación falló
     */
    boolean update(T entidad);

    /**
     * Borra el registro que corresponde a la llave primaria dada.
     *
     * @param id llave primaria del registro a borrar
     * @return {@code true} si se borró, {@code false} si la operación falló
     */
    boolean delete(K id);
}
