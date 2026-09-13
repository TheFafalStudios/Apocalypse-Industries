# Tornado Physics compatibility

## Current wind behavior

`windMode = "SAILS_ONLY"` keeps ordinary Weather2 global wind from pushing
all Sable physics objects while retaining wind effects for sail and lift
providers used by Aeronautics vehicles. Tornado physics remains enabled.

## TODO: configurable wind elevation

Implement a configurable elevation profile for ordinary global wind:

- Global wind is 0 below a configurable altitude.
- Wind ramps gradually from 0% to 100% over a configurable transition height.
- Tornado-specific forces remain separate and can still affect ground-level
  physics objects.

Suggested future configuration:

```toml
minWindAltitude = 120.0
windTransitionHeight = 40.0
```

This altitude behavior is documentation only and is not implemented yet.
