package us.mcparks.achievables.framework;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AchievableProgress {
    @Getter
    int currentProgress;
    @Getter
    int completionProgress;
    @Getter
    String progressString;
}
