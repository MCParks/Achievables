package us.mcparks.achievables.dsl.meta

import groovy.transform.Canonical
import lombok.AllArgsConstructor
import lombok.Getter
import us.mcparks.achievables.framework.Achievable

@Canonical
class AchievableWithMeta {
    Achievable achievable;
    AchievableMeta meta;
}
