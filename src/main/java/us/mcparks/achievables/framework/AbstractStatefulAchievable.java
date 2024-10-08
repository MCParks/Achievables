package us.mcparks.achievables.framework;

import com.google.gson.reflect.TypeToken;
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.util.Map;

public abstract class AbstractStatefulAchievable extends AbstractAchievable implements StatefulAchievable  {
    final String serializedInitialPlayerState;
    final String serializedInitialStaticState;

    Map<String, Object> cachedInitialPlayerState = null;
    Map<String, Object> cachedInitialStaticState = null;

    protected AbstractStatefulAchievable(String serializedInitialPlayerState, String serializedInitialStaticState) {
        this.serializedInitialPlayerState = serializedInitialPlayerState;
        this.serializedInitialStaticState = serializedInitialStaticState;
    }

    public Map<String, Object> getInitialPlayerState() {
        if (cachedInitialPlayerState == null) {
            return cachedInitialPlayerState = AchievableGsonManager.getGson().fromJson(serializedInitialPlayerState, new TypeToken<Map<String, Object>>() {
            }.getType());
        } else {
            return cachedInitialPlayerState;
        }
    }

    public Map<String, Object> getInitialStaticState() {
        if (cachedInitialStaticState == null) {
            return cachedInitialStaticState = AchievableGsonManager.getGson().fromJson(serializedInitialStaticState, new TypeToken<Map<String, Object>>() {
            }.getType());
        } else {
            return cachedInitialStaticState;
        }
    }
}
