package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.database.Model;

@ModelEntity(table = "customers")
public class Customer extends Model {

    public Long id;

    public String name;

    public String lastname;

    public String email;
}
