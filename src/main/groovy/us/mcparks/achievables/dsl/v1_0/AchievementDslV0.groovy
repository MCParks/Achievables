package us.mcparks.achievables.dsl.v1_0

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import us.mcparks.achievables.Achievables
import us.mcparks.achievables.dsl.BigalsIntegratedGroovyAchievementLanguage
import us.mcparks.achievables.dsl.meta.AchievableMeta
import us.mcparks.achievables.dsl.meta.AchievableWithMeta
import us.mcparks.achievables.dsl.meta.MetaBuilder
import us.mcparks.achievables.events.Event
import us.mcparks.achievables.groovy.BigAlAchievable
import us.mcparks.achievables.groovy.EventClosureScript

@CompileStatic
public class AchievementDslV0 {
    final static int SYNTAX_VERSION = 0
    def initialState = [:]
    MetaBuilder<?> metaBuilder = BigalsIntegratedGroovyAchievementLanguage.metaBuilderSupplier.get();
    BigAlAchievable.Builder achievableBuilder = BigAlAchievable.builder()

    static AchievableWithMeta achievement(Closure closure) {
        AchievementDslV0 achievementDslV0 = new AchievementDslV0()
        closure.delegate = achievementDslV0
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
        return achievementDslV0.buildAchievement()
    }

    def syntaxVersion(int syntaxVersion) {
        assert syntaxVersion == SYNTAX_VERSION
    }

    @CompileDynamic
    def methodMissing(String name, args) {
        try {
            metaBuilder."$name"(args[0])
        } catch (Exception ex) {
            System.out.println("metabuilder does not have property: " + name)
        }
//        if (args instanceof Object[] && args.length == 1 && (metaBuilder.hasProperty(name) || metaBuilder.metaClass.hasMethod(name, args[0].class))) {
//            System.out.println("metabuilder has property: " + name)
//            metaBuilder."$name"(args[0])
//        } else {
//            System.out.println("Properties of metabuilder: " + metaBuilder.metaClass.methods*.name)
//            throw new MissingMethodException(name, this.class, args)
//        }
    }

    @CompileDynamic
    def propertyMissing(String name) {
        if (metaBuilder.hasProperty(name)) {
            return metaBuilder."$name"
        } else {
            throw new MissingPropertyException(name, this.class)
        }
    }

    // parse as map
    def state(Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = initialState
        closure()
        achievableBuilder.withInitialState(initialState as Map<String, Object>)
    }

    def activators(Closure<Boolean> closure) {
        achievableBuilder.addSatisfiedScript(closure.dehydrate())
    }

    def deactivators(Closure<Boolean> closure) {
        achievableBuilder.addDisqualifiedScript(closure.dehydrate())
    }

    def events(Closure closure) {
        EventsDsl eventsDsl = new EventsDsl()
        closure.delegate = eventsDsl
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure()
    }

    AchievableWithMeta buildAchievement() {
        new AchievableWithMeta(achievableBuilder.build(), metaBuilder.build())
    }

    class EventsDsl {
        void on(final String eventName, final Closure closure) {
            achievableBuilder.addEventHandler(EventClosureScript.of(Achievables.getInstance().getAchievableManager().getEventClass(eventName) as Class<? extends Event>, closure.dehydrate()))
        }
    }

}
