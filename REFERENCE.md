# Reference Implementation

This library includes a reference implementation that can be used as a starting point for integrating Achievables into your own project. The reference implementation provides a CLI (Command Line Interface) for registering Achievables, submitting Events, and tracking achievement progress.

## Components

The reference implementation consists of the following components:

1. `ReferencePlayer` - A simple implementation of the `AchievablePlayer` interface
2. `ReferenceAchievableManager` - An implementation of the `AchievableManager` interface with in-memory storage
3. `TestEvent` - A simple event for testing
4. `AchievablesCLI` - A CLI application for interacting with the Achievables system

## Using the CLI

To run the reference implementation:

```
java -cp achievables.jar us.mcparks.achievables.reference.AchievablesCLI
```

### Available Commands

- `help` - Show the help message
- `exit`, `quit` - Exit the application
- `register <file>` - Register an achievable from a BIGAL file
- `list` - List all registered achievables
- `player add <id> <name>` - Add a player
- `player list` - List all players
- `player remove <id>` - Remove a player
- `event <type> <player> <data>` - Submit an event
- `status <player>` - Show achievement status for a player
- `state <player> <uuid>` - Show state for a player and achievable

### Example Usage

1. Start the CLI
2. Add a player:
   ```
   > player add player1 Alice
   ```
3. Register an achievable from a BIGAL file:
   ```
   > register example.bigal
   ```
4. Submit events:
   ```
   > event TestEvent player1 "Test data"
   ```
5. Check player's achievement status:
   ```
   > status player1
   ```
6. Check player's state for a specific achievable:
   ```
   > state player1 <uuid>
   ```

## Using in Your Own Project

You can use the reference implementation as a starting point for your own implementation:

1. Extend `ReferencePlayer` or create your own `AchievablePlayer` implementation
2. Use `ReferenceAchievableManager` directly or as a reference for implementing your own manager
3. Create your own event types for your specific application needs

## Example BIGAL Script

An example BIGAL script is included at `src/main/java/us/mcparks/achievables/reference/example.bigal`. This script defines a simple achievable that tracks counters for events.