# Other Configuration

## Welcome Signs

Replace the command from `/region visit [region]` to `/region visit [player]`.

```yaml
welcome-signs:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: false
```

For full guide on Welcome signs usage, [click here](../Usage/Welcome%20Signs.md).

## Sub-Areas

Allow sub-areas to be created on the server.

```yaml
sub-areas:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true
```

## Disabled worlds

List of worlds where players cannot claim chunks.

```yaml
disabled-worlds:
  - "world_the_end"
  - "creative"
  - "factions"
  - ...
```

## Disabled flags

Flags that players cannot modify. You can find the list of flags [here](https://tfagaming.gitbook.io/homestead/configuration/flags).

```yaml
disabled-flags:
  - "use-bells"
  - "no-fall-damage"
  - ...
```

## Disabled particles

Disable particles from spawning around region or chunk's borders.

```yaml
disabled-particles: false
```
