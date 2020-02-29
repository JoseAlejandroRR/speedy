package com.josealejandrorr.speedy.contracts.data.repositories;

import com.josealejandrorr.speedy.data.entities.Model;

import java.util.Map;
import java.util.Optional;

public interface Repository {

    //public boolean save();

    public Optional findById(Model entity, long id);

    public boolean create(Model entity);

    public boolean update(Model entity, long id);

    public boolean delete(Model entity);

    public long count(Model entity);

    public void registerModel(Model entity, String nameCollection);
}
