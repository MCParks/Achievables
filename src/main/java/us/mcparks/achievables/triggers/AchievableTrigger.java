package us.mcparks.achievables.triggers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

public interface AchievableTrigger {
    Type getType();


    @AllArgsConstructor
    @EqualsAndHashCode
    static class Type {
        @EqualsAndHashCode.Include String key;

        @Override
        public String toString() {
            return key;
        }
    }
}
