package com.ltbrew.brewbeer.presenter.model;

import com.ltbrew.brewbeer.persistence.greendao.DBRecipe;

/**
 * Created by qiusiping on 16/5/8.
 */
public class BrewHistory {
//    formula_id、begin_time、end_time、package_id、pid、state
    Long formula_id;
    String begin_time;
    String end_time;
    Long package_id;
    Long pid;
    Integer state;
    Integer ratio;
    Integer st;
    String brewingState;
    DBRecipe dbRecipe;

    public DBRecipe getDbRecipe() {
        return dbRecipe;
    }

    public void setDbRecipe(DBRecipe dbRecipe) {
        this.dbRecipe = dbRecipe;
    }

    public Long getFormula_id() {
        return formula_id;
    }

    public void setFormula_id(Long formula_id) {
        this.formula_id = formula_id;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public void setBegin_time(String begin_time) {
        this.begin_time = begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public Long getPackage_id() {
        return package_id;
    }

    public void setPackage_id(Long package_id) {
        this.package_id = package_id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getBrewingState() {
        return brewingState;
    }

    public void setBrewingState(String brewingState) {
        this.brewingState = brewingState;
    }

    public Integer getRatio() {
        return ratio;
    }

    public void setRatio(Integer ratio) {
        this.ratio = ratio;
    }

    public Integer getSt() {
        return st;
    }

    public void setSt(Integer st) {
        this.st = st;
    }

    @Override
    public String toString() {
        return "BrewHistory{" +
                "formula_id=" + formula_id +
                ", begin_time='" + begin_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", package_id=" + package_id +
                ", pid=" + pid +
                ", state=" + state +
                ", brewingState='" + brewingState + '\'' +
                ", dbRecipe=" + dbRecipe +
                '}';
    }
}
