package us.mcparks.achievables.framework;

import us.mcparks.achievables.Achievables;
import us.mcparks.achievables.groovy.BigAlAchievable;

import java.util.concurrent.ExecutionException;

public interface BackfillableAchievable extends Achievable {

    void processBackfill(AchievablePlayer player);

    boolean hasBackfill();
}
