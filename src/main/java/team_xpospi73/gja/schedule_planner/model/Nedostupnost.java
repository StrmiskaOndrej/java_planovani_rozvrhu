/*
 * authors:  Ondřej Strmiska
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model;
import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;
/**
 * Třída sloužící jako vzor pro nedostupnost
 * @author strmiska
 */
public class Nedostupnost {

    /**
     * Den nedostostupnosti
     */
    public Date den;

    /**
     * čas startu nedostupnsoti
     */
    public Time casOd;

    /**
     * čas konce nedostupnosti
     */
    public Time casDo;

    /**
     * id místnosti
     */
    public int mistnost;

    /**
     * název dne
     */
    public String denNedostupnosti;

    /**
     * Konstruktor třídy, proměnná denNedostupnosti se zjistí pomocí jiné metody
     * @param den
     * @param casOd
     * @param casDo
     * @param mistnost
     */
    public Nedostupnost(Date den, Time casOd, Time casDo, int mistnost) {
        this.den = den;
        this.casOd = casOd;
        this.casDo = casDo;
        this.mistnost = mistnost;
        
        this.denNedostupnosti = nastavDatum();
    }
    
    /**
     * Třída zjišťující, který datum je daná nedostupnost a pomocí třídy Calendar
     * zjistí o který den v týdnu se jedná
     * @return
     */
    public String nastavDatum(){
        Calendar c = Calendar.getInstance();
        c.setTime(den);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case 2:
                return "Po";
            case 3:
                return "Út";
            case 4:
                return "St";
            case 5:
                return "Čt";
            case 6:
                return "Pá";
            default:
                break;
        }
        return null;
    }
}
