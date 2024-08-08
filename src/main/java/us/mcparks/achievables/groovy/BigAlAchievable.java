package us.mcparks.achievables.groovy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import us.mcparks.achievables.Achievables;
import us.mcparks.achievables.events.Event;
import us.mcparks.achievables.events.PlayerEvent;
import us.mcparks.achievables.framework.AbstractStatefulAchievable;
import us.mcparks.achievables.framework.Achievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.triggers.AchievableTrigger;
import us.mcparks.achievables.triggers.EventAchievableTrigger;
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.util.*;
import java.util.concurrent.ExecutionException;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class BigAlAchievable extends AbstractStatefulAchievable {
    String serializedInitialState;
    Closure<Boolean> satisfiedScript;
    Closure<Boolean> disqualifiedScript;
    Multimap<AchievableTrigger.Type, Closure> eventHandlers;
    @EqualsAndHashCode.Include
    UUID uuid;

    public BigAlAchievable(UUID uuid, Map<String, Object> initialState, Closure<Boolean> isSatisfied, Closure<Boolean> isDisqualified, EventClosureScript... eventScripts) {
        this.serializedInitialState = AchievableGsonManager.getGson().toJson(initialState);
        this.satisfiedScript = isSatisfied;
        this.disqualifiedScript = isDisqualified;
        this.uuid = uuid;
        this.eventHandlers = HashMultimap.create();
        for (EventClosureScript handler : eventScripts) {
            this.eventHandlers.put(new AchievableTrigger.Type(handler.eventClass.getCanonicalName()), handler.closure);
        }
    }

    @Override
    public Collection<AchievableTrigger.Type> getTriggers() {
        return eventHandlers.keySet();
    }

    @Override
    public boolean isSatisfied(AchievablePlayer player) {
        ScriptThisObject obj = ScriptThisObject.of(player, getPlayerState(player), null);
        return satisfiedScript.rehydrate(null, obj, obj).call();
    }

    public boolean isDisqualified(AchievablePlayer player) {
        if (disqualifiedScript == null) return false;

        ScriptThisObject obj = ScriptThisObject.of(player, getPlayerState(player), null);
        return disqualifiedScript.rehydrate(null, obj, obj).call();
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
                            ScriptThisObject obj = ScriptThisObject.of(player, getPlayerState(player), ((EventAchievableTrigger) trigger).getEvent());
                            script.rehydrate(null, obj, obj).call();
                            try {
                                Achievables.getInstance().getAchievableManager().setPlayerState(player, this, obj.state);
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

    @Override
    public Map<String, Object> getInitialState() {
        return AchievableGsonManager.getGson().fromJson(serializedInitialState, new TypeToken<Map<String, Object>>() {
        }.getType());
    }


    public BigAlAchievable.Builder toBuilder() {
        return BigAlAchievable.builder()
                .uuid(uuid)
                .withInitialState(getInitialState())
                .addSatisfiedScript(satisfiedScript)
                .addDisqualifiedScript(disqualifiedScript)
                .addEventHandlers(eventHandlers.entries().stream().map(entry -> {
                    try {
                        return EventClosureScript.of((Class<? extends Event>) Class.forName(entry.getKey().toString()), entry.getValue());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toArray(EventClosureScript[]::new));
    }

    public static BigAlAchievable.Builder builder() {
        return new BigAlAchievable.Builder();
    }

    @RequiredArgsConstructor(staticName = "of")
    static class ScriptThisObject {
        final AchievablePlayer player;
        final Map<String, Object> state;
        final Event event;
    }

    public static class Builder {
        Map<String, Object> initialState = new HashMap<>();
        List<Closure<Boolean>> isSatisfied = new ArrayList<>();
        List<Closure<Boolean>> isDisqualified = new ArrayList<>();
        List<EventClosureScript> eventScripts = new ArrayList<>();
        UUID uuid;

        public Builder withInitialState(Map<String, Object> initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder addState(String key, Object initialValue) {
            if (this.initialState == null) {
                this.initialState = new HashMap<>();
            }
            initialState.put(key, initialValue);
            return this;
        }

        public Builder addSatisfiedScript(Closure<Boolean> script) {
            if (script == null) return this;

            this.isSatisfied.add(script);
            return this;
        }

        public Builder addDisqualifiedScript(Closure<Boolean> script) {
            if (script == null) return this;

            this.isDisqualified.add(script);
            return this;
        }

        public Builder addEventHandler(EventClosureScript script) {
            this.eventScripts.add(script);
            return this;
        }

        public Builder addEventHandlers(EventClosureScript... scripts) {
            this.eventScripts.addAll(Arrays.asList(scripts));
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public BigAlAchievable build() {
            //todo combine closures
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }

            return new BigAlAchievable(uuid, initialState,
                    andClosures(isSatisfied),
                    isDisqualified.isEmpty() ? null : orClosures(isDisqualified),
                    eventScripts.toArray(new EventClosureScript[0]));
        }

        /**
         * Combines a list of closures into a single closure that returns true if all closures return true.
         * @param closures A list of dehydrated closures
         * @return A single closure that returns true if all closures return true
         */
        private Closure<Boolean> andClosures(List<Closure<Boolean>> closures) {
            return new Closure<Boolean>(null) {
                @Override
                public Boolean call() {
                    for (Closure<Boolean> closure : closures) {
                        if (!closure.rehydrate(getDelegate(), getOwner(), getThisObject()).call()) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

        private Closure<Boolean> orClosures(List<Closure<Boolean>> closures) {
            return new Closure<Boolean>(null) {
                @Override
                public Boolean call() {
                    for (Closure<Boolean> closure : closures) {
                        if (closure.rehydrate(getDelegate(), getOwner(), getThisObject()).call()) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }

    }
}
