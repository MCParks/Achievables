package us.mcparks.achievables.framework;

import com.google.gson.reflect.TypeToken;
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.util.*;

public abstract class AbstractStatefulAchievable extends AbstractAchievable implements StatefulAchievable  {
    final String serializedInitialPlayerState;
    final String serializedInitialStaticState;

    Map<String,Object> cachedInitialPlayerState;
    Map<String,Object> cachedInitialStaticState;

    protected AbstractStatefulAchievable(String serializedInitialPlayerState, String serializedInitialStaticState) {
        this.serializedInitialPlayerState = serializedInitialPlayerState;
        this.serializedInitialStaticState = serializedInitialStaticState;
    }


    public Map<String, Object> getInitialPlayerState() {
        if (cachedInitialPlayerState == null) {
            cachedInitialPlayerState = deepCopy(AchievableGsonManager.getGson().fromJson(serializedInitialPlayerState, new TypeToken<Map<String, Object>>() {
            }.getType()));
        }
        return cachedInitialPlayerState;
    }

    public Map<String, Object> getInitialStaticState() {
        if (cachedInitialStaticState == null) {
            cachedInitialStaticState = deepCopy(AchievableGsonManager.getGson().fromJson(serializedInitialStaticState, new TypeToken<Map<String, Object>>() {
            }.getType()));
        }
        return cachedInitialStaticState;
    }

    private <T> T deepCopy(T object) {
        if (object == null) {
            return null;
        } else if (object instanceof List) {
            List<Object> original = (List<Object>) object;
            List<Object> copy = new ArrayList<>();
            for (Object o : original) {
                copy.add(deepCopy(o));
            }
            return (T) copy;
        } else if (object instanceof Set) {
            Set<Object> original = (Set<Object>) object;
            Set<Object> copy = new HashSet<>();
            for (Object o : original) {
                copy.add(deepCopy(o));
            }
            return (T) copy;
        } else if (object instanceof Map) {
            Map<String, Object> original = (Map<String, Object>) object;
            Map<String, Object> copy = new HashMap<>();
            for (Map.Entry<String, Object> entry : original.entrySet()) {
                copy.put(entry.getKey(), deepCopy(entry.getValue()));
            }
            return (T) copy;
        } else {
            return object;
        }
    }
}
