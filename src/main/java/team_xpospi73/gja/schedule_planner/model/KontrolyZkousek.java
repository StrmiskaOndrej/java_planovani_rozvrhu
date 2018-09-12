/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team_xpospi73.gja.schedule_planner.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * V této třídě jsou veškeré metody pro kontroly metody
 * @author strmiska
 */
public class KontrolyZkousek {
   
    /**
     * Metoda zjišťující, zda termín a čas zkoušky odpovídá požadavkům učitele.
     * V dotazu se vypíšou všechny možné termíny zkoušky a následně se zjišťuje,
     * zda vložené hodnoty odpovídají některému z termínu.
     * @param idPredmet
     * @param termin kolikátej termín
     * @param datum den zkoušky
     * @param start v kolik hodin zkouška začíná
     * @param konec v kolik hodin zkouška končí
     * @return
     * @throws SQLException 
     */
    public static int vyhovujePozadavku(int idPredmet, int termin, Date datum, Time start, Time konec) throws SQLException{
        Database.dbSuccess = true;
        int vysledek = 0;
        String sTermin = "";
        switch (termin) {
            case 1:
                sTermin = "1. termín";
                break;
            case 2:
                sTermin = "2. termín";
                break;
            case 3:
                sTermin = "3. termín";
                break;
            default:
                break;
        }
        PreparedStatement prepQuery = Database.conn.prepareStatement("SELECT id_predmetu, rozsaz, typ, datum_od, datum_do, cas_od, cas_do, termin From pozadavkyNaZkousky, terminyPozadavkuNaZkousky Where id_predmetu = ? AND pozadavkyNaZkousky.id = terminyPozadavkuNaZkousky.id_pozadavkuNaZkousky");
        prepQuery.setInt(1, idPredmet);
        ResultSet rs = prepQuery.executeQuery();   
        while (rs.next()) {
            String typ = rs.getString(3);
            Date pozDatumOd = rs.getDate(4);  //Vytažení dat z databáze (možná bude potřeba vytvořit a použít metodu na převod formátu z databáze)             
            Date pozDatumDO = rs.getDate(5);                       
            Time pozCasOd = rs.getTime(6);               
            Time pozCasDo = rs.getTime(7); 
            String sTermin2 = rs.getString(8);
            
            if((pozDatumOd.before(datum) || pozDatumOd.equals(datum)) && (pozDatumDO.after(datum) || pozDatumDO.equals(datum)) && (pozCasOd.before(start)|| pozCasOd.equals(start)) && (pozCasDo.after(konec)|| pozCasDo.equals(konec)) && typ.equals("vyhovující") && (sTermin2.equals("všechny") || sTermin2.equals(sTermin))){
                System.out.println("zkouška vyhovuje požadavku učitele");
                vysledek = 1;
                return vysledek;
            }  
            else if((pozDatumOd.before(datum) || pozDatumOd.equals(datum)) && (pozDatumDO.after(datum) || pozDatumDO.equals(datum)) && (pozCasOd.before(start)|| pozCasOd.equals(start)) && (pozCasDo.after(konec)|| pozCasDo.equals(konec)) && typ.equals("nemožný")){
                System.out.println("zkouška je v rozporu s požadavky učitele");
                vysledek = 2;
                return vysledek;
            }else{
                vysledek = 3;
            }
        }
        System.out.println("zkouška NEvyhovuje požadavku učitele");
        return vysledek;
    }

    /**
     * Metoda vracející počet míst, které v místnosti zbývají, případně vrací počet chybějících míst.
     * Tabulka mistnosti obsahujuje seznam již uvedených místností, 
     * kde každá hodnota reprezentuje počet míst s daným rozestupem (0,1,2)
     * proměnná rozestupy je získáná z požadavku na zkoušku.
     * Dotaz vypíše počet studentů, kteří mají zapsanej daný předmět.
     * Následně se podle rozestupu zjišťuje celkový počet všech dostupných míst v místnostech.
     * Na závěr se vypočítá počet přebývajích/chybějících míst a hodnota se vrátí.
     * @param idPredmet 
     * @param mistnostivedle tabulka místností s počtem míst, pokud stunenti sedí vedle sebe
     * @param mistnostiOb1 tabulka místností s počtem míst, pokud stunenti sedí ob 1 místo
     * @param mistnostiOb2 tabulka místností s počtem míst, pokud stunenti sedí ob 2 místo
     * @param rozestupy Nastavení rozestupůp mezi studenty
     * @return
     * @throws SQLException 
     */
    public static int kontrolaKapacity(int idPredmet, int[] mistnostivedle, int[] mistnostiOb1, int[] mistnostiOb2, int rozestupy) throws SQLException{
        int pocetStudentu = 0;       
        Database.dbSuccess  = true;       
        PreparedStatement prepQuery = Database.conn.prepareStatement("SELECT count(id_studenta) FROM planovaniRozvrhu.majiRegistrovane Where id_predmetu = ?");
        prepQuery.setInt(1, idPredmet); 
        ResultSet rs = prepQuery.executeQuery();        
        while (rs.next()) {          
                    pocetStudentu = rs.getInt(1);
               }
        int pocetMist = 0;
        for(int i=0; i<mistnostivedle.length; i++){
            switch (rozestupy) {
                case 0:
                    // pokud nejsou třeba žádné rozestupy
                    pocetMist += mistnostivedle[i];
                    break;
                case 1:
                    // pokud se sedí ob 1 místo
                    pocetMist += mistnostiOb1[i];
                    break;
                default:
                    // pokud se sedí ob 2 místa
                    pocetMist += mistnostiOb2[i];
                    break;
            }
        }
        pocetMist -= pocetStudentu;
        return pocetMist;
    }

    /**
     * Metoda vracející seznam studentů, u kterých dochází ke kolizi s jinou zkouškou.
     * Nejprve se naplní dotazem pole studenty, kteří mají danou zkoušku, 
     * V druhém dotazu se zjistí, které předměty se zkouší ve stejnou dobu.
     * V posledním dotazu se ptáme na seznam studentů v souběžně probíhajících zkouškách
     * a pokud se některé id studenta nachází již v naplněném listu, je idStudenta přidáno 
     * do seznamu, který je návratová hodnota 
     * @param idPredmet
     * @param idVerze verze zkoušek
     * @param datum den zkoušky
     * @param zacatekZkousky
     * @param konecZkousky
     * @return
     * @throws SQLException 
     */
    public static ArrayList<Integer> kolizeStudentu(int idPredmet, int idVerze, Date datum, Time zacatekZkousky, Time konecZkousky) throws SQLException{
        ArrayList<Integer> seznamStudentu = new ArrayList<>();
        Database.dbSuccess  = true;       
        PreparedStatement prepQuery = Database.conn.prepareStatement("SELECT id_studenta From majiRegistrovane Where id_predmetu = ?");
        prepQuery.setInt(1, idPredmet); 
        ResultSet rs = prepQuery.executeQuery();
            while (rs.next()) {                               
                    seznamStudentu.add(rs.getInt(1));  //naplní seznam id studenty, kteří mají zapsanou zkoušku         
            }

//        java.sql.Time zacatekZkousky30= zacatekZkousky;
//        zacatekZkousky30 = zacatekZkousky30.

        ArrayList<Integer> predmetyVeStejnejCas = new ArrayList<>();
        PreparedStatement prepQuery2 = Database.conn.prepareStatement("SELECT id_predmetu, datum, cas_od, cas_do, mistnosti_id, nazev, fakulta FROM casyZkousek, zkousce_prislusi, mistnosti where casyZkousek.id = zkousce_prislusi.casyZkousek_id and zkousce_prislusi.mistnosti_id = mistnosti.id and id_verze = ? and datum = ? and ((cas_od >= ? AND cas_od <= ?) OR (cas_do >= ? AND cas_do <= ?) OR (cas_od <= ? AND cas_do >= ?))");
        prepQuery2.setInt(1, idVerze); 
        prepQuery2.setDate(2, datum); 
        prepQuery2.setTime(3, zacatekZkousky);
        prepQuery2.setTime(4, konecZkousky);
        prepQuery2.setTime(5, zacatekZkousky);
        prepQuery2.setTime(6, konecZkousky);
        prepQuery2.setTime(7, zacatekZkousky);
        prepQuery2.setTime(8, konecZkousky);
        ResultSet rs2 = prepQuery2.executeQuery();
                while (rs2.next()) {                          
                        predmetyVeStejnejCas.add(rs2.getInt(1));   // získá ID z dotazu a přidá do kolekce         
               }
        ArrayList<Integer> studentiVKolizi = new ArrayList<>();
        for(int i = 0; i<=predmetyVeStejnejCas.size(); i++){
            PreparedStatement prepQuery3 = Database.conn.prepareStatement("SELECT id_studenta From majiRegistrovane Where id_predmetu = ?");
            prepQuery3.setInt(1, predmetyVeStejnejCas.get(i)); 
            ResultSet rs3 = prepQuery3.executeQuery();
                while (rs3.next()) {                           
                    int idStudenta = rs3.getInt(1);
                    if(seznamStudentu.contains(idStudenta) && !studentiVKolizi.contains(idStudenta)){                        
                       studentiVKolizi.add(idStudenta);
                    }
                }
        }      
        return studentiVKolizi;
    }
    /**
     * Metoda na základě datumu a ID verze vypíše všechny předměty, jejichž zkouška probíhá v daný den.
     * Tyto předměty přidá do kolekce. Následně se vypíše dotaz pro každý předmět v kolekci
     * a vypíše se seznam studentů, tyto studenti se přidají do kolekce. Pokud již v kolekci jsou navýší
     * se proměnná pocetZapsani, na konci metody vyřadí z kolekce všechny studenty které mají méně než 
     * 2 zkoušky a vrátí kolekci.
     * @param datum
     * @param idVerze
     * @return
     * @throws SQLException 
     */
    public static ArrayList<PocetStudentu> DveZkouskyDenne(Date datum, int idVerze) throws SQLException{
        ArrayList<Integer> predmetyVeStejnejDen = VypisPredmetyPodleData(datum, idVerze);
        ArrayList<PocetStudentu> pocetStudentu = new ArrayList<>();
        for(int i = 0; i< predmetyVeStejnejDen.size(); i++){
            pocetStudentu = naplnPocetStudentuPredmetu(predmetyVeStejnejDen.get(i), pocetStudentu);     
        }
        for(int i =0; i< pocetStudentu.size(); i++){
            if(pocetStudentu.get(i).getPocetZapsani() <= 2){
                pocetStudentu.remove(i);
                i--;
            }
        }

        return pocetStudentu;
    }
    /**
     * Metoda nejprve zjistí seznam všech studentů u zkoušky V daný, přechozí 
     * a následující den a uloží ID těchto studentů do kolekce.
     * Následně vytvoří další kolekce, které naplní uživateli, které mají zkoušku 
     * jak danný den tak předchozí/následující den. Tyto 2 kolekce znovu spojí do jedné kolekce, 
     * kde bude seznam všech studentů co mají zkoušky ve 2 dnech (nebo ve 3).
     * Možná by šlo řešit efektivněji pomocí dotazu. 
     * @param datum
     * @param idVerze
     * @return
     * @throws SQLException 
     */
    public static ArrayList<PocetStudentu> DveZkouskyDvaDny(Date datum, int idVerze) throws SQLException{
        ArrayList<Integer> predmetyVDanyDen = VypisPredmetyPodleData(datum, idVerze);

        ArrayList<PocetStudentu> pocetStudentuDanyDen = new ArrayList<>();
        for(int i = 0; i<predmetyVDanyDen.size(); i++){
            pocetStudentuDanyDen = naplnPocetStudentuPredmetu(predmetyVDanyDen.get(i), pocetStudentuDanyDen);
        }
        
        java.sql.Date predchozi= new java.sql.Date( datum.getTime() - 24*60*60*1000);
        java.sql.Date nasledujici= new java.sql.Date( datum.getTime() + 24*60*60*1000);
        ArrayList<Integer> predmetyVPredchoziDen = VypisPredmetyPodleData(predchozi, idVerze);
        ArrayList<Integer> predmetyVNasledujiciDen = VypisPredmetyPodleData(nasledujici, idVerze);
        ArrayList<PocetStudentu> pocetStudentuPredchoziDen = new ArrayList<>();
        for(int i = 0; i<predmetyVPredchoziDen.size(); i++){
            pocetStudentuPredchoziDen = naplnPocetStudentuPredmetu(predmetyVPredchoziDen.get(i), pocetStudentuPredchoziDen);
        }

        ArrayList<PocetStudentu> pocetStudentuNasledujiciDen = new ArrayList<>();
        for(int i = 0; i<predmetyVNasledujiciDen.size(); i++){
            pocetStudentuNasledujiciDen = naplnPocetStudentuPredmetu(predmetyVNasledujiciDen.get(i), pocetStudentuNasledujiciDen);
        }        
        ArrayList<PocetStudentu> pocetStudentuSpolecnePredchozi = new ArrayList<>();
        ArrayList<PocetStudentu> pocetStudentuSpolecneNasledujici = new ArrayList<>();
        ArrayList<PocetStudentu> pocetStudentuSpolecneCelkem = new ArrayList<>();
        for(int i=0; i< pocetStudentuDanyDen.size();i++){
            for(int u = 0; u < pocetStudentuPredchoziDen.size(); u++){
                if(pocetStudentuDanyDen.get(i).getIdStudent() == pocetStudentuPredchoziDen.get(u).getIdStudent()){
                    pocetStudentuSpolecnePredchozi.add(pocetStudentuDanyDen.get(i));
                    pocetStudentuSpolecneCelkem.add(pocetStudentuDanyDen.get(i));
                }
            }
        }
        
        for(int i=0; i< pocetStudentuDanyDen.size();i++){
            for(int u = 0; u < pocetStudentuNasledujiciDen.size(); u++){
                if(pocetStudentuDanyDen.get(i).getIdStudent() == pocetStudentuNasledujiciDen.get(u).getIdStudent()){
                    pocetStudentuSpolecneNasledujici.add(pocetStudentuDanyDen.get(i));
                    if(!pocetStudentuSpolecneCelkem.contains(pocetStudentuDanyDen.get(i))){
                        pocetStudentuSpolecneCelkem.add(pocetStudentuDanyDen.get(i));
                    }
                }
            }
        }

        return pocetStudentuSpolecneCelkem;
    }

    /**
     * Metoda nejdříve vypíše seznam všech předmětů zkoušeném v daném týdnu,
     * následně z těchto předmětů naplní kolekci studenty a na závěr vyhodí z kolekce studenty,
     * které mají v tomto týdnu méně než 3 zkoušky, kolekci vrátí.
     * @param datum
     * @param idVerze
     * @return
     * @throws SQLException 
     */
    public static ArrayList<PocetStudentu> triAViceZaTyden(Date datum, int idVerze) throws SQLException{

        ArrayList<Integer> seznamPredmetuVTydnu = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance(Locale.US);
        currentDate.setTime(datum);
        int firstDayOfWeek = 2;

        Calendar startDate = Calendar.getInstance(Locale.US);
        startDate.setTime(currentDate.getTime());
        //while (startDate.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
        //    startDate.add(Calendar.DATE, -1);
        //}
        int days = (startDate.get(Calendar.DAY_OF_WEEK) + 5 - firstDayOfWeek) % 5;
        startDate.add(Calendar.DATE, -days);
        Calendar endDate = Calendar.getInstance(Locale.US);
        endDate.setTime(startDate.getTime());
        endDate.add(Calendar.DATE, 6);
        Date pondeli = new java.sql.Date(startDate.getTimeInMillis());
        Date nedele = new java.sql.Date(endDate.getTimeInMillis()); 
        Database.dbSuccess  = true;       
        PreparedStatement prepQuery = Database.conn.prepareStatement("SELECT id_predmetu FROM planovaniRozvrhu.casyZkousek where id_verze = ? and datum >= ? and datum <= ?;");
        prepQuery.setInt(1, idVerze); 
        prepQuery.setDate(2, pondeli); 
        prepQuery.setDate(3, nedele); 
        ResultSet rs = prepQuery.executeQuery(); 
        while (rs.next()) {                           
            seznamPredmetuVTydnu.add(rs.getInt(1));
        }
        ArrayList<PocetStudentu> pocetStudentu = new ArrayList<>();      
        for(int i = 0; i<seznamPredmetuVTydnu.size(); i++){
            pocetStudentu = naplnPocetStudentuPredmetu(seznamPredmetuVTydnu.get(i), pocetStudentu);     
        }
        for(int i =0; i< pocetStudentu.size(); i++){
            if(pocetStudentu.get(i).getPocetZapsani() < 3){
                pocetStudentu.remove(i);
                i--;
            }
        }        
        return pocetStudentu;
    }

    /**
     * Metoda zjistí nejdříve jaké jsou všechny místnosti na FITu a přidá je do kolekce.
     * Následně vypíše seznam všech zkousek v daném termínu a pokud bude nějaká zkouška probíhat
     * v místnoti uvedené v seznamu, místnost se z kolekce odebere. Metoda vrátí seznam zbylích místností.
     * @param datum
     * @param casOD
     * @param casDO
     * @param idVerze
     * @return
     * @throws SQLException 
     */
    public static ArrayList<Integer> dostupneMistnosti(Date datum, Time casOD, Time casDO, int idVerze) throws SQLException{

        ArrayList<Integer> dostupneMistnosti = new ArrayList<>();
        PreparedStatement prepQuery = Database.conn.prepareStatement("select * from mistnosti where fakulta = 'fit'");
        ResultSet rs = prepQuery.executeQuery(); 
        while (rs.next()) {                 
            dostupneMistnosti.add(rs.getInt(1));
        }        
        PreparedStatement prepQuery2 = Database.conn.prepareStatement("select mistnosti_id from zkousce_prislusi, casyZkousek where id_verze = ? AND zkousce_prislusi.casyZkousek_id = casyZkousek.id And datum = ? AND ((cas_od >= ? AND cas_od <= ?) OR (cas_do >= ? AND cas_do <= ?) OR (cas_od <= ? AND cas_do >= ?))");
        prepQuery2.setInt(1, idVerze); 
        prepQuery2.setDate(2, datum); 
        prepQuery2.setTime(3, casOD); 
        prepQuery2.setTime(4, casDO); 
        prepQuery2.setTime(5, casOD); 
        prepQuery2.setTime(6, casDO);
        prepQuery2.setTime(7, casOD); 
        prepQuery2.setTime(8, casDO);
        ResultSet rs2 = prepQuery2.executeQuery();    
        while (rs2.next()) {     
            int mistnost = rs2.getInt(1);
            for(int i = 0; i < dostupneMistnosti.size(); i++){
                if(dostupneMistnosti.get(i) == mistnost){
                   dostupneMistnosti.remove(i);
                
                }
            }

        }    
        return dostupneMistnosti;
    }

    /**
     * Pomocný dotaz pro vypsání předmětů u zkoušky v daný datum
     * @param date
     * @param verze
     * @return
     * @throws SQLException 
     */
    public static ArrayList<Integer> VypisPredmetyPodleData(Date date, int verze) throws SQLException{
        ArrayList<Integer> seznamPredmetu = new ArrayList<Integer>();
        PreparedStatement prepQuery = Database.conn.prepareStatement("select id_predmetu from casyZkousek where datum = ? AND id_verze = ?");
        prepQuery.setDate(1, date);
        prepQuery.setInt(2, verze); 
        ResultSet rs = prepQuery.executeQuery();
                while (rs.next()) {                           
                        seznamPredmetu.add(rs.getInt(1));
               }
        return seznamPredmetu;
    }

    /**
     * Tato metoda slouží jako odkaz pro jine metody, pomocí dotazu naplní seznam 
     * studentů daného předmětu a vloží je do již vytvořené kolekce z původní metody
     * Poté co tuto metodu naplní, vrátí kolekci.
     * @param idPremetu
     * @param ps
     * @return
     * @throws SQLException 
     */
    public static ArrayList<PocetStudentu> naplnPocetStudentuPredmetu(int idPremetu, ArrayList<PocetStudentu> ps) throws SQLException{
        ArrayList<PocetStudentu> pocetStudentu = ps;
        PreparedStatement prepQuery = Database.conn.prepareStatement("SELECT id_studenta From majiRegistrovane Where id_predmetu = ?");
        prepQuery.setInt(1, idPremetu); 
        ResultSet rs = prepQuery.executeQuery();
        while (rs.next()) {       
            boolean pouzito = false;
            int idStudenta = rs.getInt(1);
                for(int i = 0; i < pocetStudentu.size(); i++){
                    if(pocetStudentu.get(i).idStudent == idStudenta){
                        pocetStudentu.get(i).zvysPocet();
                        pouzito = true;
                        pocetStudentu.get(i).pridejPredmet(idPremetu);
                    }
                }            
                if(!pouzito){                        
                   PocetStudentu student = new PocetStudentu(idStudenta);
                   pocetStudentu.add(student);
                   student.pridejPredmet(idPremetu);
                }
                
            
       }      
        return pocetStudentu;
    }
    

}

