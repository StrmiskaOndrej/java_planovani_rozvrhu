/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.io.Serializable;

/**
 *  Abstract application controller for common properties and methods
 */
public abstract class ApplicationController implements Serializable {
    // Context holders
    private int semesterId;
    private int subjectId;
    private boolean isLogged = false;
    
    // Common variables
    private String versionName;
    
    /**
     * Get actual semester
     * @return int Semester id
     */
    public int getSemesterId() {
        return semesterId;
    }
 
    /**
     * Set actual semester
     * @param semesterId Semester id
     */
    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }
    
    /**
     * Get actual subject
     * @return int Subject id
     */
    public int getSubject() {
        return subjectId;
    }
 
    /**
     * Set actual subject
     * @param subjectId Subject code
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
    
    /**
     * Get version name.
     * @return Version name.
     */
    public String getVersionName() {
        return versionName;
    }
    
    /**
     * Set version name
     * @param versionName Version name 
     */
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    
    /**
     * Get information about logged/unlogged user
     * @return boolean True if user is logged in, False otherwise
     */
    public boolean getIsLoggedStatus() {
        return isLogged;
    }
 
    /**
     * Set logged user status
     * @param isLogged True if user is logged in, False otherwise
     */
    public void setIsLoggedStatus(boolean isLogged) {
        this.isLogged = isLogged;
    }
    
    /**
     * Converting day name to related day number
     * @param dateName Name of the day in the week
     * @return Day number
     */
    public int dayNameToDayNumber(String dateName) {
        switch(dateName){
            case "Po":  return 1;
            case "Út":  return 2;
            case "St":  return 3;
            case "Čt":  return 4;
            case "Pá":  return 5;
            default:    return 1;
        }
    }
    
    /**
     * Converting day number to related day name
     * @param dayNumber Number of the day in the week
     * @return Day name
     */
    public String dayNumberToDayName(int dayNumber) {
        switch(dayNumber) {
            case 1:     return "Po";
            case 2:     return "Út";
            case 3:     return "St";
            case 4:     return "Čt";
            case 5:     return "Pá";
            default:    return "Po";
        }
    }
    
    /**
     * Printing message
     * @param msg Message for printing
     */
    public static void printMsg(Object msg) {
        System.out.println(String.valueOf(msg));
    }
}
