/*
 * authors:  Miroslav Pospíšil, Ondřej Strmiska
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.util.ArrayList;
import java.util.List;
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

/**
 * Presenter for controlling schedule planning
 */
@ManagedBean(name="ExamPlannerBean")
@ViewScoped
public class ExamPlannerBean extends ApplicationController {

    private List<Day> exam;
    private List<Day> days;

    private List<Semester> semester;

    private List<SelectItem> semesterListbox;

    private String idExamVersion;
    private String semesterListboxSelection; // USED IN version_form.xhtml
    private String semesterType; // USED IN semester_wizard.xhtml
    private String semesterYear; // USED IN semester_wizard.xhtml
    private String semesterAdacemicYear;
    private String semesterExamsFrom;
    private String semesterExamsTo;
    private String versionSemester; // USED IN version_wizard.xhtml
    private String versionName; // USED IN version_wizard.xhtml

 
    /**
     * Constructor for get schedule
     */
    @PostConstruct
    public void init() {
        Database.connectDB();
    
        try {
            this.exam = Database.getExamSchedule(0, "2016-01-01", "2016-02-01");
        }
        catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            System.out.println("Exception in loading exam: " + e.getMessage());

            this.exam = new ArrayList<>();
        }
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

        
        convertDaysSubjectsToDaysRooms();
//        convertDaysRoomsToDaysSubjects();
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
        System.out.println("velikost = "+exam.size());
        for(int i = 0; i < exam.size(); i++) {      // dny
            Day day = exam.get(i);
            day.mistnost = new ArrayList<>();
            
            List<Room> fitRooms = getRooms();
                                    
            for(int fitRoom = 0; fitRoom < fitRooms.size(); fitRoom++) {
                for(int h = 7; h < 21; h++) {
                    fitRooms.get(fitRoom).predmet.add(new Subject());
                }
                day.mistnost.add(fitRooms.get(fitRoom));
            }

            for(int j = 0; j < exam.get(i).predmet.size(); j++) {   // predmety
                System.out.println(exam.get(i).predmet.get(j).casyZkousekOd  + ", "+ exam.get(i).predmet.get(j).casyZkousekDo);
                int indexFrom = Integer.parseInt(exam.get(i).predmet.get(j).casyZkousekOd.split(":")[0]);
                int indexTo = Integer.parseInt(exam.get(i).predmet.get(j).casyZkousekDo.split(":")[0]);
        //        System.out.println("den = "+exam.get(i).getNazevDatum()+", předmět = "+exam.get(i).predmet.get(j).getPredmetNazev()+", indexFrom = "+indexFrom+", indexTo = "+indexTo);
                for(int k = 0; k < exam.get(i).predmet.get(j).mistnosti.size(); k++) {  // mistnosti
                    for(int r = 0; r < day.mistnost.size(); r++) {
                        if(day.mistnost.get(r).mistnostId == exam.get(i).predmet.get(j).mistnosti.get(k).mistnostId) {
                            for(int index = indexFrom; index < indexTo; index++) {
                                day.mistnost.get(r).predmet.set((index - 7), exam.get(i).predmet.get(j));
                            }
                        }
                    }
                    
                }
            }
            
            days.add(day);

        }
        
        
        this.days = days;
    }
    
    /**
     * Obrácené převedení
     */
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

                                            day.predmet.get(r).casyPredmetuDo = (subjectTimeTo + 1) + ":00";
                                        }
                                    }
                                    if(!foundRoom) {
                                        day.predmet.get(r).mistnosti.add(days.get(i).mistnost.get(j));
                                    }
                                }
                            }
                        }
                        if(!foundSubject) {
                            Subject subject = days.get(i).mistnost.get(j).predmet.get(k);
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
        
        
        this.exam = schedule;
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
     * Create new schedule version
     * @param event  
     */
    public void createScheduleVersion(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        boolean success = false;

//        if(Schedules.createNewScheduleVersion(versionName)) {
//            success = true;
//            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Úspěch", "Nová verze rozvrhu byla úspěšně vytvořena");
//        } else {
//            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Chyba", "Vytvoření nové verze rozvrhu se nezdařilo.");
//        }
        
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("success", success);
    } 
     
    /**
     * Adding subject to schedule
     * @param event
     */
    public void addSheduleSubject(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean success = false;
        
//        if(Schedules.addSubject(idScheduleVersion, subjectId, date, from, to)) {
//            success = true;
//        }
         
        context.addCallbackParam("success", success);
    }

    /**
     * Přidá semestr
     */
    public void addSemester() {

        //  akademickeObdobi = semesterType
        //  akademickyRok = semesterYear

    }

    /**
     * přidá verzi
     */
    public void addVersion() {

        // versionName
        // versionSemester
    }

    /**
     * Get ID of schedule version
     * @return String ID of schedule version
     */
    public String getIdScheduleVersion() {
        return idExamVersion;
    }

    /**
     * Set ID of schedule version
     * @param idScheduleVersion ID of schedule version
     */
    public void setIdScheduleVersion(String idScheduleVersion) {
        this.idExamVersion = idScheduleVersion;
    }

    /**
     * Get schedule
     * @return List of days in schedule
     */
    public List<Day> getSchedule() {
        return exam;
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

    /**
     * Vrátí seznam zkoušek
     * @return
     */
    public List<Day> getExam() {
        return exam;
    }

    /**
     * Nastaví seznam zkoušek
     * @param exam
     */
    public void setExam(List<Day> exam) {
        this.exam = exam;
    }

    /**
     * Získá ID verze zkoušek
     * @return
     */
    public String getIdExamVersion() {
        return idExamVersion;
    }

    /**
     * Nastaví ID verze zkoušek
     * @param idExamVersion
     */
    public void setIdExamVersion(String idExamVersion) {
        this.idExamVersion = idExamVersion;
    }

    /**
     * Získá akademický rok
     * @return
     */
    public String getSemesterAdacemicYear() {
        return semesterAdacemicYear;
    }

    /**
     * Nastaví akademický rok
     * @param semesterAdacemicYear
     */
    public void setSemesterAdacemicYear(String semesterAdacemicYear) {
        this.semesterAdacemicYear = semesterAdacemicYear;
    }

    /**
     * Vrátí začátek zkouškového období
     * @return
     */
    public String getSemesterExamsFrom() {
        return semesterExamsFrom;
    }

    /**
     * Nastaví začátek zkouškového období
     * @param semesterExamsFrom
     */
    public void setSemesterExamsFrom(String semesterExamsFrom) {
        this.semesterExamsFrom = semesterExamsFrom;
    }

    /**
     * Vrátí konec zkouškového období
     * @return
     */
    public String getSemesterExamsTo() {
        return semesterExamsTo;
    }

    /**
     * Nastaví konec zkouškového období
     * @param semesterExamsTo
     */
    public void setSemesterExamsTo(String semesterExamsTo) {
        this.semesterExamsTo = semesterExamsTo;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
