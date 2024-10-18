package com.nighthawk.spring_portfolio.mvc.plinko;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/plinko")
public class PlinkoApiController {

    @Autowired
    private PlinkoJpaRepository repository;

    // GET List of all Plinko simulations
    @GetMapping("/")
    public ResponseEntity<List<Plinko>> getAllSimulations() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    // POST to simulate a new Plinko game
    @PostMapping("/simulate")
    public ResponseEntity<Plinko> simulatePlinkoGame(@RequestBody PlinkoSimulationRequest request) {
        // Create a new Plinko entity and simulate a game
        Plinko newGame = new Plinko();
        int finalScore = newGame.simulatePlinko(request.getStartX(), request.getHeight());

        // Optionally store the result in the database
        // If you want to store the final score in Plinko, you'll need to add a field for it
        // newGame.setFinalScore(finalScore); // Uncomment if you have such a method

        repository.save(newGame);

        // Add the final score to the response (if you have a field to store it)
        return new ResponseEntity<>(newGame, HttpStatus.CREATED);
    }

    // GET to retrieve a specific simulation by ID
    @GetMapping("/{id}")
    public ResponseEntity<Plinko> getSimulationById(@PathVariable long id) {
        Optional<Plinko> optional = repository.findById(id);
        if (optional.isPresent()) {
            return new ResponseEntity<>(optional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

// Class to represent the request body for Plinko simulation
class PlinkoSimulationRequest {
    private int startX;
    private int height;

    // Getters and setters
    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
