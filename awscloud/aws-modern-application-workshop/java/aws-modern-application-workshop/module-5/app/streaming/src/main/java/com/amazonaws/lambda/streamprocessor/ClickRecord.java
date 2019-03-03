package com.amazonaws.lambda.streamprocessor;

import java.io.Serializable;

public class ClickRecord {

	private String userId;
    private String mysfitId;
    private String species;
    private String goodevil;
    private String lawchaos;

    public ClickRecord() {
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getMysfitId() {
        return mysfitId;
    }

    public void setMysfitId(String mysfitId) {
        this.mysfitId = mysfitId;
    }


    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }


    public String getGoodevil() {
        return goodevil;
    }

    public void setGoodevil(String goodevil) {
        this.goodevil = goodevil;
    }


    public String getLawchaos() {
        return lawchaos;
    }

    public void setLawchaos(String lawchaos) {
        this.lawchaos = lawchaos;
    }

}
