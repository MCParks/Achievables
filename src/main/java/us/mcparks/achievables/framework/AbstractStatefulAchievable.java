package us.mcparks.achievables.framework;

import com.google.gson.reflect.TypeToken;
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.lang.reflect.Type;
import java.util.*;

public abstract class AbstractStatefulAchievable extends AbstractAchievable implements StatefulAchievable  {
    final String serializedInitialPlayerState;
    final String serializedInitialStaticState;

    static Type stateType = new TypeToken<Map<String, Object>>() {
    }.getType();

    protected AbstractStatefulAchievable(String serializedInitialPlayerState, String serializedInitialStaticState) {
        this.serializedInitialPlayerState = serializedInitialPlayerState;
        this.serializedInitialStaticState = serializedInitialStaticState;
    }


    public Map<String, Object> getInitialPlayerState() {
        return AchievableGsonManager.getGson().fromJson(serializedInitialPlayerState, stateType);
    }

    public Map<String, Object> getInitialStaticState() {
        return AchievableGsonManager.getGson().fromJson(serializedInitialStaticState, stateType);
    }
}
