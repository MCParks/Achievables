package us.mcparks.achievables.groovy;

import groovy.lang.Closure;
import lombok.AllArgsConstructor;
import us.mcparks.achievables.events.Event;

import java.io.Serializable;

@AllArgsConstructor(staticName = "of")
public class EventClosureScript implements Serializable {
    Class<? extends Event> eventClass;
    Closure closure;
}
