package com.ltbrew.brewbeer.presenter.model;

import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;

/**
 * Created by 151117a on 2016/5/6.
 */
public class Recipe {
    String id;
    String id_type;
    String name;
    String ref;

    public String getCus() {
        return cus;
    }

    public void setCus(String cus) {
        this.cus = cus;
    }

    String cus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_type() {
        return id_type;
    }

    public void setId_type(String id_type) {
        this.id_type = id_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

}
