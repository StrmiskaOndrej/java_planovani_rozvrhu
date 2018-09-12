/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.model;
 
/**
 * Dummy class for basic view to functionality of Java Web project with PrimeFaces, Spring, JSF, ...
 * Class represents Model class which should deal with data, db connection and so on.
 */
public class Customer {
 
    private String firstName;
    private String lastName;
    private Integer customerId; 
 
    /**
     * Get customer's first name
     * @return String First name
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Set customer's  first name
     * @param firstName First name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Get customer's  last name
     * @return String Last name
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Set customer's last name
     * @param lastName Last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Get customer's ID
     * @return Integer ID
     */
    public Integer getCustomerId() {
        return customerId;
    }
    
    /**
     * Set customer's ID
     * @param customerId ID
     */
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
}
