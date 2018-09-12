/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  Represents subjects in week schedule planning
 */
public class Subject implements Serializable {
    // Schedules
    public int predmetKapacita;
    public String casyPredmetuOd;
    public String casyPredmetuDo;
    public int casyPredmetuVerze;
    public String casyPredmetuTyp;
    public String casyPredmetuSkupina;
    
    // Exams
    public String casyZkousekOd;
    public String casyZkousekDo;
    public int casyZkousekVerze;
    public String casyZkousekTermin;
    public boolean casyZkousekPevnyCas;
    
    // Both
    public int predmetId; // subject ID
    public int idPredmetu; // db ID
    public String predmetNazev;  
    public String predmetZkratka;
    public String predmetFakulta;
    public int idGaranta;
    
    public List<Teacher> ucitel = new ArrayList<>(); 
    public List<Room> mistnosti = new ArrayList<>();
    
    public int getIdGaranta() {
        return idGaranta;
    }

    public void setIdGaranta(int idGaranta) {
        this.idGaranta = idGaranta;
    }
    
    public int getPredmetKapacita() {
        return predmetKapacita;
    }

    public void setPredmetKapacita(int predmetKapacita) {
        this.predmetKapacita = predmetKapacita;
    }

    public String getCasyPredmetuOd() {
        return casyPredmetuOd;
    }

    public void setCasyPredmetuOd(String casyPredmetuOd) {
        this.casyPredmetuOd = casyPredmetuOd;
    }

    public String getCasyPredmetuDo() {
        return casyPredmetuDo;
    }

    public void setCasyPredmetuDo(String casyPredmetuDo) {
        this.casyPredmetuDo = casyPredmetuDo;
    }

    public int getCasyPredmetuVerze() {
        return casyPredmetuVerze;
    }

    public void setCasyPredmetuVerze(int casyPredmetuVerze) {
        this.casyPredmetuVerze = casyPredmetuVerze;
    }

    public String getCasyPredmetuTyp() {
        return casyPredmetuTyp;
    }

    public void setCasyPredmetuTyp(String casyPredmetuTyp) {
        this.casyPredmetuTyp = casyPredmetuTyp;
    }

    public String getCasyPredmetuSkupina() {
        return casyPredmetuSkupina;
    }
    
    public void setCasyPredmetuSkupina(String casyPredmetuSkupina) {
        this.casyPredmetuSkupina = casyPredmetuSkupina;
    }

    
    public String getCasyZkousekOd() {
        return casyZkousekOd;
    }

    public void setCasyZkousekOd(String casyZkousekOd) {
        this.casyZkousekOd = casyZkousekOd;
    }

    public String getCasyZkousekDo() {
        return casyZkousekDo;
    }

    public void setCasyZkousekDo(String casyZkousekDo) {
        this.casyZkousekDo = casyZkousekDo;
    }

    public int getCasyZkousekVerze() {
        return casyZkousekVerze;
    }

    public void setCasyZkousekVerze(int casyZkousekVerze) {
        this.casyZkousekVerze = casyZkousekVerze;
    }

    public String getCasyZkousekTermin() {
        return casyZkousekTermin;
    }

    public void setCasyZkousekTermin(String casyZkousekTermin) {
        this.casyZkousekTermin = casyZkousekTermin;
    }

    public boolean isCasyZkousekPevnyCas() {
        return casyZkousekPevnyCas;
    }

    public void setCasyZkousekPevnyCas(boolean casyZkousekPevnyCas) {
        this.casyZkousekPevnyCas = casyZkousekPevnyCas;
    }

    public int getPredmetId() {
        return predmetId;
    }

    public void setPredmetId(int predmetId) {
        this.predmetId = predmetId;
    }
    
    public int getIdPredmetu() {
        return idPredmetu;
    }

    public void setIdPredmetu(int idPredmetu) {
        this.idPredmetu = idPredmetu;
    }
    
    public String getPredmetNazev() {
        return predmetNazev;
    }

    public void setPredmetNazev(String predmetNazev) {
        this.predmetNazev = predmetNazev;
    }

    public String getPredmetZkratka() {
        return predmetZkratka;
    }

    public void setPredmetZkratka(String predmetZkratka) {
        this.predmetZkratka = predmetZkratka;
    }

    public String getPredmetFakulta() {
        return predmetFakulta;
    }

    public void setPredmetFakulta(String predmetFakulta) {
        this.predmetFakulta = predmetFakulta;
    }

    public List<Teacher> getUcitel() {
        return ucitel;
    }

    public void setUcitel(List<Teacher> ucitel) {
        this.ucitel = ucitel;
    }

    public List<Room> getMistnosti() {
        return mistnosti;
    }

    public void setMistnosti(List<Room> mistnosti) {
        this.mistnosti = mistnosti;
    }
}
                                                                                                                                                                                                                                             
