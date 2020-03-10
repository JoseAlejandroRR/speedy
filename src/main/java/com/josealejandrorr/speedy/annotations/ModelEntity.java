package com.josealejandrorr.speedy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelEntity {


    /**
     *
     * @return the key pivot in the database table
     */
    String pkey() default "id";

    /**
     *
     * @return the table name in database
     */
    String table();

    /**
     *
     * @return determines if use created_at, updated_at Fields
     */
    boolean timestamps() default true;

    /**
     *
     * @return determines if delete or only hide the register
     */
    boolean softDelete() default false;

}