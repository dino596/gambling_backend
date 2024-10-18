package com.nighthawk.spring_portfolio.mvc.userdb;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Convert;
import static jakarta.persistence.FetchType.EAGER;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Userdb is a POJO, Plain Old Java Object.
 * --- @Data is Lombox annotation for @Getter @Setter @ToString @EqualsAndHashCode @RequiredArgsConstructor
 * --- @AllArgsConstructor is Lombox annotation for a constructor with all arguments
 * --- @NoArgsConstructor is Lombox annotation for a constructor with no arguments
 * --- @Entity annotation is used to mark the class as a persistent Java class.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Convert(attributeName ="userdb", converter = JsonType.class)
public class Userdb {

    /** automatic unique identifier for Userdb record
     * --- Id annotation is used to specify the identifier property of the entity.
     * ----GeneratedValue annotation is used to specify the primary key generation strategy to use.
     * ----- The strategy is to have the persistence provider pick an appropriate strategy for the particular database.
     * ----- GenerationType.AUTO is the default generation type and it will pick the strategy based on the used database.
     */ 
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Many to Many relationship with UserdbRole
     * --- @ManyToMany annotation is used to specify a many-to-many relationship between the entities.
     * --- FetchType.EAGER is used to specify that data must be eagerly fetched, meaning that it must be loaded immediately.
     * --- Collection is a root interface in the Java Collection Framework, in this case it is used to store UserdbRole objects.
     * --- ArrayList is a resizable array implementation of the List interface, allowing all elements to be accessed using an integer index.
     * --- UserdbRole is a POJO, Plain Old Java Object. 
     */
    @ManyToMany(fetch = EAGER)
    private Collection<UserdbRole> roles = new ArrayList<>();

    /** email, password, roles are key attributes to login and authentication
     * --- @NotEmpty annotation is used to validate that the annotated field is not null or empty, meaning it has to have a value.
     * --- @Size annotation is used to validate that the annotated field is between the specified boundaries, in this case greater than 5.
     * --- @Email annotation is used to validate that the annotated field is a valid email address.
     * --- @Column annotation is used to specify the mapped column for a persistent property or field, in this case unique and email.
     */
    @NotEmpty
    @Size(min=5)
    @Column(unique=true)
    @Email
    private String email;

    @NotEmpty
    private String password;

    /** name, dob are attributes to describe the userdb
     * --- @NonNull annotation is used to generate a constructor with AllArgsConstructor Lombox annotation.
     * --- @Size annotation is used to validate that the annotated field is between the specified boundaries, in this case between 2 and 30 characters.
     * --- @DateTimeFormat annotation is used to declare a field as a date, in this case the pattern is specified as yyyy-MM-dd.
     */ 
    @NonNull
    @Size(min = 2, max = 30, message = "Name (2 to 30 chars)")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    /** stats is used to store JSON for daily stat$
     * --- @JdbcTypeCode annotation is used to specify the JDBC type code for a column, in this case json.
     * --- @Column annotation is used to specify the mapped column for a persistent property or field, in this case columnDefinition is specified as jsonb.
     * * * Example of JSON data:
        "stats": {
            "2022-11-13": {
                "calories": 2200,
                "steps": 8000
            }
        }
    */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,Map<String, Object>> stats = new HashMap<>(); 
    

    /** Custom constructor for Userdb when building a new Userdb object from an API call
     */
    public Userdb(String email, String password, String name, Date dob, UserdbRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.roles.add(role);
    }

    /** Custom getter to return age from dob attribute
    */
    public int getAge() {
        if (this.dob != null) {
            LocalDate birthDay = this.dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return Period.between(birthDay, LocalDate.now()).getYears(); }
        return -1;
    }

    /** 1st telescoping method to create a Userdb object with USER role
     * @param name
     * @param email
     * @param password
     * @param dob
     * @return Userdb
     *  */ 
    public static Userdb createUserdb(String name, String email, String password, String dob) {
        // By default, Spring Security expects roles to have a "ROLE_" prefix.
        return createUserdb(name, email, password, dob, Arrays.asList("ROLE_USER"));
    }
    /** 2nd telescoping method to create a Userdb object with parameterized roles
     * @param roles 
     */
    public static Userdb createUserdb(String name, String email, String password, String dob, List<String> roleNames) {
        Userdb userdb = new Userdb();
        userdb.setName(name);
        userdb.setEmail(email);
        userdb.setPassword(password);
        try {
            Date date = new SimpleDateFormat("MM-dd-yyyy").parse(dob);
            userdb.setDob(date);
        } catch (Exception e) {
            // handle exception
        }
    
        List<UserdbRole> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            UserdbRole role = new UserdbRole(roleName);
            roles.add(role);
        }
        userdb.setRoles(roles);
    
        return userdb;
    }
   
    /** Static method to initialize an array list of Userdb objects 
     * @return Userdb[], an array of Userdb objects
     */
    public static Userdb[] init() {
        ArrayList<Userdb> userdbs = new ArrayList<>();
        userdbs.add(createUserdb("Thomas Edison", "toby@gmail.com", "123toby", "01-01-1840", Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_TESTER")));
        userdbs.add(createUserdb("Alexander Graham Bell", "lexb@gmail.com", "123lex", "01-01-1847"));
        userdbs.add(createUserdb("Nikola Tesla", "niko@gmail.com", "123niko", "01-01-1850"));
        userdbs.add(createUserdb("Madam Currie", "madam@gmail.com", "123madam", "01-01-1860"));
        userdbs.add(createUserdb("Grace Hopper", "hop@gmail.com", "123hop", "12-09-1906"));
        userdbs.add(createUserdb("John Mortensen", "jm1021@gmail.com", "123Qwerty!", "10-21-1959", Arrays.asList("ROLE_ADMIN")));
        return userdbs.toArray(new Userdb[0]);
    }

    /** Static method to print Userdb objects from an array
     * @param args, not used
     */
    public static void main(String[] args) {
        // obtain Userdb from initializer
        Userdb userdbs[] = init();

        // iterate using "enhanced for loop"
        for( Userdb userdb : userdbs) {
            System.out.println(userdb);  // print object
        }
    }

}