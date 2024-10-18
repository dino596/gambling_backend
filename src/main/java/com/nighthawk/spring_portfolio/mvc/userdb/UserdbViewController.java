package com.nighthawk.spring_portfolio.mvc.userdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

// Built using article: https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html
// or similar: https://asbnotebook.com/2020/04/11/spring-boot-thymeleaf-form-validation-example/
@Controller
@RequestMapping("/mvc/userdb")
public class UserdbViewController {
    // Autowired enables Control to connect HTML and POJO Object to database easily for CRUD
    @Autowired
    private UserdbDetailsService repository;

    @GetMapping("/read")
    public String userdb(Model model) {
        List<Userdb> list = repository.listAll();
        model.addAttribute("list", list);
        return "userdb/read";
    }

    /*  The HTML template Forms and UserdbForm attributes are bound
        @return - template for userdb form
        @param - Userdb Class
    */
    @GetMapping("/create")
    public String userdbAdd(Userdb userdb) {
        return "userdb/create";
    }

    /* Gathers the attributes filled out in the form, tests for and retrieves validation error
    @param - Userdb object with @Valid
    @param - BindingResult object
     */
    @PostMapping("/create")
    public String userdbSave(@Valid Userdb userdb, BindingResult bindingResult) {
        // Validation of Decorated UserdbForm attributes
        if (bindingResult.hasErrors()) {
            return "userdb/create";
        }
        repository.save(userdb);
        repository.addRoleToUserdb(userdb.getEmail(), "ROLE_STUDENT");
        // Redirect to next step
        return "redirect:/mvc/userdb/read";
    }

    @GetMapping("/update/{id}")
    public String userdbUpdate(@PathVariable("id") int id, Model model) {
        model.addAttribute("userdb", repository.get(id));
        return "userdb/update";
    }

    @PostMapping("/update")
    public String userdbUpdateSave(@Valid Userdb userdb, BindingResult bindingResult) {
        // Validation of Decorated UserdbForm attributes
        if (bindingResult.hasErrors()) {
            return "userdb/update";
        }
        repository.save(userdb);
        repository.addRoleToUserdb(userdb.getEmail(), "ROLE_STUDENT");

        // Redirect to next step
        return "redirect:/mvc/userdb/read";
    }

    @GetMapping("/delete/{id}")
    public String userdbDelete(@PathVariable("id") long id) {
        repository.delete(id);
        return "redirect:/mvc/userdb/read";
    }

    @GetMapping("/search")
    public String userdb() {
        return "userdb/search";
    }

}