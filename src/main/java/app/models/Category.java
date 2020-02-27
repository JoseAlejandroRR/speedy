package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.database.Model;

@ModelEntity(table = "categories", timestamps = false)
public class Category extends Model {

    public Integer id;
    public String name;

}
