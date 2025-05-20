package us.mcparks.achievables.reference;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestEventTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String type = "test_event";
        ReferencePlayer player = new ReferencePlayer("player1", "TestPlayer");
        String data = "event_data";
        
        // Act
        TestEvent event = new TestEvent(type, player, data);
        
        // Assert
        assertEquals(type, event.getType(), "Event type should match the one provided in constructor");
        assertEquals(player, event.getApplicablePlayer(), "Player should match the one provided in constructor");
        assertEquals(data, event.getData(), "Event data should match the one provided in constructor");
    }
    
    @Test
    void testToString() {
        // Arrange
        ReferencePlayer player = new ReferencePlayer("player1", "TestPlayer");
        TestEvent event = new TestEvent("test_event", player, "event_data");
        
        // Act
        String result = event.toString();
        
        // Assert
        assertTrue(result.contains("test_event"), "toString should contain event type");
        assertTrue(result.contains("player1"), "toString should contain player information");
        assertTrue(result.contains("event_data"), "toString should contain event data");
    }
}