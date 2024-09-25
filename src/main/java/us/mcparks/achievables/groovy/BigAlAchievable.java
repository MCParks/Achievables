package us.mcparks.achievables.groovy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import us.mcparks.achievables.Achievables;
import us.mcparks.achievables.events.Event;
import us.mcparks.achievables.events.PlayerEvent;
import us.mcparks.achievables.framework.AbstractStatefulAchievable;
import us.mcparks.achievables.framework.AchievablePlayer;
import us.mcparks.achievables.framework.BackfillableAchievable;
import us.mcparks.achievables.triggers.AchievableTrigger;
import us.mcparks.achievables.triggers.EventAchievableTrigger;
import us.mcparks.achievables.utils.AchievableGsonManager;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class BigAlAchievable extends AbstractStatefulAchievable implements BackfillableAchievable {
    Closure<Boolean> satisfiedScript;
    Closure<Boolean> disqualifiedScript;
    Multimap<AchievableTrigger.Type, Closure> eventHandlers;
    Multimap<AchievableTrigger.Type, Closure> staticEventHandlers;

    @Setter static Supplier<Object> backfillDataSupplier = null;

    // optional script that can look up offline info to backfill state
    Closure backfillScript;

    @EqualsAndHashCode.Include
    UUID uuid;

    public BigAlAchievable(UUID uuid, Map<String, Object> initialState, Map<String, Object> initialStaticState, Closure<Boolean> isSatisfied, Closure<Boolean> isDisqualified, Closure backfillScript, EventClosureScript... eventScripts) {
        super(AchievableGsonManager.getGson().toJson(initialState), AchievableGsonManager.getGson().toJson(initialStaticState));
        this.satisfiedScript = isSatisfied;
        this.disqualifiedScript = isDisqualified;
        this.uuid = uuid;
        this.eventHandlers = HashMultimap.create();
        this.staticEventHandlers = HashMultimap.create();
        for (EventClosureScript handler : eventScripts) {
            if (handler.isStatic) {
                System.out.println("We have a static event handler for " + handler.eventClass.getCanonicalName());
                this.staticEventHandlers.put(new AchievableTrigger.Type(handler.eventClass.getCanonicalName()), handler.closure);
            } else {
                this.eventHandlers.put(new AchievableTrigger.Type(handler.eventClass.getCanonicalName()), handler.closure);
            }
        }
        this.backfillScript = backfillScript;
    }

    @Override
    public Collection<AchievableTrigger.Type> getTriggers() {
        Set<AchievableTrigger.Type> triggers = new HashSet<>();
        triggers.addAll(staticEventHandlers.keySet());
        triggers.addAll(eventHandlers.keySet());
        return triggers;
    }

    @Override
    public boolean isSatisfied(AchievablePlayer player) {
        ScriptThisObject obj = ScriptThisObject.of(player, getPlayerState(player), getStaticState(), null);
        return satisfiedScript.rehydrate(null, obj, obj).call();
    }

    public boolean isDisqualified(AchievablePlayer player) {
        if (disqualifiedScript == null) return false;

        ScriptThisObject obj = ScriptThisObject.of(player, getPlayerState(player), getStaticState(), null);
        return disqualifiedScript.rehydrate(null, obj, obj).call();
    }

    @Override
    public void process(AchievableTrigger trigger) {
        // First, check if this achievable is listening to the event statically
        if (staticEventHandlers.containsKey(trigger.getType())) {
            staticEventHandlers.get(trigger.getType()).forEach(
                    script -> {
                        ScriptThisObject obj = ScriptThisObject.of(null, null, getStaticState(), ((EventAchievableTrigger) trigger).getEvent());
                        script.rehydrate(null, obj, obj).call();
                        try {
                            Achievables.getInstance().getAchievableManager().setStaticState(this, obj.shared);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
        }

        // Then, use the default process method to handle it for each player if it also appears in the non-static event handlers
        if (eventHandlers.containsKey(trigger.getType())) {
            super.process(trigger);
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
                            ScriptThisObject obj = ScriptThisObject.of(player, getPlayerState(player), getStaticState(), ((EventAchievableTrigger) trigger).getEvent());
                            script.rehydrate(null, obj, obj).call();
                            try {
                                Achievables.getInstance().getAchievableManager().setPlayerState(player, this, obj.state);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        });


                if (isDisqualified(player)) {
                    try {
                        Achievables.getInstance().getAchievableManager().setPlayerState(player, this, getInitialPlayerState());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Achievables.getInstance().getLogger().warning("Failed to reset player state for achievable");
                    }
                }
            }
        }
    }

    @Override
    public void processBackfill(AchievablePlayer player) {
        if (backfillScript != null) {
            BackfillScriptThisObject obj = BackfillScriptThisObject.of(player, getPlayerState(player), uuid, backfillDataSupplier.get());
            backfillScript.rehydrate(null, obj, obj).call();
            try {
                Achievables.getInstance().getAchievableManager().setPlayerState(player, this, obj.state);

                if (isSatisfied(player)) {
                    Achievables.getInstance().getAchievableManager().completeAchievable(this, player);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    public BigAlAchievable.Builder toBuilder() {
        return BigAlAchievable.builder()
                .uuid(uuid)
                .withInitialState(getInitialPlayerState())
                .withInitialStaticState(getInitialStaticState())
                .addSatisfiedScript(satisfiedScript)
                .addDisqualifiedScript(disqualifiedScript)
                .setBackfillScript(backfillScript)
                .addEventHandlers(eventHandlers.entries().stream().map(entry -> {
                    try {
                        return EventClosureScript.of((Class<? extends Event>) Class.forName(entry.getKey().toString()), entry.getValue(), false);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).toArray(EventClosureScript[]::new))
                .addEventHandlers(staticEventHandlers.entries().stream().map(entry -> {
                    try {
                        return EventClosureScript.of((Class<? extends Event>) Class.forName(entry.getKey().toString()), entry.getValue(), true);
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
        final Map<String, Object> shared;
        final Event event;
    }

    @RequiredArgsConstructor(staticName = "of")
    static class BackfillScriptThisObject {
        final AchievablePlayer player;
        final Map<String, Object> state;
        final UUID achievableUUID;
        final Object backfillData;
    }

    public static class Builder {
        Map<String, Object> initialState = new HashMap<>();
        Map<String, Object> initialStaticState = new HashMap<>();
        List<Closure<Boolean>> isSatisfied = new ArrayList<>();
        List<Closure<Boolean>> isDisqualified = new ArrayList<>();
        List<EventClosureScript> eventScripts = new ArrayList<>();
        UUID uuid;

        Closure backfillScript = null;

        public Builder withInitialState(Map<String, Object> initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder withInitialStaticState(Map<String, Object> initialStaticState) {
            this.initialStaticState = initialStaticState;
            return this;
        }

        public Builder addState(String key, Object initialValue) {
            if (this.initialState == null) {
                this.initialState = new HashMap<>();
            }
            initialState.put(key, initialValue);
            return this;
        }

        public Builder setBackfillScript(Closure backfillScript) {
            this.backfillScript = backfillScript;
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

            return new BigAlAchievable(uuid, initialState, initialStaticState,
                    andClosures(isSatisfied),
                    isDisqualified.isEmpty() ? null : orClosures(isDisqualified),
                    backfillScript,
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
