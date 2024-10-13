package us.mcparks.achievables.triggers;

import lombok.Getter;
import us.mcparks.achievables.events.Event;

public class EventAchievableTrigger implements AchievableTrigger {

    Class<?> eventClass;
    @Getter Event event;

    public EventAchievableTrigger(Event event) {
        this.eventClass = event.getClass();
        this.event = event;
    }

    public EventAchievableTrigger(Class<?> event) {
        eventClass = event;
    }

    @Override
    public Type getType() {
        return new Type(eventClass.getCanonicalName());
    }

    @Override
    public String toString() {
        return eventClass.getCanonicalName();
    }
}
