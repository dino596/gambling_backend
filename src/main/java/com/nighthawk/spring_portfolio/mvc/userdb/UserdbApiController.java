package com.nighthawk.spring_portfolio.mvc.userdb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

@RestController
@RequestMapping("/api")
public class UserdbApiController {

    @Autowired
    private UserdbJpaRepository repository;

    @Autowired
    private UserdbDetailsService userdbDetailsService;

    @GetMapping("/userdb")
    public ResponseEntity<Userdb> getUserdb(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();  // Email is mapped/unmapped to username for Spring Security

        Userdb userdb = repository.findByEmail(email);

        if (userdb != null) {
            return new ResponseEntity<>(userdb, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/userdbs")
    public ResponseEntity<List<Userdb>> getUserdbs() {
        return new ResponseEntity<>( repository.findAllByOrderByNameAsc(), HttpStatus.OK);
    }


    @GetMapping("/userdb/{id}")
    public ResponseEntity<Userdb> getUserdb(@PathVariable long id) {
        Optional<Userdb> optional = repository.findById(id);
        if (optional.isPresent()) {
            Userdb userdb = optional.get();
            return new ResponseEntity<>(userdb, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);       
    }

    @DeleteMapping("/userdb/{id}")
    public ResponseEntity<Userdb> deleteUserdb(@PathVariable long id) {
        Optional<Userdb> optional = repository.findById(id);
        if (optional.isPresent()) {
            Userdb userdb = optional.get();
            repository.deleteById(id);
            return new ResponseEntity<>(userdb, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
    }

    @Getter 
    public static class UserdbDto {
        private String email;
        private String password;
        private String name;
        private String dob;
        private Integer credits;
    }

    @PostMapping("/userdb")
    public ResponseEntity<Object> postUserdb(@RequestBody UserdbDto userdbDto) {
        // Validate dob input
        Date dob;
        try {
            dob = new SimpleDateFormat("MM-dd-yyyy").parse(userdbDto.getDob());
        } catch (Exception e) {
            return new ResponseEntity<>(userdbDto.getDob() + " error; try MM-dd-yyyy", HttpStatus.BAD_REQUEST);
        }
        // A userdb object WITHOUT ID will create a new record in the database
        Userdb userdb = new Userdb(userdbDto.getEmail(), userdbDto.getPassword(), userdbDto.getName(), dob, userdbDetailsService.findRole("USER"), userdbDto.getCredits());
        userdbDetailsService.save(userdb);
        return new ResponseEntity<>(userdbDto.getEmail() + " is created successfully", HttpStatus.CREATED);
    }

    /**
     * Search for a Userdb entity by name or email.
     * @param map of a key-value (k,v), the key is "term" and the value is the search term. 
     * @return A ResponseEntity containing a list of Userdb entities that match the search term.
     */
    @PostMapping(value = "/userdbs/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> userdbSearch(@RequestBody final Map<String,String> map) {
        // extract term from RequestEntity
        String term = (String) map.get("term");

        // JPA query to filter on term
        List<Userdb> list = repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);

        // return resulting list and status, error checking should be added
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    /**
     * Adds stats to the Userdb table 
     * @param stat_map is a JSON object, example format:
        {"health":
            {"date": "2021-01-01",
            "measurements":
                {   
                    "weight": "150",
                    "height": "70",
                    "bmi": "21.52"
                }
            }
        }
    *  @return A ResponseEntity containing the Userdb entity with updated stats, or a NOT_FOUND status if not found.
    */
    @PostMapping(value = "/userdb/setStats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Userdb> userdbStats(Authentication authentication, @RequestBody final Map<String,Object> stat_map) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();  // Email is mapped/unmapped to username for Spring Security
    
        // Find a userdb by username
        Optional<Userdb> optional = Optional.ofNullable(repository.findByEmail(email));
        if (optional.isPresent()) {  // Good ID
            Userdb userdb = optional.get();  // value from findByID
    
            // Get existing stats
            Map<String, Map<String, Object>> existingStats = userdb.getStats();
    
            // Iterate through each key in the incoming stats
            for (String key : stat_map.keySet()) {
                // Extract the stats for this key from the incoming stats
                Map<String, Object> incomingStats = (Map<String, Object>) stat_map.get(key);
    
                // Extract the date and attributes from the incoming stats
                String date = (String) incomingStats.get("date");
                Map<String, Object> attributeMap = new HashMap<>(incomingStats);
                attributeMap.remove("date");
    
                // New key test. 
                if (!existingStats.containsKey(key)) { 
                    // Add the new key
                    existingStats.put(key, new HashMap<>());
                }
    
                // Existing date test. 
                if (existingStats.get(key).containsKey(date)) { // Existing date, update the attributes
                    // Make a map inside of existingStats to hold the current attributes for the date
                    Map<String, Object> existingAttributes = (Map<String, Object>) existingStats.get(key).get(date);
                    // Combine the existing attributes with these new attributes 
                    existingAttributes.putAll(attributeMap);
                } else { // New date, add the new date and attributes
                    existingStats.get(key).put(date, attributeMap);
                }
            }
    
            // Set and save the updated stats 
            userdb.setStats(existingStats);
            repository.save(userdb);  // conclude by writing the stats updates to the database
    
            // return Userdb with update to Stats
            return new ResponseEntity<>(userdb, HttpStatus.OK);
        }
        // return Bad ID
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
    }
}
