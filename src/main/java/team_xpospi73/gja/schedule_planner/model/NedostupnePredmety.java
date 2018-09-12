/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team_xpospi73.gja.schedule_planner.model;

/**
 * Třída sloužící jako vzor pro nedostupnost
 * @author strmiska
 */
public class NedostupnePredmety {

    /**
     * id předmětu
     */
    public int idPredmetu;

    /**
     * proměnná určující, kolikrát je předmět nedostupný
     */
    public int pocetNedostupnosti = 1;

    /**
     * o jaký typ předmětu se jedná (přednáška, cvičení....)
     */
    public String typ;

    /**
     * Konstruktor třídy
     * @param idPredmetu
     * @param typ
     */
    public NedostupnePredmety(int idPredmetu, String typ) {
        this.idPredmetu = idPredmetu;
        this.typ = typ;
        pocetNedostupnosti = 1;
    }
}
