package com.csd.MeWaT.utils;

import java.io.Serializable;

public class Lista implements Serializable {

    private String name;
    private String userOwner;

    public Lista(String ...pars) {
        for(int i=0;i<pars.length;i++){
            if (i==0) this.name=pars[0];
            if (i==1) this.userOwner=pars[1];
        }
    }

    public String getName() {
        return name;
    }

    public String getUserOwner() {
        return userOwner;
    }
}
