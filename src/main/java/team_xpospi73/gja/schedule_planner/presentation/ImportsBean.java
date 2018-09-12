/*
 * authors: Michael Švasta
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONObject;
import org.primefaces.model.UploadedFile;
import team_xpospi73.gja.schedule_planner.model.Database;


/**
 * Presenter for controlling all imports
 */
@ManagedBean
public class ImportsBean {
    /**
     * Imported file
     */
    private UploadedFile file;    
    /**
     * Requirement value 
     */
    String requirementCategory;
    
    String csvSemester;
    int csvYear;
    
    @PostConstruct
    public void init(){
        csvYear = 2017;
        Database.connectDB();
    }

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
     * Get value of semester for import schedule
     * @return String value of semester
     */
    public String getCsvSemester() {
        return this.csvSemester;
    }
    
    /**
     * Set semester for import schedule
     * @param csvSemester String value for csvSemester
     */
    public void setCsvSemester(String csvSemester) {
        this.csvSemester = csvSemester;
    }
    
    /**
     * Get value of year for import schedule
     * @return Int vaule of year
     */
    public int getCsvYear() {
        return this.csvYear;
    }
    
    /**
     * Set year for import schedule
     * @param csvYear Int value for csvYear
     */
    public void setCsvYear(int csvYear) {
        this.csvYear = csvYear;
    }
    
    /**
     * Get file
     * @return file
     */
    public UploadedFile getFile() {
      return file;
    }

    /**
     * Set file
     * @param file 
     */
    public void setFile(UploadedFile file) {
      this.file = file;
    }
    
    /**
     * Upload 'json'
     */
    public void uploadJSON() {
        
        try {
            if (file != null && !file.getFileName().equals("")) {                                      
                String extension = file.getFileName().substring(file.getFileName().lastIndexOf("."));
                System.out.println("Extension: " + extension); 
                /* File extension */
                switch (extension.toLowerCase()) {
                    case ".json":                        
                        importJson();
                        break;                    
                    default:                        
                        throw new IOException("You must upload '.json' file");
                }                
            }
            else {                
                throw new IOException("No file was selected");
            }
            
        }
        catch (IOException|SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        } 
    }
    
    /**
     * Upload CSV file
     */
    public void uploadCSV() {
        
        try {
            
            if (file != null && !file.getFileName().equals("")) {                                      
                String extension = file.getFileName().substring(file.getFileName().lastIndexOf("."));
                System.out.println("Extension: " + extension); 
                /* File extension */
                switch (extension.toLowerCase()) {                    
                    case ".csv":
                        if (csvYear < 2000)
                            throw new IOException("Year is too small");
                        else if (csvYear > 2030)
                            throw new IOException("Year is too big");
                        else if (requirementCategory.equals("lect"))
                            importCsvLectures();
                        else if (requirementCategory.equals("exam"))
                            importCsvExams();
                        break;                       
                    default:                        
                        throw new IOException("You must upload '.csv' file");
                }
                
            }
            else {                
                throw new IOException("No file was selected");
            }
            
        }
        catch (IOException|SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        } 
    }
    
    /**
     * Import html file with teacher's requirements
     */
    public void uploadReq() {
        
        try {
            if (file != null && !file.getFileName().equals("")) {                                      
                String extension = file.getFileName().substring(file.getFileName().lastIndexOf("."));
                System.out.println("Extension: " + extension); 
                /* File extension */
                if (extension.toLowerCase().equals(".html")) {
                    if (csvYear < 2000)
                        throw new IOException("Year is too small");
                    else if (csvYear > 2030)
                        throw new IOException("Year is too big");
                    switch(requirementCategory){
                        case "lect": uploadLecture();
                            break;
                        case "exam": uploadExam();
                            break;
                    }
                }
                else{
                    throw new IOException("You must upload '.html' file");
                }
                
            }
            else {                
                throw new IOException("No file was selected");
            }
            
        }
        catch (IOException|SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        } 
    }
    
    /**
     * Import html with students' collisions
     */
    public void uploadCollisions() {
        
        try {
            if (file != null && !file.getFileName().equals("")) {                                      
                String extension = file.getFileName().substring(file.getFileName().lastIndexOf("."));
                System.out.println("Extension: " + extension); 
                /* File extension */
                if (extension.toLowerCase().equals(".html")) {
                    if (csvYear < 2000)
                        throw new IOException("Year is too small");
                    else if (csvYear > 2030)
                        throw new IOException("Year is too big");
                    
                    importCollisions();
                }
                else{
                    throw new IOException("You must upload '.html' file");
                }
                
            }
            else {                
                throw new IOException("No file was selected");
            }
            
        }
        catch (IOException|SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message); 
            System.out.println("Error. " + e.getMessage());            
        } 
    }
    
    /**
     * Import 'csv' file
     * @throws IOException Failed or interrupted I/O operations
     */
    private void importCsvLectures() throws IOException, SQLException {        
            
        InputStream is = file.getInputstream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));        

        Database.dbSuccess  = true;
        
        Database.conn.setAutoCommit(false);
        
        
        int versionID = 1;
        int courseTimesID = 1;
        int courseRoomID = 1;
        int collisionID = 1;
        ResultSet rs = null;
        
        rs = Database.executeQuery("select max(id) from verzeRozvrhu");
        if (rs == null) {
            System.out.println("Could not select id from verzeRozvrhu");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                versionID=rs.getInt(1);
            versionID++;   
        }
        
        rs = Database.executeQuery("select max(id) from casyPredmetu");
        if (rs == null) {
            System.out.println("Could not select id from casyPredmetu");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                courseTimesID=rs.getInt(1);
            courseTimesID++;               
        }    
        
        rs = Database.executeQuery("select max(id) from predmetuPrislusi");
        if (rs == null) {
            System.out.println("Could not select id from predmetuPrislusi");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                courseRoomID=rs.getInt(1);
            courseRoomID++;               
        }
        
        rs = Database.executeQuery("select max(id) from kolize");
        if (rs == null) {
            System.out.println("Could not select id from kolize");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                collisionID=rs.getInt(1);
            collisionID++;               
        }
        
        
        //only if no error in select
        if(Database.dbSuccess )
            Database.InsertScheduleVersion(versionID, csvYear, csvSemester);
        
        
        List<String> dayCourses;
        List<String> dayCoursesType;
        List<String> dayCoursesGroups;
        List<Integer> dayCoursesHours;
        List<Integer> dayCoursesStart;
        List<String> collisionCourse1;
        List<String> collisionCourse2;
        List<Integer> collisionCounts;
        
        String inputStr;
        while ((inputStr = br.readLine()) != null) {

            //break because of error in inserting
            if(!Database.dbSuccess )
                break;
            
            dayCourses = new ArrayList<>();
            dayCoursesType = new ArrayList<>();
            dayCoursesGroups = new ArrayList<>();
            dayCoursesHours = new ArrayList<>();
            dayCoursesStart = new ArrayList<>();
            collisionCourse1 = new ArrayList<>();
            collisionCourse2 = new ArrayList<>();
            collisionCounts = new ArrayList<>();
                                    
            int column = 0; //count of columns on line
            int courseHours = 1; //length of course
            String courseName = ""; //course's name
            String roomName = ""; //course's room   
            String groupName = ""; //group's name
            String dayName = ""; //day name
            String courseType = "p";
            boolean ignore = false; //flag for ignoring +hours in course's name
            boolean group = false; //flag for group
            String collision1 = "";
            String collision2 = "";
            String collisionCount = "";
            int collisionIndex = 0;
            
            for (int i = 0; i < inputStr.length(); i++) {
                char c = inputStr.charAt(i);
                
                if (c == ',') {
                    //output += courseName+",";                    
                    if (!courseName.isEmpty()) {
                        dayCourses.add(courseName);
                        dayCoursesType.add(courseType);
                        if (groupName.isEmpty())
                            dayCoursesGroups.add("");
                        else
                            dayCoursesGroups.add(groupName);
                        dayCoursesHours.add(courseHours);
                        dayCoursesStart.add(column);                        
                        courseName = "";
                        groupName = "";
                        courseType = "p";
                    }
                    //add second collion
                    else if (!collision1.isEmpty()) {
                        collisionIndex = 0;
                        collisionCounts.add(Integer.parseInt(collisionCount));
                        collisionCourse1.add(collision1);
                        collisionCourse2.add(collision2);
                        collision1 = "";
                        collision2 = "";
                        collisionCount = "";                        
                    }
                    
                    
                    column++;                        
                                            
                    ignore = false;
                    group = false;
                                 
                }
                //day of week
                else if (column == 0) {
                    dayName += c;
                }
                //room name
                else if (column == 1) {
                    roomName += c;
                }
                //schedule
                else if (column < 16) {
                    
                    switch (c) {
                        case '~':
                            courseHours++;
                            //get index of last item
                            int lastIndex = dayCoursesHours.size()-1;
                            //inkrement last item
                            dayCoursesHours.set(lastIndex, dayCoursesHours.get(lastIndex)+1);
                            ignore = true;
                            
                            break;
                        case '+':
                            ignore = true;                            
                            break;
                        case ' ':
                            group = true;
                            ignore = true;
                        default:                      
                            courseHours = 1;
                            break;
                    }
                    
                    //ignore +hours in course's name, ignore number at start of name                    
                    if (!ignore) {
                        if (!(Character.isDigit(c) && courseName.isEmpty()))
                            courseName += c;                            
                        else if (c == '2')
                            courseType = "c";
                    }
                    // group (a)/(b)
                    else if (group && (c == 'a' || c == 'b')) {                        
                        groupName += c;
                    }
                }
                else if (column < 17) {
                    if (c == ' ') {
                        collisionIndex++;
                    }
                    else if (c != ';'){
                        switch (collisionIndex) {
                            case 0:
                                collision1 += c;
                                break;
                            case 1:
                                collision2 += c;
                                break;
                            default:
                                collisionCount += c;
                                break;
                        }
                    }
                    else if (!collision1.isEmpty()) {
                        collisionIndex = 0;
                        collisionCounts.add(Integer.parseInt(collisionCount));
                        collisionCourse1.add(collision1);
                        collisionCourse2.add(collision2);
                        collision1 = "";
                        collision2 = "";
                        collisionCount = "";                        
                    }                    
                }
                else if (column == 18)
                    break;
            }
            
            //Insert into DB
            if (!dayCourses.isEmpty()) {
                
                if (dayName.length() > 3) {
                    System.out.println("Bad date format (required 'DoW' (Day of Week)). Maybe imports file with exams instead of lectures.");
                    Database.dbSuccess  = false;
                    break;
                }
                
                
                int size = dayCourses.size();                    
                for (int j = 0; j < size; j++) {
                    /*
                    groupName = "";
                    if (!dayCoursesGroups.get(j).isEmpty()) {
                        groupName = ", Group: " + dayCoursesGroups.get(j);
                    }
                    */                  
                    
                    //Insert course time into db
                    Database.InsertCourseTimes(courseTimesID, versionID, dayName,                         
                        dayCourses.get(j), dayCoursesType.get(j),
                        dayCoursesGroups.get(j),
                        getCourseHoursStart(dayCoursesStart.get(j)), 
                        getCourseHoursEnd(dayCoursesStart.get(j), dayCoursesHours.get(j)),
                        csvYear, csvSemester);
                    
                    
                    //break because of error in inserting
                    if(!Database.dbSuccess )
                        break;
                    
                    //Insert course room into db
                    Database.InsertCourseRoom(courseRoomID, courseTimesID, roomName);
                    
                    //break because of error in inserting
                    if(!Database.dbSuccess )
                        break;
                    
                    
                    //increment primary keys
                    courseTimesID++;
                    courseRoomID++;
                }
            }  
             
            for (int k = 0; k < collisionCourse1.size(); k++) {
                //because of error in inserting
                if (!Database.dbSuccess)
                    break;
                collisionID = Database.InsertCollision(collisionID, collisionCourse1.get(k), 
                        collisionCourse2.get(k), csvYear, csvSemester, collisionCounts.get(k));
            }
            
        }            
        br.close();
        is.close();

        
        
        if (!Database.dbSuccess ){
            Database.conn.rollback();            
            System.out.println("ROLLBACK");
        }
                

        /* Message with error */
        if (!Database.dbSuccess ) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "There was some error at inserting - rollback. " +
                    "For more information see console output.");
            FacesContext.getCurrentInstance().addMessage(null, message);    
        }     
        /* Or message with successful upload */
        else {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() 
                    + " is uploaded.");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
        }
        
        Database.conn.setAutoCommit(true);
     
    }
    
    /**
     * Import 'csv' file
     * @throws IOException Failed or interrupted I/O operations
     */
    private void importCsvExams() throws IOException, SQLException {        
            
        InputStream is = file.getInputstream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));        

        Database.dbSuccess  = true;
        
        Database.conn.setAutoCommit(false);
        
        int versionID = 1;
        int examTimesID = 1;
        int examRoomID = 1;
        int limitID = 1;
        int collisionID = 1;
        ResultSet rs = null;
        
        rs = Database.executeQuery("select max(id) from verzeZkousek");
        if (rs == null) {
            System.out.println("Could not select id from verzeZkousek");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                versionID=rs.getInt(1);
            versionID++;   
        }
        
        rs = Database.executeQuery("select max(id) from casyZkousek");
        if (rs == null) {
            System.out.println("Could not select id from casyZkousek");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                examTimesID=rs.getInt(1);
            examTimesID++;               
        }
            
        rs = Database.executeQuery("select max(id) from zkousce_prislusi");
        if (rs == null) {
            System.out.println("Could not select id from zkousce_prislusi");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                examRoomID=rs.getInt(1);
            examRoomID++;               
        }   
        
        rs = Database.executeQuery("select max(id) from nedostupnost");
        if (rs == null) {
            System.out.println("Could not select id from nedostupnost");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                limitID=rs.getInt(1);
            limitID++;  
        }
                    
        rs = Database.executeQuery("select max(id) from kolize");
        if (rs == null) {
            System.out.println("Could not select id from kolize");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                collisionID=rs.getInt(1);
            collisionID++;               
        }
        
        //only if no error in inserting
        if(Database.dbSuccess )
            Database.InsertExamVersion(versionID, csvYear, csvSemester);
                       
            
        
        
        List<String> dayExams;
        List<String> dayExamsType;        
        List<Integer> dayExamsHours;
        List<Integer> dayExamsStart;
        List<String> collisionCourse1;
        List<String> collisionCourse2;
        List<Integer> collisionCounts;
        
        String inputStr;
        while ((inputStr = br.readLine()) != null) {

            //break because of error in inserting
            if(!Database.dbSuccess )
                break;
            
            dayExams = new ArrayList<>();
            dayExamsType = new ArrayList<>();            
            dayExamsHours = new ArrayList<>();
            dayExamsStart = new ArrayList<>();
            collisionCourse1 = new ArrayList<>();
            collisionCourse2 = new ArrayList<>();
            collisionCounts = new ArrayList<>();
                                    
            int column = 0; //count of columns on line
            int examHours = 1; //length of course
            String examName = ""; //course's name
            String roomName = ""; //course's room               
            String dayName = ""; //day name
            String examType = "radny";
            boolean ignore = false; //flag for ignoring +hours in course's name
            String collision1 = "";
            String collision2 = "";
            String collisionCount = "";
            int collisionIndex = 0;
            
            for (int i = 0; i < inputStr.length(); i++) {
                char c = inputStr.charAt(i);
                
                if (c == ',') {
                    //output += courseName+",";                    
                    if (!examName.isEmpty()) {
                        dayExams.add(examName);
                        dayExamsType.add(examType);                        
                        dayExamsHours.add(examHours);
                        dayExamsStart.add(column);                        
                        examName = "";                        
                        examType = "radny";
                    }                   
                     //add second collion
                    else if (!collision1.isEmpty()) {
                        collisionIndex = 0;
                        collisionCounts.add(Integer.parseInt(collisionCount));
                        collisionCourse1.add(collision1);
                        collisionCourse2.add(collision2);
                        collision1 = "";
                        collision2 = "";
                        collisionCount = "";                        
                    }
                    
                    column++;                        
                                            
                    ignore = false;
                    
                }
                //day of week
                else if (column == 0) {
                    dayName += c;
                }
                //room name
                else if (column == 1) {
                    roomName += c;
                }
                //schedule
                else if (column < 16) {
                    
                    switch (c) {
                        case '~':
                            examHours++;
                            //get index of last item
                            int lastIndex = dayExamsHours.size()-1;
                            //inkrement last item
                            dayExamsHours.set(lastIndex, dayExamsHours.get(lastIndex)+1);
                            ignore = true;
                            
                            break;
                        case '+':
                            ignore = true;                            
                            break;
                        case '(':                           
                            if (!roomName.equals("Omez."))                        
                                ignore = true;                            
                            break;
                        default:                      
                            examHours = 1;
                            break;
                    }
                    
                    //ignore +hours in course's name, ignore number at start of name                    
                    if (!ignore) {
                        if (!(Character.isDigit(c) && examName.isEmpty()))
                            examName += c;                            
                        else if (c == '2')
                            examType = "1opravny";
                        else if (c == '3')
                            examType = "2opravny";
                    }
                    
                }
                else if (column < 17) {
                    if (c == ' ') {
                        collisionIndex++;
                    }
                    else if (c != ';'){
                        switch (collisionIndex) {
                            case 0:
                                collision1 += c;
                                break;
                            case 1:
                                collision2 += c;
                                break;
                            default:
                                collisionCount += c;
                                break;
                        }
                    }
                    else if (!collision1.isEmpty()) {
                        collisionIndex = 0;
                        collisionCounts.add(Integer.parseInt(collisionCount));
                        collisionCourse1.add(collision1);
                        collisionCourse2.add(collision2);
                        collision1 = "";
                        collision2 = "";
                        collisionCount = "";                        
                    }                    
                }
                else if (column == 18)
                    break;
            }
            
            //Insert into DB
            if (!dayExams.isEmpty()) {
                int size = dayExams.size();   
                                
                if (dayName.length() < 4 || dayName.length() > 10) {
                    System.out.println("Bad date format (required 'DoW DD. MM.'). Maybe imports file with lectures instead of exams.");
                    Database.dbSuccess  = false;
                    break;
                }
                
                //add year to date
                //for winter semester increments year, because of new year
                if (csvSemester.equals("ZS")) 
                    dayName = dayName.substring(3) + (csvYear+1);
                else
                    dayName = dayName.substring(3) + csvYear;
                    
                //else
                  //  dayName = dayName.substring(3) + csvYear;
                
                for (int j = 0; j < size; j++) {

                    
                    //insert limitation
                    if (dayExams.get(j).charAt(0) == '!') {
                        
                        //inset for all rooms
                        if (roomName.equals("Omez.")) {
                            
                            char c = dayExams.get(j).charAt(1);
                            
                            //ignore !RKD or !KD
                            if (c == 'R' || c == 'K') break;
                            
                            rs = Database.executeQuery("select nazev from mistnosti");
                            if (rs == null) {
                                System.out.println("Could not select id from mistnosti");
                                Database.dbSuccess  = false;            
                            }
                            else {
                                while(rs.next()) {
                                    roomName = rs.getString(1);
                                    
                                    if (!Database.dbSuccess)
                                        break;
                                    
                                    Database.InsertLimitation(limitID, dayName, roomName,    
                                        dayExams.get(j),
                                        getCourseHoursStart(dayExamsStart.get(j)), 
                                        getCourseHoursEnd(dayExamsStart.get(j), dayExamsHours.get(j)));
                                    limitID++;
                                }                                   
                            }
                            
                        }
                        else {
                            Database.InsertLimitation(limitID, dayName, roomName,    
                                dayExams.get(j),
                                getCourseHoursStart(dayExamsStart.get(j)), 
                                getCourseHoursEnd(dayExamsStart.get(j), dayExamsHours.get(j)));
                            limitID++;
                        }
                        //break because of error in inserting
                        if(!Database.dbSuccess )
                            break;
                    }
                    else {
                        //Insert exam time into db
                        Database.InsertExamTimes(examTimesID, versionID, dayName, 
                            dayExams.get(j), dayExamsType.get(j),
                            getCourseHoursStart(dayExamsStart.get(j)), 
                            getCourseHoursEnd(dayExamsStart.get(j), dayExamsHours.get(j)),
                            csvYear, csvSemester);
                        
                        //break because of error in inserting
                        if(!Database.dbSuccess )
                            break;
                        
                        
                        //Insert course room into db
                        Database.InsertExamRoom(examRoomID, examTimesID, roomName);
                        
                        //break because of error in inserting
                        if(!Database.dbSuccess )
                            break;
                        
                        //increment primary keys
                        examTimesID++;
                        examRoomID++;
                    }
                    
                }
            }             
            
            //insert collisions
            for (int k = 0; k < collisionCourse1.size(); k++) {
                //because of error in inserting
                if (!Database.dbSuccess)
                    break;
                collisionID = Database.InsertCollision(collisionID, collisionCourse1.get(k), 
                        collisionCourse2.get(k), csvYear, csvSemester, collisionCounts.get(k));
            }
        }            
        br.close();
        is.close();
        
        

        if (!Database.dbSuccess ){
            Database.conn.rollback();            
            System.out.println("ROLLBACK");
        }
                

        /* Message with error */
        if (!Database.dbSuccess ) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "There was some error at inserting - rollback. " +
                    "For more information see console output.");
            FacesContext.getCurrentInstance().addMessage(null, message);    
        }     
        /* Or message with successful upload */
        else {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() 
                    + " is uploaded.");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
        }
        
        Database.conn.setAutoCommit(true);
        
    }
    
    /**
     * Get time of course's start from index
     * @param hoursStart Index from csv file
     * @return Time of start of course
     */
    private String getCourseHoursStart(int hoursStart){
        String start = "00:00";
        switch(hoursStart) {
            case 2: start = "07:00";
                break;
            case 3: start = "08:00";
                break;
            case 4: start = "09:00";
                break;
            case 5: start = "10:00";
                break;
            case 6: start = "11:00";
                break;
            case 7: start = "12:00";
                break;
            case 8: start = "13:00";
                break;
            case 9: start = "14:00";
                break;
            case 10: start = "15:00";
                break;
            case 11: start = "16:00";
                break;
            case 12: start = "17:00";
                break;
            case 13: start = "18:00";
                break;
            case 14: start = "19:00";
                break;
            case 15: start = "20:00";
                break;            
        }
        
        return start;
    }
    
    /**
     * Get end time of course from index and length of course
     * @param hoursStart Index of start from csv
     * @param hoursCount Length of course
     * @return End time of course
     */
    private String getCourseHoursEnd(int hoursStart, int hoursCount){
        hoursStart += hoursCount;
        String end = "00:00";
        switch(hoursStart) {            
            case 3: end = "07:50";
                break;
            case 4: end = "08:50";
                break;
            case 5: end = "09:50";
                break;
            case 6: end = "10:50";
                break;
            case 7: end = "11:50";
                break;
            case 8: end = "12:50";
                break;
            case 9: end = "13:50";
                break;
            case 10: end = "14:50";
                break;
            case 11: end = "15:50";
                break;
            case 12: end = "16:50";
                break;
            case 13: end = "17:50";
                break;
            case 14: end = "18:50";
                break;
            case 15: end = "19:50";
                break;
            case 16: end = "20:50";
                break;
            case 17: end = "21:50";
                break;
        }
        
        return end;
    }
    
    /**
     * Import 'json' file
     * @throws IOException Failed or interrupted I/O operations
     */
    private void importJson() throws IOException, SQLException{
           
        /* Create string from input file */           
        InputStream is = file.getInputstream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder strBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = br.readLine()) != null) {
            strBuilder.append(inputStr);     
        }            
        br.close();
        is.close();
        /* Json from string */
        JSONObject obj = new JSONObject(strBuilder.toString());         

        
        //Get connection
        Database.dbSuccess  = true;
        
        Database.conn.setAutoCommit(false);
        
        
        /* Parsing courses */
        JSONArray courses = obj.getJSONArray("courses");
        int lecturersID = 1, courseIDPK = 1, lecturersCoursesID = 1, semID=1;
        int studyProgramID = 1, compulsoryID = 1;
        ResultSet rs = null;
        
        rs = Database.executeQuery("select max(id) from ucitele");
        if (rs == null) {
            System.out.println("Could not select id from ucitele");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                lecturersID=rs.getInt(1);
            lecturersID++;  
        }

        rs = Database.executeQuery("select max(id) from predmety");
        if (rs == null) {
            System.out.println("Could not select id from predmety");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                courseIDPK=rs.getInt(1);
            courseIDPK++;
        }
        
                
        rs = Database.executeQuery("select max(id) from prednasejiciPredmetu");
        if (rs == null) {
            System.out.println("Could not select id from prednasejiciPredmetu");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                lecturersCoursesID=rs.getInt(1);
            lecturersCoursesID++;
        }
        

        rs = Database.executeQuery("select max(id) from semestry");
        if (rs == null) {
            System.out.println("Could not select id from semestry");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                semID=rs.getInt(1);
            semID++;
        }
        
        
        rs = Database.executeQuery("select max(id) from povinnostVOboru");
        if (rs == null) {
            System.out.println("Could not select id from povinnostVOboru");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                compulsoryID=rs.getInt(1);
            compulsoryID++;
        }
        
        
        rs = Database.executeQuery("select max(id) from obory");
        if (rs == null) {
            System.out.println("Could not select id from obory");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                studyProgramID=rs.getInt(1);
            studyProgramID++;
        }
               
        
        
        //Get year from json
        int year = Integer.parseInt(obj.get("year").toString());
        
        //not insert same semester again
        int yearID = 0;
        rs = Database.executeQuery("select max(id) from semestry where obdobi = \"ZS\" and rok = "+ year);
        while(rs != null && rs.next())
            yearID=rs.getInt(1);

        if (rs == null) {            
            Database.dbSuccess  = false;            
        }
        else if (yearID == 0) {
            semID = Database.InsertSemesters(semID, year, "ZS");
            semID++;
        }     
        else {
            System.out.println("Winter semester is already in DB for " + year);
        }
        
        int tmpYear = year+1;
        yearID = 0;
        rs = Database.executeQuery("select max(id) from semestry where obdobi = \"LS\" and rok = "+ tmpYear);
        while(rs != null && rs.next())
            yearID=rs.getInt(1);

        if (rs == null) {            
            Database.dbSuccess  = false;            
        }
        else if (yearID == 0) {
            semID = Database.InsertSemesters(semID, tmpYear, "LS");
            semID++;
        }   
        else {
            System.out.println("Summer semester is already in DB for " + tmpYear);
        }
        
        
        List<String> lecturersList = new ArrayList<>();
        for (int i = 0; i < courses.length(); i++) {
            //break because of error in inserting
            if(!Database.dbSuccess )
                break;
            
            int courseID = Integer.parseInt(courses.getJSONObject(i).getString("id"));
            String code = courses.getJSONObject(i).getString("zkratka");
            String name = courses.getJSONObject(i).getString("nazev");
            String guarantee = courses.getJSONObject(i).getString("garant");
            
            
            
            JSONArray lecturers = courses.getJSONObject(i).getJSONArray("prednas");            
            //String instructor = courses.getJSONObject(i).getString("cvicici");
            /*int compExH = courses.getJSONObject(i).getInt("poc_cv");
            int exerH = courses.getJSONObject(i).getInt("cv");
            int lectH = courses.getJSONObject(i).getInt("predn");
            int labExH = courses.getJSONObject(i).getInt("lab_cv");
            int otherH = courses.getJSONObject(i).getInt("jina");
            */
            //System.out.println(code + " " + name + " " + guarantee);                

            
            String lecturer = "";
            List<Integer> lectIDs = new ArrayList<>();            


            int len = lecturers.length();
            for (int j = 0; j <= len; j++) {
                //lecturerList.add(lecturers.get(j).toString());

                if (j < len)
                    lecturer = lecturers.get(j).toString();
                else 
                    lecturer = guarantee;

                //if lecturer isn't in course's lecturer list
                if (!lecturersList.contains(lecturer)) {
                    //add lecturer into list of course's lecturers
                    lecturersList.add(lecturer);
                    
                    //check if lecturer is in DB
                    int tmpLecturerID = Database.CheckLecturer(lecturer);
                    //break because of error in inserting
                    if(!Database.dbSuccess )
                        break;
                    
                    //if lecturer wasn't inserted yet
                    if (tmpLecturerID <= 0) {
                        //insert lecturer into table ucitele
                        Database.InsertLecturers(lecturersID, lecturer);
                        lectIDs.add(lecturersID);
                        lecturersID++;
                    }
                    else {
                        //lecturer is in db already, just add into list of course's lecturers
                        lectIDs.add(tmpLecturerID);
                        System.out.println("\""+lecturer+"\" is in DB already");
                    }                    
                }                                                          
                //break because of error in inserting
                if(!Database.dbSuccess )
                    break;
            }

                        
            //Table predmety            
            Database.InsertCourses(courseID, name, code, lecturersList.indexOf(guarantee)+1, courseIDPK);
            courseIDPK++;
            
            //Table prednasejiciPredmetu
            lecturersCoursesID = Database.InsertLecturersOfCourse(lectIDs, courseIDPK, lecturersCoursesID);     
                        
        }                        
                        

        /* Parsing study programs */        
        JSONArray programs = obj.getJSONArray("study_programs");
        for (int i = 0; i < programs.length(); i++) {                
            
            //break because of error in inserting
            if(!Database.dbSuccess )
                break;
            
            //table "obory"
            String studyProgramName = programs.getJSONObject(i).getString("name");
            String studyProgramFullName = programs.getJSONObject(i).getString("full_name");
            studyProgramID = Database.InsertSpecialisation(year, studyProgramID, studyProgramName, studyProgramFullName);
            

            //parse courses for studyProgram
            JSONArray programCourses = programs.getJSONObject(i).getJSONArray("courses");
            for (int j = 0; j < programCourses.length(); j++) {                    
                
                //break because of error in inserting
                if(!Database.dbSuccess )
                    break;
                
                int id = Integer.parseInt(programCourses.getJSONObject(j).getString("id"));
                String code = programCourses.getJSONObject(j).getString("zkratka");
                String grade = programCourses.getJSONObject(j).getString("rocnik");
                String term = programCourses.getJSONObject(j).getString("semestr");                    
                String type = programCourses.getJSONObject(j).getString("typ");
                String end = programCourses.getJSONObject(j).getString("zak");

                //table "povinnostiVOboru"
                compulsoryID = Database.InsertCompulsory(code, compulsoryID, type, studyProgramID, term, grade, studyProgramName);  
            }           
        }


        
        if (!Database.dbSuccess ){
            Database.conn.rollback();    
            System.out.println("ROLLBACK");
        }
                

        /* Message with error */
        if (!Database.dbSuccess ) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "There was some error at inserting - rollback. " +
                    "For more information see console output.");
            FacesContext.getCurrentInstance().addMessage(null, message);    
        }     
        /* Or message with successful upload */
        else {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() 
                    + " is uploaded.");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
        }
        
        Database.conn.setAutoCommit(true);
        
    }        
    
    /**
     * Import html file with teacher's requirements for lecture
     * @throws IOException 
     */
    private void uploadLecture() throws IOException, SQLException {
        InputStream is = file.getInputstream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "ISO-8859-2"));
        StringBuilder strBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = br.readLine()) != null) {            
            strBuilder.append(inputStr);              
        } 
        br.close();
        is.close();
                
        Document doc = Jsoup.parse(strBuilder.toString());
        Element table = doc.select("table").last();
        Elements rows = table.select("tr");
        
        Database.dbSuccess  = true;
         
        Database.conn.setAutoCommit(false);

        
        int reqID = 1;
        int termsID=1;
        
        //get IDs
        ResultSet rs = null;
        
        rs = Database.executeQuery("select max(id) from pozadavkyNaRozvrh");
        if (rs == null) {
            System.out.println("Could not select id from pozadavkyNaRozvrh");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                reqID=rs.getInt(1);
            reqID++;            
        }
            
        rs = Database.executeQuery("select max(id) from terminyPozadavkuNaRozvrh");
        if (rs == null) {
            System.out.println("Could not select id from terminyPozadavkuNaRozvrh");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                termsID=rs.getInt(1);
            termsID++;
        }
        
        
        List<String> dayOfWeekList;
        dayOfWeekList = new ArrayList<>();
        dayOfWeekList.add("Po");
        dayOfWeekList.add("Út");
        dayOfWeekList.add("St");
        dayOfWeekList.add("Čt");
        dayOfWeekList.add("Pá");
        
        //for all rows in table
        for (int i = 2; i < rows.size(); i++) {  
            
            //break because of error in select id or in inserting
            if(!Database.dbSuccess )
                break;
            
            Element row = rows.get(i);
            Elements columns = row.select("td");
            Element code = columns.get(0);
            
            //if course's code has rowspan
            int rowspan = 0;
            if (code.hasAttr("rowspan")) {
                rowspan = Integer.parseInt(code.attr("rowspan"));  
                i += rowspan-1;                
            }
            
            List<String> info = new ArrayList<>();            
            List<List<String>> teachersList = new ArrayList<>();
            List<List<List<String>>> canListsList = new ArrayList<>();
            List<List<List<String>>> cannotListsList = new ArrayList<>();
            List<List<String>> canList = new ArrayList<>();
            List<List<String>> cannotList = new ArrayList<>();
            List<String> teacher = new ArrayList<>();
            List<String> can = new ArrayList<>();
            List<String> cannot = new ArrayList<>();
            boolean colspan = false;         
            //for all columns in row
            for (int j = 0; j < columns.size(); j++) {
                Element column = columns.get(j);
                
                
                //break because of bug
                if (columns.size() <= 5) break;
                //first 7 columns
                else if (j < 7) {
                    info.add(column.text());
                }
                //break because of empty rows
                else if (columns.size() <= 8) break;                
                else if (j < 15) {
                    teacher.add(column.text());
                    
                    //System.out.print(column.text());
                                        
                }
                //column day of availability
                else if (j==15) {
                    if (column.hasAttr("colspan")) {
                        colspan = true;
                    }
                    else {
                        can.add(column.text());
                    }
                }
                //columns of unavailability
                else if (j > 15 && colspan) {
                    if (!column.hasAttr("colspan")) {
                        cannot.add(column.text());
                    }
                }
                //columns of availability
                else if (j < 20) {
                    can.add(column.text());
                }
                //columns of unavailability
                else {
                    if (!column.hasAttr("colspan")) {
                        cannot.add(column.text());
                    }
                }
                
                //System.out.print(column + "; ");
            }
            
            teachersList.add(teacher);            
            canList.add(can);            
            cannotList.add(cannot);
                        
            int start = i-(rowspan-1)+1;
            //for not completed rows
            for (int j = start; j <= i; j++) {                
                Element r = rows.get(j);
                columns = r.select("td");
                
                int size = columns.size();
                colspan = false;
                //for all columns in row
                for (int k = 0; k < size; k++) {
                    Element column = columns.get(k);
                    //for teacher
                    if (size > 9) {
                        if (k == 0) {
                            canListsList.add(canList);
                            cannotListsList.add(cannotList);
                            canList = new ArrayList<>();
                            cannotList = new ArrayList<>();
                                                 
                            teacher = new ArrayList<>();
                            can = new ArrayList<>();
                            cannot = new ArrayList<>();
                        }
                        //first 8 columns are with info from teacher    
                        if (k < 8)
                            teacher.add(column.text());
                        //9th column is start of teacher's availability
                        else if (k==8) {                            
                            if (column.hasAttr("colspan")) {
                                colspan = true;
                            }
                            else {
                                can.add(column.text());
                            }
                        }
                        //if teacher's availability is empty
                        else if (k > 8 && colspan) {
                            if (!column.hasAttr("colspan")) {
                                cannot.add(column.text());
                            }
                        }
                        //if teacher's availability isn't empty
                        else if (k < 13) {
                            can.add(column.text());
                        }
                        //if teacher's unavailability isn't empy
                        else {
                            if (!column.hasAttr("colspan")) {
                                cannot.add(column.text());
                            }
                        }
                        
                        //last column, save lists
                        if (k == size-1) {
                            teachersList.add(teacher);
                            canList.add(can);
                            cannotList.add(cannot);
                        }
                    }
                    //just terms
                    else {   
                        //create new lists
                        if (k == 0) {                              
                            can = new ArrayList<>();
                            cannot = new ArrayList<>();
                                               
                        }
                        //row has more than 5 column, there is empty availability + unavailability,
                        //or availability + (empty) unavailability
                        if (size >= 5) {
                            //if availability is empty
                            if (k==0) {                                    
                                if (column.hasAttr("colspan")) {
                                    colspan = true;
                                }
                                else {
                                    can.add(column.text());
                                }
                            }
                            //if availability is empty
                            else if (colspan) {
                                cannot.add(column.text());
                            }
                            //if availability isn't empty
                            else if (k < 5){
                                can.add(column.text());
                            }
                            //if teacher's unavailability isn't empy
                            else {
                                if (!column.hasAttr("colspan")) {
                                    cannot.add(column.text());
                                }
                            }
                        }
                        //row has just 4 columns, there is only unavailability 
                        else {

                            if (!column.hasAttr("colspan")) {
                                cannot.add(column.text());
                            }
                            
                        }
                        
                        //last column, save lists
                        if (k == size-1) {  
                            if (can.size() > 0)
                                canList.add(can);
                            if (cannot.size() > 0)
                                cannotList.add(cannot);
                        }
                    }
                    
                }
                
            }
            canListsList.add(canList);
            cannotListsList.add(cannotList);
            
            /*
            info list:
            1. předmět (zkratka)
            2. p pro
            3. pv pro
            4. počet (zřejmě zapsaných)
            5. max (kolik může zapsat)
            6. garant
            7. přednášející
            
            teacher list:
            1. vyučující
            2. typ (přednáška/cvičení)
            3. skupin
            4. kapacita
            5. hodin
            6. týden
            7. místnosti
            8. poznámka
            
            can list:
            1. den
            2. od
            3. do
            4. prio
            5. poznámka
            
            cannot list:
            1. den
            2. od
            3. do
            4. poznámka
            */            
            //empty line
            if (info.size() > 1) {
                
                                
                String courseCode = info.get(0);
                
                //update "kapacita" and "pocet_zapsanych"
                try {
                    Database.UpdateCourses(courseCode, Integer.parseInt(info.get(4)), Integer.parseInt(info.get(3)));                
                }
                catch (NumberFormatException e) {
                    System.out.println("Bad integer parsing (probably importing file with exams instead of lectures): " + e.getMessage());
                    Database.dbSuccess  = false;
                }
                    

                int count = 0;
                for (List<String> tl: teachersList) {
                    
                    //break because of error in inserting
                    if(!Database.dbSuccess )
                        break;
                    
                    if (tl.isEmpty())
                        break;

                    String courseTeacher = tl.get(0);
                    Database.InsertTeacherRequirement(reqID, tl, courseCode, courseTeacher, csvYear, csvSemester);

                    for (List<String> cl: canListsList.get(count)) {
                        //break because of error in inserting
                        if(!Database.dbSuccess )
                            break;
                        
                        if (!cl.isEmpty()) {
                            switch(cl.get(0)) {
                                case "-":
                                    
                                    for (int k = 0; k < 5; k++) {
                                        cl.set(0, dayOfWeekList.get(k));
                                        Database.InsertTeacherAvail(termsID, reqID, cl, courseTeacher);
                                        termsID++;
                                    }
                                break;
                                case "pondělí":
                                    cl.set(0, dayOfWeekList.get(0));
                                    Database.InsertTeacherAvail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "úterý":
                                    cl.set(0, dayOfWeekList.get(1));
                                    Database.InsertTeacherAvail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "středa":
                                    cl.set(0, dayOfWeekList.get(2));
                                    Database.InsertTeacherAvail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "čtvrtek":
                                    cl.set(0, dayOfWeekList.get(3));
                                    Database.InsertTeacherAvail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "pátek":
                                    cl.set(0, dayOfWeekList.get(4));
                                    Database.InsertTeacherAvail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                            }                                                        
                        }
                    }
                    for (List<String> cl: cannotListsList.get(count)) {
                        //break because of error in inserting
                        if(!Database.dbSuccess )
                            break;
                        
                        if (!cl.isEmpty()) {
                            switch(cl.get(0)) {
                                case "-":
                                    
                                    for (int k = 0; k < 5; k++) {
                                        cl.set(0, dayOfWeekList.get(k));
                                        Database.InsertTeacherUnavail(termsID, reqID, cl, courseTeacher);
                                        termsID++;
                                    }
                                break;
                                case "pondělí":
                                    cl.set(0, dayOfWeekList.get(0));
                                    Database.InsertTeacherUnavail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "úterý":
                                    cl.set(0, dayOfWeekList.get(1));
                                    Database.InsertTeacherUnavail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "středa":
                                    cl.set(0, dayOfWeekList.get(2));
                                    Database.InsertTeacherUnavail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "čtvrtek":
                                    cl.set(0, dayOfWeekList.get(3));
                                    Database.InsertTeacherUnavail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                                case "pátek":
                                    cl.set(0, dayOfWeekList.get(4));
                                    Database.InsertTeacherUnavail(termsID, reqID, cl, courseTeacher);
                                    termsID++;
                                break;
                            }
                        }
                    } 
                    reqID++;
                    count++;                          
                }  
            }               
        }
        
        if (!Database.dbSuccess ){
            Database.conn.rollback();            
            System.out.println("ROLLBACK");
        }
                

        /* Message with error */
        if (!Database.dbSuccess ) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "There was some error at inserting - rollback. " +
                    "For more information see console output.");
            FacesContext.getCurrentInstance().addMessage(null, message);    
        }     
        /* Or message with successful upload */
        else {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() 
                    + " is uploaded.");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
        }
        
        Database.conn.setAutoCommit(true);
        
    }
    
    /**
     * Import html file with teacher's requirements for exams
     * @throws IOException 
     */
    private void uploadExam() throws IOException, SQLException {
        InputStream is = file.getInputstream();        
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "ISO-8859-2"));
        StringBuilder strBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = br.readLine()) != null) {
            strBuilder.append(inputStr);              
        }            
        br.close();
        is.close();
        Document doc = Jsoup.parse(strBuilder.toString());
        Element table = doc.select("table").last();
        Elements rows = table.select("tr");
            
        /* DB CONNECTION */
        Database.dbSuccess  = true;
         
        Database.conn.setAutoCommit(false);        
        
        int reqID = 1;
        int termsID=1;

        //get IDs
        ResultSet rs = null;
        
        rs = Database.executeQuery("select max(id) from pozadavkyNaZkousky");
        if (rs == null) {
            System.out.println("Could not select id from pozadavkyNaZkousky");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                reqID=rs.getInt(1);
            reqID++;            
        }
        
        rs = Database.executeQuery("select max(id) from terminyPozadavkuNaZkousky");
        if (rs == null) {
            System.out.println("Could not select id from terminyPozadavkuNaZkousky");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                termsID=rs.getInt(1);
            termsID++;
        }
        
        
        //for all rows in table
        for (int i = 2; i < rows.size(); i++) {   
            
            //break because of error in inserting (or in select id)
            if(!Database.dbSuccess )
                break;
            
            Element row = rows.get(i);
            Elements columns = row.select("td");
            Element code = columns.get(0);
            
            //if course's code has rowspan
            int rowspan = 0;
            if (code.hasAttr("rowspan")) {
                rowspan = Integer.parseInt(code.attr("rowspan"));  
                i += rowspan-1;                
            }
            
            List<String> info = new ArrayList<>();            
            List<List<String>> teachersList = new ArrayList<>();
            List<List<List<String>>> canListsList = new ArrayList<>();
            List<List<List<String>>> cannotListsList = new ArrayList<>();
            List<List<String>> canList = new ArrayList<>();
            List<List<String>> cannotList = new ArrayList<>();
            List<String> teacher = new ArrayList<>();
            List<String> can = new ArrayList<>();
            List<String> cannot = new ArrayList<>();
            boolean colspan = false;         
            //for all columns in row
            for (int j = 0; j < columns.size(); j++) {
                Element column = columns.get(j);
                
                //first 3 columns
                if (j < 3) {
                    info.add(column.text());
                }
                //break because of empty rows
                else if (columns.size() <= 4) break;
                else if (j < 6) {
                    teacher.add(column.text());
                    
                    //System.out.print(column.text());
                                        
                }
                //column day of can 
                else if (j==6) {
                    if (column.hasAttr("colspan")) {
                        colspan = true;
                    }
                    else {
                        can.add(column.text());
                    }
                }
                //columns of cannot
                else if (j > 6 && colspan) {
                    if (!column.hasAttr("colspan")) {
                        cannot.add(column.text());
                    }
                }
                //columns of can
                else if (j < 16) {                    
                    can.add(column.text());
                }
                //columns of cannot
                else {
                    if (!column.hasAttr("colspan")) {
                        cannot.add(column.text());
                    }
                }
                
                //System.out.print(column + "; ");
            }
            
            teachersList.add(teacher);            
            canList.add(can);            
            cannotList.add(cannot);
                        
            int start = i-(rowspan-1)+1;
            //for not completed rows
            for (int j = start; j <= i; j++) {                
                Element r = rows.get(j);
                columns = r.select("td");
                
                int size = columns.size();
                colspan = false;
                //for all columns in row
                for (int k = 0; k < size; k++) {
                    Element column = columns.get(k);
                    
                    
                    //for teacher
                    if (size == 5 || (size == 10 && columns.get(3).hasAttr("colspan")) || size == 14 || size == 19) {
                        if (k == 0) {
                            canListsList.add(canList);
                            cannotListsList.add(cannotList);
                            canList = new ArrayList<>();
                            cannotList = new ArrayList<>();
                                                 
                            teacher = new ArrayList<>();
                            can = new ArrayList<>();
                            cannot = new ArrayList<>();
                        }
                        //teacher's info    
                        if (k < 3)
                            teacher.add(column.text());
                        //first availability column
                        else if (k==3) {                            
                            if (column.hasAttr("colspan")) {
                                colspan = true;
                            }
                            else {
                                can.add(column.text());
                            }
                        }
                        //unavailability, if availability is empty
                        else if (k > 3 && colspan) {
                            if (!column.hasAttr("colspan")) {
                                cannot.add(column.text());
                            }
                        }
                        //availability columns
                        else if (k < 13) {
                            can.add(column.text());
                        }
                        //unavailability columns
                        else {
                            if (!column.hasAttr("colspan")) {
                                cannot.add(column.text());
                            }
                        }
                        
                        if (k == size-1) {
                            teachersList.add(teacher);
                            canList.add(can);
                            cannotList.add(cannot);
                        }
                    }
                    //just for terms
                    else {   
                        //first column, reset list
                        if (k == 0) {                                
                            can = new ArrayList<>();
                            cannot = new ArrayList<>();                                               
                        }
                        //teacher's availability isn't empty
                        if (size >= 10) {
                            if (k==0) {                                    
                                if (column.hasAttr("colspan")) {
                                    colspan = true;
                                }
                                else {
                                    can.add(column.text());
                                }
                            }      
                            //availability is empty
                            else if (colspan) {
                                cannot.add(column.text());
                            }
                            //availability columns
                            else if (k < 10){
                                can.add(column.text());                                
                            }
                            //unavailability columns
                            else if (!column.hasAttr("colspan")){                                
                                cannot.add(column.text());
                            }
                        }
                        //unavailability
                        else {
                                
                            if (!column.hasAttr("colspan")) {
                                cannot.add(column.text());
                            }
                            
                        }
                        
                        if (k == size-1) {  
                            if (can.size() > 0)
                                canList.add(can);
                            if (cannot.size() > 0)
                                cannotList.add(cannot);
                        }
                    }
                    
                }
                
            }
            canListsList.add(canList);
            cannotListsList.add(cannotList);
            
             /*
            info list:
            1. předmět (zkratka)            
            2. garant
            3. stud
            
            teacher list:
            1. zkoušející
            2. rozsaz
            3. poznámka            
            
            can list:
            1. termín
            2. datum od
            3. datum do
            4. den
            5. čas od
            6. čas do
            7. hod
            8. počet kol
            9. pref. mistnost
            10. poznámka
            
            cannot list:
            1. datum od
            2. datum do
            3. den 
            4. čas od
            5. čas do
            6. poznámka
            */         
            
            /* connection to db */
            int count = 0;                    
            for (List<String> tl: teachersList) {
                //break because of error in inserting
                if(!Database.dbSuccess )
                    break;
                
                if (tl.isEmpty())
                    break;

                String courseTeacher = tl.get(0);
                Database.InsertExamsRequirement(tl, info.get(0), reqID, courseTeacher, csvYear, csvSemester);

                for (List<String> cl: canListsList.get(count)) {
                    //break because of error in inserting
                    if(!Database.dbSuccess )
                        break;
                    
                    if (!cl.isEmpty()) {
                        Database.InsertTeacherExamAvail(termsID, reqID, cl, courseTeacher);
                        termsID++;
                    }
                }
                for (List<String> cl: cannotListsList.get(count)) {
                    //break because of error in inserting
                    if(!Database.dbSuccess )
                        break;
                    
                    if (!cl.isEmpty()) {
                        Database.InsertTeacherExamUnavail(termsID, reqID, cl, courseTeacher);
                        termsID++;
                    }
                } 
                reqID++;
                count++;            
            }              
        }
        
        if (!Database.dbSuccess ){
            Database.conn.rollback();            
            System.out.println("ROLLBACK");
        }
                

        /* Message with error */
        if (!Database.dbSuccess ) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "There was some error at inserting - rollback. " +
                    "For more information see console output.");
            FacesContext.getCurrentInstance().addMessage(null, message);    
        }     
        /* Or message with successful upload */
        else {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() 
                    + " is uploaded.");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
        }
        
        Database.conn.setAutoCommit(true);
        
    }        
    
    private void importCollisions() throws IOException, SQLException {
        InputStream is = file.getInputstream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "ISO-8859-2"));
        StringBuilder strBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = br.readLine()) != null) {            
            strBuilder.append(inputStr);              
        } 
        br.close();
        is.close();
                
        Document doc = Jsoup.parse(strBuilder.toString());
        Element table = doc.select("table").last();
        Elements rows = table.select("tr");
        
        Database.dbSuccess  = true;
        /* DB CONNECTION */
         
        Database.conn.setAutoCommit(false);

        
        int colID = 1;
        
        
        //get ID
        ResultSet rs = null;
        
        rs = Database.executeQuery("select max(id) from kolize");
        if (rs == null) {
            System.out.println("Could not select id from pozadavkyNaRozvrh");
            Database.dbSuccess  = false;            
        }
        else {
            while(rs.next())
                colID=rs.getInt(1);
            colID++;            
        }
            
        List<String> coursesList = new ArrayList<>();
        
        Element row = rows.get(0);
        Elements columns = row.select("th");     
        int csize = columns.size();
        for (int i = 2; i < csize; i++) {
            coursesList.add(columns.get(i).text());            
        }
        
        for (int i = 2; i < csize; i++) {
            columns = rows.get(i).children();
            
            String course = columns.get(0).text();
            for (int j = 2; j < csize; j++) {
                
                //break because of error in inserting
                if(!Database.dbSuccess )
                    break;
                
                String count = columns.get(j).text();
                if (!count.equals("-") /*&& !columns.get(j).text().equals("0")*/ ) {                    
                    colID = Database.InsertCollision(colID, course, coursesList.get(j-2), csvYear, csvSemester, Integer.parseInt(count));                    
                }
                                
            }
            
        }
        
        if (!Database.dbSuccess ){
            Database.conn.rollback();            
            System.out.println("ROLLBACK");
        }
                

        /* Message with error */
        if (!Database.dbSuccess ) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "There was some error at inserting - rollback. " +
                    "For more information see console output.");
            FacesContext.getCurrentInstance().addMessage(null, message);    
        }     
        /* Or message with successful upload */
        else {
            FacesMessage message = new FacesMessage("Successful", file.getFileName() 
                    + " is uploaded.");                
            FacesContext.getCurrentInstance().addMessage(null, message); 
        }
        
        Database.conn.setAutoCommit(true);
        
    }
    
}
