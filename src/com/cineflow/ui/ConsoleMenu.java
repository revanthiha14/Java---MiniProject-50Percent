package com.cineflow.ui;

import com.cineflow.exception.BudgetExceededException;
import com.cineflow.model.Actor;
import com.cineflow.model.CrewMember;
import com.cineflow.model.Expense;
import com.cineflow.model.Movie;
import com.cineflow.model.Person;
import com.cineflow.model.ProductionHouse;
import com.cineflow.model.Scene;
import com.cineflow.service.MovieManager;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Text-based console user interface for CineFlow (Phase 1 / 50% Demonstration).
 * Demonstrates:
 * - OOP Principles: Encapsulation, Inheritance, Polymorphism, Abstraction
 * - Aggregation & Composition: ProductionHouse -> Movie -> Scenes, Cast/Crew, Expenses
 * - Exception Handling: BudgetExceededException, IOException
 * - Collections: ArrayList, HashMap
 * - File I/O: Text file persistence
 */
public class ConsoleMenu {
    private final MovieManager manager;
    private final Scanner scanner;

    public ConsoleMenu(MovieManager manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printHeader();
            System.out.println(" 1. Production House Management");
            System.out.println(" 2. Movie Management");
            System.out.println(" 3. Scene Management");
            System.out.println(" 4. Cast & Crew Management");
            System.out.println(" 5. Budget & Expense Management");
            System.out.println(" 6. Save Data");
            System.out.println(" 7. Load Data");
            System.out.println(" 0. Exit");
            System.out.println("==================================================");
            System.out.print("Enter choice (0-7): ");

            int choice = readInt();
            System.out.println();

            switch (choice) {
                case 1 -> manageProductionHouses();
                case 2 -> manageMovies();
                case 3 -> manageScenes();
                case 4 -> manageCastAndCrew();
                case 5 -> manageBudgetAndExpenses();
                case 6 -> {
                    saveData();
                    pause();
                }
                case 7 -> {
                    loadData();
                    pause();
                }
                case 0 -> {
                    System.out.println("Saving current state before exiting...");
                    try {
                        manager.saveData();
                        System.out.println("Data saved successfully.");
                    } catch (IOException e) {
                        System.err.println("Could not auto-save data: " + e.getMessage());
                    }
                    System.out.println("Thank you for using CineFlow! Goodbye.");
                    running = false;
                }
                default -> {
                    System.out.println("Invalid option. Please choose between 0 and 7.");
                    pause();
                }
            }
        }
    }

    private void printHeader() {
        System.out.println("\n==================================================");
        System.out.println("         CINEFLOW - FILM PRODUCTION SYSTEM        ");
        System.out.println("==================================================");
        ProductionHouse studio = manager.getActiveStudio();
        Movie movie = manager.getActiveMovie();

        String studioName = (studio != null) ? studio.getName() + " [" + studio.getId() + "]" : "None Selected";
        String movieTitle = (movie != null)
                ? movie.getTitle() + " [" + movie.getId() + "]"
                : "None Selected";

        System.out.println(" Active Studio : " + studioName);
        System.out.println(" Active Movie  : " + movieTitle);
        System.out.println("--------------------------------------------------");
    }

    // =========================================================================
    // 1. PRODUCTION HOUSE MANAGEMENT
    // =========================================================================
    private void manageProductionHouses() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- PRODUCTION HOUSE MANAGEMENT ---");
            System.out.println(" 1. View Production Houses");
            System.out.println(" 2. Switch Active Production House");
            System.out.println(" 3. Add Production House");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("Enter choice (0-3): ");

            int choice = readInt();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    viewProductionHouses();
                    pause();
                }
                case 2 -> {
                    switchProductionHouse();
                    pause();
                }
                case 3 -> {
                    addProductionHouse();
                    pause();
                }
                case 0 -> inMenu = false;
                default -> {
                    System.out.println("Invalid option. Please choose between 0 and 3.");
                    pause();
                }
            }
        }
    }

    private void viewProductionHouses() {
        List<ProductionHouse> studios = manager.getAllStudios();
        if (studios.isEmpty()) {
            System.out.println("No production houses registered.");
            return;
        }
        ProductionHouse active = manager.getActiveStudio();
        System.out.println("--- REGISTERED PRODUCTION HOUSES ---");
        for (int i = 0; i < studios.size(); i++) {
            ProductionHouse ph = studios.get(i);
            String mark = (active != null && ph.getId().equalsIgnoreCase(active.getId())) ? " * [ACTIVE]" : "";
            System.out.printf(" %d. %s%s%n", (i + 1), ph.toString(), mark);
        }
    }

    private void switchProductionHouse() {
        viewProductionHouses();
        System.out.print("\nEnter Studio ID to switch to: ");
        String id = readLine();
        if (id.isEmpty()) {
            System.out.println("Operation cancelled.");
            return;
        }
        if (manager.setActiveStudioId(id)) {
            System.out.println("Active Studio switched to: " + manager.getActiveStudio().getName());
            Movie activeMovie = manager.getActiveMovie();
            if (activeMovie != null) {
                System.out.println("Active Movie set to: " + activeMovie.getTitle());
            } else {
                System.out.println("No movies in this studio yet.");
            }
        } else {
            System.out.println("Error: Production House with ID '" + id + "' not found.");
        }
    }

    private void addProductionHouse() {
        System.out.println("--- ADD NEW PRODUCTION HOUSE ---");
        System.out.print("Enter Studio ID (e.g., PH-104): ");
        String id = readLine();
        if (id.isEmpty()) {
            System.out.println("Studio ID cannot be empty.");
            return;
        }
        if (manager.getStudio(id) != null) {
            System.out.println("Studio with ID '" + id + "' already exists!");
            return;
        }
        System.out.print("Enter Studio Name: ");
        String name = readLine();
        if (name.isEmpty()) {
            System.out.println("Studio Name cannot be empty.");
            return;
        }
        System.out.print("Enter Headquarters: ");
        String hq = readLine();

        ProductionHouse ph = new ProductionHouse(id, name, hq);
        manager.addStudio(ph);
        System.out.println("Production House '" + name + "' registered successfully!");
    }

    // =========================================================================
    // 2. MOVIE MANAGEMENT
    // =========================================================================
    private void manageMovies() {
        boolean inMenu = true;
        while (inMenu) {
            ProductionHouse studio = manager.getActiveStudio();
            String studioName = (studio != null) ? studio.getName() : "None";
            System.out.println("\n--- MOVIE MANAGEMENT (Studio: " + studioName + ") ---");
            System.out.println(" 1. View Movies (Active Studio)");
            System.out.println(" 2. View All Movies (All Studios)");
            System.out.println(" 3. Switch Active Movie");
            System.out.println(" 4. Add Movie");
            System.out.println(" 5. Search Movies (by ID, Title, Genre)");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("Enter choice (0-5): ");

            int choice = readInt();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    viewMoviesInActiveStudio();
                    pause();
                }
                case 2 -> {
                    viewAllMovies();
                    pause();
                }
                case 3 -> {
                    switchActiveMovie();
                    pause();
                }
                case 4 -> {
                    addNewMovie();
                    pause();
                }
                case 5 -> {
                    searchMovies();
                    pause();
                }
                case 0 -> inMenu = false;
                default -> {
                    System.out.println("Invalid option. Please choose between 0 and 5.");
                    pause();
                }
            }
        }
    }

    private void viewMoviesInActiveStudio() {
        ProductionHouse studio = manager.getActiveStudio();
        if (studio == null) {
            System.out.println("Please select a studio first.");
            return;
        }
        System.out.println("--- MOVIES IN " + studio.getName().toUpperCase() + " ---");
        List<Movie> movies = studio.getMovies();
        if (movies.isEmpty()) {
            System.out.println("No movies found under this studio.");
            return;
        }
        Movie activeMovie = manager.getActiveMovie();
        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            String mark = (activeMovie != null && m.getId().equalsIgnoreCase(activeMovie.getId())) ? " * [ACTIVE]" : "";
            System.out.printf(" %d. %s%s%n", (i + 1), m.toString(), mark);
        }
    }

    private void viewAllMovies() {
        List<Movie> all = manager.getAllMovies();
        if (all.isEmpty()) {
            System.out.println("No movies found in any studio.");
            return;
        }
        Movie activeMovie = manager.getActiveMovie();
        System.out.println("--- ALL MOVIES ACROSS ALL STUDIOS ---");
        for (int i = 0; i < all.size(); i++) {
            Movie m = all.get(i);
            String mark = (activeMovie != null && m.getId().equalsIgnoreCase(activeMovie.getId())) ? " * [ACTIVE]" : "";
            System.out.printf(" %d. %s%s%n", (i + 1), m.toString(), mark);
        }
    }

    private void switchActiveMovie() {
        viewMoviesInActiveStudio();
        System.out.print("\nEnter Movie ID to switch (or press Enter to cancel): ");
        String id = readLine();
        if (!id.isEmpty()) {
            if (manager.setActiveMovieId(id)) {
                System.out.println("Active Movie switched to: " + manager.getActiveMovie().getTitle());
            } else {
                System.out.println("Error: Movie with ID '" + id + "' not found.");
            }
        }
    }

    private void addNewMovie() {
        ProductionHouse studio = manager.getActiveStudio();
        if (studio == null) {
            System.out.println("Please select a studio first.");
            return;
        }
        System.out.println("--- ADD NEW MOVIE TO " + studio.getName().toUpperCase() + " ---");
        System.out.print("Enter Movie ID (e.g., MOV-05): ");
        String id = readLine();
        if (id.isEmpty()) {
            System.out.println("Movie ID cannot be empty.");
            return;
        }
        if (manager.findMovieAnywhere(id) != null) {
            System.out.println("Movie with ID '" + id + "' already exists!");
            return;
        }
        System.out.print("Enter Movie Title: ");
        String title = readLine();
        if (title.isEmpty()) {
            System.out.println("Movie Title cannot be empty.");
            return;
        }
        System.out.print("Enter Genre: ");
        String genre = readLine();
        System.out.print("Enter Director: ");
        String director = readLine();
        System.out.print("Enter Release Year: ");
        int year = readInt();
        System.out.print("Enter Total Allocated Budget ($): ");
        double budget = readDouble();

        // In Phase 1, status defaults to PLANNING cleanly without prompt
        Movie movie = new Movie(id, title, genre, director, year, budget);
        manager.addMovieToActiveStudio(movie);
        System.out.println("Movie '" + title + "' added successfully to " + studio.getName() + "!");
    }

    private void searchMovies() {
        System.out.println("--- SEARCH MOVIES ---");
        System.out.println(" 1. Search by Movie ID or Title Keyword");
        System.out.println(" 2. Filter by Genre");
        System.out.println(" 0. Back");
        System.out.print("Enter choice (0-2): ");

        int type = readInt();
        if (type == 0) return;

        List<Movie> results;
        if (type == 1) {
            System.out.print("Enter Movie ID or Title keyword: ");
            String query = readLine();
            results = manager.searchMovies(query, "");
        } else if (type == 2) {
            System.out.print("Enter Genre (e.g., Sci-Fi, Thriller, Action): ");
            String genre = readLine();
            results = manager.searchMovies("", genre);
        } else {
            System.out.println("Invalid search choice.");
            return;
        }

        if (results.isEmpty()) {
            System.out.println("\nNo movies matched your search criteria.");
            return;
        }

        System.out.println("\nSearch Results (" + results.size() + " movie(s) found):");
        for (int i = 0; i < results.size(); i++) {
            Movie m = results.get(i);
            System.out.printf(" %d. %s%n", (i + 1), m.toString());
        }

        System.out.print("\nEnter Movie ID to set as Active Movie (or press Enter to skip): ");
        String selId = readLine();
        if (!selId.isEmpty()) {
            if (manager.setActiveMovieId(selId)) {
                System.out.println("Active movie switched to: " + manager.getActiveMovie().getTitle());
            } else {
                System.out.println("Movie ID '" + selId + "' not found.");
            }
        }
    }

    // =========================================================================
    // 3. SCENE MANAGEMENT
    // =========================================================================
    private void manageScenes() {
        Movie movie = manager.getActiveMovie();
        if (movie == null) {
            System.out.println("No active movie selected. Please select a movie first.");
            return;
        }

        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- SCENE MANAGEMENT (Movie: " + movie.getTitle() + ") ---");
            System.out.println(" 1. View Scenes");
            System.out.println(" 2. Add Scene");
            System.out.println(" 3. Update Scene Status");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("Enter choice (0-3): ");

            int choice = readInt();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    viewScenes(movie);
                    pause();
                }
                case 2 -> {
                    addScene(movie);
                    pause();
                }
                case 3 -> {
                    updateSceneStatus(movie);
                    pause();
                }
                case 0 -> inMenu = false;
                default -> {
                    System.out.println("Invalid option. Please choose between 0 and 3.");
                    pause();
                }
            }
        }
    }

    private void viewScenes(Movie movie) {
        List<Scene> scenes = movie.getScenes();
        if (scenes.isEmpty()) {
            System.out.println("No scenes recorded for '" + movie.getTitle() + "'.");
            return;
        }
        System.out.println("--- SCENE LIST FOR: " + movie.getTitle().toUpperCase() + " ---");
        for (Scene sc : scenes) {
            System.out.println(" " + sc);
        }
    }

    private void addScene(Movie movie) {
        System.out.print("Enter Scene Number: ");
        int sceneNum = readInt();
        System.out.print("Enter Scene Title: ");
        String title = readLine();
        System.out.print("Enter Location / Setting: ");
        String location = readLine();
        System.out.print("Enter Status (PLANNED / IN_PROGRESS / COMPLETED): ");
        String status = readLine().toUpperCase();
        if (status.isEmpty()) status = "PLANNED";

        movie.addScene(new Scene(sceneNum, title, location, status));
        System.out.println("Scene #" + sceneNum + " added successfully!");
    }

    private void updateSceneStatus(Movie movie) {
        List<Scene> scenes = movie.getScenes();
        if (scenes.isEmpty()) {
            System.out.println("No scenes to update for this movie.");
            return;
        }
        viewScenes(movie);
        System.out.print("\nEnter Scene Number to update: ");
        int sceneNum = readInt();
        Scene target = null;
        for (Scene sc : scenes) {
            if (sc.getSceneNumber() == sceneNum) {
                target = sc;
                break;
            }
        }
        if (target == null) {
            System.out.println("Scene #" + sceneNum + " not found.");
        } else {
            System.out.println("Current status: " + target.getStatus());
            System.out.print("Enter new status (PLANNED / IN_PROGRESS / COMPLETED): ");
            String newStatus = readLine().toUpperCase();
            if (!newStatus.isEmpty()) {
                target.setStatus(newStatus);
                System.out.println("Scene #" + sceneNum + " updated to " + newStatus);
            }
        }
    }

    // =========================================================================
    // 4. CAST AND CREW MANAGEMENT (POLYMORPHISM & INHERITANCE)
    // =========================================================================
    private void manageCastAndCrew() {
        Movie movie = manager.getActiveMovie();
        if (movie == null) {
            System.out.println("No active movie selected. Please select a movie first.");
            return;
        }

        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- CAST & CREW (Movie: " + movie.getTitle() + ") ---");
            System.out.println(" 1. View Cast and Crew");
            System.out.println(" 2. Add Actor");
            System.out.println(" 3. Add Crew Member");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("Enter choice (0-3): ");

            int choice = readInt();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    viewCastAndCrew(movie);
                    pause();
                }
                case 2 -> {
                    addActor(movie);
                    pause();
                }
                case 3 -> {
                    addCrewMember(movie);
                    pause();
                }
                case 0 -> inMenu = false;
                default -> {
                    System.out.println("Invalid option. Please choose between 0 and 3.");
                    pause();
                }
            }
        }
    }

    private void viewCastAndCrew(Movie movie) {
        List<Person> personnel = movie.getCastAndCrew();
        if (personnel.isEmpty()) {
            System.out.println("No cast or crew added yet for '" + movie.getTitle() + "'.");
            return;
        }
        System.out.println("--- CAST & CREW LIST: " + movie.getTitle().toUpperCase() + " ---");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("  %-8s %-22s | %-26s | %s%n", "ID", "Name", "Role/Specialization", "Email");
        System.out.println("----------------------------------------------------------------------------------");
        for (Person p : personnel) {
            p.displayInfo();
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    private void addActor(Movie movie) {
        System.out.print("Enter Person ID (e.g. ACT-105): ");
        String id = readLine();
        System.out.print("Enter Actor's Name: ");
        String name = readLine();
        System.out.print("Enter Email: ");
        String email = readLine();
        System.out.print("Enter Character Role: ");
        String charName = readLine();

        movie.addPerson(new Actor(id, name, email, charName));
        System.out.println("Actor '" + name + "' added successfully!");
    }

    private void addCrewMember(Movie movie) {
        System.out.print("Enter Person ID (e.g. CRW-205): ");
        String id = readLine();
        System.out.print("Enter Crew Member's Name: ");
        String name = readLine();
        System.out.print("Enter Email: ");
        String email = readLine();
        System.out.print("Enter Department (e.g. Camera, Sound, VFX, Directing): ");
        String dept = readLine();

        movie.addPerson(new CrewMember(id, name, email, dept));
        System.out.println("Crew Member '" + name + "' added successfully!");
    }

    // =========================================================================
    // 5. BUDGET & EXPENSES (CUSTOM EXCEPTION HANDLING)
    // =========================================================================
    private void manageBudgetAndExpenses() {
        Movie movie = manager.getActiveMovie();
        if (movie == null) {
            System.out.println("No active movie selected. Please select a movie first.");
            return;
        }

        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- BUDGET & EXPENSES (Movie: " + movie.getTitle() + ") ---");
            System.out.println(" 1. View Budget Overview & Expenses");
            System.out.println(" 2. Add Expense");
            System.out.println(" 3. Set Movie Budget");
            System.out.println(" 0. Back to Main Menu");
            System.out.print("Enter choice (0-3): ");

            int choice = readInt();
            System.out.println();

            switch (choice) {
                case 1 -> {
                    viewBudgetAndExpenses(movie);
                    pause();
                }
                case 2 -> {
                    addExpense(movie);
                    pause();
                }
                case 3 -> {
                    setMovieBudget(movie);
                    pause();
                }
                case 0 -> inMenu = false;
                default -> {
                    System.out.println("Invalid option. Please choose between 0 and 3.");
                    pause();
                }
            }
        }
    }

    private void viewBudgetAndExpenses(Movie movie) {
        System.out.println("==================================================");
        System.out.println("         BUDGET OVERVIEW & EXPENSES               ");
        System.out.println("==================================================");
        System.out.println(" Movie           : " + movie.getTitle() + " [" + movie.getId() + "]");
        System.out.printf (" Allocated Budget: $%,12.2f%n", movie.getBudget());
        System.out.printf (" Total Expenses  : $%,12.2f%n", movie.getTotalExpenses());
        System.out.printf (" Remaining Budget: $%,12.2f (%.1f%% spent)%n",
                movie.getRemainingBudget(), movie.getBudgetUtilizationPercentage());
        System.out.println("--------------------------------------------------");

        List<Expense> expenses = movie.getExpenses();
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
        } else {
            System.out.println("Expense Records (" + expenses.size() + "):");
            for (Expense exp : expenses) {
                System.out.println("  " + exp);
            }
        }
        System.out.println("==================================================");
    }

    private void addExpense(Movie movie) {
        System.out.println("--- ADD NEW EXPENSE ---");
        System.out.printf("Current Remaining Budget: $%,.2f%n", movie.getRemainingBudget());
        System.out.print("Enter Expense ID (e.g. EXP-04): ");
        String id = readLine();
        System.out.print("Enter Category (e.g. VFX, Equipment, Costumes, Logistics): ");
        String cat = readLine();
        System.out.print("Enter Amount ($): ");
        double amount = readDouble();
        System.out.print("Enter Description: ");
        String desc = readLine();

        Expense exp = new Expense(id, cat, amount, desc);

        try {
            movie.addExpense(exp);
            System.out.println("\nExpense added successfully!");
            System.out.printf("Updated Total Expenses  : $%,.2f%n", movie.getTotalExpenses());
            System.out.printf("Updated Remaining Budget: $%,.2f%n", movie.getRemainingBudget());
        } catch (BudgetExceededException e) {
            System.err.println("\n[BUDGET EXCEEDED ERROR] " + e.getMessage());
            System.out.println("The expense was NOT added due to insufficient funds.");
        }
    }

    private void setMovieBudget(Movie movie) {
        System.out.println("--- SET MOVIE BUDGET ---");
        System.out.printf("Current Allocated Budget: $%,.2f%n", movie.getBudget());
        System.out.printf("Current Total Expenses  : $%,.2f%n", movie.getTotalExpenses());
        System.out.print("Enter New Allocated Budget ($): ");
        double newBudget = readDouble();

        if (newBudget <= 0) {
            System.out.println("Budget must be greater than zero.");
            return;
        }

        if (newBudget < movie.getTotalExpenses()) {
            System.out.printf("Warning: New budget ($%,.2f) is less than existing expenses ($%,.2f).%n",
                    newBudget, movie.getTotalExpenses());
        }

        movie.setBudget(newBudget);
        System.out.printf("Budget updated successfully to: $%,.2f%n", movie.getBudget());
        System.out.printf("Updated Remaining Budget      : $%,.2f%n", movie.getRemainingBudget());
    }

    // =========================================================================
    // 6 & 7. FILE HANDLING (SAVE / LOAD)
    // =========================================================================
    private void saveData() {
        System.out.println("Saving all production data to TXT files in data/ directory...");
        try {
            manager.saveData();
            System.out.println("Data saved successfully!");
            System.out.println("Files updated: data/studios.txt, movies.txt, scenes.txt, cast_crew.txt, expenses.txt");
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        System.out.println("Loading production data from TXT files in data/ directory...");
        try {
            manager.loadData();
            System.out.println("Data loaded successfully from text files!");
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    // =========================================================================
    // HELPER INPUT METHODS
    // =========================================================================
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    private int readInt() {
        while (true) {
            if (!scanner.hasNextLine()) {
                return 0;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid integer. Please enter a number: ");
            }
        }
    }

    private double readDouble() {
        while (true) {
            if (!scanner.hasNextLine()) {
                return 0.0;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Please enter a valid amount: ");
            }
        }
    }

    private void pause() {
        if (!scanner.hasNextLine()) {
            return;
        }
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /* =========================================================================
     * PHASE-2 FEATURES (PRESERVED - UNCOMMENT / ACTIVATE IN PHASE 2)
     * The following features are preserved for Phase 2:
     * 1. Movie Status / Lifecycle Management (PLANNING -> PRODUCTION -> POST_PRODUCTION -> RELEASED)
     * 2. Movie Statistics / Overview Dashboard
     * 3. Audience & Critic Review System (Ratings 1-5 & Comments)
     * 4. Rating-based Movie Filtering
     * =========================================================================
     *
     * private void updateMovieStatus() {
     *     Movie movie = manager.getActiveMovie();
     *     if (movie == null) return;
     *     System.out.println("--- UPDATE MOVIE STATUS ---");
     *     System.out.println("Current Status: " + movie.getStatus());
     *     System.out.println("1. PLANNING | 2. PRODUCTION | 3. POST_PRODUCTION | 4. RELEASED");
     *     int c = readInt();
     *     switch (c) {
     *         case 1 -> movie.setStatus(MovieStatus.PLANNING);
     *         case 2 -> movie.setStatus(MovieStatus.PRODUCTION);
     *         case 3 -> movie.setStatus(MovieStatus.POST_PRODUCTION);
     *         case 4 -> movie.setStatus(MovieStatus.RELEASED);
     *     }
     * }
     *
     * private void viewMovieStatistics() {
     *     Movie movie = manager.getActiveMovie();
     *     if (movie == null) return;
     *     System.out.println("=== MOVIE STATISTICS & OVERVIEW ===");
     *     System.out.println("Status         : " + movie.getStatus());
     *     System.out.println("Average Rating : " + movie.getAverageRating() + " / 5.0 Stars");
     *     System.out.println("Total Reviews  : " + movie.getReviews().size());
     * }
     *
     * private void addMovieReview() {
     *     Movie movie = manager.getActiveMovie();
     *     if (movie == null) return;
     *     System.out.print("Enter Reviewer Name: ");
     *     String name = readLine();
     *     System.out.print("Enter Rating (1-5): ");
     *     int r = readInt();
     *     System.out.print("Enter Comment: ");
     *     String comment = readLine();
     *     movie.addReview(new Review(name, r, comment));
     * }
     *
     * private void viewMovieReviews() {
     *     Movie movie = manager.getActiveMovie();
     *     if (movie == null) return;
     *     for (Review r : movie.getReviews()) {
     *         System.out.println(" " + r);
     *     }
     * }
     * ========================================================================= */
}
