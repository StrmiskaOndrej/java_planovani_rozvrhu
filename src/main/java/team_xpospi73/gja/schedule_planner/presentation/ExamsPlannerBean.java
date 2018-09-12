/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;
 
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.context.RequestContext;
import team_xpospi73.gja.schedule_planner.model.entity.Day;

/**
 * Presenter for controlling planning of exams
 */
@ManagedBean
public class ExamsPlannerBean extends ApplicationController {
    private String idExamsVersion;
    
    private List<Day> exams;
    
    /**
     * Get schedule
     * @return List of days in exams
     */
    public List<Day> getExams() {
        return exams;
    }
 
    /**
     * Constructor for get exams
     */
    @PostConstruct
    public void examsInit()  {
        List<Day> exams = new ArrayList<Day>();
//        List<Day> schedule = Schedules.getSchedule(idScheduleVersion);

        this.exams = exams;
    }
 
    /**
     * Get ID of exams version
     * @return String ID of exams version
     */
    public String getIdExamsVersion() {
        return idExamsVersion;
    }
 
    /**
     * Set ID of exams version
     * @param idExamsVersion ID of exams version
     */
    public void setIdExamsVersion(String idExamsVersion) {
        this.idExamsVersion = idExamsVersion;
    }
    
    /**
     * Save minor working updates
     * @param event
     */
    public void saveExamsChange(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean success = false;
        
//        if(Exams.saveChange(idExamsVersion, subjectId, date, from, to)) {
//            success = true;
//        }
         
        context.addCallbackParam("success", success);
    } 
    
    /**
     * Create new exams version
     * @param event
     */
    public void createExamsVersion(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        boolean success = false;

//        if(Exams.createNewExamsVersion(versionName)) {
//            success = true;
//            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Úspěch", "Nová verze zkoušek byla úspěšně vytvořena");
//        } else {
//            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Chyba", "Vytvoření nové verze zkoušek se nezdařilo.");
//        }
        
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("success", success);
    } 
    
    /**
     * Save minor working updates
     * @param event
     */
    public void addSubjectExam(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean success = false;
        
//        if(Exams.addSubjectExam(idExamsVersion, subjectId, date, from, to)) {
//            success = true;
//        }
         
        context.addCallbackParam("success", success);
    } 
}
