package app.models;

import com.josealejandrorr.speedy.annotations.ModelEntity;
import com.josealejandrorr.speedy.database.Model;

import java.util.ArrayList;
import java.util.Date;

@ModelEntity(table = "users")
public class User extends Model {

    public Integer id;
    public String name;
    public String lastname;
    public String document;
    public String bio;
    public String direction;
    public String state;
    public String country;
    //public Date birthday;
    public int status;


    public Device device;

    public ArrayList<Device> devices = null;

    public boolean timestamps = true;
    //private ArrayList payments = null;
    //protected boolean algo;

    public String getName()
    {
        return this.name;
    }

    public void setName(String _name)
    {
        this.name = _name;
    }

    /*public ArrayList payments()
    {
        if(this.payments==null) this.payments = this.hasMany(Payment.class,"user_id");
        return this.payments;
    }*/

    public Device device()
    {
        if(this.device == null) {
            System.out.println("cheko");
            this.device = (Device)  this.hasOne(Device.class, "user_id");
        }
        //System.out.println(this.device.id);
        return this.device;
    }

    public ArrayList<Device> devices()
    {
        if(this.devices == null) {
            System.out.println("cheko2");
            this.devices = this.hasMany(Device.class, "user_id");
        }
        return this.devices;
    }

}
