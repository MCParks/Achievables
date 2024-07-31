package us.mcparks.achievables.simple;

import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Language;
import us.mcparks.achievables.events.Event;
import java.io.Serializable;

@AllArgsConstructor(staticName = "of")
public class EventScript implements Serializable {
    Class<? extends Event> eventClass;
    @Language("groovy")
    String scriptText;
}
