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
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;


public interface Achievable {
    // Returns a collection of triggers that this Achievable is capable of responding to
    Collection<AchievableTrigger.Type> getTriggers();

    // Returns true if the player has achieved this Achievable
    boolean isSatisfied(AchievablePlayer player);

    // Processes the given trigger for the given player
    void process(AchievablePlayer player, AchievableTrigger trigger, boolean savePlayerState);

    // Processes the given trigger for all players
    default void process(AchievableTrigger trigger) {
        for (AchievablePlayer player : getApplicablePlayers()) {
            try {
                // if this trigger is player specific and not for this player, skip this player
                if (((EventAchievableTrigger) trigger).getEvent() instanceof PlayerEvent &&
                        !((PlayerEvent) ((EventAchievableTrigger) trigger).getEvent()).getApplicablePlayer().equals(player)) {
                    continue;
                }
                // if not satisfied, process this trigger for the player and check if it's satisfied
                if (!Achievables.getInstance().getAchievableManager().isCompleted(this, player)) {
                    process(player, trigger, true);

                    // if that made us satisfy the achievement, complete it for the player
                    if (isSatisfied(player)) {
                        Achievables.getInstance().getAchievableManager().completeAchievable(this, player);
                    }
                } else {
                    // even if it's satisfied, we still need to process the trigger just in case there is static state -- we dont need to save the player state though!
                    process(player, trigger, false);
                }
            } catch (Exception ex) {
                Achievables.getInstance().getLogger().log(Level.SEVERE, "Error processing trigger " + trigger + " for player " + player + "in achievable " + getUUID().toString(), ex);
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
        return AchievableGsonManager.getGson().toJson(this);
    }
}
