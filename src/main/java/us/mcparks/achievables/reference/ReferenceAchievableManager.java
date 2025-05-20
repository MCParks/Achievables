package us.mcparks.achievables.reference;

import us.mcparks.achievables.AchievableManager;
import us.mcparks.achievables.events.Event;
import us.mcparks.achievables.framework.Achievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.framework.StatefulAchievable;
import us.mcparks.achievables.triggers.AchievableTrigger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * A reference implementation of AchievableManager that provides in-memory storage 
 * of achievement state and progress.
 */
public class ReferenceAchievableManager implements AchievableManager {
    private final Logger logger = Logger.getLogger(ReferenceAchievableManager.class.getName());
    
    // Map of registered achievements
    private final Map<UUID, Achievable> achievables = new ConcurrentHashMap<>();
    
    // Map of achievement completion status for players
    private final Map<String, Set<UUID>> completedAchievables = new ConcurrentHashMap<>();
    
    // Map of player-specific achievement state
    private final Map<String, Map<UUID, Map<String, Object>>> playerState = new ConcurrentHashMap<>();
    
    // Map of static state for stateful achievements
    private final Map<UUID, Map<String, Object>> staticState = new ConcurrentHashMap<>();
    
    // Collection of current players
    private final Collection<AchievablePlayer> currentPlayers = new HashSet<>();
    
    // Map of event class names to actual event classes
    private final Map<String, Class<? extends Event>> eventClasses = new ConcurrentHashMap<>();
    
    /**
     * Registers an achievable with the manager
     * @param achievable The achievable to register
     */
    public void registerAchievable(Achievable achievable) {
        achievables.put(achievable.getUUID(), achievable);
        logger.info("Registered achievable: " + achievable.getUUID());
    }
    
    /**
     * Registers an event class
     * @param eventClassName The name of the event class
     * @param eventClass The event class
     */
    public void registerEventClass(String eventClassName, Class<? extends Event> eventClass) {
        eventClasses.put(eventClassName, eventClass);
        logger.info("Registered event class: " + eventClassName);
    }
    
    /**
     * Adds a player to the current players collection
     * @param player The player to add
     */
    public void addPlayer(AchievablePlayer player) {
        currentPlayers.add(player);
        logger.info("Added player: " + player);
    }
    
    /**
     * Removes a player from the current players collection
     * @param player The player to remove
     */
    public void removePlayer(AchievablePlayer player) {
        currentPlayers.remove(player);
        logger.info("Removed player: " + player);
    }

    @Override
    public void processTrigger(AchievableTrigger trigger) {
        logger.info("Processing trigger: " + trigger.getType());
        
        // Find all achievables that respond to this trigger
        for (Achievable achievable : achievables.values()) {
            if (achievable.getTriggers().contains(trigger.getType())) {
                achievable.process(trigger);
            }
        }
    }

    @Override
    public boolean isCompleted(Achievable achievable, AchievablePlayer player) {
        if (!(player instanceof ReferencePlayer)) {
            return false;
        }
        
        String playerId = ((ReferencePlayer) player).getId();
        return completedAchievables.containsKey(playerId) &&
                completedAchievables.get(playerId).contains(achievable.getUUID());
    }

    @Override
    public void completeAchievable(Achievable achievable, AchievablePlayer player) {
        if (!(player instanceof ReferencePlayer)) {
            return;
        }
        
        String playerId = ((ReferencePlayer) player).getId();
        completedAchievables.computeIfAbsent(playerId, k -> new HashSet<>()).add(achievable.getUUID());
        logger.info("Player " + player + " completed achievable: " + achievable.getUUID());
    }

    @Override
    public Collection<AchievablePlayer> getCurrentPlayers() {
        return Collections.unmodifiableCollection(currentPlayers);
    }

    @Override
    public Map<String, Object> getPlayerState(AchievablePlayer player, StatefulAchievable achievable) {
        if (!(player instanceof ReferencePlayer)) {
            return null;
        }
        
        String playerId = ((ReferencePlayer) player).getId();
        UUID achievableId = achievable.getUUID();
        
        if (!playerState.containsKey(playerId) || !playerState.get(playerId).containsKey(achievableId)) {
            return null;
        }
        
        return new HashMap<>(playerState.get(playerId).get(achievableId));
    }

    @Override
    public void setPlayerState(AchievablePlayer player, StatefulAchievable achievable, Map<String, Object> state, boolean persist) throws ExecutionException {
        if (!(player instanceof ReferencePlayer)) {
            return;
        }
        
        String playerId = ((ReferencePlayer) player).getId();
        UUID achievableId = achievable.getUUID();
        
        playerState.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(achievableId, new HashMap<>(state));
        
        logger.fine("Set player state for " + player + " on achievable " + achievableId);
    }

    @Override
    public Map<String, Object> getStaticState(StatefulAchievable achievable) {
        UUID achievableId = achievable.getUUID();
        
        if (!staticState.containsKey(achievableId)) {
            return null;
        }
        
        return new HashMap<>(staticState.get(achievableId));
    }

    @Override
    public void setStaticState(StatefulAchievable achievable, Map<String, Object> state) throws ExecutionException {
        UUID achievableId = achievable.getUUID();
        staticState.put(achievableId, new HashMap<>(state));
        logger.fine("Set static state for achievable " + achievableId);
    }

    @Override
    public void initializePlayerState(AchievablePlayer player, StatefulAchievable achievable) throws ExecutionException {
        setPlayerState(player, achievable, achievable.getInitialPlayerState(), true);
        logger.fine("Initialized player state for " + player + " on achievable " + achievable.getUUID());
    }

    @Override
    public void initializeStaticState(StatefulAchievable achievable) throws ExecutionException {
        setStaticState(achievable, achievable.getInitialStaticState());
        logger.fine("Initialized static state for achievable " + achievable.getUUID());
    }

    @Override
    public Class<? extends Event> getEventClass(String eventClassName) {
        return eventClasses.get(eventClassName);
    }
    
    /**
     * Gets all registered achievables
     * @return A collection of registered achievables
     */
    public Collection<Achievable> getAchievables() {
        return Collections.unmodifiableCollection(achievables.values());
    }
    
    /**
     * Gets an achievable by its UUID
     * @param uuid The UUID of the achievable
     * @return The achievable, or null if not found
     */
    public Achievable getAchievable(UUID uuid) {
        return achievables.get(uuid);
    }
}