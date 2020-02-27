package com.josealejandrorr.speedy.database;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JOptionPane;

public class NotificationDB implements INotificationDB {

    private String name;
    private String sql;
    private String hashCode;
    private String checksum;
    private Model model;

    private ArrayList listeners;

    public NotificationDB(String _name, String _sql, Object _model)
    {
        this.name = _name;
        this.sql = _sql;
        this.hashCode =  String.valueOf(_sql.hashCode());
        this.model = (Model)_model;
        //System.out.println(this.getSql()+"==");
        //System.out.println(this.getHashCode()+" AKA");
        listeners = new ArrayList();
        listeners.add(this);
    }

    public String getName() {
        return name;
    }
    public Model getModel() {
        return model;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSql() {
        return sql;
    }
    public void setSql(String sql) {
        this.sql = sql;
    }
    public String getHashCode() {
        return hashCode;
    }
    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }
    public String getChecksum() {
        return checksum;
    }
    public void setChecksum(String checksum) {
        this.checksum = checksum;
        //triggerChecksumEvent();
    }

    public void updateChecksum(String checksum) {
        this.checksum = checksum;
        triggerChecksumEvent();
    }

    public static NotificationDB find(String code)
    {
        NotificationDB obj = null;
        Iterator it = Conexion.notifications.iterator();
        //System.out.println("FINDING "+sql);
        while(it.hasNext())
        {
            NotificationDB notify = (NotificationDB)it.next();
            //System.out.println(notify.getHashCode()+" =/ "+code);
            if(notify.getHashCode().equals(code)){
                //System.out.println("FINDING 2 "+notify.getSql());
                obj = notify;
            }
        }
        return obj;
    }

    public void addEventListener(INotificationDB listener)
    {
        listeners.add(listener);
    }

    private void triggerChecksumEvent() {

        ListIterator li = listeners.listIterator();
        while (li.hasNext()) {
            INotificationDB listener = (INotificationDB) li.next();
            EventNotificationDB readerEvObj = new EventNotificationDB(this, this);
            (listener).onChecksumChange(readerEvObj);
        }
    }

    @Override
    public void onChecksumChange(EventNotificationDB ev) {
        // TODO Auto-generated method stub

    }

    public void updateData()
    {
        //this.setChecksum(checksum);
        System.out.println("Notify "+name+" TABLA "+this.model.table);
        //CacheDB.loadRequest(model.getClass(), sql);
    }
}
