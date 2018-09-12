/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model.entity;

import java.io.Serializable;

/**
 *  Represents subject teachers in week schedule planning
 */
public class Teacher implements Serializable {
    public int ucitelId;
    public String ucitelJmeno;
    public String ucitelEmail;

    public int getUcitelId() {
        return ucitelId;
    }

    public void setUcitelId(int ucitelId) {
        this.ucitelId = ucitelId;
    }

    public String getUcitelJmeno() {
        return ucitelJmeno;
    }

    public void setUcitelJmeno(String ucitelJmeno) {
        this.ucitelJmeno = ucitelJmeno;
    }

    public String getUcitelEmail() {
        return ucitelEmail;
    }

    public void setUcitelEmail(String ucitelEmail) {
        this.ucitelEmail = ucitelEmail;
    }
    
    
}
