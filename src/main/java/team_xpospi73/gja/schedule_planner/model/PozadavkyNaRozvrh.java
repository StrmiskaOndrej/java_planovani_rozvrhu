/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team_xpospi73.gja.schedule_planner.model;

import team_xpospi73.gja.schedule_planner.model.entity.Subject;

/**
 * Pomocná třída pro výpis požadavků na rozvrh
 * @author strmiska
 */
public class PozadavkyNaRozvrh {
    Subject predmet;
    int pocetHodin;
    String typ;

    /**
     * nastaví počet zbývajících hodin
     * @param pocetHodin
     */
    public void setPocetHodin(int pocetHodin) {
        this.pocetHodin = pocetHodin;
    }

    /**
     * nastaví typ požadavku (přednáška, cvičení....)
     * @param typ
     */
    public void setTyp(String typ) {
        this.typ = typ;
    }

    /**
     * vrátí počet hodin
     * @return
     */
    public int getPocetHodin() {
        return pocetHodin;
    }

    /**
     * vrátí typ požadavku
     * @return
     */
    public String getTyp() {
        return typ;
    }

    /**
     * nastaví předmět
     * @param predmet
     */
    public void setPredmet(Subject predmet) {
        this.predmet = predmet;
    }

    /**
     * Vrátí předmět
     * @return
     */
    public Subject getPredmet() {
        return predmet;
    }
    
    /**
     * Vytvoří předmět včetně a názvu a zkratky
     * @param idPredmetu
     * @param nazev
     * @param zkratka
     */
    public void vytvorSubject(int idPredmetu, String nazev, String zkratka){
        predmet = new Subject();
        
        predmet.setIdPredmetu(idPredmetu);
        predmet.setPredmetNazev(nazev);
        predmet.setPredmetZkratka(zkratka);
        
    }

    /**
     * Konstruktor požadavku
     * @param pocetHodin
     * @param typ
     */
    public PozadavkyNaRozvrh(int pocetHodin, String typ) {
        this.pocetHodin = pocetHodin;
        this.typ = typ;
    }

}
