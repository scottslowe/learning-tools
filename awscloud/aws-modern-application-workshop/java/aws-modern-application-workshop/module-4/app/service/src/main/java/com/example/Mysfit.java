package com.example;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="MysfitsTable")
public class Mysfit {

    private String mysfitId;
    private String name;
    private String species;
    private Integer age;
    private String description;
    private String goodevil;
    private String lawchaos;
    private String thumbImageUri;
    private String profileImageUri;
    private Integer likes;
    private Boolean adopted;

    public Mysfit() {
    }

    @DynamoDBHashKey(attributeName="MysfitId")
    public String getMysfitId() {
        return mysfitId;
    }

    public void setMysfitId(String mysfitId) {
        this.mysfitId = mysfitId;
    }

    @DynamoDBAttribute(attributeName="Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName="Species")
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    @DynamoDBAttribute(attributeName="Age")
    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @DynamoDBAttribute(attributeName="Description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBAttribute(attributeName="GoodEvil")
    public String getGoodevil() {
        return goodevil;
    }

    public void setGoodevil(String goodevil) {
        this.goodevil = goodevil;
    }

    @DynamoDBAttribute(attributeName="LawChaos")
    public String getLawchaos() {
        return lawchaos;
    }

    public void setLawchaos(String lawchaos) {
        this.lawchaos = lawchaos;
    }

    @DynamoDBAttribute(attributeName="ThumbImageUri")
    public String getThumbImageUri() {
        return thumbImageUri;
    }

    public void setThumbImageUri(String thumbImageUri) {
        this.thumbImageUri = thumbImageUri;
    }

    @DynamoDBAttribute(attributeName="ProfileImageUri")
    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    @DynamoDBAttribute(attributeName="Likes")
    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    @DynamoDBAttribute(attributeName="adopted")
    public Boolean getAdopted() {
        return adopted;
    }

    public void setAdopted(Boolean adopted) {
        this.adopted = adopted;
    }
}
