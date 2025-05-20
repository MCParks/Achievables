package us.mcparks.achievables.reference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import us.mcparks.achievables.framework.Achievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.framework.StatefulAchievable;
import us.mcparks.achievables.triggers.AchievableTrigger;
import us.mcparks.achievables.triggers.EventAchievableTrigger;

import static org.mockito.Mockito.*;

public class ReferenceAchievableManagerAdvancedTests {

    private ReferenceAchievableManager manager;
    private ReferencePlayer player;
    private StatefulAchievable mockAchievable;
    private UUID achievableUuid;
    
    @BeforeEach
    public void setUp() {
        manager = new ReferenceAchievableManager();
        player = new ReferencePlayer("test-player", "Test Player");
        manager.addPlayer(player);
        
        // Create mock achievable
        achievableUuid = UUID.randomUUID();
        mockAchievable = mock(StatefulAchievable.class);
        when(mockAchievable.getUUID()).thenReturn(achievableUuid);
        
        Map<String, Object> initialPlayerState = new HashMap<>();
        initialPlayerState.put("counter", 0);
        when(mockAchievable.getInitialPlayerState()).thenReturn(initialPlayerState);
        
        Map<String, Object> initialStaticState = new HashMap<>();
        initialStaticState.put("globalCounter", 0);
        when(mockAchievable.getInitialStaticState()).thenReturn(initialStaticState);
        
        manager.registerAchievable(mockAchievable);
    }
    
    @Test
    public void testAchievableRegistration() {
        // Verify achievable was registered
        Collection<Achievable> achievables = manager.getAchievables();
        assertEquals(1, achievables.size(), "Should have 1 registered achievable");
        assertTrue(achievables.contains(mockAchievable), "Should contain registered achievable");
        
        // Verify retrieving achievable by UUID
        Achievable retrievedAchievable = manager.getAchievable(achievableUuid);
        assertEquals(mockAchievable, retrievedAchievable, "Should retrieve registered achievable by UUID");
        assertNull(manager.getAchievable(UUID.randomUUID()), "Should return null for unregistered UUID");
    }
    
    @Test
    public void testAchievableCompletion() {
        // Initial state
        assertFalse(manager.isCompleted(mockAchievable, player), 
                "Achievable should not be completed initially");
        
        // Complete achievable
        manager.completeAchievable(mockAchievable, player);
        
        // Verify completion status
        assertTrue(manager.isCompleted(mockAchievable, player), 
                "Achievable should be marked as completed after completion");
    }
    
    @Test
    public void testPlayerStateManagement() throws ExecutionException {
        // Initial state should be null
        assertNull(manager.getPlayerState(player, mockAchievable), 
                "Player state should be null initially");
        
        // Initialize player state
        manager.initializePlayerState(player, mockAchievable);
        
        // Verify state after initialization
        Map<String, Object> playerState = manager.getPlayerState(player, mockAchievable);
        assertNotNull(playerState, "Player state should not be null after initialization");
        assertEquals(0, playerState.get("counter"), "Counter should be initialized to 0");
        
        // Update player state
        Map<String, Object> newState = new HashMap<>();
        newState.put("counter", 5);
        newState.put("newField", "value");
        manager.setPlayerState(player, mockAchievable, newState, true);
        
        // Verify updated state
        playerState = manager.getPlayerState(player, mockAchievable);
        assertEquals(5, playerState.get("counter"), "Counter should be updated to 5");
        assertEquals("value", playerState.get("newField"), "New field should be added");
    }
    
    @Test
    public void testStaticStateManagement() throws ExecutionException {
        // Initial static state should be null
        assertNull(manager.getStaticState(mockAchievable), 
                "Static state should be null initially");
        
        // Initialize static state
        manager.initializeStaticState(mockAchievable);
        
        // Verify state after initialization
        Map<String, Object> staticState = manager.getStaticState(mockAchievable);
        assertNotNull(staticState, "Static state should not be null after initialization");
        assertEquals(0, staticState.get("globalCounter"), "Global counter should be initialized to 0");
        
        // Update static state
        Map<String, Object> newState = new HashMap<>();
        newState.put("globalCounter", 10);
        newState.put("newGlobalField", "global-value");
        manager.setStaticState(mockAchievable, newState);
        
        // Verify updated state
        staticState = manager.getStaticState(mockAchievable);
        assertEquals(10, staticState.get("globalCounter"), "Global counter should be updated to 10");
        assertEquals("global-value", staticState.get("newGlobalField"), "New global field should be added");
    }
    
    @Test
    public void testTriggerProcessing() {
        // Setup trigger types for mock achievable
        AchievableTrigger.Type triggerType = new AchievableTrigger.Type("test-trigger");
        when(mockAchievable.getTriggers()).thenReturn(java.util.Collections.singleton(triggerType));
        
        // Create test trigger
        AchievableTrigger trigger = mock(AchievableTrigger.class);
        when(trigger.getType()).thenReturn(triggerType);
        
        // Process trigger
        manager.processTrigger(trigger);
        
        // Verify achievable.process was called
        verify(mockAchievable).process(trigger);
        
        // Test with non-matching trigger
        AchievableTrigger nonMatchingTrigger = mock(AchievableTrigger.class);
        when(nonMatchingTrigger.getType()).thenReturn(new AchievableTrigger.Type("other-trigger"));
        
        // Reset mock
        reset(mockAchievable);
        
        // Process non-matching trigger
        manager.processTrigger(nonMatchingTrigger);
        
        // Verify achievable.process was not called
        verify(mockAchievable, never()).process(nonMatchingTrigger);
    }
    
    @Test
    public void testNonReferencePlayerHandling() throws ExecutionException {
        // Create a non-ReferencePlayer
        AchievablePlayer nonReferencePlayer = mock(AchievablePlayer.class);
        
        // Test isCompleted
        assertFalse(manager.isCompleted(mockAchievable, nonReferencePlayer), 
                "isCompleted should return false for non-ReferencePlayer");
        
        // Test completeAchievable
        manager.completeAchievable(mockAchievable, nonReferencePlayer);
        assertFalse(manager.isCompleted(mockAchievable, nonReferencePlayer), 
                "completeAchievable should have no effect for non-ReferencePlayer");
        
        // Test getPlayerState
        assertNull(manager.getPlayerState(nonReferencePlayer, mockAchievable), 
                "getPlayerState should return null for non-ReferencePlayer");
        
        // Test setPlayerState
        Map<String, Object> state = new HashMap<>();
        state.put("test", "value");
        manager.setPlayerState(nonReferencePlayer, mockAchievable, state, true);
        assertNull(manager.getPlayerState(nonReferencePlayer, mockAchievable), 
                "setPlayerState should have no effect for non-ReferencePlayer");
        
        // Test initializePlayerState
        manager.initializePlayerState(nonReferencePlayer, mockAchievable);
        assertNull(manager.getPlayerState(nonReferencePlayer, mockAchievable), 
                "initializePlayerState should have no effect for non-ReferencePlayer");
    }
}