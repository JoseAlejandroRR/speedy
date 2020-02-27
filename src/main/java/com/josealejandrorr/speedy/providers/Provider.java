package com.josealejandrorr.speedy.providers;

import com.josealejandrorr.speedy.contracts.providers.IProvider;

public class Provider implements IProvider {

    private Object instance;

    public Provider()
    {

    }


    @Override
    public Object getInstance() {
        return instance;
    }

    public void create(Object instance)
    {
        if(this.instance == null) {
            this.instance = instance;
        }
    }

}
