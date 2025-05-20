package us.mcparks.achievables.reference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import us.mcparks.achievables.framework.AchievablePlayer;

class ReferenceAchievableManagerTest {

    private ReferenceAchievableManager manager;
    private ReferencePlayer player1;
    private ReferencePlayer player2;
    
    @BeforeEach
    void setUp() {
        manager = new ReferenceAchievableManager();
        player1 = new ReferencePlayer("player1", "TestPlayer1");
        player2 = new ReferencePlayer("player2", "TestPlayer2");
    }
    
    @Test
    void testPlayerManagement() {
        // Test initial state
        assertTrue(manager.getCurrentPlayers().isEmpty(), "Should start with no players");
        
        // Add players
        manager.addPlayer(player1);
        manager.addPlayer(player2);
        
        // Verify players were added
        Collection<AchievablePlayer> currentPlayers = manager.getCurrentPlayers();
        assertEquals(2, currentPlayers.size(), "Should have 2 players after adding");
        assertTrue(currentPlayers.contains(player1), "Should contain player1");
        assertTrue(currentPlayers.contains(player2), "Should contain player2");
        
        // Remove a player
        manager.removePlayer(player1);
        
        // Verify player was removed
        currentPlayers = manager.getCurrentPlayers();
        assertEquals(1, currentPlayers.size(), "Should have 1 player after removing");
        assertFalse(currentPlayers.contains(player1), "Should not contain removed player");
        assertTrue(currentPlayers.contains(player2), "Should still contain other player");
    }
    
    @Test
    void testRegisterEventClass() {
        // Register event class
        manager.registerEventClass("TestEvent", TestEvent.class);
        
        // Verify registration
        assertEquals(TestEvent.class, manager.getEventClass("TestEvent"), 
                "Should return registered event class");
        assertNull(manager.getEventClass("UnknownEvent"), 
                "Should return null for unregistered event class");
    }
}