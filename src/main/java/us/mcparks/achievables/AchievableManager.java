package us.mcparks.achievables;

import com.google.gson.reflect.TypeToken;
import us.mcparks.achievables.events.Event;
import us.mcparks.achievables.framework.Achievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.framework.StatefulAchievable;
import us.mcparks.achievables.triggers.AchievableTrigger;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface AchievableManager {
    Type achievableStateMapType = new TypeToken<Map<String,Object>>(){}.getType();

    void processTrigger(AchievableTrigger trigger);

    boolean isCompleted(Achievable achievable, AchievablePlayer player);

    void completeAchievable(Achievable achievable, AchievablePlayer player);

    Collection<AchievablePlayer> getCurrentPlayers();

    Map<String,Object> getPlayerState(AchievablePlayer player, StatefulAchievable achievable);

    void setPlayerState(AchievablePlayer player, StatefulAchievable achievable, Map<String,Object> state, boolean persist) throws ExecutionException;

    default void setPlayerState(AchievablePlayer player, StatefulAchievable achievable, Map<String,Object> state) throws ExecutionException {
        setPlayerState(player, achievable, state, true);
    }

    Map<String, Object> getStaticState(StatefulAchievable achievable);

    void setStaticState(StatefulAchievable achievable, Map<String, Object> state) throws ExecutionException;

    void initializePlayerState(AchievablePlayer player, StatefulAchievable achievable) throws ExecutionException;

    void initializeStaticState(StatefulAchievable achievable) throws ExecutionException;

    Class<? extends Event> getEventClass(String eventClassName);

}
