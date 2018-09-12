/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team_xpospi73.gja.schedule_planner.model;

import java.io.FileDescriptor;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import sun.nio.ch.Net;
import team_xpospi73.gja.schedule_planner.model.Database;
import team_xpospi73.gja.schedule_planner.model.entity.Subject;
/**
 * V této třídě jsou veškeré metody pro kontroly rozvrhu
 * @author strmiska
 */
public class KontrolyRozvrhu {
    /**
     * Kontroluje zda umístění předmětu v rozvrhu vyhovuje požadavku učitele.
     * V dotazu se vypíšou všechny možné termíny a následně se zjišťuje,
     * zda vložené hodnoty odpovídají některému z termínu a typu rozvrhu (přednáška, cvičení...).
     * @param idPredmet
     * @param typ přednáška/cvičení....
     * @param den den předmětu 
     * @param start v kolik hodin předmět začíná
     * @param konec v kolik hodin předmět končí
     * @return
     * @throws SQLException 
     */
    public static int vyhovujePozadavku(int idPredmet, String den, Time start, Time konec, String typ) throws SQLException{
        Database.dbSuccess = true;
        int vysledek = 0;
        String result = "";
        
        PreparedStatement prepQuery = Database.conn.prepareStatement("Select typ_terminu, den, od, do, typ From pozadavkyNaRozvrh, terminyPozadavkuNaRozvrh Where id_predmetu = ? AND pozadavkyNaRozvrh.id = terminyPozadavkuNaRozvrh.id_PozadavkuNaRozvrh");
        prepQuery.setInt(1, idPredmet); 

        ResultSet rs = prepQuery.executeQuery();
        while (rs.next()) {   
           boolean vyhovujici = false;
           String stringResult = rs.getString(1);
           if(rs.getString(1).equals("vyhovující")){
               vyhovujici = true;
           }
           String day = rs.getString(2);
           Time pozCasOd = rs.getTime(3);               
           Time pozCasDo = rs.getTime(4);  
           String typPozadavku = rs.getString(5);
            if((pozCasOd.before(start)|| pozCasOd.equals(start)) && (pozCasDo.after(konec)|| pozCasDo.equals(konec)) && den.equals(day) && vyhovujici && typ.equals(typPozadavku)){
                result = "rozvrh vyhovuje požadavku učitele";
                vysledek = 1;
                return vysledek;
            }else if((pozCasOd.before(start)|| pozCasOd.equals(start)) && (pozCasDo.after(konec)|| pozCasDo.equals(konec) && den.equals(day)) && !vyhovujici && typ.equals(typPozadavku)){
                result = "termín nevyhovuje požadavku učitele"; 
                vysledek = 2;
                return vysledek;
            }else{
                result = "termín není v požadavku uveden";
                vysledek = 3;
            }             
        }    
        System.out.println(result);
        return vysledek;
    }
    
    
    /**
     * Kontroluje zda naplánované místnosti rozvrhu splňují požadavanou kapacitu,
     * která je uvedena v požadavku. Vstupním požadavkem je id předmětu a seznam
     * místností (včetně jejich kapacity).
     * @param idPredmet id předmětu
     * @param mistnosti pole místností, ve kterých se předmět vyučuje
     * @return
     * @throws SQLException 
     */
    public static int kontrolaKapacity(int idPredmet, int[] mistnosti) throws SQLException{
        Database.dbSuccess  = true;
        
        PreparedStatement prepQuery = Database.conn.prepareStatement("SELECT kapacita From PozadavkyNaRozvrh Where ID_PREDMETU = ?");
        prepQuery.setInt(1, idPredmet); 

        ResultSet rs = prepQuery.executeQuery();
        int kapacita = 0;
        while (rs.next()) {          
                    kapacita = rs.getInt(1);
               }
        int pocetMist = 0;
        for(int i=0; i<mistnosti.length; i++){

                pocetMist += mistnosti[i];

        }
        pocetMist -= kapacita;
       
        return pocetMist;
    }
    
    /**
     * Zkontroluje, zda se předmět nepřekrýva s jiným povinným předmětu.
     * Neprve se zkontroluje, zda je předmět vůbec povinný a zapíše se seznam oborů,
     * ve kterých je předmět povinný. Následně se zjistí seznam všech dalších povinných předmětů
     * v daných oborech. Ve třetím kroku dotaz vypíše všechny předměty probíhající v danou dobu.
     * Pokud některý z aktuálně probíhaných předmětů je ve výše uvedeném seznamu, vrací se false
     * @param idPredmet id předmětu
     * @param verzeRozvrhu pro získání již zapsaných předmětů
     * @param den den předmětu
     * @param start v kolik hodin předmět začíná
     * @param konec v kolik hodin předmět končí
     * @return 
     * @throws java.sql.SQLException 
     */
    public static ArrayList<Subject> prekrivanyPovinnychPredmetu(int idPredmet, int verzeRozvrhu, String den, Time start, Time konec) throws SQLException{
        Database.dbSuccess  = true;
        
        ArrayList<Subject> prekrivanePredmety = new ArrayList<>(); 
        ArrayList<Integer> seznamOboru = new ArrayList<>();
        ArrayList<Integer> seznamPredmetu = new ArrayList<>();
        PreparedStatement prepQuery1 = Database.conn.prepareStatement("SELECT id_oboru From PovinnostVOboru Where id_predmetu = ? AND (typ_povinnosti = 'P' OR typ_povinnosti = 'PV')");
        prepQuery1.setInt(1, idPredmet);
        ResultSet rs = prepQuery1.executeQuery();        
        while (rs.next()) {          
                    seznamOboru.add(rs.getInt(1));
        }
        
        if(!seznamOboru.isEmpty()){
            for(int i = 0; i<seznamOboru.size(); i++){
                PreparedStatement prepQuery2 = Database.conn.prepareStatement("SELECT id_predmetu From PovinnostVOboru Where id_oboru = ? AND (typ_povinnosti = 'P' OR typ_povinnosti = 'PV')");
                prepQuery2.setInt(1, seznamOboru.get(i));
                ResultSet rs2 = prepQuery2.executeQuery();
                while (rs2.next()) {    
                    if(!seznamPredmetu.contains(rs2.getInt(1)) && rs2.getInt(1) != idPredmet){
                        seznamPredmetu.add(rs2.getInt(1));
                    }
                }
            }            
        }
        PreparedStatement prepQuery3 = Database.conn.prepareStatement("SELECT id_predmetu, nazev, zkratka FROM casyPredmetu, predmety Where casyPredmetu.id_predmetu = predmety.id and id_verze=? AND den =? AND ((cas_od >= ? AND cas_od <= ?) OR (cas_do >= ? AND cas_do <= ?) OR (cas_od <= ? AND cas_do >= ?))");
        prepQuery3.setInt(1, verzeRozvrhu);
        prepQuery3.setString(2, den);
        prepQuery3.setTime(3, start);
        prepQuery3.setTime(4, konec);
        prepQuery3.setTime(5, start);
        prepQuery3.setTime(6, konec);
        prepQuery3.setTime(7, start);
        prepQuery3.setTime(8, konec);
        ResultSet rs3 = prepQuery3.executeQuery();       
        while (rs3.next()) {          
            if(seznamPredmetu.contains(rs3.getInt(1))){
                Subject pp = new Subject();
                pp.setIdPredmetu(rs3.getInt(1));
                pp.setPredmetNazev(rs3.getString(2));
                pp.setPredmetZkratka(rs3.getString(3));
                prekrivanePredmety.add(pp);
            }
        }        
        return prekrivanePredmety;
    }
    
    /**
     * Metoda vracející, které typy (kterého předmětu) odpadnou a kolikrát, kvůli nedostupnostem.
     * V 1. kroku metoda našte všechny nedostupnosti do svého pole.
     * V 2. kroku pro každou nedostupnost vypíše seznam předmětů a jeho typu v danou dobu
     * a přidá je do dalšího svého listu (pokud v tom listu již nejsou), následně tento list navrátí
     * @param verzeRozvrhu pro získání již zapsaných předmětů
     * @return
     * @throws SQLException 
     */
    public static ArrayList<NedostupnePredmety> kontrolaNedostupnychPredmetu(int verzeRozvrhu) throws SQLException{
        ArrayList<Nedostupnost> nedostupnosti = new ArrayList<Nedostupnost>();
        ArrayList<NedostupnePredmety> nedostupnePredmety = new ArrayList<NedostupnePredmety>();
        PreparedStatement prepQuery1 = Database.conn.prepareStatement("SELECT * From Nedostupnost");
        ResultSet rs = prepQuery1.executeQuery();         
        while (rs.next()) {          
                    Nedostupnost ned = new Nedostupnost(rs.getDate(4), rs.getTime(5), rs.getTime(6), rs.getInt(2));
                    nedostupnosti.add(ned);
        }
        if(!nedostupnosti.isEmpty()){
            
            for(int i = 0; i < nedostupnosti.size(); i++){        
                PreparedStatement prepQuery2 = Database.conn.prepareStatement("SELECT CasyPredmetu.id_predmetu, CasyPredmetu.typ FROM CasyPredmetu, PredmetuPrislusi\n" +
                            "WHERE CasyPredmetu.id = PredmetuPrislusi.id_casu_predmetu AND CasyPredmetu.id_verze = ? AND \n" +
                            "CasyPredmetu.den = ? AND ((cas_od >= ? AND cas_od <= ?) OR (cas_do >= ? AND cas_do <= ?) \n" +
                            "OR (cas_od <= ? AND cas_do >= ?)) AND id_mistnosti = ?");
                prepQuery2.setInt(1, verzeRozvrhu);
                prepQuery2.setString(2, nedostupnosti.get(i).denNedostupnosti);
                prepQuery2.setTime(3, nedostupnosti.get(i).casOd);
                prepQuery2.setTime(4, nedostupnosti.get(i).casDo);
                prepQuery2.setTime(5, nedostupnosti.get(i).casOd);
                prepQuery2.setTime(6, nedostupnosti.get(i).casDo);
                prepQuery2.setTime(7, nedostupnosti.get(i).casOd);
                prepQuery2.setTime(8, nedostupnosti.get(i).casDo);
                prepQuery2.setInt(9, nedostupnosti.get(i).mistnost);
                ResultSet rs2 = prepQuery2.executeQuery(); 
                while (rs2.next()) {      
                    boolean obsahuje = false;
                    for (NedostupnePredmety nedP : nedostupnePredmety) {
                        if(nedP.idPredmetu == rs2.getInt(1) && nedP.typ.equals(rs2.getString(2))){
                            nedP.pocetNedostupnosti++;
                            obsahuje = true;
                        }
                    }
                    if(!obsahuje){
                       // PocetStudentu student = new PocetStudentu(idStudenta);
                       NedostupnePredmety nedPr = new NedostupnePredmety(rs2.getInt(1), rs2.getString(2));
                       nedostupnePredmety.add(nedPr);
                    }
                }
            }  
        }     
        return nedostupnePredmety;
    
    }

    /**
     * Metoda vypisující všechny dosud nenaplánované předměty v semestru.
     * V 1. řádě se pomocí dotazů zjistí seznam všech předmětů, které se vyučijí
     * v daném semestru (dle povinnosti v oboru) a jsou na ně vypsány požadavky.
     * V druhém kroku se kontrolují již naplánované předměty. Pokud je peřdmět naplánovanán,
     * zmizí ze seznamu požadavaků
     * @param idSemestru výběr semestru
     * @param verzeRozvrhu pro získání již zapsaných předmětů
     * @return
     * @throws SQLException
     */
    public static ArrayList<PozadavkyNaRozvrh> seznamNenaplanovanyPredmetu(int idSemestru, int verzeRozvrhu) throws SQLException{
        ArrayList<PozadavkyNaRozvrh> seznamPredmetu = new ArrayList<>();
        Database.dbSuccess  = true;
        
        PreparedStatement prepQuery1 = Database.conn.prepareStatement("SELECT distinct povinnostVOboru.id_predmetu, predmety.nazev, typ, hodin, predmety.zkratka FROM povinnostVOboru, obory, pozadavkyNaRozvrh, predmety where obory.id = id_oboru and povinnostVOboru.id_predmetu = pozadavkyNaRozvrh.id_predmetu and predmety.id = pozadavkyNaRozvrh.id_predmetu and id_semestru = ?;");
        prepQuery1.setInt(1, idSemestru);
        ResultSet rs = prepQuery1.executeQuery();        
        while (rs.next()) {       
            PozadavkyNaRozvrh pozadavek = new PozadavkyNaRozvrh(rs.getInt(4), rs.getString(3));
            pozadavek.vytvorSubject(rs.getInt(1), rs.getString(2), rs.getString(5));
            seznamPredmetu.add(pozadavek); 
        }       
        PreparedStatement prepQuery2 = Database.conn.prepareStatement("SELECT id_predmetu, cas_od, cas_do, typ FROM planovaniRozvrhu.casyPredmetu where id_verze = ?;");
        prepQuery2.setInt(1, verzeRozvrhu);
        ResultSet rs2 = prepQuery2.executeQuery();        
        while (rs2.next()) {               
             int id = rs2.getInt(1);
//             Time casOd = rs2.getTime(2);
//             Time casDo = rs2.getTime(3);
             String typ = rs2.getString(4);
             if(typ.equals("p")){
                 typ = "přednáška";
             }else if(typ.equals("c")){
                 typ = "cvičení";
             }          
             for(int i = 0; i < seznamPredmetu.size(); i++){
                  if(seznamPredmetu.get(i).getPredmet().getIdPredmetu() == id && seznamPredmetu.get(i).getTyp().equals(typ)){
                      if(seznamPredmetu.get(i).getPocetHodin() > 3){
                          seznamPredmetu.get(i).pocetHodin = seznamPredmetu.get(i).pocetHodin - 2;
                      }else{
                        seznamPredmetu.remove(i);
                        i--;
                      }
                  }   
             }
                     
        }
        
        return seznamPredmetu;
    }
    
}
