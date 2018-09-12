package team_xpospi73.gja.schedule_planner.model;

import java.util.ArrayList;
import team_xpospi73.gja.schedule_planner.model.entity.Subject;

/**
 * Třída fungující jako šablona pro metody zjišťující studenty a jejich počet 
(např. seznam studentů, kteří mají 3 zkoučky za týden)
 * @author strmiska
 */
public class PocetStudentu {
    int idStudent;
    int pocetZapsani;
    ArrayList<Subject> seznamPredmetu = new ArrayList<>();

    /**
     * Konstruktor třídy
     * @param id
     */
    public PocetStudentu(int id) {
        idStudent = id;
        pocetZapsani = 1;
    }
    
    /**
     * Přidá předmět
     * @param id
     */
    public void pridejPredmet(int id) {
        Subject subject = new Subject();
        subject.setIdPredmetu(id);
        seznamPredmetu.add(subject);
    }

    /**
     * vrátí id studenta
     * @return
     */
    public int getIdStudent() {
        return idStudent;
    }

    /**
     * vrátí počet zapsání
     * @return
     */
    public int getPocetZapsani() {
        return pocetZapsani;
    }
    
    /**
     * zvýší počet zapsání o 1
     */
    public void zvysPocet(){
        pocetZapsani++;
    }
}
