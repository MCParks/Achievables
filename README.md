
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

_Documentation forthcoming_