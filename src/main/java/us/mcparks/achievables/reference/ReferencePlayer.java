package us.mcparks.achievables.reference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import us.mcparks.achievables.framework.AchievablePlayer;

/**
 * A simple implementation of AchievablePlayer for the reference implementation.
 * Represents a player by a unique ID and name.
 */
@EqualsAndHashCode
public class ReferencePlayer implements AchievablePlayer {
    @Getter
    private final String id;
    
    @Getter
    private final String name;
    
    public ReferencePlayer(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "Player{id='" + id + "', name='" + name + "'}";
    }
}