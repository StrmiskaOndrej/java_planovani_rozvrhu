/*
 * authors:  Miroslav Pospíšil
 * project: Schedule planner
 */
package team_xpospi73.gja.schedule_planner.presentation;

import java.util.ArrayList;
import java.util.List;
 
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
 
import team_xpospi73.gja.schedule_planner.model.Customer;
 
/**
 * Dummy class for basic view to functionality of Java Web project with PrimeFaces, Spring, JSF, ...
 * Class represents Presenter which should deal with program logic. 
 * Managing data transfer between views (Web Pages) and Model classes, controlling which view should be displayed and so on.
 */
@ManagedBean
public class CustomerBean {
    private List<Customer> customers;
 
    /**
     * Get list of Customers
     * @return List customers
     */
    public List<Customer> getCustomers() {
        return customers;
    }
 
    /**
     * Explicit constructor which sets dummy customers
     */
    @PostConstruct
    public void setup()  {
        List<Customer> customers = new ArrayList<Customer>();
 
        Customer customer1 = new Customer();
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setCustomerId(123456);
 
        customers.add(customer1);
 
        Customer customer2 = new Customer();
        customer2.setFirstName("Adam");
        customer2.setLastName("Scott");
        customer2.setCustomerId(98765);

        customers.add(customer2);
 
        Customer customer3 = new Customer();
        customer3.setFirstName("Jane");
        customer3.setLastName("Doe");
        customer3.setCustomerId(65432);
 
        customers.add(customer3);
        this.customers = customers;
    }
}