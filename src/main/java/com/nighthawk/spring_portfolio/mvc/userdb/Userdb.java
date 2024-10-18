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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(fetch = EAGER)
    private Collection<UserdbRole> roles = new ArrayList<>();

    @NotEmpty
    @Size(min=5)
    @Column(unique=true)
    @Email
    private String email;

    @NotEmpty
    private String password;

    @NonNull
    private Integer credits;

    @NonNull
    @Size(min = 2, max = 30, message = "Name (2 to 30 chars)")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,Map<String, Object>> stats = new HashMap<>(); 
    

    public Userdb(String email, String password, String name, Date dob, UserdbRole role, Integer credits) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dob = dob;
        this.roles.add(role);
        this.credits = credits;
    }

    public int getAge() {
        if (this.dob != null) {
            LocalDate birthDay = this.dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return Period.between(birthDay, LocalDate.now()).getYears(); }
        return -1;
    }

    public static Userdb createUserdb(String name, String email, String password, String dob, Integer credits) {
        return createUserdb(name, email, password, dob, Arrays.asList("ROLE_USER"), credits);
    }

    public static Userdb createUserdb(String name, String email, String password, String dob, List<String> roleNames, Integer credits) {
        Userdb userdb = new Userdb();
        userdb.setName(name);
        userdb.setEmail(email);
        userdb.setPassword(password);
        userdb.setCredits(credits);
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

    public static Userdb[] init() {
        ArrayList<Userdb> userdbs = new ArrayList<>();
        userdbs.add(createUserdb("Thomas Edison", "toby@gmail.com", "123toby", "01-01-1840", Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_TESTER"), 100));
        userdbs.add(createUserdb("Alexander Graham Bell", "lexb@gmail.com", "123lex", "01-01-1847", 200));
        userdbs.add(createUserdb("Nikola Tesla", "niko@gmail.com", "123niko", "01-01-1850", 150));
        userdbs.add(createUserdb("Madam Currie", "madam@gmail.com", "123madam", "01-01-1860", 180));
        userdbs.add(createUserdb("Grace Hopper", "hop@gmail.com", "123hop", "12-09-1906", 220));
        userdbs.add(createUserdb("John Mortensen", "jm1021@gmail.com", "123Qwerty!", "10-21-1959", Arrays.asList("ROLE_ADMIN"), 300));
        return userdbs.toArray(new Userdb[0]);
    }

    public static void main(String[] args) {
        Userdb userdbs[] = init();
        for( Userdb userdb : userdbs) {
            System.out.println(userdb);
        }
    }
}
