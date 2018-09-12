/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import javax.faces.bean.ManagedBean;

/**
 * Presenter for controlling page moves
 */
@ManagedBean(name="MovesBean")
public class MovesBean {
    
    /**
     * Function for moving to lectures page
     * @return String Name of view
     */
    public String moveToLecturesPage() {
        return "lectures";
    }

    /**
     * Function for moving to exams page
     * @return String Name of viewK
     */
    public String moveToExamsPage() {
        return "exams";
    }

    /**
     * Function for moving to import page
     * @return String Name of view
     */
    public String moveToImportPage() {
        return "import";
    }
    
    /**
     * Function for moving to export page
     * @return String Name of view
     */
    public String moveToExportPage() {
        return "export";
    }

    /**
     * Function for moving to index page
     * @return String Name of view
     */
    public String moveToIndex() {
        return "index";
    }
}
