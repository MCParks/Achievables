package us.mcparks.achievables.reference;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReferencePlayerTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String id = "player1";
        String name = "TestPlayer";
        
        // Act
        ReferencePlayer player = new ReferencePlayer(id, name);
        
        // Assert
        assertEquals(id, player.getId(), "Player ID should match the one provided in constructor");
        assertEquals(name, player.getName(), "Player name should match the one provided in constructor");
    }
    
    @Test
    void testToString() {
        // Arrange
        ReferencePlayer player = new ReferencePlayer("player1", "TestPlayer");
        
        // Act
        String result = player.toString();
        
        // Assert
        assertTrue(result.contains("player1"), "toString should contain player ID");
        assertTrue(result.contains("TestPlayer"), "toString should contain player name");
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Arrange
        ReferencePlayer player1 = new ReferencePlayer("player1", "TestPlayer");
        ReferencePlayer player2 = new ReferencePlayer("player1", "TestPlayer");
        ReferencePlayer player3 = new ReferencePlayer("player2", "OtherPlayer");
        
        // Assert
        assertEquals(player1, player2, "Players with the same ID and name should be equal");
        assertNotEquals(player1, player3, "Players with different ID should not be equal");
        assertNotEquals(player1, null, "Player should not be equal to null");
        assertNotEquals(player1, "Not a player", "Player should not be equal to other types");
        
        // HashCode
        assertEquals(player1.hashCode(), player2.hashCode(), "Hash codes should be equal for equal objects");
        assertNotEquals(player1.hashCode(), player3.hashCode(), "Hash codes should differ for different objects");
    }
}