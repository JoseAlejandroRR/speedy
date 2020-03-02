package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.data.entities.Model;

@ModelEntity(table = "products")
public class Product extends Model {

    public long id;

    public String name;

    public String description;

    public Category category;

    public long category_id;

    public long brand_id;

    public Double price;

    public int stock;

    public int status;
}
