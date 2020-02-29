package com.josealejandrorr.speedy.data.entities;

public class EntityFilter {

    public String field;
    public String operator;
    public FilterOperator conditional;
    public String value;

    public EntityFilter(String field, String operator, String value, FilterOperator conditional) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.conditional = conditional;
    }


    public String getField() {
        return field;
    }

    public String getOperator() {
        return operator;
    }

    public FilterOperator getConditional() {
        return conditional;
    }

    public String getValue() {
        return value;
    }
}


