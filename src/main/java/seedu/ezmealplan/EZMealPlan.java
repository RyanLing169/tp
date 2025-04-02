package seedu.ezmealplan;

import seedu.command.Command;
import seedu.exceptions.EZMealPlanException;
import seedu.food.Meal;
import seedu.logic.MealManager;
import seedu.meallist.Meals;
import seedu.storage.Storage;
import seedu.ui.UserInterface;
import seedu.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class EZMealPlan {
    /**
     * Main entry-point for the java.duke.Duke application.
     */
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {
        String fileName = "EZMealPlan.log";
        setupLogger(fileName);
        UserInterface ui = new UserInterface();
        MealManager mealManager = new MealManager();
        checkConstructedLists(mealManager);
        // Check for valid meals that are present in the user list but not in the main list
        // and add these meals to the main list.
        mealManager.compareLists();
        logger.fine("running EZMealPlan");
        ui.printGreetingMessage();
        String userInput;
        while (true) {
            ui.prompt();
            userInput = ui.readInput();
            // extracts out the command from the user input
            Command command = Parser.parse(userInput);
            executeCommand(command, mealManager, ui);
            if (command.isExit()) {
                break;
            }
        }
        logger.fine("exiting EZMealPlan");
    }

    private static void checkConstructedLists(MealManager mealManager) {
        // Create and load both main meal list (mainList.txt) and user meal list (userList.txt)
        try {
            Storage.createListFiles();
            constructMainList(mealManager);
            constructUserList(mealManager);
        } catch (IOException ioException) {
            System.err.println("Could not load tasks: " + ioException.getMessage());
        }
    }


    private static void constructUserList(MealManager mealManager) throws IOException {
        File userMealFile = Storage.getUserListFile();
        Meals userMeals = mealManager.getUserMeals();
        constructList(mealManager, userMealFile, userMeals);
    }

    private static void constructMainList(MealManager mealManager) throws IOException {
        File mainMealFile = Storage.getMainListFile();
        Meals mainMeals = mealManager.getMainMeals();
        constructList(mealManager, mainMealFile, mainMeals);
    }

    private static void constructList(MealManager mealManager, File selectedFile, Meals selectedMeals)
            throws IOException {
        // Retrieve saved meals from the respective file and append them into the respective Meals class
        // If the file (mainList.txt) is empty, preset meals are appended into the MainMeals class instead.
        List<Meal> mealList = Storage.loadExistingList(selectedFile);
        // Load pre-set meals if the meal list from the main list file is empty.
        if (mealList.isEmpty() && selectedFile.equals(Storage.getMainListFile())) {
            mealList = Storage.loadPresetMeals();
        }
        for (Meal meal : mealList) {
            extractMealIntoList(meal, selectedMeals, mealManager);
        }
    }

    private static void extractMealIntoList(Meal meal, Meals meals, MealManager mealManager) {
        //Throw error message if detected an ingredient with invalid price and skips to the next meal.
        try {
            mealManager.addMeal(meal, meals);
        } catch (EZMealPlanException ezMealPlanException) {
            System.err.println(ezMealPlanException.getMessage());
            System.err.println("The current meal will be skipped.\n");
            logger.info("EZMealPlanException triggered");
        }
    }

    private static void executeCommand(Command command, MealManager mealManager, UserInterface ui) {
        try {
            // Executes the command parsed out
            command.execute(mealManager, ui);
        } catch (EZMealPlanException ezMealPlanException) {
            ui.printErrorMessage(ezMealPlanException);
            logger.info("EZMealPlanException triggered");
        }
    }

    private static void setupLogger(String fileName) {
        LogManager.getLogManager().reset();
        logger.setLevel(Level.ALL);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.WARNING);
        logger.addHandler(consoleHandler);
        createLogFile(fileName);
    }

    private static void createLogFile(String fileName) {
        try {
            FileHandler fileHandler = new FileHandler(fileName, true);
            fileHandler.setLevel(Level.FINE);
            logger.addHandler(fileHandler);
        } catch (IOException ioException) {
            logger.log(Level.WARNING, "File logger is not working.", ioException);
        }
    }
}
