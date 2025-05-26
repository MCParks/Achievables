package us.mcparks.achievables.reference;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ReferenceCoreTest {
    
    @Test
    void testReferencePlayer() {
        // Arrange
        String id = "player1";
        String name = "TestPlayer";
        
        // Act
        ReferencePlayer player = new ReferencePlayer(id, name);
        
        // Assert
        assertEquals(id, player.getId(), "Player ID should match");
        assertEquals(name, player.getName(), "Player name should match");
    }
    
    @Test
    void testTestEvent() {
        // Arrange
        String type = "test_event";
        ReferencePlayer player = new ReferencePlayer("player1", "TestPlayer");
        String data = "event_data";
        
        // Act
        TestEvent event = new TestEvent(type, player, data);
        
        // Assert
        assertEquals(type, event.getType(), "Event type should match");
        assertEquals(player, event.getApplicablePlayer(), "Player should match");
        assertEquals(data, event.getData(), "Event data should match");
    }
}