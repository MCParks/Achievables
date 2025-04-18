
# Achievables

This repository contains a Java library that defines "Achievables," a system that can be used to define Achievements, Daily Challenges, Quests, and other things that players can "achieve" in a video game-like setting.

This codebase also includes an extensible Groovy-based programming language called BIGAL (BigAl's Integrated Groovy Achievement Language) that allows you to define Achievables (with other metadata necessary to define an Achievement) in a human-readable and version-controllable format.

We use Achievables on MCParks to power Daily Challenges and Achievements.

# BIGAL version 0

Version 0 of BIGAL


## Example: Magic Kingdom Mountaineer

```groovy
achievement {
    syntaxVersion 0
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
        (state.rodeSpaceMountain == true) && (state.rodeSplashMountain == true) && (state.rodeBigThunderMountain == true)
    }

    deactivators {
        state.warpedOrTeleported == true
    }

    events {
        on("CompleteRideEvent") {
            if (event.rideId == 32) {
                state.rodeSpaceMountain = true
            }
            if (event.rideId == 7) {
                state.rodeBigThunderMountain = true
            }
            if (event.rideId == 9) {
                state.rodeSplashMountain = true
            }
        }
        on("TeleportPlayerToPlayerEvent") {
            state.warpedOrTeleported = true
        }
        on("PlayerWarpEvent") {
            state.warpedOrTeleported = true
        }
    }
}
```

### Achievement Metadata

```groovy
    syntaxVersion 0
    name "Magic Kingdom Mountaineer"
    description "Ride the three mountains of Magic Kingdom without warping or teleporting"
    icon "IRON_SPADE:20"
    reward "MONEY:500"
```
- `syntaxVersion 0` indicates that this achievement is written using version 0 of the interpreter. This way, the achievement backend knows how to parse the rest of the file
- `name` and `description` will be shown to guests in various places to identify this achievement. The name should be snappy, may involve a pun, reference, or otherwise use wit to sum up what this achievement is all about
- `icon` represents the ItemStack that should be used as the GUI icon for this achievement when it is rendered in an inventory GUI on the server. You may use `<MATERIAL_NAME:DAMAGE_VALUE>` notation to describe an item of the given damage value with `{Unbreakable:1b}` set on it
- `reward` will be used in the future to pay out rewards for completing the achievement. Presently this value is ignored.

### Main mechanics

#### State

An achievement has some initial default `state` of variables.

```groovy
    state {
        rodeSpaceMountain = false
        rodeBigThunderMountain = false
        rodeSplashMountain = false
        warpedOrTeleported = false
    }
```

Each variable represents something we want to keep track of to use when determining whether a player has completed the achievement.
In the case of `Magic Kingdom Mountaineer`, we want to keep track of whether the player has ridden each of the mountains -- `rodeSpaceMountain`, `rodeBigThunderMountain`, and `rodeBigThunderMountain`.
We may also want to keep track of data to determine whether a player is _disqualified_ for an achievement and needs to start from scratch. In this case, we want to keep track of whether a player `warpedOrTeleported`.
Each of these variables is represented by a boolean `true` or `false` value. State variables may be other types too, like integers or strings of text.

#### Activators & Deactivators

The `activators` block is an expression that must evaluate to a boolean `true` or `false`. An achievement is considered complete when the `activators` block evaluates to `true`.

```groovy
    activators {
        (state.rodeSpaceMountain == true) && (state.rodeSplashMountain == true) && (state.rodeBigThunderMountain == true)
    }
```

Generally, you'll be querying the `state` of the achievement to determine whether the achievement has been completed. To access an element of the `state`, write `state.<variableName>` as you see above.

Some comparators that may be useful when writing an activator are:
- "&&" (logical "and," evaluates to `true` when both inputs are `true`)
- "||" (logical "or," evaluates to `true`)
- "!" (logical "not" -- put this in front of a boolean to turn `true` into `false` and vice versa)
- "<" and ">" ("less than" and "greater than", use to compare numbers on either side)
- "==" ("equals", checks if both sides are the same)

When your achievement depends on multiple things being `true`, it is wise to wrap things in parentheses so the order of operations is obvious.

Similarly to the `activators` block, the `deactivators` block is a boolean expression that signifies that the player done something that should disqualify their progress. If the `deactivators` block ever evaluates to `true`, the player's `state` will be reset to the default `state` and they will have to start over.

```groovy
    deactivators {
        state.warpedOrTeleported == true
    }
```

#### Changing the State: Responding to Events

In order to transform the achievement's state, we listen for events that occur in the game. A full list of events that exist on MCParks can be found [here](). When an event occurs, we can write code to respond to it.

```groovy
    events {
        on("CompleteRideEvent") {
            if (event.rideId == 32) {
                state.rodeSpaceMountain = true
            }
            if (event.rideId == 7) {
                state.rodeBigThunderMountain = true
            }
            if (event.rideId == 9) {
                state.rodeSplashMountain = true
            }
        }
        on("TeleportPlayerToPlayerEvent") {
            state.warpedOrTeleported = true
        }
        on("PlayerWarpEvent") {
            state.warpedOrTeleported = true
        }
    }
```

In this example, "Magic Kingdom Mountaineer" listens for three different events: `CompleteRideEvent`, `TeleportPlayerToPlayerEvent`, and `PlayerWarpEvent`.

When a `TeleportPlayerToPlayerEvent` or `PlayerWarpEvent` occurs, we set the `warpedOrTeleported` variable to `true`.

When a `CompleteRideEvent` occurs, we need to determine whether the ride that was completed was one of the rides that we care about. We can do this by checking the `rideId` field of the event.
To query the fields of an event, use `event.<fieldName>` as you see above. You also have access to the `state` should you need to compare fields of the event to the current state of the achievement.

To change the `state` of the achievement, simply write `state.<variableName> = <newValue>`.


# Integrating this library into your own Java project

The examples given above include events specific to MCParks. There is some assembly required if you wish to use this library for your own Minecraft server, video game, or other JVM project.

## Basic Integration Steps

### 1. Add Achievables as a Dependency

Add the Achievables library to your build configuration:

#### For Gradle:
```groovy
dependencies {
    implementation 'us.mcparks:achievables:1.0.0' // Replace with the actual version
}
```

#### For Maven:
```xml
<dependency>
    <groupId>us.mcparks</groupId>
    <artifactId>achievables</artifactId>
    <version>1.0.0</version> <!-- Replace with the actual version -->
</dependency>
```

### 2. Implement the AchievableManager Interface

Create a class that implements `us.mcparks.achievables.AchievableManager`. This class will be responsible for managing the lifecycle of achievements and processing triggers.

```java
public class YourAchievableManager implements us.mcparks.achievables.AchievableManager {
    // Implementation of required methods
    // ...
}
```

### 3. Initialize the Achievables System

In your application startup code, initialize the Achievables system:

```java
// Initialize with your implementation of AchievableManager
Achievables.initialize(yourAchievableManager);

// Set logging
Achievables.getInstance().setLogger(yourLogger);

// Configure metadata builder (optional)
Achievables.getInstance().setAchievableMetaBuilderSupplier(() -> YourAchievementMeta.builder());

// Configure JSON serialization/deserialization for your player type
Achievables.customizeGson(gsonBuilder -> 
    gsonBuilder.registerTypeAdapter(AchievablePlayer.class, new YourPlayerDeserializer()));
Achievables.customizeGson(gsonBuilder -> 
    gsonBuilder.registerTypeAdapter(AchievablePlayer.class, new YourPlayerSerializer()));
```

## Key Components to Implement

### 1. Player Representation

Create a class that implements `AchievablePlayer` to represent your game's players:

```java
public class YourPlayer implements AchievablePlayer {
    // Player implementation
}
```

Create serializer and deserializer for your player class:

```java
public class YourPlayerSerializer implements JsonSerializer<AchievablePlayer> {
    @Override
    public JsonElement serialize(AchievablePlayer player, Type type, JsonSerializationContext context) {
        // Convert YourPlayer to JSON
    }
}

public class YourPlayerDeserializer implements JsonDeserializer<AchievablePlayer> {
    @Override
    public AchievablePlayer deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        // Convert JSON to YourPlayer
    }
}
```

### 2. Event System

Define your game's events and map them to the Achievables event system:

```java
public class YourGameEvent implements us.mcparks.achievables.events.Event {
    // Event implementation
}

public class YourPlayerJumpEvent extends YourGameEvent {
    // Player jump event implementation
}

public class YourItemCollectEvent extends YourGameEvent {
    // Item collect event implementation
}
```

Implement the `getEventClass` method in your AchievableManager:

```java
@Override
public Class<? extends Event> getEventClass(String eventClassName) {
    // Map event class names to your game's event classes
    switch(eventClassName) {
        case "PlayerJumpEvent": return YourPlayerJumpEvent.class;
        case "ItemCollectEvent": return YourItemCollectEvent.class;
        // Add more mappings as needed (more likely: do this dynamically with reflection)
        default: return null;
    }
}
```

### 3. State Persistence

Implement methods to store and retrieve achievement state:

```java
@Override
public void setPlayerState(AchievablePlayer player, StatefulAchievable achievable, Map<String,Object> state, boolean persist) {
    // Store the player's state for the achievable
    // If persist is true, save to your persistent storage (database, etc)
}

@Override
public Map<String,Object> getPlayerState(AchievablePlayer player, StatefulAchievable achievable) {
    // Retrieve the player's state for the achievable from your storage
}
```

### 4. Achievement Completion Tracking

Implement methods to track completed achievements:

```java
@Override
public void completeAchievable(Achievable achievable, AchievablePlayer player) {
    // Mark the achievable as completed for the player
    // Store this information in your persistent storage (database, etc)
    // Trigger any rewards or notifications
}

@Override
public boolean isCompleted(Achievable achievable, AchievablePlayer player) {
    // Check if the player has completed the achievable
}
```



## Loading and Registering Achievables

Your `AchievableManager` needs a data structure to hold the loaded achievables. It is recommended
to use a `Multimap<AchievableTrigger.Type, Achievable>` to index the achievables by their trigger type to performantly handle event triggers.
```java
// in your AchievableManager
Multimap<AchievableTrigger.Type, Achievable> achievables = Multimaps.synchronizedSetMultimap(HashMultimap.create());

public void registerAchievable(Achievable achievable) {
        for (AchievableTrigger.Type triggerType : achievable.getTriggers()) {
            //System.out.println("Registering achievable " + achievable.getUUID().toString() + "with trigger " + triggerType.toString());
            achievables.put(triggerType, achievable);
        }
    }

public void unregisterAchievable(Achievable achievable) {
    for (AchievableTrigger.Type triggerType : achievable.getTriggers()) {
        achievables.remove(triggerType, achievable);
    }
}

```
Load your BIGAL achievement definitions and register them with your manager:

```java
List<String> bigAlFileContents = // Load your BIGAL files;

for (String fileContent : bigAlFileContents) {
    // Parse the BIGAL file content
    AchievableWithMeta parsedAchievable = BigalsIntegratedGroovyAchievementLanguage.interpret(fileContent);
    //
    
    // Register the achievable with your manager
    yourAchievableManager.registerAchievable(parsedAchievable.getAchievable());

    // you might want to use the metadata for other purposes! We use it to display the achievement in the GUI, keep track of the rewards, determine whether or not the file should be activated, etc.
}

```

## Processing Events and Triggers

To process game events and convert them to achievement triggers:

```java
// In your event handling system
public void onYourGameEvent(YourGameEvent event) {
    // Convert to an Achievable Trigger
    AchievableTrigger trigger = new AchievableTrigger(
        AchievableTrigger.Type.EVENT, 
        event,
        (YourPlayer) event.getPlayer()
    );
    
    // Process the trigger
    yourAchievableManager.processTrigger(trigger);
}
```


```java
// In your AchievableManager implementation
@Override
public void processTrigger(AchievableTrigger trigger) {
    // Check if the trigger matches any registered achievements
    for (Achievable achievable : registeredAchievables.get(trigger.getType())) {
        try {
            achievable.process(trigger);
        } catch (Exception e) {
            // do whatever you want with caught exceptions
        }
        
    }
}
```

You may wish to set up a designated thread to process triggers in the background, especially if your game has a lot of events firing frequently. This will help keep your main game loop responsive. 