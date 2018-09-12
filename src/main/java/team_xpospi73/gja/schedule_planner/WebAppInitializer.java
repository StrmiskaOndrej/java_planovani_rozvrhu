/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import team_xpospi73.gja.schedule_planner.model.Database;
import team_xpospi73.gja.schedule_planner.model.KontrolyRozvrhu;
import team_xpospi73.gja.schedule_planner.model.KontrolyZkousek;
import team_xpospi73.gja.schedule_planner.model.PocetStudentu;
import team_xpospi73.gja.schedule_planner.model.PozadavkyNaRozvrh;
import team_xpospi73.gja.schedule_planner.model.entity.Day;
//import team_xpospi73.gja.schedule_planner.model.Database;

@EnableWebMvc
@Configuration
@ComponentScan
public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext sc) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        sc.addListener(new ContextLoaderListener(context));
        try {
            
            
            Database.connectDB();
            ResultSet rs = null;
            try {
            rs = Database.executeQuery("SELECT * FROM predmety");
            } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
            }            
            testujKontrolu();
            Database.disconnectDB();
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void testujKontrolu() throws SQLException, ParseException{
//        String hour1 = "16:00:00";
//        java.sql.Time myTime = java.sql.Time.valueOf(hour1);
//        String hour2 = "17:50:00";
//        java.sql.Time myTime2 = java.sql.Time.valueOf(hour2);
//        
//        try {
//            int test = KontrolyRozvrhu.vyhovujePozadavku(1, "Po", myTime, myTime2);
//            if(test==1){
//                System.out.println("ok");
//            }else if (test==2){
//                System.out.println("bad");
//            }else{
//                System.out.println("neutral");
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//-----------------------------------------------------------------------------------------------
//        int[] mistnosti = {300, 90};
//        int kapacita = KontrolyRozvrhu.kontrolaKapacity(1, mistnosti, 1);
//        System.out.println("zbývající kapacita = "+kapacita);
//----------------------------------------------------------------------------------
//        String hour1 = "17:00:00";
//        java.sql.Time start = java.sql.Time.valueOf(hour1);
//        String hour2 = "18:50:00";
//        java.sql.Time konec = java.sql.Time.valueOf(hour2);
//        ArrayList<Integer> prekrivanePredmety = KontrolyRozvrhu.prekrivanyPovinnychPredmetu(3, 0, "Po", start, konec);
//        if(prekrivanePredmety.isEmpty()){
//            System.out.println("ok"); 
//        }else{
//            System.out.println("Předmět je v kolizi"); 
//        }
//------------------------------------------------------------------------------------
//        ArrayList<NedostupnePredmety> np = new ArrayList<>();
//        np = KontrolyRozvrhu.kontrolaNedostupnychPredmetu(0);
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
//        Date parsed2 = format.parse("20160104");
//        java.sql.Date datum = new java.sql.Date(parsed2.getTime());
//          String hour1 = "08:00:00";
//          java.sql.Time myTime = java.sql.Time.valueOf(hour1);
//          String hour2 = "09:50:00";
//          java.sql.Time myTime2 = java.sql.Time.valueOf(hour2);
//          int vysledek = KontrolyZkousek.vyhovujePozadavku(143, 1, datum, myTime, myTime2);
//        int mistnostiPrimo[] = {300, 90};
//        int mistnostiOb1[] = {150, 45};
//        int mistnostiOb2[] = {100, 30};
//        int kapacita = KontrolyZkousek.kontrolaKapacity(1, mistnostiPrimo, mistnostiOb1, mistnostiOb2, 1);
//        System.out.println("zbývající kapacita = "+kapacita);
//          SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
//          Date parsed2 = format.parse("20160105");
//          java.sql.Date datum = new java.sql.Date(parsed2.getTime());
//          String hour1 = "14:00:00";
//          java.sql.Time zacatekZkousky = java.sql.Time.valueOf(hour1);
//          String hour2 = "16:50:00";
//          java.sql.Time konecZkousky = java.sql.Time.valueOf(hour2);
//          ArrayList<Integer> prekrivanePredmety = KontrolyZkousek.kolizeStudentu(1, 1, datum, zacatekZkousky, konecZkousky);
//          ArrayList<PocetStudentu> pocetStudentu = KontrolyZkousek.DveZkouskyDenne(datum, 1);
//          ArrayList<PocetStudentu> pocetStudentu = KontrolyZkousek.triAViceZaTyden(datum, 1);

 //           ArrayList<PozadavkyNaRozvrh> seznamPredmetu = KontrolyRozvrhu.seznamNenaplanovanyPredmetu(1, 0);
//        List<Day> schedule = Database.getSchedule(0);
        List<Day> exams = Database.getExamSchedule(0, "2016-01-01", "2016-02-01");
          
           
    }

}