package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.database.Model;

@ModelEntity(table = "devices")
public class Device extends Model {

    public int id;
    public String name;
    public String serial;
    //public Integer category_id;

    public Category category;


    //public String table = "devices";

    public Category category()
    {
        if (category == null) {
            category = (Category) this.belongsTo(Category.class, "category_id");
        }
        return this.category;
    }
}
