package com.josealejandrorr.speedy.contracts.data.repositories;

import com.josealejandrorr.speedy.data.entities.EntityFilter;

import java.util.ArrayList;

public class DatabaseQuery {

    public ArrayList<String> tables;

    public ArrayList<String> fieldSelecteds;

    public ArrayList<EntityFilter> filters;

    public long limitFrom = 0;

    public long limitTo = -1;

    public String orderByField;

    public String orderByMode = "ASC";

}
