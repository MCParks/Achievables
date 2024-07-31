package us.mcparks.achievables.framework;

import lombok.Setter;
import us.mcparks.achievables.Achievables;

import java.util.Collection;


public abstract class AbstractAchievable implements Achievable {
    @Setter
    protected Collection<AchievablePlayer> applicablePlayers = null;

    @Override
    public Collection<AchievablePlayer> getApplicablePlayers() {
        return applicablePlayers != null ? applicablePlayers : Achievables.getInstance().getAchievableManager().getCurrentPlayers();
    }
}
