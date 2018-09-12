/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.primefaces.context.RequestContext;
import team_xpospi73.gja.schedule_planner.model.Database;
import team_xpospi73.gja.schedule_planner.model.entity.*;
import static team_xpospi73.gja.schedule_planner.presentation.ApplicationController.printMsg;

/**
 * Presenter for controlling schedule planning
 */
@ManagedBean(name="SchedulePlannerBean")
@ViewScoped
public class SchedulePlannerBean extends ApplicationController {

    private List<Day> schedule;
    private List<Day> days;

    private List<Semester> semester;

    private List<SelectItem> semesterListbox;

    private String idScheduleVersion;
    private String semesterListboxSelection; // USED IN version_form.xhtml
    private String semesterType; // USED IN semester_wizard.xhtml
    private String semesterYear; // USED IN semester_wizard.xhtml
    private String semesterAdacemicYear;
    private String semesterExamsFrom;
    private String semesterExamsTo;
    
    private String versionSemester; // USED IN version_wizard.xhtml
    
    private List<List<String>> weekCollisions;
 
    /**
     * Constructor for get schedule
     */
    @PostConstruct
    public void init() {
        Database.connectDB();
        
        loadSchedule(getActiveScheduleVersionID());
        loadSemesters();       
    }
 
    private int getActiveScheduleVersionID() {
        ResultSet rs = null;
        int id = 0;
        try {
            rs = Database.executeQuery("select max(id) from verzeRozvrhu where je_aktivni_flag = 1");
            if (rs == null) {
                System.out.println("Could not select id from verzeRozvrhu"); 
                return 0;
            }
            else {
                while(rs.next())
                    id=rs.getInt(1);                
            }
        }
        catch (SQLException e) {            
            System.out.println("Exception in loading schedule: " + e.getMessage());            
        }
                
        return id;
    }
    
    /**
     * Loading whole schedule
     * @param schedule 
     */
    private void loadSchedule(int schedule) {
        idScheduleVersion = String.valueOf(schedule);
        
        try {
            this.schedule = Database.getSchedule(schedule);
        }
        catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            System.out.println("Exception in loading schedule: " + e.getMessage());

            this.schedule = new ArrayList<>();
        }
        
        convertDaysSubjectsToDaysRooms();
    }
    
    /**
     * Loading semesters and versions and adding them to listbox
     */
    private void loadSemesters() {
        try {
            this.semester = Database.getSemestry();
        }
        catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            System.out.println("Exception in loading semesters: " + e.getMessage());

            this.semester = new ArrayList<>();
        }

        List<SelectItem> semesterListbox = new ArrayList<>();
        
        for(int i = 0; i < this.semester.size(); i++) {
            SelectItemGroup group1 = new SelectItemGroup(this.semester.get(i).akademickyRok + " " + this.semester.get(i).obdobi);
            SelectItem[] selectItem = new SelectItem[this.semester.get(i).scheduleVersion.size()];
            for(int j = 0; j < this.semester.get(i).scheduleVersion.size(); j++) {
                SelectItem option = new SelectItem(this.semester.get(i).scheduleVersion.get(j).nazev);
                selectItem[j] = option;
            }
            group1.setSelectItems(selectItem);
            semesterListbox.add(group1);
        }

        this.semesterListbox = semesterListbox;
    }
    
    /**
     * Returns list of rooms (may be only for test needs)
     * @return List of Rooms objects
     */
    private List<Room> getRooms() {
        List<Room> fitRooms;
        try {
            fitRooms = Database.getMistnosti();
        }
        catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            System.out.println("Exception in loading Rooms: " + e.getMessage());

            fitRooms = new ArrayList<>();
        }

        return fitRooms;
    }
    
    /**
     * Converting structure of days-subjects-rooms,teachers to days-rooms-subjects
     * (for easily printing to table in front-end)
     */
    public void convertDaysSubjectsToDaysRooms() {
        List<Day> days = new ArrayList<>();
        
        for(int i = 0; i < schedule.size(); i++) {      // dny
            Day day = schedule.get(i);
            day.mistnost = new ArrayList<>();
            
            List<Room> fitRooms = getRooms();
                                    
            for(int fitRoom = 0; fitRoom < fitRooms.size(); fitRoom++) {
                for(int h = 7; h < 21; h++) {
                    fitRooms.get(fitRoom).predmet.add(new Subject());
                }
                day.mistnost.add(fitRooms.get(fitRoom));
            }
            
            for(int j = 0; j < schedule.get(i).predmet.size(); j++) {   // predmety
//                printMsg(schedule.get(i).predmet.get(j).predmetZkratka);
                int indexFrom = Integer.parseInt(schedule.get(i).predmet.get(j).casyPredmetuOd.split(":")[0]);
                int indexTo = Integer.parseInt(schedule.get(i).predmet.get(j).casyPredmetuDo.split(":")[0]);
                
                for(int k = 0; k < schedule.get(i).predmet.get(j).mistnosti.size(); k++) {  // mistnosti
                    for(int r = 0; r < day.mistnost.size(); r++) {
                        if(day.mistnost.get(r).mistnostId == schedule.get(i).predmet.get(j).mistnosti.get(k).mistnostId) {
                            for(int index = indexFrom; index < indexTo; index++) {
                                day.mistnost.get(r).predmet.set((index - 7), schedule.get(i).predmet.get(j));
                            }
                        }
                    }
                    
                }
            }

            days.add(day);
        }
        
//        for(int i = 0; i < days.size(); i++) {
//            System.out.println(days.get(i).denNazev);
//            for(int j = 0; j < days.get(i).mistnost.size(); j++) {
//                System.out.println("-- " + days.get(i).mistnost.get(j).mistnostNazev);
//                String subjects = "---- ";
//                for(int k = 0; k < days.get(i).mistnost.get(j).predmet.size(); k++) {
//                    if(k == days.get(i).mistnost.get(j).predmet.size() - 1) {
//                        subjects += days.get(i).mistnost.get(j).predmet.get(k).predmetZkratka;
//                    }
//                    else {
//                        subjects += days.get(i).mistnost.get(j).predmet.get(k).predmetZkratka + ", ";
//                    }
//                }
//                System.out.println(subjects);
//            }
//        }
        
        this.days = days;
    }
    
    public void convertDaysRoomsToDaysSubjects() {
        List<Day> schedule = new ArrayList<>();
        
        for(int i = 0; i < days.size(); i++) {
            Day day = days.get(i);
            day.predmet = new ArrayList<>();
            
            for(int j = 0; j < days.get(i).mistnost.size(); j++) {   // mistnosti
                
                for(int k = 0; k < days.get(i).mistnost.get(j).predmet.size(); k++) {  // predmety
                    if(days.get(i).mistnost.get(j).predmet.get(k).predmetZkratka != null) {
                        boolean foundSubject = false;
                        for(int r = 0; r < day.predmet.size(); r++) {
                            if(day.predmet.get(r).predmetZkratka.equals(days.get(i).mistnost.get(j).predmet.get(k).predmetZkratka)) {
                                int subjectTimeFrom = Integer.parseInt(day.predmet.get(r).casyPredmetuOd.split(":")[0]);
                                int subjectTimeTo = Integer.parseInt(day.predmet.get(r).casyPredmetuDo.split(":")[0]);
                                int daysSubjectTimeFrom = (k + 7);

                                if((subjectTimeFrom <= daysSubjectTimeFrom) && (subjectTimeTo >= daysSubjectTimeFrom)) {
                                    foundSubject = true;

                                    boolean foundRoom = false;
                                    for(int s = 0; s < day.predmet.get(r).mistnosti.size(); s++) {
                                        if(day.predmet.get(r).mistnosti.get(s).mistnostId == days.get(i).mistnost.get(j).mistnostId) {
                                            foundRoom = true;

                                            day.predmet.get(r).casyPredmetuDo = subjectTimeTo + ":00";
                                        }
                                    }
                                    if(!foundRoom) {
                                        day.predmet.get(r).mistnosti.add(days.get(i).mistnost.get(j));
                                    }
                                }
                            }
                        }
                        if(!foundSubject) {
//                            printMsg(days.get(i).mistnost.get(j).predmet.get(k).idPredmetu);
                            Subject subject = days.get(i).mistnost.get(j).predmet.get(k);
                            subject.predmetId = days.get(i).mistnost.get(j).predmet.get(k).idPredmetu;
                            subject.casyPredmetuOd = (k + 7) + ":00";
                            subject.casyPredmetuDo = (k + 7 + 1) + ":00";
                            subject.mistnosti = new ArrayList<>();
                            subject.mistnosti.add(days.get(i).mistnost.get(j));
                            day.predmet.add(subject);    
                        }
                    }
                }
            }

            schedule.add(day);
        }
        
//        printMsg("--------------------------------------------------------------");
//        printMsg("Subjects: " + schedule.get(0).predmet.size());
//        
//        for(int i = 0; i < schedule.size(); i++) {
//            printMsg(schedule.get(i).denNazev);
//            for(int j = 0; j < schedule.get(i).predmet.size(); j++) {
//                printMsg(schedule.get(i).predmet.get(j).predmetId + " " + schedule.get(i).predmet.get(j).predmetZkratka + ": " + schedule.get(i).predmet.get(j).casyPredmetuOd + " - " + schedule.get(i).predmet.get(j).casyPredmetuDo);
//                for(int k = 0; k < schedule.get(i).predmet.get(j).mistnosti.size(); k++) {
//                    printMsg(schedule.get(i).predmet.get(j).mistnosti.get(k).mistnostNazev); 
//                }
//            }
//        }
        
        this.schedule = schedule;
    }
    
    /**
     * Select semester version
     * @param event
     */
    public void selectSemesterVersion(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        Boolean success = false;
        
        printMsg("Switching schedule version to: " + semesterListboxSelection);
        
        message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "This schedule version does not exists.");
        success = false;
         
        for(int i = 0; i < semester.size(); i++) {
            for(int j = 0; j < semester.get(i).scheduleVersion.size(); j++) {
                if(semesterListboxSelection.equals(semester.get(i).scheduleVersion.get(j).nazev)) {
                    
                    boolean flag = true;
                    try {
                        Database.UpdateScheduleVersion();
                    } catch (SQLException ex) {
                        Logger.getLogger(SchedulePlannerBean.class.getName()).log(Level.SEVERE, null, ex);
                        flag = false;
                    }
                    
                    if (flag) {
                        int tmpID = semester.get(i).scheduleVersion.get(j).verzeId;
                        loadSchedule(tmpID);
                        try {
                           Database.UpdateScheduleVersionA(tmpID);
                        } catch (SQLException ex) {
                            Logger.getLogger(SchedulePlannerBean.class.getName()).log(Level.SEVERE, null, ex);
                            flag = false;
                        }
                        if (flag) {
                            
                            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Success", "Schedule version has been succesfuly loaded.");
                            success = true;
                            
                            context.update("form");
                        }
                    }
                }
            }
        }
        
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("addSemesterSuccess", success);
    } 
    
    /**
     * Save minor working updates
     * @param event
     */
    public void saveScheduleChange(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean success = false;
        
//        if(Schedules.saveChange(idScheduleVersion, subjectId, date, from, to)) {
//            success = true;
//        }
         
        context.addCallbackParam("success", success);
    } 

    /**
     * Save new semester
     * @param event 
     */
    public void addSemester(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        Boolean success = false;
        
        printMsg("Creating new semester by type: " + semesterType + " with year: " + semesterYear);
        if(semesterExamsFrom == null) {
            semesterExamsFrom = "";
        }
        else if(semesterExamsTo == null) {
            semesterExamsTo = "";
        }
        
        try {
            if(Database.insertSemester(semesterType, Integer.parseInt(semesterYear), semesterAdacemicYear, semesterExamsFrom, semesterExamsTo)) {
                message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Success", "Semester has been successfuly created.");
                success = true;
                
                loadSemesters();
                context.update("schedule_version_form");
                context.update("version_form");
            }
            else {
                message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "Semester with these data already exists.");
                success = false;
            }
        }
        catch (SQLException e) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", e.getMessage());
            success = false;
        }
         
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("addSemesterSuccess", success);
    }

    /**
     * Save new semester version.
     * @param event 
     */
    public void addVersion(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        Boolean success = false;
        
        printMsg("Creating new version by semester ID: " + versionSemester + " with name: " + getVersionName());
        
        try {
            if(Database.insertScheduleVersion(Integer.parseInt(versionSemester), getVersionName())) {
                message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Success", "Semester schedule version has been sucessfuly created.");
                success = true;
                
                loadSemesters();
                context.update("schedule_version_form");
            }
            else {
                message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "Semester schedule version with these data already exists.");
                success = false;
            }
        }
        catch (SQLException e) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", e.getMessage());
            success = false;
        }
         
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("addVersionSuccess", success);
    }
    
    public void saveSchedule() {
        printMsg("Saving");
        
        convertDaysRoomsToDaysSubjects();
        try {
            Database.setSchedule(schedule, Integer.parseInt(idScheduleVersion));
        }
        catch (SQLException e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
    
    /**
     * Get ID of schedule version
     * @return String ID of schedule version
     */
    public String getIdScheduleVersion() {
        return idScheduleVersion;
    }

    /**
     * Set ID of schedule version
     * @param idScheduleVersion ID of schedule version
     */
    public void setIdScheduleVersion(String idScheduleVersion) {
        this.idScheduleVersion = idScheduleVersion;
    }

    /**
     * Get schedule
     * @return List of days in schedule
     */
    public List<Day> getSchedule() {
        return schedule;
    }

    /**
     * Get days
     * @return List of formatted days with rooms
     */
    public List<Day> getDays() {
        return days;
    }

    /**
     * Get semester
     * @return List of semesters
     */
    public List<Semester> getSemester() {
        return semester;
    }

    /**
     * Get semesterListbox
     * @return List of semesters for listbox
     */
    public List<SelectItem> getSemesterListbox() {
        return semesterListbox;
    }

    /**
     * Get semesterListboxSelection
     * @return Selection from listbox
     */
    public String getSemesterListboxSelection() {
        return semesterListboxSelection;
    }

    /**
     * Set semesterListboxSelection
     * @param semesterListboxSelection Selection from listbox
     */
    public void setSemesterListboxSelection(String semesterListboxSelection) {
        this.semesterListboxSelection = semesterListboxSelection;
    }

    /**
     * Set semesterType
     * @param semesterType Selection from selectOneMenu
     */
    public void setSemesterType(String semesterType) {
        this.semesterType = semesterType;
    }

    /**
     * Get semesterType
     * @return Selection from selectOneMenu
     */
    public String getSemesterType() {
        return semesterType;
    }

    /**
     * Set semesterYear
     * @param semesterYear input field for new semester
     */
    public void setSemesterYear(String semesterYear) {
        this.semesterYear = semesterYear;
    }

    /**
     * Get semesterYear
     * @return content of input field for new semester
     */
    public String getSemesterYear() {
        return semesterYear;
    }

    /**
     * Set versionSemester
     * @param versionSemester Selection from selectOneMenu
     */
    public void setVersionSemester(String versionSemester) {
        this.versionSemester = versionSemester;
    }

    /**
     * Get versionSemester
     * @return Selection from selectOneMenu
     */
    public String getVersionSemester() {
        return versionSemester;
    }

    public String getSemesterAdacemicYear() {
        return semesterAdacemicYear;
    }

    public void setSemesterAdacemicYear(String semesterAdacemicYear) {
        this.semesterAdacemicYear = semesterAdacemicYear;
    }

    public String getSemesterExamsFrom() {
        return semesterExamsFrom;
    }

    public void setSemesterExamsFrom(String semesterExamsFrom) {
        this.semesterExamsFrom = semesterExamsFrom;
    }

    public String getSemesterExamsTo() {
        return semesterExamsTo;
    }

    public void setSemesterExamsTo(String semesterExamsTo) {
        this.semesterExamsTo = semesterExamsTo;
    }
    
    public List<List<String>> getWeekCollisions() {
        return weekCollisions;
    }
    
    public void setWeekCollisions(List<List<String>> weekCollisions) {
        this.weekCollisions = weekCollisions;
    }
}
