package us.mcparks.achievables.framework;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.*;
import us.mcparks.achievables.Achievables;
import us.mcparks.achievables.triggers.AchievableTrigger;
import us.mcparks.achievables.events.PlayerEvent;
import us.mcparks.achievables.triggers.EventAchievableTrigger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public interface Achievable {
    // Returns a collection of triggers that this Achievable is capable of responding to
    Collection<AchievableTrigger.Type> getTriggers();
    Gson gson = new GsonBuilder().registerTypeAdapter(Multimap.class,
                    (JsonSerializer<Multimap>) (multimap, type, jsonSerializationContext) -> jsonSerializationContext.serialize(multimap.asMap()))
            .registerTypeAdapter(Multimap.class,
                    (JsonDeserializer<Multimap>) (jsonElement, type, jsonDeserializationContext) -> {
                        final SetMultimap<AchievableTrigger.Type, String> map = Multimaps.newSetMultimap(new HashMap<>(),
                                () -> Sets.newHashSet());
                        for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
                            for (JsonElement element : (JsonArray) entry.getValue()) {
                                map.get(new AchievableTrigger.Type(entry.getKey()))
                                        .add(element.getAsString());
                            }
                        }
                        return map;
                    })
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    // Returns true if the player has achieved this Achievable
    boolean isSatisfied(AchievablePlayer player);

    // Processes the given trigger for the given player
    void process(AchievablePlayer player, AchievableTrigger trigger);

    // Processes the given trigger for all players
    default void process(AchievableTrigger trigger) {
        for (AchievablePlayer player : getApplicablePlayers()) {
            // if this trigger is player specific and not for this player, skip this player
            if (((EventAchievableTrigger) trigger).getEvent() instanceof PlayerEvent &&
                    !((PlayerEvent) ((EventAchievableTrigger) trigger).getEvent()).getApplicablePlayer().equals(player)) {
                continue;
            }
            // if not satisfied, process this trigger for the player
            if (!Achievables.getInstance().getAchievableManager().isCompleted(this, player)) {
                process(player, trigger);

                // if that made us satisfy the achievement, complete it for the player
                if (isSatisfied(player)) {
                    Achievables.getInstance().getAchievableManager().completeAchievable(this, player);
                }
            }

        }
    }

    /**
     *
     * @return a globally unique identifier for this achievable
     */
    UUID getUUID();


    Collection<AchievablePlayer> getApplicablePlayers();

    void setApplicablePlayers(Collection<AchievablePlayer> players);

    default void addApplicablePlayer(AchievablePlayer player) {
        getApplicablePlayers().add(player);
    }

    default void removeApplicablePlayer(AchievablePlayer player) {
        getApplicablePlayers().remove(player);
    }

    default String toJson() {
        return gson.toJson(this);
    }
}
