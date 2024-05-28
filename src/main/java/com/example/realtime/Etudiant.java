package com.example.realtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Etudiant {
    private int id;

    private String nom;

    private String adresse;

    private Integer bourse;

    private String type;

    public Etudiant(int id, String nom, String adresse, Integer bourse) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.bourse = bourse;
    }

    public Etudiant(String nom, String adresse, Integer bourse) {
        this.nom = nom;
        this.adresse = adresse;
        this.bourse = bourse;
    }
    @JsonCreator
    public Etudiant(
                    @JsonProperty("type") String type,
                    @JsonProperty("nom") String nom,
                    @JsonProperty("adresse") String adresse,
                    @JsonProperty("bourse") int bourse) {
        this.type = type;
        this.nom = nom;
        this.adresse = adresse;
        this.bourse = bourse;
    }

    @JsonCreator
    public Etudiant(
            @JsonProperty("id") int id,
            @JsonProperty("type") String type,
            @JsonProperty("nom") String nom,
            @JsonProperty("adresse") String adresse,
            @JsonProperty("bourse") int bourse) {
        this.id = id;
        this.type = type;
        this.nom = nom;
        this.adresse = adresse;
        this.bourse = bourse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getBourse() {
        return bourse;
    }

    public void setBourse(Integer bourse) {
        this.bourse = bourse;
    }
}
