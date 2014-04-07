package com.DVLA.testapp.app;

import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * Created by breezed on 07/04/14.
 */
public class vehRecord {
    public String VRM;
    public String Make;
    public String Model;
    public DateTime FirstReg;
    public DateTime Tax;
    public DateTime MOT;
    public Boolean Insured;

    public String getVRM() {
        return VRM;
    }

    public String getMake() {
        return Make;
    }

    public String getModel() {
        return Model;
    }

    public DateTime getFirstReg() {
        return FirstReg;
    }

    public DateTime getTax() {
        return Tax;
    }

    public DateTime getMOT() {
        return MOT;
    }

    public Boolean getInsured() {
        return Insured;
    }

    public Boolean isTaxed() {
        if(getTax() == null) {return false;}
        DateTime now = new DateTime().withTimeAtStartOfDay();
        if(getTax().isAfter(now)) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean isMOTed() {
        if(getMOT() == null) {return false;}
        DateTime now = new DateTime().withTimeAtStartOfDay();
        if(getMOT().isAfter(now)) {
            return true;
        } else {
            return false;
        }
    }

    public Integer vehicleAge() {
        if(getFirstReg() == null) {return -1;}
        DateTime now = new DateTime().withTimeAtStartOfDay();
        Integer diff = new DateTime(now.getMillis() - getFirstReg().getMillis()).getYear();
        return diff;
    }

}

