package com.cineflow;

import com.cineflow.exception.BudgetExceededException;
import com.cineflow.model.Actor;
import com.cineflow.model.CrewMember;
import com.cineflow.model.Expense;
import com.cineflow.model.Movie;
import com.cineflow.model.Person;
import com.cineflow.model.ProductionHouse;
import com.cineflow.model.Scene;
import com.cineflow.service.FileService;
import com.cineflow.service.MovieManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Automated Verification Test Suite for Phase 1 / 50% Demonstration.
 * Verifies all 6 core Phase-1 features and exception handling.
 */
public class Phase1Verification {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   CINEFLOW - PHASE 1 / 50% DEMO SYSTEM VERIFICATION SUITE     ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 7;

        try {
            // Test 1: Production House Management
            testProductionHouseManagement();
            passed++;
            System.out.println("[PASS] 1. Production House Management");

            // Test 2: Movie Management (Add, View, Switch, Search)
            testMovieManagement();
            passed++;
            System.out.println("[PASS] 2. Movie Management");

            // Test 3: Scene Management (Add, View, Update status)
            testSceneManagement();
            passed++;
            System.out.println("[PASS] 3. Scene Management");

            // Test 4: Cast & Crew Management (Polymorphism & Inheritance)
            testCastAndCrewManagement();
            passed++;
            System.out.println("[PASS] 4. Cast & Crew Management");

            // Test 5: Budget & Expense Management
            testBudgetAndExpenses();
            passed++;
            System.out.println("[PASS] 5. Budget & Expense Management");

            // Test 6: Custom Exception (BudgetExceededException)
            testBudgetExceededException();
            passed++;
            System.out.println("[PASS] 6. BudgetExceededException Handling");

            // Test 7: File Handling (Save & Load Data)
            testFileHandling();
            passed++;
            System.out.println("[PASS] 7. File Handling (Save & Load Persistence)");

        } catch (Throwable e) {
            System.err.println("[FAIL] Verification encountered an error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("===============================================================");
        System.out.printf("Verification Results: %d / %d Tests Passed%n", passed, total);
        System.out.println("===============================================================");

        if (passed == total) {
            System.out.println("ALL PHASE 1 FEATURES VERIFIED SUCCESSFULLY!");
        } else {
            System.exit(1);
        }
    }

    private static void testProductionHouseManagement() {
        MovieManager manager = new MovieManager();
        ProductionHouse newPh = new ProductionHouse("PH-999", "A24 Films", "New York, NY");
        manager.addStudio(newPh);

        assert manager.getStudio("PH-999") != null : "Studio was not added!";
        assert manager.setActiveStudioId("PH-999") : "Failed to switch active studio!";
        assert manager.getActiveStudio().getName().equals("A24 Films") : "Active studio mismatch!";
    }

    private static void testMovieManagement() {
        MovieManager manager = new MovieManager();
        ProductionHouse ph = manager.getActiveStudio();
        int initialCount = ph.getMovies().size();

        // Add movie
        Movie movie = new Movie("MOV-99", "Dune: Part Three", "Sci-Fi", "Denis Villeneuve", 2027, 2000000.0);
        manager.addMovieToActiveStudio(movie);
        assert ph.getMovies().size() == initialCount + 1 : "Movie count did not increase!";

        // View / Switch
        assert manager.setActiveMovieId("MOV-99") : "Failed to switch active movie!";
        assert manager.getActiveMovie().getId().equals("MOV-99") : "Active movie mismatch!";

        // Search by ID/title
        List<Movie> searchById = manager.searchMovies("MOV-99", "");
        assert !searchById.isEmpty() && searchById.get(0).getTitle().contains("Dune") : "Search by ID failed!";

        List<Movie> searchByTitle = manager.searchMovies("Dune", "");
        assert !searchByTitle.isEmpty() : "Search by title failed!";

        // Search by genre
        List<Movie> searchByGenre = manager.searchMovies("", "Sci-Fi");
        assert !searchByGenre.isEmpty() : "Search by genre failed!";

        // Verify clean Phase 1 toString
        String str = movie.toString();
        assert !str.contains("Status:") && !str.contains("Stars") : "Movie toString should not expose Phase 2 status/ratings in Phase 1: " + str;
    }

    private static void testSceneManagement() {
        Movie movie = new Movie("MOV-T1", "Test Film", "Drama", "Director X", 2026, 100000.0);
        movie.addScene(new Scene(1, "Opening Scene", "Studio Lot", "PLANNED"));
        movie.addScene(new Scene(2, "Climax", "Desert", "IN_PROGRESS"));

        assert movie.getScenes().size() == 2 : "Scenes count mismatch!";
        assert movie.getScenes().get(0).getStatus().equals("PLANNED") : "Scene 1 status incorrect!";

        // Update scene status
        movie.getScenes().get(0).setStatus("COMPLETED");
        assert movie.getScenes().get(0).getStatus().equals("COMPLETED") : "Scene status update failed!";
    }

    private static void testCastAndCrewManagement() {
        Movie movie = new Movie("MOV-T2", "Test Film 2", "Action", "Director Y", 2026, 200000.0);
        Actor actor = new Actor("ACT-901", "Tom Hardy", "tom@agency.com", "Max");
        CrewMember crew = new CrewMember("CRW-901", "Roger Deakins", "roger@cam.com", "Cinematography");

        movie.addPerson(actor);
        movie.addPerson(crew);

        assert movie.getCastAndCrew().size() == 2 : "Cast & Crew count mismatch!";
        // Polymorphism check
        Person p1 = movie.getCastAndCrew().get(0);
        Person p2 = movie.getCastAndCrew().get(1);
        assert p1.getRole().contains("Actor") : "Actor role polymorphism check failed!";
        assert p2.getRole().contains("Crew") : "Crew role polymorphism check failed!";
    }

    private static void testBudgetAndExpenses() throws BudgetExceededException {
        Movie movie = new Movie("MOV-T3", "Budget Test", "Thriller", "Director Z", 2026, 100000.0);
        assert movie.getBudget() == 100000.0 : "Budget mismatch!";
        assert movie.getRemainingBudget() == 100000.0 : "Initial remaining budget mismatch!";

        // Add expense within budget
        movie.addExpense(new Expense("EXP-01", "Props", 20000.0, "Vintage car"));
        assert movie.getTotalExpenses() == 20000.0 : "Total expenses mismatch!";
        assert movie.getRemainingBudget() == 80000.0 : "Remaining budget mismatch!";

        // Set / update budget
        movie.setBudget(150000.0);
        assert movie.getBudget() == 150000.0 : "Updated budget mismatch!";
        assert movie.getRemainingBudget() == 130000.0 : "Remaining budget after update mismatch!";
    }

    private static void testBudgetExceededException() {
        Movie movie = new Movie("MOV-T4", "Exception Test", "Horror", "Director H", 2026, 50000.0);
        boolean exceptionThrown = false;
        try {
            // Attempt to add expense exceeding 50,000 budget
            movie.addExpense(new Expense("EXP-EX", "CGI Monster", 60000.0, "High end CGI"));
        } catch (BudgetExceededException e) {
            exceptionThrown = true;
            assert e.getMessage().contains("exceeds remaining budget") : "Exception message content mismatch!";
        }
        assert exceptionThrown : "BudgetExceededException was NOT thrown when expense exceeded budget!";
    }

    private static void testFileHandling() throws IOException {
        String testDir = "data_test";
        File dir = new File(testDir);
        if (!dir.exists()) dir.mkdirs();

        FileService testService = new FileService(testDir);
        MovieManager manager = new MovieManager();

        // Save
        testService.saveAllData(manager.getAllStudios());

        // Check file exists
        File f = new File(testDir, "movies.txt");
        assert f.exists() && f.length() > 0 : "movies.txt was not written properly!";

        // Load
        List<ProductionHouse> loaded = testService.loadAllData();
        assert !loaded.isEmpty() : "Loaded studios list is empty!";
        assert !loaded.get(0).getMovies().isEmpty() : "Loaded movies list is empty!";

        // Cleanup test directory
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) file.delete();
        }
        dir.delete();
    }
}
