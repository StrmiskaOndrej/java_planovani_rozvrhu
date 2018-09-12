/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.io.IOException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.primefaces.context.RequestContext;

/**
 * Presenter for controlling user's views (login, userSettings, ...)
 */
@ManagedBean(name="UserBean")
public class UserBean extends ApplicationController {
    private final String USER = "admin";
    private final String PASSWORD = "admin";
    
    private String username;     
    private String password;
    
    /**
     * Get user's name
     * @return String User's name
     */
    public String getUsername() {
        return username;
    }
 
    /**
     * Set user's name
     * @param username User's name 
     */
    public void setUsername(String username) {
        this.username = username;
    }
 
    /**
     * Get user's password
     * @return String User's password
     */
    public String getPassword() {
        return password;
    }
 
    /**
     * Set user's password
     * @param password User's password
     */
    public void setPassword(String password) {
        this.password = password;
    }
        
    /**
     * Deal with processing of login event
     * @param event ActionEvent with view's login form
     */
    public void login(ActionEvent event) throws IOException {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
         
        if(username != null && username.equals(USER) && password != null && password.equals(PASSWORD)) {
            setIsLoggedStatus(true);
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Vítejte", username);
        } else {
            setIsLoggedStatus(false);
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Přihlášení se nezdařilo", "Špatné přihlašovací údaje");
        }
         
        FacesContext.getCurrentInstance().addMessage(null, message);
        context.addCallbackParam("loggedIn", getIsLoggedStatus());
    }   
}
