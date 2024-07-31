package us.mcparks.achievables.framework;

import us.mcparks.achievables.Achievables;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface StatefulAchievable extends Achievable {

    Map<String, Object> getInitialState();

    default Map<String, Object> getPlayerState(AchievablePlayer player) {
        Map<String, Object> state = Achievables.getInstance().getAchievableManager().getPlayerState(player, this);
        try {
            if (state == null) {
                Achievables.getInstance().getAchievableManager().initializePlayerState(player, this);
            }

        } catch (ExecutionException e) {
            Achievables.getInstance().getLogger().warning("Failed to set player state for achievable");
            e.printStackTrace();
        }

        return state == null ? getInitialState() : state;
    }
}
