/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents days
 */
public class Day implements Serializable {
    // Schedules
    public String denNazev;
        
    // Exams
    public int denDatumRok;
    public int denDatumMesic;
    public int denDatumDen;
    public String nazevDatum;

    public String getNazevDatum() {
        return nazevDatum;
    }

    public void setNazevDatum() {
        this.nazevDatum = denDatumDen+"."+denDatumMesic+"."+denDatumRok;
    }
            
    // Both
    public List<Subject> predmet = new ArrayList<>();
    public List<Room> mistnost = new ArrayList<>();

    public List<Room> getMistnost() {
        return mistnost;
    }

    public void setMistnost(List<Room> mistnost) {
        this.mistnost = mistnost;
    }

    public String getDenNazev() {
        return denNazev;
    }

    public void setDenNazev(String denNazev) {
        this.denNazev = denNazev;
    } 

    public int getDenDatumRok() {
        return denDatumRok;
    }

    public void setDenDatumRok(int denDatumRok) {
        this.denDatumRok = denDatumRok;
    } 

    public int getDenDatumMesic() {
        return denDatumMesic;
    }

    public void setDenDatumMesic(int denDatumMesic) {
        this.denDatumMesic = denDatumMesic;
    } 

    public int getDenDatumDen() {
        return denDatumDen;
    }

    public void setDenDatumDen(int denDatumDen) {
        this.denDatumDen = denDatumDen;
    } 

    public List<Subject> getPredmet() {
        return predmet;
    }

    public void setPredmet(List predmet) {
        this.predmet = predmet;
    } 
}
