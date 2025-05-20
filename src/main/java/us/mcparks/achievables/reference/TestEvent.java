package us.mcparks.achievables.reference;

import lombok.Getter;
import us.mcparks.achievables.events.Event;
import us.mcparks.achievables.events.PlayerEvent;
import us.mcparks.achievables.framework.AchievablePlayer;

/**
 * A simple event implementation for testing achievables
 */
public class TestEvent implements Event, PlayerEvent {
    @Getter
    private final String type;
    
    @Getter
    private final AchievablePlayer applicablePlayer;
    
    @Getter
    private final String data;
    
    public TestEvent(String type, AchievablePlayer player, String data) {
        this.type = type;
        this.applicablePlayer = player;
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "TestEvent{type='" + type + "', player=" + applicablePlayer + ", data='" + data + "'}";
    }
}