package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.data.entities.Model;

@ModelEntity(table = "categories")
public class Payment extends Model {

    public Integer id;
    public String name;
}
