# Economy

The Economy is an important feature for survival servers, especially when players want to rent their regions to other players, or maybe they need to pay taxes to upkeep their regions.

If the economy feature is enabled (an economy plugin was found), 5 sub-features will be enabled by default:

## Bank

Each region has a bank, where region owners deposit and withdraw money. It's the safest place for players to keep their money!

!!! info "Information"

    This sub-feature cannot be disabled.

## Renting Regions

It's an option for players to rent their region to other players for an amount of money for a defined period.

```yaml
renting:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true

  # The minimum rent value.
  min-rent: 500.0 # $500

  # The maximum rent value.
  max-rent: 10000000.0 # $10M (M = Million)
```

## Selling Regions

It's an option for players to sell their region to other players for an amount of money.

```yaml
selling:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true

  # The minimum sell value.
  min-sell: 10.0 # $10

  # The maximum sell value.
  max-sell: 1000000000.0 # $1B (B = Billion)
```

## Region Upkeep

Require all region owners to pay for upkeep for every week (configurable) based on the number of chunks they've claimed.

!!! warning

    If you change one of the settings for the upkeep system, you must restart the server.

```yaml
upkeep:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true

  # How much should a region pay per chunk?
  # Example: A region has 8 claimed chunks. If the amount per chunk is $100, the region must pay $800.
  per-chunk: 100.0 # $100

  # Should the last claimed chunks, which the land could not pay for, be unclaimed?
  # Example: A region has 10 claimed chunks, but it can pay the upkeep only for 6, so the 4 last claimed chunks will be unclaimed.
  unclaim-chunks: true

  # Exclude region from upkeep, if it was recently created?
  # This is an important feature, because newly created regions may not have enough money to upkeep, so this setting will give a period of time for
  # region owners to pay in the future.
  # Use seconds below. To disable this setting, use 0 (no delay).
  start-upkeep: 604800 # 1 week (in seconds)

  # Set the upkeep timer for regions.
  # Example: If you set 1 week, and then a region paid the upkeep, the timer will reset to 1 week for THAT region. This means the timer won't make all
  # regions to upkeep at the same time.
  upkeep-timer: 604800 # 1 week (in seconds)
```

## Member Taxes

Require all members of a region to pay taxes to stay trusted for every week (configurable).

!!! warning

    If you change one of the settings for the member taxes system, you must restart the server.

```yaml
taxes:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true

  # The minimum tax value.
  min-tax: 0.0 # $0

  # The maximum tax value.
  max-tax: 10000.0 # $10k (k = Thousand)

  # Set the tax timer for region members:
  # Example: If you set 1 week, and then a member paid taxes, the timer will reset to 1 week for THAT member. This means the timer won't make all
  # members to pay taxes at the same time.
  tax-timer: 604800 # 1 week (in seconds)
```
