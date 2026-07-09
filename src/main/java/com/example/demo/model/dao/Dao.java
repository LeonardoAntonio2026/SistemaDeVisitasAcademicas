package com.example.demo.model.dao;
import java.util.List;

public interface Dao<T, K> {
    boolean create(T entidad);
    List<T> getAll();
    T getById(K id);
    boolean update(T entidad);
    boolean delete(K id);
}