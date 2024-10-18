package com.nighthawk.spring_portfolio.mvc.plinko;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.Random;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Plinko {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Method to simulate a Plinko drop and calculate the final score
    public int simulatePlinko(int start_x, int height) {
        Random random = new Random();
        int x = start_x;

        for (int y = 0; y < height; y++) {
            // Randomly decide whether the ball moves left (-1) or right (+1)
            x += random.nextBoolean() ? 1 : -1;

            // Ensure x stays within bounds (for example, 0 to y to form a triangle)
            if (x < 0) {
                x = 0;
            } else if (x > y) {
                x = y;
            }

            // Log the peg hit
            System.out.println("Hit peg at (" + x + ", " + y + ")");
        }

        // Example: Final x position determines the score (mapped to some predefined scores)
        int finalScore = calculateFinalScore(x);
        System.out.println("Final position: (" + x + ", " + height + ") with score: " + finalScore);

        return finalScore;
    }

    // Placeholder for final score calculation based on final x position
    private int calculateFinalScore(int final_x) {
        // Example scoring system (this would correspond to your multiplier slots)
        int[] scoreTable = {33, 11, 4, 2, 1, 0, 1, 2, 4, 11, 33};
        if (final_x >= 0 && final_x < scoreTable.length) {
            return scoreTable[final_x];
        }
        return 0;  // Default score if out of bounds
    }
}
