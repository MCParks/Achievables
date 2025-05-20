package us.mcparks.achievables.reference;

import us.mcparks.achievables.Achievables;
import us.mcparks.achievables.dsl.BigalsIntegratedGroovyAchievementLanguage;
import us.mcparks.achievables.dsl.meta.AchievableWithMeta;
import us.mcparks.achievables.events.Event;
import us.mcparks.achievables.framework.Achievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.triggers.AchievableTrigger;
import us.mcparks.achievables.triggers.EventAchievableTrigger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A CLI application for interacting with the Achievables library.
 * Allows registering achievables, submitting events, and testing achievables.
 */
public class AchievablesCLI {
    private static final Logger logger = Logger.getLogger(AchievablesCLI.class.getName());
    private static final ReferenceAchievableManager manager = new ReferenceAchievableManager();
    private static final Map<String, ReferencePlayer> players = new HashMap<>();
    
    public static void main(String[] args) {
        // Initialize the Achievables system with our manager
        Achievables.initialize(manager);
        
        // Register the TestEvent class
        manager.registerEventClass("TestEvent", TestEvent.class);
        
        // Welcome message
        System.out.println("Achievables Reference Implementation CLI");
        System.out.println("Type 'help' for a list of commands");
        
        // Read commands from standard input
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            
            if (line.isEmpty()) {
                continue;
            }
            
            String[] parts = line.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String args1 = parts.length > 1 ? parts[1] : "";
            
            try {
                switch (command) {
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                    case "quit":
                        running = false;
                        break;
                    case "register":
                        registerAchievable(args1);
                        break;
                    case "list":
                        listAchievables();
                        break;
                    case "player":
                        handlePlayerCommand(args1);
                        break;
                    case "event":
                        submitEvent(args1);
                        break;
                    case "status":
                        showStatus(args1);
                        break;
                    case "state":
                        showState(args1);
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println("Type 'help' for a list of commands");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error executing command: " + line, e);
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        System.out.println("Goodbye!");
    }
    
    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help                         - Show this help message");
        System.out.println("  exit, quit                   - Exit the application");
        System.out.println("  register <file>              - Register an achievable from a BIGAL file");
        System.out.println("  list                         - List all registered achievables");
        System.out.println("  player add <id> <name>       - Add a player");
        System.out.println("  player list                  - List all players");
        System.out.println("  player remove <id>           - Remove a player");
        System.out.println("  event <type> <player> <data> - Submit an event");
        System.out.println("  status <player>              - Show achievement status for a player");
        System.out.println("  state <player> <uuid>        - Show state for a player and achievable");
    }
    
    private static void registerAchievable(String fileName) throws IOException {
        if (fileName.isEmpty()) {
            System.out.println("Please specify a file name");
            return;
        }
        
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            return;
        }
        
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        AchievableWithMeta achievableWithMeta = BigalsIntegratedGroovyAchievementLanguage.interpret(content);
        Achievable achievable = achievableWithMeta.getAchievable();
        
        manager.registerAchievable(achievable);
        System.out.println("Registered achievable: " + achievable.getUUID());
        System.out.println("  Name: " + achievableWithMeta.getMeta().getName());
        System.out.println("  Description: " + achievableWithMeta.getMeta().getDescription());
    }
    
    private static void listAchievables() {
        Collection<Achievable> achievables = manager.getAchievables();
        if (achievables.isEmpty()) {
            System.out.println("No achievables registered");
            return;
        }
        
        System.out.println("Registered achievables:");
        for (Achievable achievable : achievables) {
            System.out.println("  " + achievable.getUUID());
        }
    }
    
    private static void handlePlayerCommand(String args) {
        if (args.isEmpty()) {
            System.out.println("Missing player command. Use 'player add', 'player list', or 'player remove'");
            return;
        }
        
        String[] parts = args.split("\\s+", 3);
        String subCommand = parts[0].toLowerCase();
        
        switch (subCommand) {
            case "add":
                if (parts.length < 3) {
                    System.out.println("Usage: player add <id> <name>");
                    return;
                }
                addPlayer(parts[1], parts[2]);
                break;
            case "list":
                listPlayers();
                break;
            case "remove":
                if (parts.length < 2) {
                    System.out.println("Usage: player remove <id>");
                    return;
                }
                removePlayer(parts[1]);
                break;
            default:
                System.out.println("Unknown player command: " + subCommand);
                System.out.println("Use 'player add', 'player list', or 'player remove'");
        }
    }
    
    private static void addPlayer(String id, String name) {
        ReferencePlayer player = new ReferencePlayer(id, name);
        players.put(id, player);
        manager.addPlayer(player);
        System.out.println("Added player: " + player);
    }
    
    private static void listPlayers() {
        if (players.isEmpty()) {
            System.out.println("No players");
            return;
        }
        
        System.out.println("Players:");
        for (ReferencePlayer player : players.values()) {
            System.out.println("  " + player);
        }
    }
    
    private static void removePlayer(String id) {
        ReferencePlayer player = players.get(id);
        if (player == null) {
            System.out.println("Player not found: " + id);
            return;
        }
        
        players.remove(id);
        manager.removePlayer(player);
        System.out.println("Removed player: " + player);
    }
    
    private static void submitEvent(String args) {
        if (args.isEmpty()) {
            System.out.println("Usage: event <type> <player> <data>");
            return;
        }
        
        String[] parts = args.split("\\s+", 3);
        if (parts.length < 3) {
            System.out.println("Usage: event <type> <player> <data>");
            return;
        }
        
        String type = parts[0];
        String playerId = parts[1];
        String data = parts[2];
        
        ReferencePlayer player = players.get(playerId);
        if (player == null) {
            System.out.println("Player not found: " + playerId);
            return;
        }
        
        // Create and submit the event
        TestEvent event = new TestEvent(type, player, data);
        AchievableTrigger trigger = new EventAchievableTrigger(
                new AchievableTrigger.Type("TestEvent"), event);
        
        manager.processTrigger(trigger);
        System.out.println("Submitted event: " + event);
    }
    
    private static void showStatus(String playerId) {
        if (playerId.isEmpty()) {
            System.out.println("Usage: status <player>");
            return;
        }
        
        ReferencePlayer player = players.get(playerId);
        if (player == null) {
            System.out.println("Player not found: " + playerId);
            return;
        }
        
        System.out.println("Achievement status for player: " + player);
        boolean found = false;
        
        for (Achievable achievable : manager.getAchievables()) {
            boolean completed = manager.isCompleted(achievable, player);
            boolean satisfied = achievable.isSatisfied(player);
            
            System.out.println("  " + achievable.getUUID() + ":");
            System.out.println("    Completed: " + completed);
            System.out.println("    Currently satisfied: " + satisfied);
            found = true;
        }
        
        if (!found) {
            System.out.println("No achievables registered");
        }
    }
    
    private static void showState(String args) {
        if (args.isEmpty()) {
            System.out.println("Usage: state <player> <uuid>");
            return;
        }
        
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: state <player> <uuid>");
            return;
        }
        
        String playerId = parts[0];
        String uuidStr = parts[1];
        
        ReferencePlayer player = players.get(playerId);
        if (player == null) {
            System.out.println("Player not found: " + playerId);
            return;
        }
        
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format: " + uuidStr);
            return;
        }
        
        Achievable achievable = manager.getAchievable(uuid);
        if (achievable == null) {
            System.out.println("Achievable not found: " + uuid);
            return;
        }
        
        if (!(achievable instanceof us.mcparks.achievables.framework.StatefulAchievable)) {
            System.out.println("Achievable is not stateful: " + uuid);
            return;
        }
        
        us.mcparks.achievables.framework.StatefulAchievable statefulAchievable = 
                (us.mcparks.achievables.framework.StatefulAchievable) achievable;
        
        Map<String, Object> state = statefulAchievable.getPlayerState(player);
        
        System.out.println("State for player " + player + " and achievable " + uuid + ":");
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }
}