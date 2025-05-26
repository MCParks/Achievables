package us.mcparks.achievables.reference;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReferenceCoreTests {
    
    @Test
    public void testReferencePlayer() {
        // Create a player
        ReferencePlayer player = new ReferencePlayer("test-id", "Test Player");
        
        // Verify properties
        assertEquals("test-id", player.getId());
        assertEquals("Test Player", player.getName());
        
        // Test toString
        String playerString = player.toString();
        assertTrue(playerString.contains("test-id"));
        assertTrue(playerString.contains("Test Player"));
        
        // Test equals and hashCode
        ReferencePlayer samePayer = new ReferencePlayer("test-id", "Test Player");
        ReferencePlayer differentPlayer = new ReferencePlayer("other-id", "Other Player");
        
        assertEquals(player, samePayer);
        assertNotEquals(player, differentPlayer);
        assertEquals(player.hashCode(), samePayer.hashCode());
        assertNotEquals(player.hashCode(), differentPlayer.hashCode());
    }
    
    @Test
    public void testTestEvent() {
        // Create a player and event
        ReferencePlayer player = new ReferencePlayer("test-id", "Test Player");
        TestEvent event = new TestEvent("test-type", player, "test-data");
        
        // Verify properties
        assertEquals("test-type", event.getType());
        assertEquals(player, event.getApplicablePlayer());
        assertEquals("test-data", event.getData());
        
        // Test toString
        String eventString = event.toString();
        assertTrue(eventString.contains("test-type"));
        assertTrue(eventString.contains("test-id"));
        assertTrue(eventString.contains("test-data"));
    }
    
    @Test
    public void testReferenceAchievableManager() {
        // Create manager and players
        ReferenceAchievableManager manager = new ReferenceAchievableManager();
        ReferencePlayer player1 = new ReferencePlayer("player1", "Player One");
        ReferencePlayer player2 = new ReferencePlayer("player2", "Player Two");
        
        // Test player management
        assertTrue(manager.getCurrentPlayers().isEmpty());
        
        manager.addPlayer(player1);
        manager.addPlayer(player2);
        assertEquals(2, manager.getCurrentPlayers().size());
        assertTrue(manager.getCurrentPlayers().contains(player1));
        assertTrue(manager.getCurrentPlayers().contains(player2));
        
        manager.removePlayer(player1);
        assertEquals(1, manager.getCurrentPlayers().size());
        assertFalse(manager.getCurrentPlayers().contains(player1));
        assertTrue(manager.getCurrentPlayers().contains(player2));
        
        // Test event registration
        manager.registerEventClass("TestEvent", TestEvent.class);
        assertEquals(TestEvent.class, manager.getEventClass("TestEvent"));
        assertNull(manager.getEventClass("UnknownEvent"));
    }
}