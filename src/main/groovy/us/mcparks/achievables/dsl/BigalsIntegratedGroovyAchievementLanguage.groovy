package us.mcparks.achievables.dsl


import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import us.mcparks.achievables.dsl.meta.AchievableWithMeta
import us.mcparks.achievables.dsl.meta.MetaBuilder
import us.mcparks.achievables.groovy.BigAlAchievable
import us.mcparks.achievables.dsl.v1_0.AchievementDslV0
import us.mcparks.achievables.utils.GroovyEvaluator
import us.mcparks.achievables.utils.GroovyScriptCachingBuilder

import java.util.function.Supplier


/**
 * A Groovy DSL for writing achievements -- a more readable alternative to the Java API.
 *                                          ^^^^^ Copilot wrote this, I think I just got roasted?
 *
 */
/*
Example syntax:
achievement {
    name "Magic Kingdom Mountaineer"
    description "Ride the three mountains of Magic Kingdom without warping or teleporting"
    icon "IRON_SPADE:20"
    reward "MONEY:500"

    state {
        rodeSpaceMountain = false
        rodeBigThunderMountain = false
        rodeSplashMountain = false
        warpedOrTeleported = false
    }

    activators {
        state.rodeSpaceMountain && state.rodeSplashMountain && state.rodeBigThunderMountain
    }

    deactivators {
        equals(warpedOrTeleported, true)
    }

    events {
        on(CompleteRideEvent) {
            if (event.rideId == 1) {
                state.rodeSpaceMountain = true
            }
            if (event.rideId == 2) {
                state.rodeBigThunderMountain = true
            }
            if (event.rideId == 3) {
                state.rodeSplashMountain = true
            }
        }
        on(TeleportPlayerToPlayerEvent) {
            state.warpedOrTeleported = true
        }
        on(PlayerWarpEvent) {
            state.warpedOrTeleported = true
        }
    }
}

syntactic sugar version:

achievement {
    name "Magic Kingdom Mountaineer"
    description "Ride the three mountains of Magic Kingdom without warping or teleporting"
    icon "IRON_SPADE:20"
    reward "MONEY:500"

    global {
        spaceMountainId = 1
        bigThunderMountainId = 2
        splashMountainId = 3
    }

    activators {
        completeRide(id: spaceMountainId)
        completeRide(id: splashMountainId)
        completeRide(id: bigThunderMountainId)
    }

    deactivators {
        event(name="PlayerWarpEvent")
        event(name="TeleportPlayerToPlayerEvent")
    }
}

Under the hood, this is just a wrapper around the SimpleStateAchievable.Builder class.
completeRide is an example of a helper method that one could write in the DSL as sugar for listening to the CompleteRideEvent.
The DSL allows for the user to write helper methods to more concisely express their achievements.

 */

public final class BigalsIntegratedGroovyAchievementLanguage {
    static GroovyEvaluator bigalEvaluator = createEvaluator("${AchievementDslV0.class.getName()}")
    static GroovyEvaluator versionEvaluator = createEvaluator("${VersionDsl.class.getName()}")

    static Supplier<MetaBuilder<?>> metaBuilderSupplier = MetaBuilder::new

    static GroovyEvaluator createEvaluator(String... classNames) {
        def importCustomizer = new ImportCustomizer()
        for (String className : classNames) {
            importCustomizer.addStaticStars(className)
        }
        def configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer)
        return new GroovyEvaluator(new GroovyScriptCachingBuilder(new GroovyShell(configuration)))
    }

    static AchievableWithMeta interpret(String achievementCodeAsString) {
        return interpret(achievementCodeAsString, null)
    }

    static AchievableWithMeta interpret(String achievementCodeAsString, UUID uuid) {
        // The string represents an achievement in the DSL, so we need to parse it and convert it to a Java object
        // First, let's use the VersionDsl to determine which version of the DSL we're using

        // Tell the GroovyShell about the VersionDsl
        int version = versionEvaluator.evaluateExpression(achievementCodeAsString)
        if (version == 0) {
            //System.out.println("Using version 0")
            AchievableWithMeta achievement = bigalEvaluator.evaluateExpression(achievementCodeAsString)
            if (uuid != null) {
                // convert to builder and back to add the uuid
                return new AchievableWithMeta(((BigAlAchievable) achievement.achievable).toBuilder().uuid(uuid).build(), achievement.meta)
            } else {
                return achievement
            }

        }
        return null
    }

}


