/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents semester
 */
public class Semester {
    // Schedules
    public List<Version> scheduleVersion = new ArrayList<>();
    
    // Exams
    public List<Version> examsVersion = new ArrayList<>();
    
    // Both
    public int semestrId;
    public String obdobi;
    public int rok;
    public String akademickyRok;
    public String zkouskoveOd;
    public String zkouskoveDo;

    public List<Version> getScheduleVersion() {
        return scheduleVersion;
    }

    public void setScheduleVersion(List<Version> scheduleVersion) {
        this.scheduleVersion = scheduleVersion;
    }

    public List<Version> getExamsVersion() {
        return examsVersion;
    }

    public void setExamsVersion(List<Version> examsVersion) {
        this.examsVersion = examsVersion;
    }

    public int getSemestrId() {
        return semestrId;
    }

    public void setSemestrId(int semestrId) {
        this.semestrId = semestrId;
    }

    public String getObdobi() {
        return obdobi;
    }

    public void setObdobi(String obdobi) {
        this.obdobi = obdobi;
    }

    public int getRok() {
        return rok;
    }

    public void setRok(int rok) {
        this.rok = rok;
    }

    public String getAkademickyRok() {
        return akademickyRok;
    }

    public void setAkademickyRok(String akademickyRok) {
        this.akademickyRok = akademickyRok;
    }

    public String getZkouskoveOd() {
        return zkouskoveOd;
    }

    public void setZkouskoveOd(String zkouskoveOd) {
        this.zkouskoveOd = zkouskoveOd;
    }

    public String getZkouskoveDo() {
        return zkouskoveDo;
    }

    public void setZkouskoveDo(String zkouskoveDo) {
        this.zkouskoveDo = zkouskoveDo;
    }
    
    
}
