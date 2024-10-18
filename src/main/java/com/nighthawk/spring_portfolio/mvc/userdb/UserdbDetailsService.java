package com.nighthawk.spring_portfolio.mvc.userdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
This class has an instance of Java Persistence API (JPA)
-- @Autowired annotation. Allows Spring to resolve and inject collaborating beans into our bean.
-- Spring Data JPA will generate a proxy instance
-- Below are some CRUD methods that we can use with our database
*/
@Service
@Transactional
public class UserdbDetailsService implements UserDetailsService {  // "implements" ties ModelRepo to Spring Security
    // Encapsulate many object into a single Bean (Userdb, Roles, and Scrum)
    @Autowired  // Inject UserdbJpaRepository
    private UserdbJpaRepository userdbJpaRepository;
    @Autowired  // Inject RoleJpaRepository
    private UserdbRoleJpaRepository userdbRoleJpaRepository;
    @Autowired // Inject PasswordEncoder
    private PasswordEncoder passwordEncoder;

    /* loadUserByUsername Overrides and maps Userdb & Roles POJO into Spring Security */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Userdb userdb = userdbJpaRepository.findByEmail(email); // setting variable user equal to the method finding the username in the database
        if(userdb==null) {
			throw new UsernameNotFoundException("User not found with username: " + email);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        userdb.getRoles().forEach(role -> { //loop through roles
            authorities.add(new SimpleGrantedAuthority(role.getName())); //create a SimpleGrantedAuthority by passed in role, adding it all to the authorities list, list of roles gets past in for spring security
        });
        // train spring security to User and Authorities
        User user = new User(userdb.getEmail(), userdb.getPassword(), authorities);
        return user;
    }

    /* Userdb Section */

    public  List<Userdb>listAll() {
        return userdbJpaRepository.findAllByOrderByNameAsc();
    }

    // custom query to find match to name or email
    public  List<Userdb>list(String name, String email) {
        return userdbJpaRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(name, email);
    }

    // custom query to find anything containing term in name or email ignoring case
    public  List<Userdb>listLike(String term) {
        return userdbJpaRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
    }

    // custom query to find anything containing term in name or email ignoring case
    public  List<Userdb>listLikeNative(String term) {
        String like_term = String.format("%%%s%%",term);  // Like required % rappers
        return userdbJpaRepository.findByLikeTermNative(like_term);
    }

    // encode password prior to sava
    public void save(Userdb userdb) {
        userdb.setPassword(passwordEncoder.encode(userdb.getPassword()));
        userdbJpaRepository.save(userdb);
    }

    public Userdb get(long id) {
        return (userdbJpaRepository.findById(id).isPresent())
                ? userdbJpaRepository.findById(id).get()
                : null;
    }

    public Userdb getByEmail(String email) {
        return (userdbJpaRepository.findByEmail(email));
    }

    public void delete(long id) {
        userdbJpaRepository.deleteById(id);
    }

    public void defaults(String password, String roleName) {
        for (Userdb userdb: listAll()) {
            if (userdb.getPassword() == null || userdb.getPassword().isEmpty() || userdb.getPassword().isBlank()) {
                userdb.setPassword(passwordEncoder.encode(password));
            }
            if (userdb.getRoles().isEmpty()) {
                UserdbRole role = userdbRoleJpaRepository.findByName(roleName);
                if (role != null) { // verify role
                    userdb.getRoles().add(role);
                }
            }
        }
    }

    public  List<UserdbRole>listAllRoles() {
        return userdbRoleJpaRepository.findAll();
    }

    public UserdbRole findRole(String roleName) {
        return userdbRoleJpaRepository.findByName(roleName);
    }

    public void addRoleToUserdb(String email, String roleName) { // by passing in the two strings you are giving the user that certain role
        Userdb userdb = userdbJpaRepository.findByEmail(email);
        if (userdb != null) {   // verify userdb
            UserdbRole role = userdbRoleJpaRepository.findByName(roleName);
            if (role != null) { // verify role
                boolean addRole = true;
                for (UserdbRole roleObj : userdb.getRoles()) {    // only add if user is missing role
                    if (roleObj.getName().equals(roleName)) {
                        addRole = false;
                        break;
                    }
                }
                if (addRole) userdb.getRoles().add(role);   // everything is valid for adding role
            }
        }
    }
    
}