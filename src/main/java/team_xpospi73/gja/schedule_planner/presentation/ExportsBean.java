/*
 * authors:  Michael Švasta
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import team_xpospi73.gja.schedule_planner.model.Database;

/**
 * Presenter for controlling all exports
 */
@ManagedBean
public class ExportsBean {
    
    /**
     * Value of study program
     */
    String studyProgram;    
    
    /**
     * Requirement value 
     */
    String requirementCategory;
    
    /**
     * Get value of requirement category
     * @return String value (lect/exam)
     */
    public String getRequirementCategory() {
        return this.requirementCategory;
    }
    
    /**
     * Set requirement category
     * @param requirementCategory String value (lect/exam)
     */
    public void setRequirementCategory(String requirementCategory) {
        this.requirementCategory = requirementCategory;
    }
        
    /**
     * Get study program value
     * @return String value of study program
     */
    public String getStudyProgram() {
        return this.studyProgram;
    }
    
    /**
     * Set study program value
     * @param studyProgram String value for study program
     */
    public void setStudyProgram(String studyProgram) {
        this.studyProgram = studyProgram;
    }        
    
    /**
     * Constructor 
     */
    @PostConstruct
    public void init() {
        Database.connectDB();

    }
    
    
    /**
     * Public method for export schedule in csv
     */
    public void exportCSV() {
        try {
            
            if (requirementCategory.equals("exam"))
                exportCSVExamSchedule();
            else
                exportCSVLectureSchedule();
            
        }
        catch (SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        }     
    }
     
    /**
     * Export terms in csv file
     */
    public void exportCSVTerms() {
        try {
            
            if (requirementCategory.equals("exam"))
                exportCSVExamTerms();
            else
                exportCSVLectureTerms();
            
        }
        catch (SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        }  
    }
    
    /**
     * Export HTML file
     */
    public void exportHTML() {
        try {
            
            exportHTMLwithTerms();
            
        }
        catch (SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        } 
    }
    
    /**
     * Export terms in html format for lectures
     * @throws java.sql.SQLException 
     */
    public void exportHTMLwithTerms() throws SQLException {    
        List<String> codes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<Integer> idsPK = new ArrayList<>();
        List<Integer> years = new ArrayList<>();
        
        List<String> trs = new ArrayList<>();
        ResultSet rs = null;
        
        Database.dbSuccess  = true;
        
        /* Select study program */
        String getCoursesTerms;   
        if (requirementCategory.equals("lect")) {
            getCoursesTerms = "select DISTINCT p.zkratka, p.nazev, p.predmet_id, p.id, s.rok "
                + "from predmety p "
                    + "join casyPredmetu c on (p.id = c.id_predmetu) "
                    + "join verzeRozvrhu vr on (c.id_verze = vr.id) "
                    + "join semestry s on (vr.id_semestru = s.id) "
                    + "join obory o on (s.id = o.id_semestru) "
                    + "join povinnostVOboru v on (v.id_oboru = o.id and v.id_predmetu = p.id) "
                + "where v.typ_povinnosti like \"P%\" "
                    + "and o.zkratka like \""+studyProgram+"\" "
                    + "and vr.je_aktivni_flag = 1 "
                + "order by p.zkratka";        
        }
        else {
            getCoursesTerms = "select DISTINCT p.zkratka, p.nazev, p.predmet_id, p.id, s.rok "
                + "from predmety p "
                    + "join casyZkousek c on (p.id = c.id_predmetu) "
                    + "join verzeZkousek vr on (c.id_verze = vr.id) "
                    + "join semestry s on (vr.id_semestru = s.id) "
                    + "join obory o on (s.id = o.id_semestru) "
                    + "join povinnostVOboru v on (v.id_oboru = o.id and v.id_predmetu = p.id) "
                + "where v.typ_povinnosti like \"P%\" "
                    + "and o.zkratka like \""+studyProgram+"\" "
                    + "and vr.je_aktivni_flag = 1 "
                + "order by p.zkratka";        
        }
        
        
        rs = Database.executeQuery(getCoursesTerms);
        if (rs == null) {
            
            throw new SQLException("Could not select courses from active schedule");
        }
        else {
            while(rs.next()) {
                codes.add(rs.getString(1));            
                names.add(rs.getString(2)); 
                ids.add(rs.getString(3));
                idsPK.add(rs.getInt(4));
                years.add(rs.getInt(5));
            }   
        }
           
        if (codes.isEmpty())
            throw new SQLException("There are no data for export!");
        
        for (int i = 0; i < codes.size(); i++) {
            trs.add(createHTML(codes.get(i), names.get(i), ids.get(i), idsPK.get(i), years.get(i)));
        }
        
        try {
            createFile(trs, studyProgram+"_"+years.get(0)+"_"+requirementCategory+".html", "text/html");                  
        }
        catch(IOException e){
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Exception in export HTML: " + e.getMessage());            
        }

        
                
    }
    
    /**
     * Create tr with course
     * @param code Course's code
     * @param name Course's name
     * @param id Course's id
     * @param idPK Course's primary key
     * @param year Year of course
     * @return String value with "tr"
     */
    private String createHTML(String code, String name, String id, int idPK, int year) throws SQLException {
        List<String>tds = new ArrayList<>();
        List<String>hours = new ArrayList<>();
        
        ResultSet rs = null;
        String getCoursesTerms = "";
        
        if (requirementCategory.equals("lect")) {
            getCoursesTerms = "select DISTINCT c.skupina, concat(s.rok,\"-00-00\"), time_format(cas_od,'%H:%i'), time_format(cas_do,'%H:%i')"
                + "from predmety p join casyPredmetu c on (p.id = c.id_predmetu) "
                + "join verzeRozvrhu v on (c.id_verze = v.id) "
                + "join semestry s on (v.id_semestru = s.id) "
                //+ "where s.rok = "+year+" and s.obdobi = \""+csvSemester+"\" and p.id = "+idPK;            
                + "where v.je_aktivni_flag=1 and p.id = "+idPK;
        
        }
        else {
            getCoursesTerms = "select distinct c.datum, time_format(cas_od,'%H:%i'), time_format(cas_do,'%H:%i')"
                + "from predmety p join casyZkousek c on (p.id = c.id_predmetu) "
                + "join verzeZkousek v on (c.id_verze = v.id) "
                + "join semestry s on (v.id_semestru = s.id) "
                //+ "where s.rok = "+year+" and s.obdobi = \""+csvSemester+"\" and p.id = "+idPK;        
                + "where v.je_aktivni_flag=1 and p.id = "+idPK;
        }
        
        rs = Database.executeQuery(getCoursesTerms);
        if (rs == null) {
            
            throw new SQLException("Could not select terms");
        }
        else {            
            while(rs.next()) {                                
                
                if (requirementCategory.equals("lect")) 
                    hours.add(getHours(rs.getString(2), rs.getString(3), rs.getString(4)));
                else
                    hours.add(getHours(rs.getString(1), rs.getString(2), rs.getString(3)));                
            }   
        }
        
        /* Add course id and name */
        tds.add(getCourseName(id, code, name));
        /* Add "stanoví učitel" */
        tds.add(getTeacher());  
        /* List with course's hours */
        
        //hours.add(getHours(csvYear,start,end));
        
        tds.addAll(hours);
        int hoursCount = hours.size();
        /* 4 different hours for each course */
        if (hoursCount < 3) {
            /* Add empty hours */
            for (int i = hoursCount; i < 3; i++)
                tds.add(getEmptyHours(year));
        }
        
        
        String tr = "<tr>";
        for (String td : tds) {
            tr += "<td>"+td+"</td>\n";
        }
        tr += "</tr>\n";
                        
        return tr;
    }
    
    /**
     * Get course name in h2
     * @param id Course's id
     * @param code Course's code
     * @param name Course's name
     * @return h2+p
     */
    private String getCourseName(String id, String code, String name) {                
        String h2 = "<h2><a href=\"http://www.fit.vutbr.cz/study/course-l.php?id="+id+"\">"+code+"</a></h2>\n";
        String p = "<p class=\"h\"><span class=\"mensiP\">"+name+"</span></p>";
        return h2+p;
    }
    
    /**
     * Get teacher in p
     * @return p1+p2
     */
    private String getTeacher() {
        String teacher = "učitel"; //I have no idea what it means and where that value is
        String p1 = "<p class=\"h\"><i><span class=\"normalniP\">stanoví</span></i></p>\n";
        String p2 = "<p><i><span class=\"normalniP\">"+teacher+"</span></i></p>\n";
        return p1+p2;
    }
    
    /**
     * Get hours
     * @param year
     * @param start
     * @param end
     * @return 
     */
    private String getHours(String year, String start, String end) {                       
        String p1 = "<p class=\"h\"><span class=\"mensiP\">"+year.substring(0,4)+"-<b>"+year.substring(5)+"<span class=\"hyperlink\"></span></b></span></p>\n";
        String p21 = "<p class=\"h\"><i><span class=\"mensiP\">"+start.split(":")[0]+"-<sup>"+start.split(":")[1];
        String p22 = "</sup> – "+end.split(":")[0]+"-<sup>"+end.split(":")[1]+"</sup></span></i></p>\n";
        
        return p1+p21+p22;
    }
    
    /**
     * Get empty hours
     * @param year
     * @return 
     */
    private String getEmptyHours(int year) {                       
        String p1 = "<p class=\"h\"><span class=\"mensiP\">"+year+"-<b>00-00<span class=\"hyperlink\"></span></b></span></p>\n";
        String p2 = "<p class=\"h\"><i><span class=\"mensiP\">-<sup></sup> – -<sup></sup></span></i></p>\n";
        
        return p1+p2;
    }
    
    
    /**
     * Return string from int day of week
     * @param c
     * @return 
     */
    private String getDay(char c){
        switch (c){
            case '0':
                return "Ne";
            case '1':
                return "Po";
            case '2':
                return "Út";
            case '3':
                return "St";        
            case '4':
                return "Čt";
            case '5':
                return "Pá";
            case '6':
                return "So";
        }
        return "Un";
    }
    
    /**
     * Create file from string array
     * @param data String list with lines
     * @param filename String with name of file
     * @throws IOException 
     */
    private void createFile(List<String> data, String filename, String contentType) throws IOException {
        try {            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            response.reset();
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            OutputStream out = response.getOutputStream();
            
            /* Writes line by line */
            for (String s: data)
                out.write(s.getBytes());
            out.flush();
            out.close();

            facesContext.responseComplete();

        } 
        finally {
            FacesMessage message = new FacesMessage("Succesful", filename + " was exported");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Export is completed");
        }
    }
    
    /**
     * Export csv with terms of exams
     * @throws SQLException 
     */
    private void exportCSVExamTerms() throws SQLException {
        ResultSet rs = null;
        List<String> terms = new ArrayList<>();
        
        Database.dbSuccess  = true;
        
        /* Select study program */
        //getCoursesTerms(studyProgram, csvSemester);     
        String getCoursesTerms = "select GROUP_CONCAT(DISTINCT date_format(c.datum, \"%d. %m.\"),\" \", " //day. month.
                + "date_format(c.datum, \"/%w\"), \" \", "               //day of week
                + "time_format(c.cas_od, \"%H:%i\"), \"-\", "           //start
                + "time_format(c.cas_do, \"%H:%i\") "                   //end
                + "order by c.datum separator \", \") as datum, "       //order and separator
                + "p.zkratka "                                          //course name
            + "from predmety p left join casyZkousek c on (p.id = c.id_predmetu) "
            + "join verzeZkousek v on (c.id_verze = v.id) "
            + "join semestry s on (v.id_semestru = s.id) "
            //+ "where s.rok = "+csvYear+" and s.obdobi = \""+csvSemester+"\" group by p.zkratka order by datum, p.zkratka";
            + "where v.je_aktivni_flag=1 group by p.zkratka order by datum, p.zkratka";
        

        
        rs = Database.executeQuery(getCoursesTerms);
        if (rs == null) {
            
            throw new SQLException("Could not select terms");
        }
        else {
            boolean empty = true;
            
            while(rs.next()) {
                empty = false;
                String tmp = rs.getString(1);
                /*System.out.println(getDay(tmp.charAt(8)) + tmp.substring(0, 7)+ tmp.substring(9,23)
                        +getDay(tmp.charAt(31)) + tmp.substring(22,30) + tmp.substring(32,46)
                        +getDay(tmp.charAt(54)) + tmp.substring(45,53) + tmp.substring(55));*/
                
                // "/" is identifier of day of week
                int indexOf = tmp.indexOf("/");
                String output = rs.getString(2)+", ";
                while(indexOf > -1){

                    String time = tmp.substring(indexOf+3);                    
                    output += getDay(tmp.charAt(indexOf+1)) + " " +tmp.substring(0, indexOf) + time.substring(0, 11)+", ";
                    
                    if (time.length() < 13)
                        break;
                    tmp = time.substring(13);
                    indexOf = tmp.indexOf("/");
                }      
                
                terms.add(output+"\n");  
            }   
            
            if (empty)
                throw new SQLException("There are no data for export!");
        }
        
        try {
            createFile(terms, requirementCategory+"_terms.csv", "text/comma-separated-values");                  
        }
        catch(IOException e){
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Exception in export HTML: " + e.getMessage());            
        }

        
            
    }
    
    /**
     * Export terms of lectures in csv
     * @throws SQLException 
     */
    private void exportCSVLectureTerms() throws SQLException {
        ResultSet rs = null;
        List<String> terms = new ArrayList<>();
        
        Database.dbSuccess  = true;
        
        /* Select study program */
        //getCoursesTerms(studyProgram, csvSemester);     
        String getCoursesTerms = "select GROUP_CONCAT(DISTINCT ifnull(if(c.skupina = 'b', 'a', c.skupina), 'a'), " //skupina (from 'b' or null makes 'a')
                + "c.den,\" \", "                                                               //den
                + "time_format(c.cas_od, \"%H:%i\"),\"-\", "                                    //cas_od
                + "time_format(c.cas_do, \"%H:%i\")) as datum, "                                //cas_do
                + "p.zkratka "                                                                  //zkratka
                + "from predmety p join casyPredmetu c on (p.id = c.id_predmetu) "
                + "join verzeRozvrhu v on (c.id_verze = v.id) "
                + "join semestry s on (v.id_semestru = s.id) "                
                //+ "where s.rok = "+csvYear+" and s.obdobi = \""+csvSemester+"\" group by p.zkratka order by datum, p.zkratka";                
                + "where v.je_aktivni_flag=1 group by p.zkratka order by datum, p.zkratka";

        
        rs = Database.executeQuery(getCoursesTerms);
        if (rs == null) {
            
            throw new SQLException("Could not select terms");
        }
        else {
            boolean empty = true;
            while(rs.next()) {
                empty = false;
                String tmp = rs.getString(1);
                /*System.out.println(getDay(tmp.charAt(8)) + tmp.substring(0, 7)+ tmp.substring(9,23)
                        +getDay(tmp.charAt(31)) + tmp.substring(22,30) + tmp.substring(32,46)
                        +getDay(tmp.charAt(54)) + tmp.substring(45,53) + tmp.substring(55));*/
                
                int indexOf = tmp.indexOf(",");                
                String output = rs.getString(2)+", ";
                int count = 0;                                           
                               
                while(indexOf > -1){                    
                    output += tmp.substring(1, indexOf) + ", ";                    
                    
                    tmp = tmp.substring(indexOf+1);
                    indexOf = tmp.indexOf(",");
                    count++;
                }      
                
                //just one value or last value
                if (indexOf < 0) {
                    output += tmp.substring(1) + ", ";                                      
                    count++;
                }     
                
                
                for (int i = count; i < 3; i++)
                    output += "-, ";
                
                terms.add(output+"\n");  
            }   
            
            if (empty)
                throw new SQLException("There are no data for export!");
        }
        
        try {
            createFile(terms, requirementCategory+"_terms.csv", "text/comma-separated-values");                  
        }
        catch(IOException e){
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Exception in export HTML: " + e.getMessage());            
        }

        
            
    }
    
    /**
     * Export csv file with schedule
     * @throws java.sql.SQLException
     */
    public void exportCSVLectureSchedule() throws SQLException {                     
        ResultSet rs = null;
        List<String> data;
        List<String> data2;
        List<String> courses = new ArrayList<>();
        List<String> starts = new ArrayList<>();
        List<String> ends = new ArrayList<>();
        List<String> rooms = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        List<String> days = new ArrayList<>();
        List<String> types = new ArrayList<>();
        
        
        
        Database.dbSuccess  = true;
        
        /* Select study program */
        //getCoursesTerms(studyProgram, csvSemester);     
        String getCoursesTerms = 
                "select  p.zkratka, "
                    + "c.den, "
                    + "m.nazev, "
                    + "ifnull(c.skupina, 'x'), "
                    + "time_format(c.cas_od, \"%H:%i\") as cas_od, "
                    + "time_format(c.cas_do, \"%H:%i\") as cas_do, "
                    + "c.typ "
                + "from predmety p join casyPredmetu c on (p.id = c.id_predmetu) "
                    + "join verzeRozvrhu vr on (c.id_verze = vr.id) "
                    + "join predmetuPrislusi pp on (pp.id_casu_predmetu = c.id) "
                    + "join mistnosti m on (pp.id_mistnosti = m.id) "
                + "where vr.je_aktivni_flag = 1 "
                + "order by c.den, m.nazev, cas_od";

        rs = Database.executeQuery(getCoursesTerms);
        if (rs == null) {
            
            throw new SQLException("Could not select schedule");
        }
        else {
            while(rs.next()) {
                courses.add(rs.getString(1));
                days.add(rs.getString(2));
                rooms.add(rs.getString(3));
                groups.add(rs.getString(4));
                starts.add(rs.getString(5));
                ends.add(rs.getString(6));
                types.add(rs.getString(7));
            }   
        }
        if (courses.isEmpty())
            throw new SQLException("There are no data for export!");
        
        
        data = createCSVData(courses, days, starts, ends, groups, types, rooms);
        
        data2 = createCSVCompulsory();
        
        List<String> tmpData2 = data2.subList(0, data2.size());
        
        int size = data.size();
        int size2 = tmpData2.size();
        int count = 0;
        for (int i = 0; i < size; i++) {
            
            //index in chart list 
            if (count < size2) {
                String d = data.get(i); //schedule line
                String d2 = tmpData2.get(count); //chart line
                
                //if schedule line isn't header
                if (!d.equals("HLAVICKA,\n")) {
                    //and chart line isn't header
                    if (!d2.equals("HLAVICKA,\n")) {
                        //merge lines
                        d += d2;                 
                        //increment index
                        count++;
                    }        
                    //if chart line is header
                    else {
                        //add empty columns until schedule line is header
                        for (int j = 17; j < 38; j++)
                            d += ",";
                        d+="\n";
                    } 
                }
                //if schedule line is header
                else {
                    //find index of header in chart
                    count = tmpData2.indexOf("HLAVICKA,\n");                    
                    if (count < 0) break;
                    
                    //sublist withou first header
                    tmpData2 = tmpData2.subList(count+1, size2); 
                    
                    count = 0;
                    size2 = tmpData2.size();
                }
                data.set(i, d);
            }
            //no more chart lines, add empty columns
            else if (i < (size-1)){
                String d = data.get(i);
                for (int j = 17; j < 38; j++)
                    d += ",";
                d+="\n";
                data.set(i, d);
            }
            
        }
        
        try {
            createFile(data, requirementCategory+"_terms.csv", "text/comma-separated-values");                  
        }
        catch(IOException e){
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Exception in export CSV: " + e.getMessage());            
        }

        
            
    }
    
    /**
     * Create csv data from db data
     * @param courses List of courses
     * @param days List of days
     * @param starts List of start time of courses
     * @param ends List of end times of courses
     * @param groups List of group of courses
     * @param types List of types of courses
     * @param rooms List of rooms of courses
     * @return List with csv data
     */
    private List<String> createCSVData(List<String> courses, List<String> days, List<String> starts,
            List<String> ends, List<String> groups, List<String> types, List<String> rooms) throws SQLException {
        List<String> data = new ArrayList<>();
        
        //collisions
        List<List<String>> weekCollisions = Database.getCollisionsScheduleWeek();
        if (!Database.dbSuccess)
            throw new SQLException("Could not select collisions");
        
        String output = "HLAVICKA,\n";
        int size = days.size();
        String lastDay = days.get(0);
        String lastRoom = "nothing";
        int lastComma = 38;
        int dayOfWeekIndex = 0;
        for (int i = 0; i < size; i++) {
            String day = days.get(i);
            String room = rooms.get(i);
            
            
            List<String> dayList = weekCollisions.get(dayOfWeekIndex);        
            
            //rooms aren't same
            if (!room.equals(lastRoom)) { 
                //add empty column
                for (int j = lastComma; j < 17; j++)
                    output += ",";
                
                if (!dayList.isEmpty() && lastComma != 38) {
                    //remove last comma
                    output = output.substring(0,output.length()-1);
                    output += dayList.get(0) + ";";
                    dayList.remove(0);
                    if (!dayList.isEmpty()) {
                        output += dayList.get(0);
                        dayList.remove(0);
                    } 
                    weekCollisions.set(dayOfWeekIndex, dayList);
                    output += ",";
                }
                
                data.add(output);
                //new day
                if (!day.equals(lastDay)) {
                    
                    //add fake room to show all collisions
                    //if it's not desirable, delete this condition
                    //data.add("HLAVICKA,\n"); 
                    //dayOfWeekIndex++;
                    if (dayList.isEmpty())    {                
                        data.add("HLAVICKA,\n");  
                        //new list
                        dayOfWeekIndex++;
                    }                    
                    else {
                        room = "FAKE";
                        i--;
                    }

                }
                
                output = day + "," + room + ",";
                lastComma = 2;                
            }
            
            if (!room.equals("FAKE")) {
                int start = getCourseHoursStart(starts.get(i));
                int end = getCourseHoursEnd(ends.get(i));

                //add empty column
                for (int j = lastComma; j < start; j++)
                    output += ",";

                //add identifier
                if (types.get(i).equals("c"))
                    output += "2";

                //add course name
                output += courses.get(i);
                String group = groups.get(i);
                //if course has group, add group
                if (!group.equals("x"))
                    output += " ("+group+")";
                output += ",";


                for (int j = start; j < end; j++)
                    output += "~,";

                lastComma = end+1;
                lastRoom = room;
                lastDay = day;
            }
        }
        //last row
        //add empty column
        for (int j = lastComma; j < 17; j++)
            output += ",";
        data.add(output);
        data.add("HLAVICKA,\n");
        
        return data;
    }
    
    
    /**
     * Get csv data of compulsory for lectures
     * @return List with csv
     * @throws SQLException 
     */
    private List<String> createCSVCompulsory() throws SQLException {
        ResultSet rs = null;
        
        List<String> data = new ArrayList<>();
        
        
        List<String> specs = new ArrayList<>();
        List<String> compulsoryTypes = new ArrayList<>();
        List<String> courseTypes = new ArrayList<>();
        List<String> courseDays = new ArrayList<>();
        
        
        String getSpec = 
            "select distinct p.zkratka,"
                + " o.zkratka, "
                + "v.typ_povinnosti, "
                + "c.typ, "
                + "c.den "
            + "from predmety p join casyPredmetu c on (p.id = c.id_predmetu) "
                + "join verzeRozvrhu vr on (c.id_verze = vr.id) "                    
                + "join semestry s on (vr.id_semestru = s.id) "
                + "join obory o on (s.id = o.id_semestru) "
                + "join povinnostVOboru v on (v.id_oboru = o.id and v.id_predmetu = p.id) "
            + "where vr.je_aktivni_flag = 1 "
                + "and v.typ_povinnosti like \"P%\" "                    
            + "order by c.den, substring(o.zkratka, 2), o.zkratka, v.typ_povinnosti, c.typ";
        

        rs = Database.executeQuery(getSpec);
        if (rs == null) {
            
            throw new SQLException("Could not select compulsory");
        }
        else {
            while(rs.next()) {
                specs.add(rs.getString(2));
                compulsoryTypes.add(rs.getString(3));
                courseTypes.add(rs.getString(4));
                courseDays.add(rs.getString(5));
            }   
        }
        if (specs.isEmpty())
            throw new SQLException("There are no data for export!");
        
        
        int size = specs.size();
        
        //BIT   MBI MBS MGM MIN MIS MMI MMM MPV MSK
        //1 2 3 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2
        String output = "";
        String lastDay = "";
        String lastSpec = "2MSK";
        List<String> otherLines = new ArrayList<>();
        List<String> otherLinesSpec = new ArrayList<>();    
        int otherLinesCount = 0;
        for (int i = 0; i < size; i++) {
            String day = courseDays.get(i);
            String spec = specs.get(i);
            
            //new day
            if (!day.equals(lastDay)) {
                if (!output.isEmpty()) {
                    
                    int newIndex = getSpecIndex(lastSpec);
                    
                    //add empty columns                    
                    for (int j = newIndex; j < 21; j++) {
                        output += ",";
                    }
                    data.add("HLAVICKA,\n");
                    data.add(output + "\n");
                            
                    //add empty columns
                    for (int k = 0; k < otherLinesSpec.size(); k++) {
                        newIndex = getSpecIndex(otherLinesSpec.get(k));
                            
                        String s = otherLines.get(k);
                        
                        for (int j = newIndex; j < 21; j++) {
                            s += ",";
                        }
                        
                        data.add(s+"\n");                                                
                    }
                    
                }
                output = "";
                otherLines = new ArrayList<>();
                otherLinesSpec = new ArrayList<>();
            }
            
            int index = getSpecIndex(spec);
            int lastIndex = getSpecIndex(lastSpec);
            //not same
            if (index != lastIndex) {
                if (lastIndex > index)
                    lastIndex = 0;
                
                for (int j = lastIndex; j < index; j++) {
                    output += ",";
                }
                
                output += "$";
                if (courseTypes.get(i).equals("c"))
                    output += "2";
                
                if (compulsoryTypes.get(i).equals("P"))
                    output += "P";
                else
                    output += "PV";
                                             
            }
            //same value
            else {
                String line = "";
                
                //insert first on first line
                if (otherLinesSpec.isEmpty()) {
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.add(spec);
                    
                    for (int j = 0; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("c"))
                        line += "2";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.add(line);
                    otherLinesCount = 1;
                    
                }
                //insert other on first line
                //(last value in first row isn't same, so insert)
                else if (!otherLinesSpec.get(0).equals(spec)) {
                    
                    line = otherLines.get(0);
                    
                    //get index of last spec
                    int newLastIndex = getSpecIndex(otherLinesSpec.get(0));
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.set(0, spec);
                    //System.out.println("novy: " + day +": "+spec + " - " + compulsoryTypes.get(i) + " " + courseTypes.get(i));
                    
                    for (int j = newLastIndex; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("c")) {
                        line += "2";
                        
                    }

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.set(0, line);
                    otherLinesCount = 1;
                }
                //if last value on first line is same, insert first on other line
                //(insert first on other lines)
                else if (otherLinesSpec.size() <= otherLinesCount) {                    
                    
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.add(spec); 
                    //System.out.println("dalsi novy: " + day +": "+spec + " - " + compulsoryTypes.get(i));
                    
                    for (int j = 0; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("c"))
                        line += "2";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.add(line);
                    otherLinesCount++;
                }        
                //insert other on other line
                //(if there is more lines than 1, and those lines aren't empty)
                else {
                    
                    line = otherLines.get(otherLinesCount);
                    
                    //get index of last spec
                    int newLastIndex = getSpecIndex(otherLinesSpec.get(otherLinesCount));
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.set(otherLinesCount, spec);
                    //System.out.println("dalsi: " + day +": "+spec + " - " + compulsoryTypes.get(i));
                    
                    for (int j = newLastIndex; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("c"))
                        line += "2";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.set(otherLinesCount, line);
                    otherLinesCount++;
                }
            }
            
            lastDay = day;            
            lastSpec = spec;
        }
        //add last day
        if (!output.isEmpty()) {
            int newIndex = getSpecIndex(lastSpec);
                    
            //add empty columns                    
            for (int j = newIndex; j < 21; j++) {
                output += ",";
            }
            data.add("HLAVICKA,\n");
            data.add(output + "\n");

            //add empty columns
            for (int k = 0; k < otherLinesSpec.size(); k++) {
                newIndex = getSpecIndex(otherLinesSpec.get(k));

                String s = otherLines.get(k);
                for (int j = newIndex; j < 21; j++) {
                    s += ",";
                }
                data.add(s+"\n");
            }
        }
        
        return data;
    }
    
    /**
     * Export csv file with schedule for exams
     * @throws java.sql.SQLException
     */
    public void exportCSVExamSchedule() throws SQLException {                     
        ResultSet rs = null;
        List<String> data;
        List<String> data2;
        List<String> courses = new ArrayList<>();
        List<String> starts = new ArrayList<>();
        List<String> ends = new ArrayList<>();
        List<String> rooms = new ArrayList<>();
        List<String> terms = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<String> days = new ArrayList<>();
        
        
        
        Database.dbSuccess  = true;
        
        
        String getCoursesTerms = 
                "select  p.zkratka, "
                    + "date_format(c.datum, \"%e. %c.\"), "
                    + "m.nazev, "
                    + "time_format(c.cas_od, \"%H:%i\") as cas_od, "
                    + "time_format(c.cas_do, \"%H:%i\") as cas_do, "
                    + "c.termin, "
                    + "date_format(c.datum, \"%w\") "
                + "from predmety p "
                    + "join casyZkousek c on (p.id = c.id_predmetu) "
                    + "join verzeZkousek vr on (c.id_verze = vr.id) "
                    + "join zkousce_prislusi zp on (zp.casyZkousek_id = c.id) "
                    + "join mistnosti m on (zp.mistnosti_id = m.id) "
                + "where vr.je_aktivni_flag = 1 "
                + "order by c.datum, m.nazev, cas_od ";

        rs = Database.executeQuery(getCoursesTerms);
        if (rs == null) {
            
            throw new SQLException("Could not select schedule");
        }
        else {
            while(rs.next()) {
                courses.add(rs.getString(1));
                dates.add(rs.getString(2));
                rooms.add(rs.getString(3));
                starts.add(rs.getString(4));
                ends.add(rs.getString(5));
                terms.add(rs.getString(6));
                days.add(rs.getString(7));
            }   
        }
        if (courses.isEmpty())
            throw new SQLException("There are no data for export!");
        
        
        List<String> lreasons = new ArrayList<>();
        List<String> ldates  = new ArrayList<>();
        List<String> ldays = new ArrayList<>();
        List<String> lrooms = new ArrayList<>();        
        
        String getLimitation = 
                "select duvod, "
                    + "datum, "
                    + "date_format(n.datum, \"%e. %c.\"), "
                    + "date_format(n.datum, \"%w\"), "
                    + "time_format(n.cas_od, \"%H:%i\") as cas_od, "
                    + "time_format(n.cas_do, \"%H:%i\") as cas_do, "
                    + "count(distinct datum, duvod, id_mistnosti) as pocet "
                + "from nedostupnost n "
                    + "join mistnosti m on (m.id = n.id_mistnosti) "
                + "where n.datum > "
                    + "(select s.zkouskove_od "
                    + "from verzeZkousek vr "
                        + "join semestry s on (vr.id_semestru = s.id) "
                    + "where vr.je_aktivni_flag = 1) "
                + "and n.datum < "
                    + "(select s.zkouskove_do "
                    + "from verzeZkousek vr "
                        + "join semestry s on (vr.id_semestru = s.id) "
                    + "where vr.je_aktivni_flag = 1) "
                + "group by duvod, datum, n.cas_od, n.cas_do "
                + "having pocet > 5 "
                + "order by datum ";
                
        rs = Database.executeQuery(getLimitation);
        if (rs == null) {
            
            throw new SQLException("Could not select limitation");
        }
        else {
            while(rs.next()) {
                lreasons.add(rs.getString(1));
                ldates.add(rs.getString(3));
                ldays.add(rs.getString(4));
            }   
        }
                
        
        //get schedule data
        data = createCSVDataExamRooms(courses, dates, starts, ends, terms, rooms, days);
        
        //get chart data
        data2 = createCSVCompulsoryExamRooms();
        
        List<String> tmpData2 = data2.subList(0, data2.size());
        
        int size = data.size();
        int size2 = tmpData2.size();
        int count = 0;       
        
        //for each element
        for (int i = 0; i < size; i++) {
                
            //get line from schedule
            String d = data.get(i);
            
            //if index in tmpData2
            if (count < size2) {
                
                //get line from chart
                String d2 = tmpData2.get(count);                
                
                //if line is header (fake header is between days)
                if (d.equals("HLAVICKA,\n") || d.equals("FAKE,\n")) {
                    
                    //get index of line with header in chart
                    count = tmpData2.indexOf("HLAVICKA,\n");                    
                    
                    //if no other header, end
                    if (count < 0) break;
                    
                    //get sublist without first header
                    tmpData2 = tmpData2.subList(count+1, size2); 
                    
                    count = 0;
                    size2 = tmpData2.size();    
                    
                    //get day from next and previous lines
                    String day = "None";
                    String lastDay = "Pá";
                    if (data.size() > i+1)
                        day = data.get(i+1).substring(0,2);
                    if (i > 0)
                        lastDay = data.get(i-1).substring(0,2);
                    
                    boolean limit = false;
                    //if there is one missing day
                    if (day.equals("Po") && !lastDay.equals("Pá")) limit = true;
                    else if (day.equals("Út") && !lastDay.equals("Po")) limit = true;
                    else if (day.equals("St") && !lastDay.equals("Út")) limit = true;
                    else if (day.equals("Čt") && !lastDay.equals("St")) limit = true;
                    else if (day.equals("Pá") && !lastDay.equals("Čt")) limit = true;
                    
                    //add empty line with "Omez." room for last day
                    if (i > 0) {
                        String dd = data.get(i-1);
                        String line = dd.substring(0, dd.indexOf(",")) + ",Omez.,";

                        
                        for (int j = 2; j < 38; j++)
                            line += ",";

                        line += "\n";
                        
                        //add before header
                        data.add(i, line);

                        i++;
                        size++;
                    }
                    
                    //if there is missin day
                    if (limit) {
                        //add "Omez." line                        
                        if (!ldates.isEmpty()) {                            
                            
                            String lday = getDay(ldays.get(0).charAt(0));
                            String date = ldates.get(0);
                            String reason = lreasons.get(0);
                            String line2 = lday + " " + date + ",Omez.,!" + reason;
                            
                            for (int j = 2; j < 15; j++)
                                line2 += ",~";
                            for (int j = 15; j < 38; j++)
                                line2 += ",";

                            line2 += "\n";
                            
                            //new day line
                            if (lday.equals("Po")) {                                                                
                                //add behind header
                                data.add(i,d);
                                d = line2;
                                i++;
                            }
                            else {                 
                                //add before header
                                data.add(i, line2);
                                i++;
                                
                            }
                            size++;
                            //remove used lines
                            ldates.remove(0);
                            lreasons.remove(0);                            
                            ldays.remove(0);                                                                                      
                        }
                    }                    
                }    
                //line of schedule is not header
                else {
                    //if line of chart is not header
                    if (!d2.equals("HLAVICKA,\n") ) {
                        //add chart to schedule
                        d += d2;                        
                        //increment index
                        count++;
                    }        
                    //if chart line is header
                    else {
                        //add empty chart and index is not incremented until on header in schedule line
                        for (int j = 17; j < 38; j++)
                            d += ",";
                        d+="\n";
                    }                    
                }
                
                //set new data
                data.set(i, d);
                
                                
            }
            //if  chart is empty and line is header
            else if (d.equals("HLAVICKA,\n")){
                //add empty line
                String dd = data.get(i-1);
                String line = dd.substring(0, dd.indexOf(",")) + ",Omez.,";

                for (int j = 2; j < 38; j++)
                    line += ",";

                line += "\n";

                data.add(i, line);

                i++;
                size++;
                data.set(i, d);
            }
            //if chart is empty, add empty char
            else if (i < (size-1)){                
                for (int j = 17; j < 38; j++)
                    d += ",";
                d+="\n";
                                
                data.set(i, d);
            }
            
        }
        
        //remove fake header
        while(data.remove("FAKE,\n")){}
        
        try {
            createFile(data, requirementCategory+"_terms.csv", "text/comma-separated-values");                  
        }
        catch(IOException e){
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Exception in export HTML: " + e.getMessage());            
        }

        
            
    }
    
    
    /**
     * Create csv data from db data
     * @param courses List of courses
     * @param days List of days
     * @param starts List of start time of courses
     * @param ends List of end times of courses
     * @param terms List of types of courses
     * @param rooms List of rooms of courses
     * @return List with csv data
     */
    private List<String> createCSVDataExamRooms(List<String> courses, List<String> dates, List<String> starts,
            List<String> ends, List<String> terms, List<String> rooms, List<String> days) throws SQLException {
        List<String> data = new ArrayList<>();
        ResultSet rs = null;                
        
        //collisions
        List<List<String>> examsCollisions = Database.getCollisionsExamsAll();
        if (!Database.dbSuccess)
            throw new SQLException("Could not select collisions");
        
        
        String output = "HLAVICKA,\n";
        int size = dates.size();
        String lastDate = dates.get(0);
        String lastRoom = "nothing";
        int lastComma = 38;
        int dayOfExamIndex = 0;
        for (int i = 0; i < size; i++) {
            String date = dates.get(i);
            String room = rooms.get(i);
            String day = getDay(days.get(i).charAt(0));        
            List<String> dayList = examsCollisions.get(dayOfExamIndex);
            
            //if rooms aren't same
            if (!room.equals(lastRoom)) {
                //add empty column
                for (int j = lastComma; j < 17; j++)
                    output += ",";
                
                if (!dayList.isEmpty() && lastComma != 38) {
                    //remove last comma
                    output = output.substring(0,output.length()-1);
                    output += dayList.get(0) + ";";
                    dayList.remove(0);
                    if (!dayList.isEmpty()) {
                        output += dayList.get(0);
                        dayList.remove(0);
                    } 
                    examsCollisions.set(dayOfExamIndex, dayList);
                    output += ",";
                }
                
                //add line
                data.add(output);
                if (!date.equals(lastDate)) {
                    
                    //add fake room to show all collisions
                    //if it's not desirable, delete condition dayList.isEmtpy()
                    //and just leave this:
                    //if (day.equals("Po"))
                    //    data.add("HLAVICKA,\n");
                    //else //add fake header between days
                    //  data.add("FAKE,\n");                       
                    //dayOfExamIndex++;
                    
                    if (dayList.isEmpty())    {
                        if (day.equals("Po"))
                            data.add("HLAVICKA,\n");
                        else //add fake header between days
                            data.add("FAKE,\n");
                        
                        dayOfExamIndex++;
                    }
                    else {
                        //fake room because of showing all collision
                        room = "FAKE";
                        i--;
                    }
                                        
                }
                 
                output = day+" "+ date + "," + room + ",";
                lastComma = 2;
            }
            
            if (!room.equals("FAKE")) {
                int start = getCourseHoursStart(starts.get(i));
                int end = getCourseHoursEnd(ends.get(i));

                //add empty column
                for (int j = lastComma; j < start; j++)
                    output += ",";

                //add identifier
                if (terms.get(i).equals("1opravny"))
                    output += "2";
                else if (terms.get(i).equals("2opravny"))
                    output += "3";

                //add course name
                output += courses.get(i);            

                output += ",";


                for (int j = start; j < end; j++)
                    output += "~,";

                lastComma = end+1;
                lastRoom = room;
                lastDate = date;
            }
        }
        //last row
        //add empty column
        for (int j = lastComma; j < 17; j++)
            output += ",";
        data.add(output);
        data.add("HLAVICKA,\n");
        
        return data;
    }
    
    
    /**
     * Get csv data with compulsory for exam
     * @return List with csv
     * @throws SQLException 
     */
    private List<String> createCSVCompulsoryExamRooms() throws SQLException {
        ResultSet rs = null;
        
        List<String> data = new ArrayList<>();
        
        
        List<String> specs = new ArrayList<>();
        List<String> compulsoryTypes = new ArrayList<>();
        List<String> courseTypes = new ArrayList<>();
        List<String> courseDays = new ArrayList<>();
        List<String> daysOfWeek = new ArrayList<>();
        
        
        String getSpec = 
            "select distinct p.zkratka, "
                + "o.zkratka, "
                + "v.typ_povinnosti, "
                + "c.termin, "
                + "c.datum, "
                + "date_format(c.datum, \"%w\") "
            + "from predmety p "
                + "join casyZkousek c on (p.id = c.id_predmetu) "
                + "join verzeZkousek vz on (c.id_verze = vz.id) "
                + "join zkousce_prislusi zp on (zp.casyZkousek_id = c.id) "
                + "join semestry s on (vz.id_semestru = s.id) "
                + "join obory o on (s.id = o.id_semestru) "
                + "join povinnostVOboru v on (v.id_oboru = o.id and v.id_predmetu = p.id) "
            + "where vz.je_aktivni_flag = 1 "
                + "and v.typ_povinnosti like \"P%\" "
            + "order by c.datum, substring(o.zkratka, 2), o.zkratka, v.typ_povinnosti";
        

        rs = Database.executeQuery(getSpec);
        if (rs == null) {
            
            throw new SQLException("Could not select compulsory");
        }
        else {
            while(rs.next()) {
                specs.add(rs.getString(2));
                compulsoryTypes.add(rs.getString(3));
                courseTypes.add(rs.getString(4));
                courseDays.add(rs.getString(5));
                daysOfWeek.add(rs.getString(6));
            }   
        }
        
        if (specs.isEmpty())
            throw new SQLException("There are no data for export!");
        
        
        int size = specs.size();
        
        //BIT   MBI MBS MGM MIN MIS MMI MMM MPV MSK
        //1 2 3 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2 1 2
        String output = "";
        String lastDay = "";
        String lastSpec = "2MSK";
        List<String> otherLines = new ArrayList<>();
        List<String> otherLinesSpec = new ArrayList<>();    
        int otherLinesCount = 0;
        boolean first = true;
        for (int i = 0; i < size; i++) {
            String day = courseDays.get(i);
            String spec = specs.get(i);
            String dayOfWeek = getDay(daysOfWeek.get(i).charAt(0));
            
            //new day
            if (!day.equals(lastDay)) {
                if (!output.isEmpty()) {
                    
                    int newIndex = getSpecIndex(lastSpec);
                    
                    //add empty columns                    
                    for (int j = newIndex; j < 21; j++) {
                        output += ",";
                    }
                    
                    data.add("HLAVICKA,\n");
                                        
                    data.add(output + "\n");
                            
                    //add empty columns
                    for (int k = 0; k < otherLinesSpec.size(); k++) {
                        newIndex = getSpecIndex(otherLinesSpec.get(k));
                            
                        String s = otherLines.get(k);
                        
                        for (int j = newIndex; j < 21; j++) {
                            s += ",";
                        }
                        
                        data.add(s+"\n");                                                
                    }
                    
                }
                output = "";
                otherLines = new ArrayList<>();
                otherLinesSpec = new ArrayList<>();
            }
            
            int index = getSpecIndex(spec);
            int lastIndex = getSpecIndex(lastSpec);
            //not same
            if (index != lastIndex) {
                if (lastIndex > index)
                    lastIndex = 0;
                
                for (int j = lastIndex; j < index; j++) {
                    output += ",";
                }
                
                output += "$";
                if (courseTypes.get(i).equals("1opravny"))
                    output += "2";
                else if (courseTypes.get(i).equals("2opravny"))
                    output += "3";
                
                if (compulsoryTypes.get(i).equals("P"))
                    output += "P";
                else
                    output += "PV";
                                             
            }
            //same value
            else {
                String line = "";                
                
                //insert first
                if (otherLinesSpec.isEmpty()) {
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.add(spec);
                    //System.out.println("prazdny: " + day +": "+spec + " - " + compulsoryTypes.get(i));
                    
                    for (int j = 0; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("1opravny"))
                        line += "2";
                    else if (courseTypes.get(i).equals("2opravny"))
                        line += "3";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.add(line);
                    otherLinesCount = 1;
                    
                }
                //last value in first row isn't same, so insert on first line
                else if (!otherLinesSpec.get(0).equals(spec)) {
                    
                    line = otherLines.get(0);
                    
                    //get index of last spec
                    int newLastIndex = getSpecIndex(otherLinesSpec.get(0));
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.set(0, spec);
                    //System.out.println("novy: " + day +": "+spec + " - " + compulsoryTypes.get(i) + " " + courseTypes.get(i));
                    
                    for (int j = newLastIndex; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("1opravny"))
                        line += "2";
                    else if (courseTypes.get(i).equals("2opravny"))
                        line += "3";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.set(0, line);
                    otherLinesCount = 1;
                }
                //if last value on first line is same, insert first on other line(s)
                else if (otherLinesSpec.size() <= otherLinesCount) {                    
                    
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.add(spec); 
                    //System.out.println("dalsi novy: " + day +": "+spec + " - " + compulsoryTypes.get(i));
                    
                    for (int j = 0; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("1opravny"))
                        line += "2";
                    else if (courseTypes.get(i).equals("2opravny"))
                        line += "3";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.add(line);
                    otherLinesCount++;
                }        
                //insert other on other line
                //(if there is more lines than 1, and those lines arent empty)
                else {
                    
                    line = otherLines.get(otherLinesCount);
                    
                    //get index of last spec
                    int newLastIndex = getSpecIndex(otherLinesSpec.get(otherLinesCount));
                    int newIndex = getSpecIndex(spec);
                    otherLinesSpec.set(otherLinesCount, spec);                    
                    
                    for (int j = newLastIndex; j < newIndex; j++) {
                        line += ",";
                    }

                    line += "$";
                    if (courseTypes.get(i).equals("1opravny"))
                        line += "2";
                    else if (courseTypes.get(i).equals("2opravny"))
                        line += "3";

                    if (compulsoryTypes.get(i).equals("P"))
                        line += "P";
                    else
                        line += "PV";

                    otherLines.set(otherLinesCount, line);
                    otherLinesCount++;
                }
            }
            
            lastDay = day;            
            lastSpec = spec;
        }
        //add last day
        if (!output.isEmpty()) {
            int newIndex = getSpecIndex(lastSpec);
                    
            //add empty columns                    
            for (int j = newIndex; j < 21; j++) {
                output += ",";
            }          
            data.add("HLAVICKA,\n");
            data.add(output + "\n");

            //add empty columns
            for (int k = 0; k < otherLinesSpec.size(); k++) {
                newIndex = getSpecIndex(otherLinesSpec.get(k));

                String s = otherLines.get(k);
                for (int j = newIndex; j < 21; j++) {
                    s += ",";
                }
                data.add(s+"\n");
            }
        }
        
        return data;
    }
    
    /**
     * Get index of study program (specialisation)
     * @param spec Study program code
     * @return Identifier of study program
     */
    private int getSpecIndex(String spec) {
        //BIT   MBI MBS MGM MIN MIS MMI MMM MPV MSK
        switch(spec) {
            case "1BIT": return 0;
            case "2BIT": return 1;
            case "3BIT": return 2;
            case "1MBI": return 3;
            case "2MBI": return 4;
            case "1MBS": return 5;
            case "2MBS": return 6;
            case "1MGM": return 7;
            case "2MGM": return 8;
            case "1MIN": return 9;
            case "2MIN": return 10;
            case "1MIS": return 11;
            case "2MIS": return 12;
            case "1MMI": return 13;
            case "2MMI": return 14;
            case "1MMM": return 15;
            case "2MMM": return 16;
            case "1MPV": return 17;
            case "2MPV": return 18;
            case "1MSK": return 19;
            case "2MSK": return 20;
        }
        return -1;
    }
    
    
    /**
    * Get time of course's start from index
    * @param hoursStart Index from csv file
    * @return Time of start of course
    */
    private int getCourseHoursStart(String hoursStart){
        int start = 2;
        switch(hoursStart) {
            case "07:00": start = 2;
                break;
            case "08:00": start = 3;
                break;
            case "09:00": start = 4;
                break;
            case "10:00": start = 5;
                break;
            case "11:00": start = 6;
                break;
            case "12:00": start = 7;
                break;
            case "13:00": start = 8;
                break;
            case "14:00": start = 9;
                break;
            case "15:00": start = 10;
                break;
            case "16:00": start = 11;
                break;
            case "17:00": start = 12;
                break;
            case "18:00": start = 13;
                break;
            case "19:00": start = 14;
                break;
            case "20:00": start = 15;
                break;            
        }
        
        return start;
    }
    
    /**
     * Get time of course's start from index
     * @param hoursStart Index from csv file
     * @return Time of start of course
     */
    private int getCourseHoursEnd(String hoursEnd){
        int start = 2;
        switch(hoursEnd) {
            case "07:50": start = 2;
                break;
            case "08:50": start = 3;
                break;
            case "09:50": start = 4;
                break;
            case "10:50": start = 5;
                break;
            case "11:50": start = 6;
                break;
            case "12:50": start = 7;
                break;
            case "13:50": start = 8;
                break;
            case "14:50": start = 9;
                break;
            case "15:50": start = 10;
                break;
            case "16:50": start = 11;
                break;
            case "17:50": start = 12;
                break;
            case "18:50": start = 13;
                break;
            case "19:50": start = 14;
                break;
            case "20:50": start = 15;
                break;            
        }
        
        return start;
    }        
}
