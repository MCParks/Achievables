package us.mcparks.achievables.groovy;

import groovy.lang.Closure;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import us.mcparks.achievables.events.Event;

import java.io.Serializable;

@AllArgsConstructor(staticName = "of")
public class EventClosureScript implements Serializable {
    final Class<? extends Event> eventClass;
    final Closure closure;
    final boolean isStatic;
}
