/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  Represents rooms in week schedule planning
 */
public class Room implements Serializable {
    public int mistnostId;
    public String mistnostNazev;
    public int mistnostKapacita;
    public int mistnostKapacitaOb1;
    public int mistnostKapacitaOb2;
    public String mistnostFakulta;
    
    public List<Subject> predmet = new ArrayList<>();

    public List<Subject> getPredmet() {
        return predmet;
    }

    public void setPredmet(List<Subject> predmet) {
        this.predmet = predmet;
    }

    public int getMistnostId() {
        return mistnostId;
    }

    public void setMistnostId(int mistnostId) {
        this.mistnostId = mistnostId;
    } 

    public String getMistnostNazev() {
        return mistnostNazev;
    }

    public void setMistnostNazev(String mistnostNazev) {
        this.mistnostNazev = mistnostNazev;
    } 

    public int getMistnostKapacita() {
        return mistnostKapacita;
    }

    public void setMistnostKapacita(int mistnostKapacita) {
        this.mistnostKapacita = mistnostKapacita;
    } 

    public int getMistnostKapacitaOb1() {
        return mistnostKapacitaOb1;
    }

    public void setMistnostKapacitaOb1(int mistnostKapacitaOb1) {
        this.mistnostKapacitaOb1 = mistnostKapacitaOb1;
    } 

    public int getMistnostKapacitaOb2() {
        return mistnostKapacitaOb2;
    }

    public void setMistnostKapacitaOb2(int mistnostKapacitaOb2) {
        this.mistnostKapacitaOb2 = mistnostKapacitaOb2;
    } 

    public String getMistnostFakulta() {
        return mistnostFakulta;
    }

    public void setMistnostFakulta(String mistnostFakulta) {
        this.mistnostFakulta = mistnostFakulta;
    } 
}
