package us.mcparks.achievables.simple;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import org.apache.groovy.util.Maps;
import org.intellij.lang.annotations.Language;
import us.mcparks.achievables.Achievables;
import us.mcparks.achievables.events.PlayerEvent;
import us.mcparks.achievables.framework.AbstractStatefulAchievable;
import us.mcparks.achievables.framework.Achievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.utils.AchievableGsonManager;
import us.mcparks.achievables.utils.GroovyEvaluator;
import us.mcparks.achievables.triggers.AchievableTrigger;
import us.mcparks.achievables.triggers.EventAchievableTrigger;
import java.util.*;
import java.util.concurrent.ExecutionException;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class SimpleStateAchievable extends AbstractStatefulAchievable {
    String serializedInitialState;
    @Language("groovy")
    String isSatisfiedScript;
    @Language("groovy")
    String isDisqualifiedScript;
    Multimap<AchievableTrigger.Type, String> eventHandlers;
    transient GroovyEvaluator evaluator = new GroovyEvaluator();
    @EqualsAndHashCode.Include
    UUID uuid;

    @SafeVarargs
    public SimpleStateAchievable(Map<String, Object> initialState, String isSatisfied, String isDisqualified, EventScript... eventScripts) {
        this(UUID.randomUUID(), initialState, isSatisfied, isDisqualified, eventScripts);
    }

    public SimpleStateAchievable(UUID uuid, Map<String, Object> initialState, String isSatisfied, String isDisqualified, EventScript... eventScripts) {
        this.uuid = uuid;
        this.eventHandlers = HashMultimap.create();
        serializedInitialState = AchievableGsonManager.getGson().toJson(initialState);
        this.isSatisfiedScript = isSatisfied;
        this.isDisqualifiedScript = isDisqualified;

        for (EventScript handler : eventScripts) {
            this.eventHandlers.put(new AchievableTrigger.Type(handler.eventClass.getCanonicalName()), handler.scriptText);
        }
    }

    @SafeVarargs
    public SimpleStateAchievable(Map<String, Object> initialState, String isSatisfied, EventScript... eventScripts) {
        this(initialState, isSatisfied, null, eventScripts);
    }

    private SimpleStateAchievable(Map<String, Object> initialState, String isSatisfiedScript, String isDisqualifiedScript, Multimap<AchievableTrigger.Type, String> eventHandlers, UUID uuid) {
        serializedInitialState = AchievableGsonManager.getGson().toJson(initialState);
        this.isSatisfiedScript = isSatisfiedScript;
        this.isDisqualifiedScript = isDisqualifiedScript;
        this.eventHandlers = eventHandlers;
        this.uuid = uuid;
    }

    public SimpleStateAchievable() {
        evaluator = new GroovyEvaluator();
    }

    @Override
    public Collection<AchievableTrigger.Type> getTriggers() {
        return eventHandlers.keySet();
    }

    @Override
    public boolean isSatisfied(AchievablePlayer player) {
        boolean result = (boolean) runScriptForPlayer(player, isSatisfiedScript);
        return result;
    }

    public boolean isDisqualified(AchievablePlayer player) {
        if (isDisqualifiedScript == null) {
            return false;
        } else {
            return (boolean) runScriptForPlayer(player, isDisqualifiedScript);
        }
    }

    @Override
    public void process(AchievablePlayer player, AchievableTrigger trigger) {
        if (trigger instanceof EventAchievableTrigger) {
            if (((EventAchievableTrigger) trigger).getEvent() instanceof PlayerEvent &&
                    !((PlayerEvent) ((EventAchievableTrigger) trigger).getEvent()).getApplicablePlayer().equals(player)) {
                return;
            }

            if (eventHandlers.containsKey(trigger.getType())) {
                eventHandlers.get(trigger.getType()).forEach(
                        script -> {
                            runScriptForPlayer(player, script, Maps.of("event", ((EventAchievableTrigger) trigger).getEvent()));
                            try {
                                Achievables.getInstance().getAchievableManager().setPlayerState(player, this, (Map<String, Object>) evaluator.getVariable("state"));
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        });


                if (isDisqualified(player)) {
                    try {
                        Achievables.getInstance().getAchievableManager().setPlayerState(player, this, getInitialState());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Achievables.getInstance().getLogger().warning("Failed to reset player state for achievable");
                    }
                }
            }
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public static SimpleStateAchievable fromJson(String json) {
        return AchievableGsonManager.getGson().fromJson(json, SimpleStateAchievable.class);
    }

    private Object runScriptForPlayer(AchievablePlayer player, String script) {
        return runScriptForPlayer(player, script, null);
    }

    private Object runScriptForPlayer(AchievablePlayer player, String script, Map<String, Object> vars) {
        evaluator.setVariable("state", new HashMap<>(getPlayerState(player)));
        evaluator.setVariable("player", player);


        if (vars != null) {
            for (Map.Entry<String,Object> entry : vars.entrySet()) {
                evaluator.setVariable(entry.getKey(), entry.getValue());
            }
        }

        Object result = evaluator.evaluateExpression(script + "()");
        if (vars != null) {
            for (Map.Entry<String,Object> entry : vars.entrySet()) {
                evaluator.removeVariable(entry.getKey());
            }
        }

        return result;

    }

    @Override
    public Map<String, Object> getInitialState() {
        return AchievableGsonManager.getGson().fromJson(serializedInitialState, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    static class ScriptHolder {
        ScriptHolder(String script) {

        }

        ScriptHolder(Closure script) {

        }


    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder<T extends Builder<T>> {
        Map<String, Object> initialState = null;
        List<String> isSatisfiedScripts = new ArrayList<>();
        List<String> isDisqualifiedScripts = new ArrayList<>();
        List<EventScript> eventScripts = new ArrayList<>();

        public Builder() {
        }

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T withInitialState(Map<String, Object> initialState) {
            if (this.initialState != null) {
                throw new UnsupportedOperationException("Initial state already set");
            }
            this.initialState = initialState;
            return self();
        }

        public T addState(String key, Object initialValue) {
            if (this.initialState == null) {
                this.initialState = new HashMap<>();
            }
            this.initialState.put(key, initialValue);
            return self();
        }

        public T addSatisfiedScript(String isSatisfiedScript) {
            isSatisfiedScripts.add(isSatisfiedScript);
            return self();
        }
        public T addDisqualifiedScript(String isDisqualifiedScript) {
            isDisqualifiedScripts.add(isDisqualifiedScript);
            return self();
        }
        public T addEventScript(EventScript eventScript) {
            this.eventScripts.add(eventScript);
            return self();
        }


        public SimpleStateAchievable build() {
            String isSatisfiedScript = String.join("() && ", isSatisfiedScripts);
            String isDisqualifiedScript = isDisqualifiedScripts.isEmpty() ? null : String.join("() || ", isDisqualifiedScripts);
            return new SimpleStateAchievable(initialState, isSatisfiedScript, isDisqualifiedScript, eventScripts.toArray(new EventScript[0]));
        }
    }
}
