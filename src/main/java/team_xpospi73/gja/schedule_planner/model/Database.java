/*
 * authors:  David Dressler, xdress00
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import team_xpospi73.gja.schedule_planner.WebAppInitializer;
import team_xpospi73.gja.schedule_planner.model.entity.Day;
import team_xpospi73.gja.schedule_planner.model.entity.Room;
import team_xpospi73.gja.schedule_planner.model.entity.Semester;
import team_xpospi73.gja.schedule_planner.model.entity.Subject;
import team_xpospi73.gja.schedule_planner.model.entity.Teacher;
import team_xpospi73.gja.schedule_planner.model.entity.Version;

public class Database {
    
    public static Connection conn;
    
    public static Boolean dbSuccess;

    private static final String username = "root";
    private static final String password = "*****";
    
    
        public static void connectDB() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver ()); 
            conn = DriverManager.getConnection("jdbc:mysql://localhost/planovaniRozvrhu?" +
                                   "useUnicode=true&characterEncoding=utf-8" +
                                   "&user="+username+"&password="+password);
        }
        catch(SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            dbSuccess = false;
        }
    } 
        
    public static void disconnectDB() {
        try {
            conn.close();
        }
        catch(SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    
    public static ResultSet executeQuery(String sqlQuery) throws SQLException {
        Statement stmt;
        ResultSet rs = null;
            
        stmt = conn.createStatement();

        try {
            rs = stmt.executeQuery(sqlQuery);
        }
        catch(SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        return rs;
    }
        
    
    /**
     *
     * @param obdobi
     * @param rok
     * @param akademickyRok
     * @param zkouskoveOd
     * @param zkouskoveDo
     * @return True on success, otherwise false
     * @throws java.sql.SQLException
     */
    public static Boolean insertSemester(String obdobi, int rok, String akademickyRok,
            String zkouskoveOd, String zkouskoveDo) throws SQLException {
        PreparedStatement prepQuery, prepQuery2; 
        ResultSet rsInsertSemester;
        
        if(!isSemesterDuplicited(rok, obdobi, akademickyRok)) {
            prepQuery2 = conn.prepareStatement("select max(id) from semestry");
            rsInsertSemester = prepQuery2.executeQuery();
            int semID = 0;

            if(rsInsertSemester.next()) {
                semID=rsInsertSemester.getInt(1);
                semID++;
            }              

            prepQuery = conn.prepareStatement("insert into semestry (id, obdobi, "
                    + "rok, akademicky_rok, zkouskove_od, zkouskove_do) "
                    + "values (?,?,?,?,?,?)");
            prepQuery.setInt(1, semID);            
            prepQuery.setString(2, obdobi);
            prepQuery.setInt(3, rok);
            prepQuery.setString(4, akademickyRok);
            prepQuery.setString(5, zkouskoveOd);
            prepQuery.setString(6, zkouskoveDo);
            prepQuery.executeUpdate();                
            prepQuery.close();
                
            return true;
        }
        return false;
    }
    
    /**
     *
     * @param idSemestru
     * @param nazevVerze
     * @return True on success, otherwise false
     * @throws java.sql.SQLException
     */
    public static Boolean insertScheduleVersion(int idSemestru, String nazevVerze) throws SQLException{
        PreparedStatement prepQuery, prepQuery2, prepQuery3, prepQuery4;
        ResultSet rsInsertScheduleVersion;
        if(!isScheduleVersionDuplicited(nazevVerze)) {
            try {
                prepQuery = conn.prepareStatement("update verzeRozvrhu "
                    + "set je_aktivni_flag = 0 where je_aktivni_flag = 1");

                prepQuery.executeUpdate();  
                prepQuery.close();            

                prepQuery2 = conn.prepareStatement("select max(id) from verzeRozvrhu");
                rsInsertScheduleVersion = prepQuery2.executeQuery();
                int verzeRozvrhuID = 0;

                if(rsInsertScheduleVersion.next()) {
                    verzeRozvrhuID=rsInsertScheduleVersion.getInt(1);
                    verzeRozvrhuID++;
                } 

                prepQuery4 = conn.prepareStatement("select max(cislo_verze) from verzeRozvrhu");
                rsInsertScheduleVersion = prepQuery2.executeQuery();
                int cisloVerze = 0;

                if(rsInsertScheduleVersion.next()) {
                    cisloVerze=rsInsertScheduleVersion.getInt(1);
                    cisloVerze++;
                } 

                prepQuery3 = conn.prepareStatement(   
                    "insert into verzeRozvrhu (id, id_semestru, nazev_verze,"
                            + " cislo_verze, je_aktivni_flag)"
                            + "values (?,?,?,?,?)");
                prepQuery3.setInt(1, verzeRozvrhuID);            
                prepQuery3.setInt(2, idSemestru);
                prepQuery3.setString(3, nazevVerze);
                prepQuery3.setInt(4, cisloVerze);
                prepQuery3.setInt(5, 1);           
                prepQuery3.executeUpdate();                
                prepQuery3.close();
                System.out.println("Schedule's version was inserted successfully!");
            }
            catch(SQLException ex){
                System.out.println("Could not insert schedule's version: " + ex.getMessage());
                dbSuccess = false;
            }
            return true;
        }
        return false;
    }
    
    public static Boolean insertExamVersion(int idSemestru, String nazevVerze) throws SQLException{
        PreparedStatement prepQuery, prepQuery2, prepQuery3, prepQuery4;
        ResultSet rsInsertScheduleVersion;
        
        if(!isExamVersionDuplicited(nazevVerze)) {
            try {
                prepQuery = conn.prepareStatement("update verzeZkousek "
                    + "set je_aktivni_flag = 0 where je_aktivni_flag = 1");

                prepQuery.executeUpdate();  
                prepQuery.close();            

                prepQuery2 = conn.prepareStatement("select max(id) from verzeZkousek");
                rsInsertScheduleVersion = prepQuery2.executeQuery();
                int verzeZkousekID = 0;

                if(rsInsertScheduleVersion.next()) {
                    verzeZkousekID=rsInsertScheduleVersion.getInt(1);
                    verzeZkousekID++;
                } 

                prepQuery4 = conn.prepareStatement("select max(cislo_verze) from verzeZkousek");
                rsInsertScheduleVersion = prepQuery2.executeQuery();
                int cisloVerze = 0;

                if(rsInsertScheduleVersion.next()) {
                    cisloVerze=rsInsertScheduleVersion.getInt(1);
                    cisloVerze++;
                } 

                prepQuery3 = conn.prepareStatement(   
                    "insert into verzeZkousek (id, id_semestru, nazev_verze,"
                            + " cislo_verze, je_aktivni_flag)"
                            + "values (?,?,?,?,?)");
                prepQuery3.setInt(1, verzeZkousekID);            
                prepQuery3.setInt(2, idSemestru);
                prepQuery3.setString(3, nazevVerze);
                prepQuery3.setInt(4, cisloVerze);
                prepQuery3.setInt(5, 1);           
                prepQuery3.executeUpdate();                
                prepQuery3.close();
                System.out.println("Schedule's version was inserted successfully!");
            }
            catch(SQLException ex){
                System.out.println("Could not insert schedule's version: " + ex.getMessage());
                dbSuccess = false;
            }
            return true;
        }
        return false;
    }
    
    /**
     *
     * @param idVerze
     * @param zkouskoveOd
     * @param zkouskoveDo
     * @return List of all days as Day object in exam version.
     * @throws SQLException
     * @throws java.text.ParseException
     */
    public static List<Day> getExamSchedule(int idVerze, String zkouskoveOd, 
            String zkouskoveDo) throws SQLException, ParseException {
        
        List<Day> examSchedule = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date dateOd = format.parse(zkouskoveOd);
        
        cal.setTime(dateOd);
        
        java.util.Date dateDo = format.parse(zkouskoveDo);
        
        while(!cal.getTime().after(dateDo)) {
            Day day;
            day = getDayExam(format.format(cal.getTime()),idVerze); 
            examSchedule.add(day);
            cal.add(Calendar.DATE, 1);
        }
        
        return examSchedule;
    }
    
    /**
     *
     * @param datum
     * @param idVerze
     * @return Day of exams
     * @throws SQLException
     */
    public static Day getDayExam(String datum, int idVerze) throws SQLException {
        PreparedStatement prepQuery,prepQuery2;             
        ResultSet rsGetDay,rsGetDay2,rsGetDay3;
        
        List<Subject> subjects = new ArrayList<>();
        
        Day day = new Day();

        day.setDenDatumDen(Integer.parseInt(datum.substring(8, 10)));
        day.setDenDatumMesic(Integer.parseInt(datum.substring(5, 7)));
        day.setDenDatumRok(Integer.parseInt(datum.substring(0, 4)));
        day.setNazevDatum();
        
        rsGetDay = getCasyZkousek(idVerze);
        
        List<Integer> idcasuzkousek = new ArrayList();
                
        try {
            while(rsGetDay.next()) {
                if(rsGetDay.getString(5).equals(datum)) {
                    prepQuery2 = conn.prepareStatement("select max(verze) "
                            + "from casyZkousek "
                            + "WHERE id_predmetu = \""+rsGetDay.getInt(3)+"\"");
                        rsGetDay3 = prepQuery2.executeQuery();
                        int verzeZkouskyMax = 1;

                        if(rsGetDay3.next()) {
                            verzeZkouskyMax=rsGetDay3.getInt(1);
                        }
                        
                    if(verzeZkouskyMax == rsGetDay.getInt(4)) {
                        Subject subject = new Subject();
                        idcasuzkousek.add(rsGetDay.getInt(1));
                        subject.setIdPredmetu(rsGetDay.getInt(3));
                        subject.setCasyZkousekVerze(rsGetDay.getInt(4));
                        subject.setCasyZkousekOd(rsGetDay.getString(6));
                        subject.setCasyZkousekDo(rsGetDay.getString(7));
                        subject.setCasyZkousekTermin(rsGetDay.getString(8));  

                        if(rsGetDay.getInt(9) == 1) {
                            subject.setCasyZkousekPevnyCas(true);
                        }
                        else {
                            subject.setCasyZkousekPevnyCas(false);
                        }

                        subjects.add(subject);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }           

        for (Subject subject : subjects) {
            rsGetDay2 = getPredmetById(subject.getIdPredmetu());
        
            while(rsGetDay2.next()) {
                subject.setIdGaranta(rsGetDay2.getInt(2));
                subject.setPredmetId(rsGetDay2.getInt(3));
                subject.setPredmetNazev(rsGetDay2.getString(4));
                subject.setPredmetZkratka(rsGetDay2.getString(5));
                subject.setPredmetFakulta(rsGetDay2.getString(6));
                subject.setPredmetKapacita(rsGetDay2.getInt(7));   
            }
        }
        
        for (Subject subject : subjects) {
            List<Teacher> teachers = new ArrayList();
            teachers = getPrednasejiciPredmetu(subject.getIdPredmetu());
            subject.setUcitel(teachers);
        }
        
        int i = 0;
        for (Subject subject : subjects) {

            List<Room> mistnosti = new ArrayList();
            mistnosti = getZkouscePrislusi(idcasuzkousek.get(i));
            i++;
            subject.setMistnosti(mistnosti);
        }
           
        day.setPredmet(subjects);       

        return day;
    }
    
        /**
     *
     * @param idCasuZkousek
     * @return List of Rooms of subject.
     * @throws SQLException
     */
    public static List<Room> getZkouscePrislusi(int idCasuZkousek) throws SQLException {
        List<Room> mistnosti = new ArrayList();
        PreparedStatement prepQuery;
        ResultSet rsPredmetuPrislusi;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM zkousce_prislusi "
                + "WHERE casyZkousek_id = \""+idCasuZkousek+"\"");
        rsPredmetuPrislusi = prepQuery.executeQuery();

        try {
            while(rsPredmetuPrislusi.next()) {
                Room room = new Room();
                int idMistnosti = rsPredmetuPrislusi.getInt(2);
                room = getMistnostByID(idMistnosti);
                mistnosti.add(room);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }                  
        
        return mistnosti;
    }
    
    /**
     *
     * @param daysSchedule
     * @param idVerzeRozvrhu
     * @return True on success, otherwise false
     * @throws SQLException
     */
    public static Boolean setSchedule(List<Day> daysSchedule, int idVerzeRozvrhu) throws SQLException {
        PreparedStatement prepQuery, prepQuery2,prepQuery3,prepQuery4;
        PreparedStatement prepQuery5, prepQuery6,prepQuery7,prepQuery8,prepQuery9; 
        ResultSet rsSetSchedule,rsSetSchedule2,rsSetSchedule3,rsSetSchedule4;
        ResultSet rsSetSchedule5,rsSetSchedule6;
        
        for (Day day : daysSchedule) {
            for (Subject subject : day.getPredmet()) {
                int idcasuPredmetu = -1;
                int idPredmetu = -1;
                
                try {
                    prepQuery9 = Database.conn.prepareStatement(
                    "SELECT id "
                    + "FROM predmety "
                    + "WHERE zkratka = \""+subject.getPredmetZkratka()+"\" ");

                rsSetSchedule6 = prepQuery9.executeQuery(); 

                if (rsSetSchedule6.next())
                    idPredmetu = rsSetSchedule6.getInt(1);
                
                subject.setIdPredmetu(idPredmetu);
                
                prepQuery = Database.conn.prepareStatement(
                    "SELECT id "
                    + "FROM casyPredmetu "
                    + "WHERE id_verze = \""+idVerzeRozvrhu+"\" "
                    + "AND id_predmetu = \""+ subject.getIdPredmetu()+ "\" "
                    + "AND den = \""+ day.getDenNazev() + "\" "        
                    + "AND cas_od = \""+ subject.getCasyPredmetuOd()+ ":00.000000\" "        
                    + "AND cas_do = \""+ subject.getCasyPredmetuDo()+ ":00.000000\" ");

                rsSetSchedule = prepQuery.executeQuery(); 

                if (rsSetSchedule.next())
                    idcasuPredmetu = rsSetSchedule.getInt(1);
                
                // Create new casuPredmetu
                if(idcasuPredmetu == -1) {
                    prepQuery2 = conn.prepareStatement("select max(id) from casyPredmetu");
                    rsSetSchedule2 = prepQuery2.executeQuery();
                    int casyPredmetuID = 0;

                    if(rsSetSchedule2.next()) {
                        casyPredmetuID=rsSetSchedule2.getInt(1);
                        casyPredmetuID++;
                    } 
                    
                    prepQuery4 = conn.prepareStatement("select max(verze) from casyPredmetu "
                            + "WHERE id_predmetu = \""+subject.getIdPredmetu()+"\"");
                    rsSetSchedule3 = prepQuery4.executeQuery();
                    int casyPredmetuVerze = 0;

                    if(rsSetSchedule3.next()) {
                        casyPredmetuVerze=rsSetSchedule3.getInt(1);
                        casyPredmetuVerze++;
                    } 
                    
                    prepQuery3 = conn.prepareStatement(   
                    "insert into casyPredmetu (id, id_verze, id_predmetu,"
                            + " verze, den, cas_od, cas_do, typ, skupina)"
                            + "values (?,?,?,?,?,?,?,?,?)");
                    prepQuery3.setInt(1, casyPredmetuID);            
                    prepQuery3.setInt(2, idVerzeRozvrhu);
                    prepQuery3.setInt(3, subject.getIdPredmetu());
                    prepQuery3.setInt(4, casyPredmetuVerze);
                    prepQuery3.setString(5, day.getDenNazev());     
                    prepQuery3.setString(6, subject.getCasyPredmetuOd()); 
                    prepQuery3.setString(7, subject.getCasyPredmetuDo()); 
                    prepQuery3.setString(8, subject.getCasyPredmetuTyp()); 
                    prepQuery3.setString(9, subject.getCasyPredmetuSkupina()); 
                    
                    prepQuery3.executeUpdate();                
                    prepQuery3.close();
                    
                    for (Room room : subject.getMistnosti()) {
                        prepQuery5 = conn.prepareStatement("select max(id) from predmetuPrislusi");
                        rsSetSchedule4 = prepQuery5.executeQuery();
                        int idPredmetuPrislusi = 0;

                        if(rsSetSchedule4.next()) {
                            idPredmetuPrislusi=rsSetSchedule4.getInt(1);
                            idPredmetuPrislusi++;
                        }
                        
                        prepQuery6 = conn.prepareStatement(   
                        "insert into predmetuPrislusi (id, id_casu_predmetu, id_mistnosti)"
                                + "values (?,?,?)");
                        prepQuery6.setInt(1, idPredmetuPrislusi);            
                        prepQuery6.setInt(2, casyPredmetuID);
                        prepQuery6.setInt(3, room.getMistnostId());

                        prepQuery6.executeUpdate();                
                        prepQuery6.close();
                    }
                }
                else {
                    // Rooms for casyPredmetu that exist
                    for (Room room : subject.getMistnosti()) {
                        prepQuery = Database.conn.prepareStatement(
                        "SELECT id "
                        + "FROM predmetuPrislusi "
                        + "WHERE id_casu_predmetu = \""+idcasuPredmetu+"\" "
                        + "AND id_mistnosti = \""+ room.getMistnostId()+"\" ");

                        rsSetSchedule = prepQuery.executeQuery(); 
                        int idPredmetuPrislusi = -1;
                        
                        if (rsSetSchedule.next())
                            idPredmetuPrislusi = rsSetSchedule.getInt(1);
                        
                        // If room is not in predmetuPrislusi
                        if(idPredmetuPrislusi == -1) {
                            prepQuery7 = conn.prepareStatement("select max(id) from predmetuPrislusi");
                            rsSetSchedule5 = prepQuery7.executeQuery();
                            int idPredmetuPrislusi_ = 0;

                            if(rsSetSchedule5.next()) {
                                idPredmetuPrislusi_=rsSetSchedule5.getInt(1);
                                idPredmetuPrislusi_++;
                            }
                            
                            prepQuery8 = conn.prepareStatement(   
                            "insert into predmetuPrislusi (id, id_casu_predmetu, id_mistnosti)"
                                    + "values (?,?,?)");
                            prepQuery8.setInt(1, idPredmetuPrislusi_);            
                            prepQuery8.setInt(2, idcasuPredmetu);
                            prepQuery8.setInt(3, room.getMistnostId());

                            prepQuery8.executeUpdate();                
                            prepQuery8.close();
                            
                        }
                        
                    }    
                }
                
                } catch (SQLException ex) {
                    Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
                }   
            }
        }           
        
        return true;
    }
    
    /**
     *
     * @param idVerze
     * @return List of all days as Day object (Monday - Friday) in schedule version.
     * @throws SQLException
     */
    public static List<Day> getSchedule(int idVerze) throws SQLException {
        List<Day> schedule = new ArrayList<>();
        
        Day monday = new Day();
        monday = getDaySchedule("Po",idVerze);
        
        Day tuesday = new Day();
        tuesday = getDaySchedule("Út",idVerze);
        
        Day wednesday = new Day();
        wednesday = getDaySchedule("St",idVerze);
        
        Day thursday = new Day();
        thursday = getDaySchedule("Čt",idVerze);
        
        Day friday = new Day();
        friday = getDaySchedule("Pá",idVerze);
        
        schedule.add(monday);
        schedule.add(tuesday);
        schedule.add(wednesday);
        schedule.add(thursday);
        schedule.add(friday);
        
        return schedule;
    }
    
    /**
     *
     * @param nazevDne
     * @param idVerze
     * @return Day of schedule
     * @throws SQLException
     */
    public static Day getDaySchedule(String nazevDne, int idVerze) throws SQLException {
        PreparedStatement prepQuery;             
        ResultSet rsGetDay,rsGetDay2,rsGetDay3;
        
        List<Subject> subjects = new ArrayList<>();
        
        Day day = new Day();
        day.denNazev = nazevDne;
                
        rsGetDay = getCasyPredmetu(idVerze);
        
        List<Integer> idcasupredmetu = new ArrayList();
                
        try {
            while(rsGetDay.next()) {
                if(rsGetDay.getString(5).equals(nazevDne)) {
                    prepQuery = conn.prepareStatement("select max(verze) "
                            + "from casyPredmetu "
                            + "WHERE id_predmetu = \""+rsGetDay.getInt(3)+"\"");
                        rsGetDay3 = prepQuery.executeQuery();
                        int verzePredmetuMax = 1;

                        if(rsGetDay3.next()) {
                            verzePredmetuMax=rsGetDay3.getInt(1);
                        }
                        
                    if(verzePredmetuMax == rsGetDay.getInt(4)) {
                        Subject subject = new Subject();
                        idcasupredmetu.add(rsGetDay.getInt(1));
                        subject.setIdPredmetu(rsGetDay.getInt(3));
                        subject.setCasyPredmetuVerze(rsGetDay.getInt(4));
                        subject.setCasyPredmetuOd(rsGetDay.getString(6));
                        subject.setCasyPredmetuDo(rsGetDay.getString(7));
                        subject.setCasyPredmetuTyp(rsGetDay.getString(8));  
                        subject.setCasyPredmetuSkupina(rsGetDay.getString(9));  
                        subjects.add(subject);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }           

        for (Subject subject : subjects) {
            rsGetDay2 = getPredmetById(subject.getIdPredmetu());
                      
            while(rsGetDay2.next()) {
                subject.setIdGaranta(rsGetDay2.getInt(2));
                subject.setPredmetId(rsGetDay2.getInt(3));
                subject.setPredmetNazev(rsGetDay2.getString(4));
                subject.setPredmetZkratka(rsGetDay2.getString(5));
                subject.setPredmetFakulta(rsGetDay2.getString(6));
                subject.setPredmetKapacita(rsGetDay2.getInt(7));   
            }
        }
        
        for (Subject subject : subjects) {
            List<Teacher> teachers = new ArrayList();
            teachers = getPrednasejiciPredmetu(subject.getIdPredmetu());
            subject.setUcitel(teachers);
        }
        
        int i = 0;   
        for (Subject subject : subjects) {
            List<Room> mistnosti = new ArrayList();
            mistnosti = getPredmetuPrislusi(idcasupredmetu.get(i));
            i++;
            subject.setMistnosti(mistnosti);
        }
        
        day.setPredmet(subjects);
        
        
        return day;
    }
    
    /**
     *
     * @param idCasuPredmetu
     * @return List of Rooms of subject.
     * @throws SQLException
     */
    public static List<Room> getPredmetuPrislusi(int idCasuPredmetu) throws SQLException {
        List<Room> mistnosti = new ArrayList();
        PreparedStatement prepQuery;
        ResultSet rsPredmetuPrislusi;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM predmetuPrislusi "
                + "WHERE id_casu_predmetu = \""+idCasuPredmetu+"\"");
        rsPredmetuPrislusi = prepQuery.executeQuery();

        try {
            while(rsPredmetuPrislusi.next()) {
                Room room = new Room();
                int idMistnosti = rsPredmetuPrislusi.getInt(3);
                room = getMistnostByID(idMistnosti);
                mistnosti.add(room);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }                  
        
        return mistnosti;
    }
    
    /**
     *
     * @param idPredmetu
     * @return ResultSet of subject.
     * @throws SQLException
     */
    public static ResultSet getPredmetById(int idPredmetu) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsPredmet;

        prepQuery = Database.conn.prepareStatement("SELECT * FROM predmety "
                + "WHERE id = \""+idPredmetu+"\"");
        rsPredmet = prepQuery.executeQuery();
          
        return rsPredmet;
    }
    
    /**
     *
     * @param idPredmetu
     * @return List of all teachers of subject.
     * @throws java.sql.SQLException
     */
    public static List<Teacher> getPrednasejiciPredmetu(int idPredmetu) throws SQLException {
        PreparedStatement prepQuery, prepQuery2;
        ResultSet rsPrednasejiciPredmetu,rsPrednasejiciPredmetu2;
        
        List<Teacher> teachers = new ArrayList();
        
        prepQuery = Database.conn.prepareStatement("SELECT id_ucitele FROM prednasejiciPredmetu "
                + "WHERE id_predmetu = \""+idPredmetu+"\"");
        rsPrednasejiciPredmetu = prepQuery.executeQuery();
        
        try {
            while(rsPrednasejiciPredmetu.next()) {
                Teacher teacher = new Teacher();
                teacher.setUcitelId(rsPrednasejiciPredmetu.getInt(1));
                teachers.add(teacher);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
        List<Teacher> teachersFull = new ArrayList();
        
        for (Teacher teacher : teachers) {
            Teacher teacherFull = new Teacher();
            teacherFull = getPrednasejiciPredmetuById(teacher.getUcitelId());    
            teachersFull.add(teacherFull);
        }
        
        return teachersFull;
    }
    
    /**
     *
     * @param idTeacher
     * @return Teacher
     * @throws SQLException
     */
    public static Teacher getPrednasejiciPredmetuById(int idTeacher) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsPrednasejiciPredmetuById;
        Teacher teacher = new Teacher();
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM ucitele "
                + "WHERE id = \""+idTeacher+"\"");
        rsPrednasejiciPredmetuById = prepQuery.executeQuery();
        
        try {
            if(rsPrednasejiciPredmetuById.next()) {
                teacher.setUcitelId(rsPrednasejiciPredmetuById.getInt(1));
                teacher.setUcitelJmeno(rsPrednasejiciPredmetuById.getString(2));
                teacher.setUcitelEmail(rsPrednasejiciPredmetuById.getString(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }  
        
        return teacher;
    }
    
    /**
     *
     * @param idGaranta
     * @return Garant of subject as Teacher object.
     * @throws SQLException
     */
    public static Teacher getGarantPredmetuById(int idGaranta) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsGarantPredmetuById;
        Teacher garant = new Teacher();
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM ucitele "
                + "WHERE id = \""+idGaranta+"\"");
        rsGarantPredmetuById = prepQuery.executeQuery();
        
        try {
            if(rsGarantPredmetuById.next()) {
                garant.setUcitelId(rsGarantPredmetuById.getInt(1));
                garant.setUcitelJmeno(rsGarantPredmetuById.getString(2));
                garant.setUcitelEmail(rsGarantPredmetuById.getString(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }  
        
        return garant;
    }
    
    /**
     *
     * @return List of all semesters as Semester object.
     * @throws SQLException
     */
    public static List<Semester> getSemestry() throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsSemestry;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM semestry ");
        rsSemestry = prepQuery.executeQuery();
        List<Semester> semesters = new ArrayList<>();
        
        try {
            while(rsSemestry.next()) {
                Semester semester = getSemestrBy(rsSemestry.getString(2), rsSemestry.getInt(3));
                semesters.add(semester);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }     
        
        return semesters;
    }
    
    /**
     *
     * @param obdobi
     * @param rok
     * @return Semester object.
     * @throws SQLException
     */
    public static Semester getSemestrBy(String obdobi, int rok) throws SQLException {
        PreparedStatement prepQuery,prepQuery2,prepQuery3;
        ResultSet rsSemestr,rsSemestr2,rsSemestr3;
        
        Semester semestr = new Semester();
        
        List<Version> schedulesV = new ArrayList<>();
        List<Version> examsV = new ArrayList<>();
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM semestry "
                + "WHERE obdobi = \""+obdobi+"\" "
                        + "AND rok = \""+rok+"\"");
        rsSemestr = prepQuery.executeQuery();
        
        try {
            if(rsSemestr.next()) {
                semestr.setSemestrId(rsSemestr.getInt(1));
                semestr.setObdobi(rsSemestr.getString(2));
                semestr.setRok(rsSemestr.getInt(3));
                semestr.setAkademickyRok(rsSemestr.getString(4));
                semestr.setZkouskoveOd(rsSemestr.getString(5));
                semestr.setZkouskoveDo(rsSemestr.getString(6));
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }     
        
        prepQuery2 = Database.conn.prepareStatement("SELECT * FROM verzeRozvrhu "
                + "WHERE id_semestru = \""+semestr.getSemestrId()+"\"");
        rsSemestr2 = prepQuery2.executeQuery();
        
        try {
            while(rsSemestr2.next()) {
                Version version = new Version();
                
                version.setVerzeId(rsSemestr2.getInt(1));
                version.setNazev(rsSemestr2.getString(3));
                version.setCisloVerze(rsSemestr2.getInt(4));
                
                if(rsSemestr2.getInt(5) == 1) {
                    version.setJeAktivniFlag(true);
                }
                else {
                    version.setJeAktivniFlag(false);
                }
 
                schedulesV.add(version);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }     
        
        semestr.setScheduleVersion(schedulesV);
        
        prepQuery3 = Database.conn.prepareStatement("SELECT * FROM verzeZkousek "
                + "WHERE id_semestru = \""+semestr.getSemestrId()+"\"");
        rsSemestr3 = prepQuery3.executeQuery();
        
        try {
            while(rsSemestr3.next()) {
                Version version = new Version();
                
                version.setVerzeId(rsSemestr3.getInt(1));
                version.setNazev(rsSemestr3.getString(3));
                version.setCisloVerze(rsSemestr3.getInt(4));
                
                if(rsSemestr3.getInt(5) == 1) {
                    version.setJeAktivniFlag(true);
                }
                else {
                    version.setJeAktivniFlag(false);
                }
 
                examsV.add(version);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }     
        
        semestr.setExamsVersion(examsV);
        
        return semestr;
    }
    
    /**
     *
     * @param idSemestru
     * @return ResultSet of all versions of schedules.
     * @throws SQLException
     */
    public static ResultSet getVerzeRozvrhu(int idSemestru) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsVerzeRozvrhu;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM verzeRozvrhu "
                + "WHERE id_semestru = \""+idSemestru+"\"");
        rsVerzeRozvrhu = prepQuery.executeQuery();
        
        return rsVerzeRozvrhu;
    }
    
    /**
     *
     * @param idVerze
     * @return ResultSet of all versions of subjects in schedule.
     * @throws SQLException
     */
    public static ResultSet getCasyPredmetu(int idVerze) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsCasyPredmetu;

        prepQuery = Database.conn.prepareStatement("SELECT id,id_verze,"
                + "id_predmetu,verze,den,TIME_FORMAT(cas_od,'%H:%i') AS cas_od,"
                + "TIME_FORMAT(cas_do,'%H:%i') AS cas_do,typ,skupina \n"
                + "FROM casyPredmetu "
                + "WHERE id_verze = \""+idVerze+"\"");
        rsCasyPredmetu = prepQuery.executeQuery();
        
        return rsCasyPredmetu;
    }
    
    /**
     *
     * @param idSemestru
     * @return ResultSet of all versions of Exams.
     * @throws SQLException
     */
    public static ResultSet getVerzeZkousek(int idSemestru) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsVerzeZkousek;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM verzeZkousek "
                + "WHERE id_semestru = \""+idSemestru+"\"");
        rsVerzeZkousek = prepQuery.executeQuery();
        
        return rsVerzeZkousek;
    }
    
    /**
     *
     * @param idVerze
     * @return ResultSet of all versions of exams in schedule.
     * @throws SQLException
     */
    public static ResultSet getCasyZkousek(int idVerze) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsCasyZkousek;
        
        prepQuery = Database.conn.prepareStatement("SELECT id,id_verze,"
                + "id_predmetu,verze,datum,TIME_FORMAT(cas_od,'%H:%i') AS cas_od,"
                + "TIME_FORMAT(cas_do,'%H:%i') AS cas_do,termin,pevny_cas_flag \n"
                + "FROM casyZkousek "
                + "WHERE id_verze = \""+idVerze+"\"");

        rsCasyZkousek = prepQuery.executeQuery();
        
        return rsCasyZkousek;
    }
     
    /**
     *
     * @return List of all rooms as Room object.
     * @throws SQLException
     */
    public static List<Room> getMistnosti() throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsMistnosti;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM mistnosti ");
        rsMistnosti = prepQuery.executeQuery();
        List<Room> rooms = new ArrayList<>();
        
        try {
            while(rsMistnosti.next()) {
                Room room = new Room();
                room.setMistnostId(rsMistnosti.getInt(1));
                room.setMistnostNazev(rsMistnosti.getString(2));
                room.setMistnostKapacita(rsMistnosti.getInt(3));
                room.setMistnostKapacitaOb1(rsMistnosti.getInt(4));
                room.setMistnostKapacitaOb2(rsMistnosti.getInt(5));
                room.setMistnostFakulta(rsMistnosti.getString(5));
                rooms.add(room);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }     
        
        return rooms;
    }
    
    /**
     *
     * @param mistnostID
     * @return Room object.
     * @throws SQLException
     */
    public static Room getMistnostByID(int mistnostID) throws SQLException {
        PreparedStatement prepQuery;
        ResultSet rsMistnostByID;
        
        prepQuery = Database.conn.prepareStatement("SELECT * FROM mistnosti "
                + "WHERE id = \""+mistnostID+"\"");
        rsMistnostByID = prepQuery.executeQuery();
        Room room = new Room();
        
        try {
            if(rsMistnostByID.next()) {
                room.setMistnostId(rsMistnostByID.getInt(1));
                room.setMistnostNazev(rsMistnostByID.getString(2));
                room.setMistnostKapacita(rsMistnostByID.getInt(3));
                room.setMistnostKapacitaOb1(rsMistnostByID.getInt(4));
                room.setMistnostKapacitaOb2(rsMistnostByID.getInt(5));
                room.setMistnostFakulta(rsMistnostByID.getString(5));
            }
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return room;
    }
    
    
    /**
     * Return true, if semester already exists
     * @param year Year
     * @param term Semester type
     * @param academicYear Academic year
     * @return True if exist
     * @throws SQLException 
     */
    public static boolean isSemesterDuplicited(int year, String term, String academicYear) throws SQLException {
        PreparedStatement pre;
        ResultSet rs = null;
                    
        int id = 0;
        try {
            pre = Database.conn.prepareStatement(
                "select ifnull(max(id), -1) "
                + "from semestry "
                + "where obdobi = \""+term+"\" "
                + "and akademicky_rok = \""+academicYear + "\" "
                + "and rok = "+ year);

            rs = pre.executeQuery(); 
                                  
            if (rs.next())
                id = rs.getInt(1);
                        
            if (id < 0)
                return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return true;
    }
    
    /**
     * Return true, if version already exists
     * @param name Name of version
     * @return True if exist
     * @throws SQLException 
     */
    public static boolean isScheduleVersionDuplicited(String name) throws SQLException {
        PreparedStatement pre;
        ResultSet rs = null;
                    
        int id = -1;
        try {
            pre = Database.conn.prepareStatement(
                "select ifnull(max(id), -1) "
                + "from verzeRozvrhu "
                + "where nazev_verze = \""+name+"\"");

            rs = pre.executeQuery(); 
                                  
            if (rs.next())
                id = rs.getInt(1);
                        
            if (id < 0)
                return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return true;
    }
    
    
    /**
     * Return true, if version already exists
     * @param name Name of version
     * @return True if exist
     * @throws SQLException 
     */
    public static boolean isExamVersionDuplicited(String name) throws SQLException {
        PreparedStatement pre;
        ResultSet rs = null;
                    
        int id = -1;
        try {
            pre = Database.conn.prepareStatement(
                "select ifnull(max(id), -1) "
                + "from verzeZkousek "
                + "where nazev_verze = \""+name+"\"");

            rs = pre.executeQuery(); 
                                  
            if (rs.next())
                id = rs.getInt(1);
                        
            if (id < 0)
                return false;
            
        } catch (SQLException ex) {
            Logger.getLogger(WebAppInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return true;
    }
    
    /***********************
     * IMPORTS      
     **********************/
    /**
     * Check, if lecturer already exist
     * @param lecturer Name of lecturer
     * @return True if exist, other false
     */
    public static int CheckLecturer(String lecturer){
        PreparedStatement pres;
        try {
            int lectID = 0;
            pres = Database.conn.prepareStatement("select max(id) from ucitele where jmeno = \""+lecturer+"\"");
            ResultSet rs = pres.executeQuery();
            while(rs.next())
                lectID=rs.getInt(1);

            //return id of lecturer or 0
            return lectID;
                                
        } catch (SQLException ex) {
            System.out.println("Could not select \""+lecturer+"\": " + ex.getMessage());      
            dbSuccess = false;
        }   
        return -1;
    }
    
    /**
     * Insert lecturers into DB
     * @param lecturersID Primary key of table "ucitele"
     * @param lecturer Name of lecturer which is inserted into DB
     */
    public static void InsertLecturers(int lecturersID, String lecturer) {
        PreparedStatement pre;
        try {
            
            
            pre = Database.conn.prepareStatement("insert into ucitele (id, jmeno) values (?,?)");
            pre.setInt(1, lecturersID); 
            pre.setString(2, lecturer);
            pre.executeUpdate();                        
            pre.close();
            
            System.out.println("\""+lecturer +"\" was inserted successfully!");
        } catch (SQLException ex) {
            System.out.println("Could not insert \""+lecturer+"\": " + ex.getMessage());        
            dbSuccess = false;
        }   
    }
    
    /**
     * Insert courses into DB
     * @param courseID ID of course from webpage
     * @param name Name of course
     * @param code Code of course
     * @param idOfGuarantee FK from "ucitele" on guarantee of course
     * @param courseIDPK PK of "predmet"
     */
    public static void InsertCourses(int courseID, String name, String code, int idOfGuarantee, int courseIDPK) {
        PreparedStatement pre;        
        try {
            pre = Database.conn.prepareStatement("insert into predmety (predmet_id, nazev, zkratka, id_garanta, id) values (?,?,?,?,?)");
            pre.setInt(1, courseID);
            pre.setString(2, name);                                        
            pre.setString(3, code);
            pre.setInt(4, idOfGuarantee);
            pre.setInt(5, courseIDPK);
            
            pre.executeUpdate();                
            pre.close();
            System.out.println("\""+code+"\" was inserted successfully!");                
        } 
        catch (SQLException ex){
            System.out.println("Could not insert course \""+code+"\": " + ex.getMessage());   
            dbSuccess = false;
        }
    }
    
    /**
     * Insert lecturers for course
     * @param lectIDs List with FK from "ucitele" for course
     * @param courseIDPK FK of course from "predmet"
     * @param lecturersCoursesID PK of "prednasejiciPredmetu"
     * @return Incremented value of PK
     */
    public static int InsertLecturersOfCourse(List<Integer> lectIDs, int courseIDPK, int lecturersCoursesID) {
        PreparedStatement pre;
        try {
            //each teacher with course
            for (int l: lectIDs) {                    
                pre = Database.conn.prepareStatement("insert into prednasejiciPredmetu (id_predmetu, id_ucitele, id) values (?,?,?)");
                pre.setInt(1, courseIDPK-1);
                pre.setInt(2, l);                                        
                pre.setInt(3, lecturersCoursesID);                                                
                
                pre.executeUpdate();                
                pre.close();
                System.out.println("PrednasejiciPredmetu was inserted successfully!");                
                lecturersCoursesID++;
            }

        } 
        catch (SQLException ex){
            System.out.println("Could not insert lecturer of course: " + ex.getMessage());   
            dbSuccess = false;
        }
        
        return lecturersCoursesID;
    }
    
    /**
     * Insert semesters into DB
     * @param semID PK of table "semestry"
     * @param year Year of semester
     * @param semester Semester name
     * @return Primary key
     */
    public static int InsertSemesters(int semID, int year, String semester){
        PreparedStatement pre;
        
        try {   
            //Winter semester
            pre = Database.conn.prepareStatement("insert into semestry (id, obdobi, rok, akademicky_rok, zkouskove_od, zkouskove_do) values (?,?,?,?,?,?)");
            pre.setInt(1, semID);            
            pre.setString(2, semester);
            pre.setInt(3, year);
            if (semester.equals("ZS")) {
                pre.setString(4, year+"/"+(year+1));
            
                //fake values
                pre.setDate(5, java.sql.Date.valueOf((year+1)+"-1-1")); //datum od
                pre.setDate(6, java.sql.Date.valueOf((year+1)+"-2-9")); //datum od
            }
            else {
                pre.setString(4, (year-1)+"/"+year);
                
                //fake values
                pre.setDate(5, java.sql.Date.valueOf(year+"-5-5")); //datum od
                pre.setDate(6, java.sql.Date.valueOf(year+"-6-15")); //datum od
            }
                
            pre.executeUpdate();
            pre.close();                  
            
            System.out.println("\""+semester+"\" was inserted successfully!");            
        }
        catch (SQLException ex){
            System.out.println("Could not insert semester: " + ex.getMessage());    
            dbSuccess = false;
        }
        
        return semID;
    }
    
    /**
     * Insert study program into DB, each studyProgram is inserted
     * 4 times (for BIT it's 6 times) (2 times for semesters (Winter and Summer) 
     * and 2 times for grades (3 times for BIT) - 1W, 1S, 2W, 2S (3W, 3S))
     * @param year Year of semester
     * @param studyProgramID PK of "obory" table
     * @param studyProgram Name of studyProgram
     * @return Incremented PK of "obory"
     */
    public static int InsertSpecialisation(int year, int studyProgramID, String studyProgram, String studyProgramFullName) {
        PreparedStatement pres1, pres2, pres3;
        PreparedStatement prew1, prew2, prew3;
        
        int tmpYear = year+1;
        
        try {   
            //Insert studyProgram for summer semester
            //studyProgram for first year
            pres1 = Database.conn.prepareStatement("insert into obory (id, zkratka, nazev, id_semestru) "
                    + "values (?,?,?,"
                    + "(select max(id) from semestry where rok = \""+tmpYear+"\" and obdobi = \"LS\"))");
            pres1.setInt(1, studyProgramID);                                 
            pres1.setString(2, "1"+studyProgram);
            pres1.setString(3, studyProgramFullName);
            pres1.executeUpdate();
            pres1.close();
            studyProgramID++;            
            
            //studyProgram for second year
            pres2 = Database.conn.prepareStatement("insert into obory (id, zkratka, nazev, id_semestru) "
                    + "values (?,?,?,"
                    + "(select max(id) from semestry where rok = \""+tmpYear+"\" and obdobi = \"LS\"))");
            pres2.setInt(1, studyProgramID);                                 
            pres2.setString(2, "2"+studyProgram);
            pres2.setString(3, studyProgramFullName);
            pres2.executeUpdate();
            pres2.close();
            studyProgramID++;

            
            //Insert studyProgram for winter semester
            //studyProgram for first year
            prew1 = Database.conn.prepareStatement("insert into obory (id, zkratka, nazev, id_semestru) "
                    + "values (?,?,?,"
                    + "(select max(id) from semestry where rok = \""+year+"\" and obdobi = \"ZS\"))");
            prew1.setInt(1, studyProgramID);                                 
            prew1.setString(2, "1"+studyProgram);    
            prew1.setString(3, studyProgramFullName);
            prew1.executeUpdate();
            prew1.close();
            studyProgramID++;
            
            //studyProgram for second year
            prew2 = Database.conn.prepareStatement("insert into obory (id, zkratka, nazev, id_semestru) "
                    + "values (?,?,?,"
                    + "(select max(id) from semestry where rok = \""+year+"\" and obdobi = \"ZS\"))");
            prew2.setInt(1, studyProgramID);                                 
            prew2.setString(2, "2"+studyProgram);   
            prew2.setString(3, studyProgramFullName);
            prew2.executeUpdate();
            prew2.close();
            studyProgramID++;
            
            //studyProgram for third year
            if (studyProgram.equals("BIT")){  
                //summer
                pres3 = Database.conn.prepareStatement("insert into obory (id, zkratka, nazev, id_semestru) "
                        + "values (?,?,?,"
                        + "(select max(id) from semestry where rok = \""+tmpYear+"\" and obdobi = \"LS\"))");
                pres3.setInt(1, studyProgramID);                                 
                pres3.setString(2, "3BIT");
                pres3.setString(3, studyProgramFullName);
                pres3.executeUpdate();                
                pres3.close();
                studyProgramID++;
                
                //winter
                prew3 = Database.conn.prepareStatement("insert into obory (id, zkratka, nazev, id_semestru) "
                        + "values (?,?,?,"
                        + "(select max(id) from semestry where rok = \""+year+"\" and obdobi = \"ZS\"))");
                prew3.setInt(1, studyProgramID);                                 
                prew3.setString(2, "3BIT");    
                prew3.setString(3, studyProgramFullName);
                prew3.executeUpdate();
                prew3.close();
                studyProgramID++;
            }
            
            System.out.println("\""+ studyProgram +"\" was inserted successfully!");
        }
        catch (SQLException ex){
            System.out.println("Could not insert studyProgram \""+studyProgram+"\": " + ex.getMessage());    
            dbSuccess = false;
        }
        
        return studyProgramID;
    }
    
    /**
     * Insert compulsory of course for studyProgram
     * If grade of course is "X", course is inserted to all grades of studyProgram
     * @param code Code of course
     * @param compulsoryID PK of table "povinnostVOboru"
     * @param type Type of compulsory
     * @param studyProgramID FK of "obory"
     * @param term Semester name
     * @param grade Grade of studyProgram for course
     * @return Incremented PK
     */
    public static int InsertCompulsory(String code, int compulsoryID, String type, int studyProgramID, 
            String term, String grade, String studyProgram) {
        PreparedStatement pre1, pre2, pre3;
        try {   
            //Insert for third grade 
            if (grade.equals("3") || (grade.equals("X") && studyProgram.equals("BIT"))) {
                pre3 = Database.conn.prepareStatement("insert into povinnostVOboru (id, typ_povinnosti, id_oboru, id_predmetu) "
                    + "values (?,?,?,"
                    + "(select max(id) from predmety where zkratka = \"" + code + "\"))");
                pre3.setInt(1, compulsoryID);
                if (type.length() > 2)
                    pre3.setString(2, type.substring(0,2));                        
                else
                    pre3.setString(2, type);


                if (term.equals("Z"))
                    pre3.setInt(3, studyProgramID-1);
                else
                    pre3.setInt(3, studyProgramID-2);                                                
                
                pre3.executeUpdate();
                pre3.close();        
                compulsoryID++;
            }

            if (studyProgram.equals("BIT")) {
                //third year is only for BIT, so subtract two
                studyProgramID -= 2;
            }
            
            //insert for second grade
            if (grade.equals("2") || grade.equals("X")) {
                pre2 = Database.conn.prepareStatement("insert into povinnostVOboru (id, typ_povinnosti, id_oboru, id_predmetu) "
                    + "values (?,?,?,"
                    + "(select max(id) from predmety where zkratka = \"" + code + "\"))");
                pre2.setInt(1, compulsoryID);
                if (type.length() > 2)
                    pre2.setString(2, type.substring(0,2));                        
                else
                    pre2.setString(2, type);

                
                if (term.equals("Z"))
                    pre2.setInt(3, studyProgramID-1);
                else
                    pre2.setInt(3, studyProgramID-3);                    

                pre2.executeUpdate();
                pre2.close();        
                compulsoryID++;
            }
            
            //insert for first grade
            if (grade.equals("1") || grade.equals("X")) {
            
                pre1 = Database.conn.prepareStatement("insert into povinnostVOboru (id, typ_povinnosti, id_oboru, id_predmetu) "
                    + "values (?,?,?,"
                    + "(select max(id) from predmety where zkratka = \"" + code + "\"))");
                pre1.setInt(1, compulsoryID);
                if (type.length() > 2)
                    pre1.setString(2, type.substring(0,2));                        
                else
                    pre1.setString(2, type);


                if (term.equals("Z"))
                    pre1.setInt(3, studyProgramID-2);
                else
                    pre1.setInt(3, studyProgramID-4);           

                pre1.executeUpdate();
                pre1.close();        
                compulsoryID++;
            }            
                        

            System.out.println("\""+ type +"\" was inserted successfully for \""+code+"\"!");
        }
        catch (SQLException ex){
            System.out.println("Could not insert compulsory \""+type+"\" for \""+code+"\": " + ex.getMessage());     
            dbSuccess = false;
        }
        
        return compulsoryID;
    }
    
    /**
     * Update count of student for course
     * @param code Code of course
     * @param capacity Max count of student of course 
     * @param studentCount Real count of student of course
     */
    public static void UpdateCourses(String code, int capacity, int studentCount) {
        PreparedStatement pre;        
        try {
            pre = Database.conn.prepareStatement("update predmety "
                + "set kapacita = ?, pocet_zapsanych_studentu = ? where zkratka = ?");


            pre.setInt(1, capacity);
            pre.setInt(2, studentCount);
            pre.setString(3, code);

            pre.executeUpdate();                
            pre.close();
            System.out.println(""+code+"'s \"kapacita\" and \"pocet_zapsanych_studentu\" updated successfully!");
        }
        catch (SQLException ex) {
            System.out.println("Could not update "+code+"'s count of students!");
            dbSuccess = false;
        }
    }
    
    /**
     * Insert requirements of teacher for lectures into DB
     * @param reqID PK of "pozadavkyRozvrhu"
     * @param tl List with values of requirements
     * @param code Code of course
     * @param teacher Teacher's name
     * @param year Year of requirement
     * @param semester Semester of requirement
     */
    public static void InsertTeacherRequirement(int reqID, List<String> tl, String code, String teacher, int year, String semester){
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(                    
                "insert into pozadavkyNaRozvrh (typ, skupin, kapacita, hodin, tyden, mistnosti, poznamka, id, id_vyucujiciho, id_predmetu)" 
                    + "values (?, ?, ?, ?, ?, ?, ?, ?,"                    
                    + "(select max(id) from ucitele where jmeno = \""+teacher+"\"),"
                    + "(select max(p.id) from predmety p join povinnostVOboru v on (p.id = v.id_predmetu) "
                        + "join obory o on (v.id_oboru = o.id) "
                        + "join semestry s on (o.id_semestru = s.id) "
                        + "where p.zkratka = \""+code+"\" and s.rok = "+ year +" and s.obdobi = \""+semester+"\"))");


            pre.setString(1, tl.get(1)); //typ
            pre.setInt(2, Integer.parseInt(tl.get(2))); //skupin
            pre.setInt(3, Integer.parseInt(tl.get(3))); //kapacita
            pre.setInt(4, Integer.parseInt(tl.get(4))); //hodin
            pre.setString(5, tl.get(5)); //tyden
            pre.setString(6, tl.get(6)); //mistnosti
            pre.setString(7, tl.get(7)); //poznamka
            pre.setInt(8, reqID);


            pre.executeUpdate();                
            pre.close();
            
            System.out.println(code+"'s requirement of lecture for "+teacher+" was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert "+code+"'s requirement for \""+teacher+"\": " + ex.getMessage());
            dbSuccess = false;
        }
    }
    
    /**
     * Insert teachar's availability for lectures into DB
     * @param termsID PK of terminyPozadavkuRozvrhu
     * @param reqID FK from pozadavkyRozvrhu
     * @param cl List with terms of availability
     * @param teacher Name of teacher
     */
    public static void InsertTeacherAvail(int termsID, int reqID, List<String> cl, String teacher){
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(   
                "insert into terminyPozadavkuNaRozvrh (den, od, do, prio, poznamka, id, typ_terminu, id_PozadavkuNaRozvrh)"
                    + "values (?, ?, ?, ?, ?," 
                    + termsID + ", \"vyhovující\"," + reqID+")");                                                

            pre.setString(1, cl.get(0)); //den
            pre.setString(2, cl.get(1)); //od
            pre.setString(3, cl.get(2)); //do
            if (cl.get(3).equals("med"))
                pre.setString(4, "medium"); //prio
            else
                pre.setString(4, cl.get(3)); //prio

            pre.setString(5, cl.get(4)); //poznamka 

            pre.executeUpdate();                
            pre.close();
            System.out.println(teacher+"'s availability for lecture for " +cl.get(0) + " was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert teacher's availability for \""+teacher+"\": " + ex.getMessage());
            dbSuccess = false;
        }
    }
    
    /**
     * Insert teachar's unavailability for lectures into DB
     * @param termsID PK of terminyPozadavkuRozvrhu
     * @param reqID FK from pozadavkyRozvrhu
     * @param cl List with terms of unavailability
     * @param teacher Name of teacher
     */
    public static void InsertTeacherUnavail(int termsID, int reqID, List<String> cl, String teacher){
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(   
                "insert into terminyPozadavkuNaRozvrh (den, od, do, poznamka, id, typ_terminu, id_PozadavkuNaRozvrh)"
                    + "values (?, ?, ?, ?," 
                    + termsID + ", \"nemožný\"," + reqID+")");                                                

            pre.setString(1, cl.get(0)); //den
            pre.setString(2, cl.get(1)); //od
            pre.setString(3, cl.get(2)); //do

            pre.setString(4, cl.get(3)); //poznamka                                

            pre.executeUpdate();                
            pre.close();   
            System.out.println(teacher+"'s unavailability for lecture for " + cl.get(0) + " was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert teacher's availability for \""+teacher+"\": " + ex.getMessage());
            dbSuccess = false;
        }
    }

    /**
     * Insert requirements of teacher for exams into DB
     * @param tl List with values of requirements
     * @param code Code of course
     * @param reqID PK of "pozadavkyNaZkousky"
     * @param teacher Name of teacher
     * @param year Year of requirement
     * @param semester Semester of requirement
     */
    public static void InsertExamsRequirement(List<String> tl, String code, int reqID, String teacher, int year, String semester) {
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(                    
                "insert into pozadavkyNaZkousky (rozsaz, poznamka, id, id_zkousejiciho, id_predmetu)" 
                    + "values (?, ?, ?,"                     
                    + "(select max(id) from ucitele where jmeno = \""+teacher+"\"),"
                    + "(select max(p.id) from predmety p join povinnostVOboru v on (p.id = v.id_predmetu) "
                        + "join obory o on (v.id_oboru = o.id) "
                        + "join semestry s on (o.id_semestru = s.id) "
                        + "where p.zkratka = \""+code+"\" and s.rok = "+ year +" and s.obdobi = \""+semester+"\"))");

            String tmp = tl.get(1);
            if (tmp.equals("ob 1"))
                pre.setString(1, "ob1"); //rozsaz
            else if (tmp.equals("ob 2"))
                pre.setString(1, "ob2"); //rozsaz
            else
                pre.setString(1, tmp); //rozsaz


            pre.setString(2, tl.get(2)); //poznamka
            pre.setInt(3, reqID);                    

            pre.executeUpdate();                
            pre.close();
            
            System.out.println(code+"'s requirement of exam for "+teacher+" was inserted successfully!");
        }
        catch (SQLException ex) {
            System.out.println("Could not insert "+code+"'s requirement for \""+teacher+"\": " + ex.getMessage());
            dbSuccess = false;
        }
    }
    
    /**
     * Insert teachar's availability for exams into DB
     * @param termsID PK of terminyPozadavkuNaZkousku
     * @param reqID FK from pozadavkyNaZkousku
     * @param cl List with terms of availability
     * @param teacher Name of teacher
     */
    public static void InsertTeacherExamAvail(int termsID, int reqID, List<String> cl, String teacher){
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(   
                "insert into terminyPozadavkuNaZkousky (termin, datum_od, datum_do, "
                    + "den, cas_od, cas_do, `hod.`, pocet_kol, pref_mistnosti, poznamka, "
                    + "id, typ, id_pozadavkuNaZkousky)"
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " 
                    + termsID + ", \"vyhovující\"," + reqID+")");                                                


            pre.setString(1, cl.get(0)); //termin
            pre.setDate(2, java.sql.Date.valueOf(cl.get(1))); //datum od
            pre.setDate(3, java.sql.Date.valueOf(cl.get(2))); //datum do
            pre.setString(4, cl.get(3)); //den
            pre.setString(5, cl.get(4)); //cas_od
            pre.setString(6, cl.get(5)); //cas_do
            pre.setInt(7, Integer.parseInt(cl.get(6))); //hod
            pre.setInt(8, Integer.parseInt(cl.get(7))); //pocet kol
            pre.setString(9, cl.get(8)); //pref_mistnost
            pre.setString(10, cl.get(9)); //poznamka           

            pre.executeUpdate();                
            pre.close();
            System.out.println(teacher+"'s availability for exam for " + cl.get(0) + " was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert teacher's availability for \""+teacher+"\": " + ex.getMessage());
            dbSuccess = false;
        }
        catch(IllegalArgumentException ex) {
            System.out.println("Bad date value. Probably importing file with lectures instead of exams: " + ex.getMessage());
            dbSuccess = false;
        }
    }
    
    /**
     * Insert teachar's unavailability for exams into DB
     * @param termsID PK of terminyPozadavkuNaZkousku
     * @param reqID FK from pozadavkyNaZkousku
     * @param cl List with terms of unavailability
     * @param teacher Name of teacher
     */
    public static void InsertTeacherExamUnavail(int termsID, int reqID, List<String> cl, String teacher){
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(   
                "insert into terminyPozadavkuNaZkousky (datum_od, datum_do, "
                    + "den, cas_od, cas_do, poznamka, "
                    + "id, typ, id_pozadavkuNaZkousky)"
                    + "values (?, ?, ?, ?, ?, ?, " 
                    + termsID + ", \"nemožný\"," + reqID + ")");                                                

            pre.setDate(1, java.sql.Date.valueOf(cl.get(0))); //datum od
            pre.setDate(2, java.sql.Date.valueOf(cl.get(1))); //datum do
            pre.setString(3, cl.get(2)); //den
            pre.setString(4, cl.get(3)); //cas_od
            pre.setString(5, cl.get(4)); //cas_do                          
            pre.setString(6, cl.get(5)); //poznamka            

            pre.executeUpdate();                
            pre.close();
            System.out.println(teacher+"'s unavailability for exam was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert teacher's availability for \""+teacher+"\": " + ex.getMessage());
            dbSuccess = false;
        }
        catch(IllegalArgumentException ex) {
            System.out.println("Bad date value. Probably importing file with lectures instead of exams: " + ex.getMessage());
            dbSuccess = false;
        }
    }
    
    /**
     * Deactived schedule version     
     * @throws SQLException 
     */
    public static void UpdateScheduleVersion() throws SQLException {
        PreparedStatement preu;
        preu = Database.conn.prepareStatement("update verzeRozvrhu "
                + "set je_aktivni_flag = 0 where je_aktivni_flag = 1");

        preu.executeUpdate();  
        preu.close();  
    }
    
    /**
     * Active schedule version
     * @param id
     * @throws SQLException 
     */
    public static void UpdateScheduleVersionA(int id) throws SQLException {
        PreparedStatement preu;
        preu = Database.conn.prepareStatement("update verzeRozvrhu "
                + "set je_aktivni_flag = 1 where id = " +id);

        preu.executeUpdate();  
        preu.close();  
    }
    
    /**
     * Insert schedule version into DB
     * @param versionID PK of verzeRozvrhu
     * @param year Year of schedule
     * @param semester Semester of schedule
     */
    public static void InsertScheduleVersion(int versionID, int year, String semester){
        PreparedStatement pre, preu;
        try {
            Database.UpdateScheduleVersion();
            
            pre = Database.conn.prepareStatement(   
                "insert into verzeRozvrhu (id, cislo_verze, je_aktivni_flag, nazev_verze, id_semestru)"
                    + "values (?, 1, 1,"
                    + "(select date_format(sysdate(), \"import_%Y-%m-%d_%T\")),"
                    + "(select max(id) from semestry where rok = \""+year+"\" and obdobi = \""+semester+"\"))"); 

            pre.setInt(1, versionID); //datum od
            

            pre.executeUpdate();                
            pre.close();
            System.out.println("Schedule's version was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert schedule's version: " + ex.getMessage());
            dbSuccess = false;
        }
        
    }
    
    /**
     * Insert course times into DB
     * @param courseTimesID PK of casyPredmetu
     * @param versionID FK from verzeRozvrhu
     * @param dayName Day of week for course
     * @param courseName Course code
     * @param courseType Course type (p, c, n...)
     * @param group Group of course
     * @param courseStart Course time of start
     * @param courseEnd Course time of end
     * @param year Year of course
     * @param semester Semester of course
     */
    public static void InsertCourseTimes(int courseTimesID, int versionID, String dayName,            
            String courseName, String courseType, String group, String courseStart, String courseEnd,
            int year, String semester){
        
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(   
                    "insert into casyPredmetu (id, id_verze, cas_od, cas_do, typ, den, skupina, verze, id_predmetu)" 
                    + "values (?, ?, ?, ?, ?, ?, ?, 1," 
                    + "(select max(p.id) from predmety p join povinnostVOboru v on (p.id = v.id_predmetu) "
                        + "join obory o on (v.id_oboru = o.id) "
                        + "join semestry s on (o.id_semestru = s.id) "
                        + "where p.zkratka = \""+courseName+"\" and s.rok = "+ year +" and s.obdobi = \""+semester+"\"))");
                        
                            

            pre.setInt(1, courseTimesID); //primary key
            pre.setInt(2, versionID); //fk
            pre.setString(3, courseStart);
            pre.setString(4, courseEnd);
            pre.setString(5, courseType);
            pre.setString(6, dayName);
            
            if (group.isEmpty())
                pre.setString(7, null);
            else
                pre.setString(7, group);
                        
            pre.executeUpdate();                
            pre.close();
            System.out.println(courseName + "'s time was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert \""+courseName+"\"'s time: " + ex.getMessage());
            dbSuccess = false;
        }
        
    }
    
    /**
     * Insert room of lecture
     * @param courseRoomID Primary key
     * @param courseTimesID FK from casyZkousek
     * @param roomName Name of room
     */
    public static void InsertCourseRoom(int courseRoomID, int courseTimesID, String roomName){
        
        PreparedStatement pre, pres;
        try {
            int roomID = 0;
            pres = Database.conn.prepareStatement("select max(id) from mistnosti where nazev = \""+roomName+"\"");
            ResultSet rs = pres.executeQuery();
            while(rs.next())
                roomID=rs.getInt(1);
            
            //there is no such room
            if (roomID == 0)
                InsertNewRoom(roomName);
            
            pre = Database.conn.prepareStatement(   
                    "insert into predmetuPrislusi (id, id_casu_predmetu, id_mistnosti)" +
                        "values (?, ?, "+
                        "(select max(id) from mistnosti where nazev = \""+roomName+"\"))");
                            
            pre.setInt(1, courseRoomID); //pk
            pre.setInt(2, courseTimesID); //fk

            pre.executeUpdate();                
            pre.close();
            System.out.println("\"predmetuPrislusi\" for \""+ roomName +"\" was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert \"predmetuPrislusi\" for \""+roomName+"\": " + ex.getMessage());
            dbSuccess = false;
        }
    }
    
    /**
     * Insert new Room
     * @param roomName
     */
    public static void InsertNewRoom(String roomName) {
        int roomID = 1;
        PreparedStatement pre, pres;       
        try {
            
            pres = Database.conn.prepareStatement("select max(id) from mistnosti");
            ResultSet rs = pres.executeQuery();
            while(rs.next())
                roomID=rs.getInt(1);
            roomID++;   
            
            pre = Database.conn.prepareStatement(   
                    "INSERT INTO mistnosti (id, nazev) VALUES (?, ?)");
                            
            pre.setInt(1, roomID); //pk
            pre.setString(2, roomName); //room name

            pre.executeUpdate();                
            pre.close();
            System.out.println("\""+ roomName +"\" was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert \""+roomName+"\": " + ex.getMessage());            
            dbSuccess = false;
        }
    }
    
    /**
     * Insert exam version into DB
     * @param versionID PK of verzeRozvrhu
     * @param year Year of schedule
     * @param semester Semester of schedule
     */
    public static void InsertExamVersion(int versionID, int year, String semester){
        PreparedStatement pre, preu;
        try {
            preu = Database.conn.prepareStatement("update verzeZkousek "
                + "set je_aktivni_flag = 0 where je_aktivni_flag = 1");
            
            preu.executeUpdate();  
            preu.close();            
            
            pre = Database.conn.prepareStatement(   
                "insert into verzeZkousek (id, cislo_verze, je_aktivni_flag, nazev_verze, id_semestru)"
                    + "values (?, 1, 1,"
                    + "(select date_format(sysdate(), \"import_%Y-%m-%d_%T\")),"
                    + "(select max(id) from semestry where rok = \""+year+"\" and obdobi = \""+semester+"\"))"); 

            pre.setInt(1, versionID); //datum od
            

            pre.executeUpdate();                
            pre.close();
            System.out.println("Exam's version was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert exam's version: " + ex.getMessage());
            dbSuccess = false;
        }
        
    }
    
    /**
     * Insert exam times into DB
     * @param examTimesID PK of casyPredmetu
     * @param versionID FK from verzeRozvrhu
     * @param date Date of exam
     * @param examName Course code
     * @param examType Course type (p, c, n...)
     * @param examStart Course time of start
     * @param examEnd Course time of end
     * @param year Year of course for exam
     * @param semester Semester of course for exam
     */
    public static void InsertExamTimes(int examTimesID, int versionID, String date,            
            String examName, String examType, String examStart, String examEnd,
            int year, String semester){
        
        PreparedStatement pre;
        try {
            pre = Database.conn.prepareStatement(   
                    "insert into casyZkousek (id, id_verze, cas_od, cas_do, termin, datum, verze, id_predmetu)" 
                    + "values (?, ?, ?, ?, ?, STR_TO_DATE(?, \"%d. %m.%Y\"), 1," 
                    + "(select max(p.id) from predmety p join povinnostVOboru v on (p.id = v.id_predmetu) "
                        + "join obory o on (v.id_oboru = o.id) "
                        + "join semestry s on (o.id_semestru = s.id) "
                        + "where p.zkratka = \""+examName+"\" and s.rok = "+ year +" and s.obdobi = \""+semester+"\"))");
                            

            pre.setInt(1, examTimesID); //primary key
            pre.setInt(2, versionID); //fk
            pre.setString(3, examStart);
            pre.setString(4, examEnd);
            pre.setString(5, examType);
            pre.setString(6, date);
            

            pre.executeUpdate();                
            pre.close();
            System.out.println(examName + "'s time was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert \""+examName+"\"'s time: " + ex.getMessage());
            dbSuccess = false;
        }        
    }
    
    /**
     * Insert exam times into DB
     * @param examTimesID PK of casyPredmetu
     * @param date Date of exam
     * @param roomName Name of room
     * @param reason Reason of limitation
     * @param examStart Course time of start
     * @param examEnd Course time of end
     */
    public static void InsertLimitation(int examTimesID, String date, String roomName,
            String reason, String examStart, String examEnd){
        
        //remove '!'
        reason = reason.substring(1);
        
        PreparedStatement pre, pres;
        try {
            
                       
            int roomID = 0;
            pres = Database.conn.prepareStatement("select max(id) from mistnosti where nazev = \""+roomName+"\"");
            ResultSet rs = pres.executeQuery();
            while(rs.next())
                roomID=rs.getInt(1);

            //there is no such room
            if (roomID == 0)
                InsertNewRoom(roomName);



            pre = Database.conn.prepareStatement(   
                    "insert into nedostupnost (id, cas_od, cas_do, datum, duvod, id_mistnosti)" +
                        "values (?, ?, ?, STR_TO_DATE(?, \"%d. %m.%Y\"), ?," +
                        "(select max(id) from mistnosti where nazev = \""+roomName+"\"))");


            pre.setInt(1, examTimesID); //primary key            
            pre.setString(2, examStart);
            pre.setString(3, examEnd);
            pre.setString(4, date);            
            pre.setString(5, reason);
            

            pre.executeUpdate();                
            pre.close();
            System.out.println(reason + " in "+date+" for "+roomName+" was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert \""+reason+"\": " + ex.getMessage());
            dbSuccess = false;
        }        
    }
    
    /**
     * Insert room of exam
     * @param examRoomID Primary key
     * @param examTimesID FK from casyZkousek
     * @param roomName Name of room
     */
    public static void InsertExamRoom(int examRoomID, int examTimesID, String roomName){
        PreparedStatement pre, pres;
        try {
            int roomID = 0;
            pres = Database.conn.prepareStatement("select max(id) from mistnosti where nazev = \""+roomName+"\"");
            ResultSet rs = pres.executeQuery();
            while(rs.next())
                roomID=rs.getInt(1);
            
            //there is no such room
            if (roomID == 0)
                InsertNewRoom(roomName);
            
            
            pre = Database.conn.prepareStatement(   
                    "insert into zkousce_prislusi (id, casyZkousek_id, mistnosti_id)" +
                        "values (?, ?, "+
                        "(select max(id) from mistnosti where nazev = \""+roomName+"\"))");
                            
            pre.setInt(1, examRoomID); //pk
            pre.setInt(2, examTimesID); //fk
            
            pre.executeUpdate();                
            pre.close();
            System.out.println("\"zkousce_prislusi\" for \""+ roomName +"\" was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert \"zkousce_prislusi\" for \""+roomName+"\": " + ex.getMessage());
            dbSuccess = false;
        }
    }
     
    /**
     * Insert collision into DB
     * @param colID Primary key
     * @param code1 Name of course
     * @param code2 Name of course in collision
     * @param year Year of semester
     * @param semester Semester of course
     * @param count Count of students in collision
     * @return Primary key
     */     
    public static int InsertCollision(int colID, String code1, String code2,
            int year, String semester, int count){
        PreparedStatement pre, pres;
        try {
            int id1 = SelectCourseForCollision(code1, year, semester);
            int id2 = SelectCourseForCollision(code2, year, semester);
            
            if (id1 == 0 || id2 == 0)
                return colID;
            
            
            //check if collision was already inserted
            int alreadyID = 0;
            pres = Database.conn.prepareStatement(
                    "select max(id) "
                    + "from kolize "
                    + "where id_predmetu = " +id1
                    + " and id_predmetu_v_kolizi = " +id2);
                    
            ResultSet rs = pres.executeQuery();
            if (rs == null) {
                System.out.println("Could not select ID.");
                dbSuccess = false;
                return colID;
            }   
            while(rs.next())
                alreadyID=rs.getInt(1);
            
            //it is already in db
            if (alreadyID > 0) {
                System.out.println("Count of students ("+count+") for "+code1+" and "+code2+" was inserted already!");
                return colID;
            }
            pres.close();
            
            pre = Database.conn.prepareStatement(   
                    "insert into kolize (id, pocet_studentu_v_kolizi, id_predmetu, id_predmetu_v_kolizi) "
                    + "values (?,?,?,?)");
                            
            pre.setInt(1, colID); //pk
            pre.setInt(2, count); //count of students
            pre.setInt(3, id1); //count of students
            pre.setInt(4, id2); //count of students
            
            pre.executeUpdate();                
            pre.close();
            
            colID++;
            
            System.out.println("Count of students ("+count+") for "+code1+" and "+code2+" was inserted successfully!");
        }
        catch(SQLException ex){
            System.out.println("Could not insert count "+count+" for "+code1+" and "+code2+": " + ex.getMessage());
            dbSuccess = false;
        }
        
        return colID;
    }
     
    /**
     * Select course for collision
     * @param code Code of course
     * @param year Year of course
     * @param semester Semester of course
     * @return 
     */
    private static int SelectCourseForCollision(String code, int year, String semester){
        PreparedStatement pre;
        int id1 = 0;
        
        try {            
            pre = Database.conn.prepareStatement(
                    "(select max(p.id) "
                    + "from predmety p "
                        + "join povinnostVOboru v on (p.id = v.id_predmetu) "
                        + "join obory o on (v.id_oboru = o.id) "
                        + "join semestry s on (o.id_semestru = s.id) "
                    + "where p.zkratka = \""+code+"\" "
                        + "and s.rok = "+year+" "
                        + "and s.obdobi = \""+semester+"\")");
                    
                    
            ResultSet rs = pre.executeQuery();
            if (rs == null) {
                System.out.println("Could not select ID of "+code+".");
                dbSuccess = false;
                return 0;
            }                
            while(rs.next())
                id1=rs.getInt(1);
                            
            pre.close();                        
            
        }
        catch(SQLException ex){
            System.out.println("Could not select ID of "+code+".");
            dbSuccess = false;
        }
        
        return id1;
    }
    
    /********************
     * Export
     *******************/
    
    /**
     * Get list of days of week with collisions
     * @return List with days of week with collisions
     */
    public static List<List<String>> getCollisionsScheduleWeek() {
        
        dbSuccess = true;
        
        List<List<String>> weekList = new ArrayList<>();
        
        List<String> days = new ArrayList<>();
        days.add("Po");
        days.add("Út");
        days.add("St");
        days.add("Čt");
        days.add("Pá");
        
        String day = "";
        try {
            for (String d: days) {
                day = d;
                List<String> dayList = Database.getCollisionsScheduleDay(d);
                weekList.add(dayList);
            }
        }
        catch (SQLException e) {
            System.out.println("Could not select collisions for day " + day+".");
            dbSuccess = false;
        }
        
        return weekList;
    }
    
    /**
     * Get all couples (but not duplicated) of collisions for day
     * @param day Day of schedule
     * @return List with collisions
     * @throws SQLException 
     */
    public static List<String> getCollisionsScheduleDay(String day) throws SQLException {
        ResultSet rs = null;
                     
        
        List<String> data = new ArrayList<>();
                
        
        String getCollisons = 
                "select k.pocet_studentu_v_kolizi, "
                    + "p.zkratka, "
                    + "pp.zkratka "
                + "from kolize k "
                    + "join predmety p on (k.id_predmetu = p.id) "
                    + "join predmety pp on (pp.id = k.id_predmetu_v_kolizi) "
                + "where p.id in "
                    + "(select distinct p.id "
                    + "from predmety p "
                        + "join casyPredmetu c on (p.id = c.id_predmetu) "
                        + "join verzeRozvrhu vr on (c.id_verze = vr.id)    "
                    + "where vr.je_aktivni_flag = 1 "
                        + "and c.den = \""+day+"\" ) "
                + "and pp.id in "
                    + "(select distinct p.id "
                    + "from predmety p "
                        + "join casyPredmetu c on (p.id = c.id_predmetu) "
                        + "join verzeRozvrhu vr on (c.id_verze = vr.id)    "
                    + "where vr.je_aktivni_flag = 1 "
                        + "and c.den = \""+day+"\" ) "
                + "and pocet_studentu_v_kolizi > 0 "
                + "order by p.zkratka, pp.zkratka";

        rs = Database.executeQuery(getCollisons);
        if (rs == null) {            
            throw new SQLException("Could not select collision for day " + day);
        }
        else {
            while(rs.next()) {
                String c1 = rs.getString(2);
                String c2 = rs.getString(3);
                String line = c1 +" "+ c2 +" " + rs.getInt(1);
                String lineReversed = c2 +" "+ c1 +" " + rs.getInt(1);
                
                if (!data.contains(lineReversed))
                    data.add(line);
            }   
        }
        
        return data;
    }
    
    /**
     * Get list of days of week with collisions
     * @return List with days of week with collisions
     */
    public static List<List<String>> getCollisionsExamsAll() {
        
        dbSuccess = true;
        
        List<List<String>> allList = new ArrayList<>();
        
        
        String day = "";
        try {
            List<String> days = getExamsDays();
            if (days.isEmpty())
                dbSuccess = false;
            
            for (String d: days) {
                day = d;
                List<String> dayList = Database.getCollisionsExamsDay(d);
                allList.add(dayList);
            }
        }
        catch (SQLException e) {
            System.out.println("Could not select collisions for day " + day+".");
            dbSuccess = false;
        }
        
        
        return allList;
    }
    
    /**
     * Get list of all days of exam
     * @return
     * @throws SQLException 
     */
    public static List<String> getExamsDays() throws SQLException{
    
        ResultSet rs = null;
                             
        List<String> data = new ArrayList<>();
        
        String getCollisons = 
                "select distinct c.datum "
                + "from casyZkousek c "
                    + "join verzeZkousek vr on (c.id_verze = vr.id) "
                + "where vr.je_aktivni_flag = 1 "
                + "order by c.datum ";

        rs = Database.executeQuery(getCollisons);
        if (rs == null) {            
            throw new SQLException("Could not select collision for days of exams");
        }
        else {
            while(rs.next()) {
                data.add(rs.getString(1));
            }   
        }
        
        return data;
    }
    
    
    /**
     * Get all couples (but not duplicated) of collisions for day
     * @param day Day of schedule
     * @return List with collisions
     * @throws SQLException 
     */
    public static List<String> getCollisionsExamsDay(String day) throws SQLException {
        ResultSet rs = null;
                     
        
        List<String> data = new ArrayList<>();
                
        
        String getCollisons = 
                "select k.pocet_studentu_v_kolizi, "
                    + "p.zkratka, "
                    + "pp.zkratka "
                + "from kolize k "
                    + "join predmety p on (k.id_predmetu = p.id) "
                    + "join predmety pp on (pp.id = k.id_predmetu_v_kolizi) "
                + "where p.id in "
                    + "(select distinct p.id "
                    + "from predmety p "
                        + "join casyZkousek c on (p.id = c.id_predmetu) "
                        + "join verzeZkousek vr on (c.id_verze = vr.id)    "
                    + "where vr.je_aktivni_flag = 1 "
                        + "and c.datum = \""+day+"\" ) "
                + "and pp.id in "
                    + "(select distinct p.id "
                    + "from predmety p "
                        + "join casyZkousek c on (p.id = c.id_predmetu) "
                        + "join verzeZkousek vr on (c.id_verze = vr.id)    "
                    + "where vr.je_aktivni_flag = 1 "
                        + "and c.datum = \""+day+"\" ) "
                + "and pocet_studentu_v_kolizi > 0 "
                + "order by p.zkratka, pp.zkratka";

        rs = Database.executeQuery(getCollisons);
        if (rs == null) {            
            throw new SQLException("Could not select collision for day " + day);
        }
        else {
            while(rs.next()) {
                String c1 = rs.getString(2);
                String c2 = rs.getString(3);
                String line = c1 +" "+ c2 +" " + rs.getInt(1);
                String lineReversed = c2 +" "+ c1 +" " + rs.getInt(1);
                
                if (!data.contains(lineReversed))
                    data.add(line);
            }   
        }
        
        
        return data;
    }
}
