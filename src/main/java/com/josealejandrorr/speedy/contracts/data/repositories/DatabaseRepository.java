package com.josealejandrorr.speedy.contracts.data.repositories;

import com.josealejandrorr.speedy.data.entities.EntityFilter;
import com.josealejandrorr.speedy.data.entities.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public interface DatabaseRepository extends Repository {

    public Optional firstOne(Model entity);

    public Optional lastOne(Model entity);

    public Optional first(Model entity, HashMap<String, Object> conditions);

    public Optional last(Model entity, HashMap<String, Object> conditions);

    public Optional search(Model entity, DatabaseQuery query);
}
