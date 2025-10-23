# Flags

Flags are specific permissions applied to global players, members, and nature.

There are **35+** flags for players and **20+** flags for the environment.

To set default flag values, use `true` for **Allow**, and `false` for **Deny**.

```yaml
default-players-flags:
  break-blocks: false
  place-blocks: false
  containers: false
  ...
  
default-world-flags:
  passive-entities-spawn: true
  hostile-entities-spawn: true
  entities-grief: false
  ...
```

## Player Flags
| Flag                    | Default | Explanation                                                                             |
| ----------------------- | :-------: | --------------------------------------------------------------------------------------- |
| break-blocks            | Deny    | Allow players to break blocks?                               |
| place-blocks            | Deny    | Allow players to place blocks?                               |
| containers              | Deny    | Allow players to open containers?                            |
| doors                   | Deny    | Allow players to open doors?                                 |
| trap-doors              | Deny    | Allow players to open trap doors?                            |
| fence-gates             | Deny    | Allow players to open fence gates?                           |
| use-anvil               | Deny    | Allow players to use anvils?                                 |
| redstone                | Deny    | Allow players to use redstone?                               |
| levers                  | Deny    | Allow players to use levers?                                 |
| buttons                 | Deny    | Allow players to use buttons?                                |
| pressure-plates         | Deny    | Allow players to use pressure plates?                        |
| use-bells               | Deny    | Allow players to use bells?                                  |
| trigger-tripwire        | Deny    | Allow players to trigger tripwires?                          |
| frost-walker            | Deny    | Allow players to use frost walker enchanted boots?           |
| harvest-crops           | Deny    | Allow players to harvest (break) crops?                      |
| block-trampling         | Deny    | Allow players to trample Farmland and Turtle Eggs?           |
| general-interaction     | Deny    | Allow players to interact (in general)?                      |
| armor-stands            | Deny    | Allow players to take off armor from armor stands?           |
| interact-entities       | Deny    | Allow players to interact with entities?                     |
| item-frame-rotation     | Deny    | Allow players to rotate items from item frames?              |
| damage-passive-entities | Deny    | Allow players to kill passive (Cow, Sheep…) entities?        |
| damage-hostile-entities | Deny    | Allow players to kill hostile (Zombie, Skeleton…) entities?  |
| trade-villagers         | Deny    | Allow players to trade with Villagers?                       |
| ignite                  | Deny    | Allow players to ignite fire?                                |
| vehicles                | Deny    | Allow players to ride vehicles (Minecarts, Horses…)?         |
| teleport-spawn          | Deny    | Allow players to teleport to region’s spawn?                 |
| passthrough             | Allow   | Allow players to go inside the region?                       |
| pvp                     | Deny    | Allow players to fight each other?                           |
| take-fall-damage        | Deny    | Allow players to take fall damage?                           |
| teleport                | Deny    | Allow players to teleport with Ender Pearl or Chorus Fruit?  |
| throw-potions           | Deny    | Allow players to throw potions?                              |
| pickup-items            | Allow   | Allow players to pick items?                                 |
| sleep                   | Deny    | Allow players to sleep within the region?                    |
| trigger-raid            | Deny    | Allow players to trigger raid?                               |
| elytra                  | Deny    | Allow players to fly with an Elytra?                         |

## World Flags

| Flag                     | Default | Explanation                                                                              |
| ------------------------ | :-------: | ---------------------------------------------------------------------------------------- |
| passive-entities-spawn   | Allow   | Allow passive entities to spawn?                              |
| hostile-entities-spawn   | Allow   | Allow hostile entities to spawn?                              |
| entities-grief           | Deny    | Allow entities (except Creeper) to grief?                     |
| entities-damage-entities | Deny    | Allow entities to kill each other?                            |
| leaves-decay             | Allow   | Allow leaves to decay?                                        |
| fire-spread              | Deny    | Allow fire to spread within the region?                       |
| liquid-flow              | Deny    | Allow any liquid to flow into the region?                     |
| explosions-damage        | Deny    | Allow explosions (except Wither) to damage the region?        |
| wither-damage            | Deny    | Allow wither to damage the region?                            |
| wilderness-pistons       | Deny    | Allow wilderness pistons to push/pull blocks to your region?  |
| wilderness-dispensers    | Deny    | Allow wilderness dispensers to dispense inside your region?   |
| wilderness-minecarts     | Deny    | Allow wilderness minecarts to enter your region?              |
| plant-growth             | Allow   | Allow plants to grow inside the region?                       |
| grass-growth             | Allow   | Allow grass to spread inside the region?                      |
| sculk-spread             | Allow   | Allow sculk to spread inside the region?                      |
| player-glowing           | Allow   | Give the glowing effect to players?                           |
| snow-melting             | Allow   | Allow snow to melt?                                           |
| ice-melting              | Allow   | Allow ice to melt?                                            |
| snowman-trails           | Allow   | Allow Snow Golem to leave snow trails?                        |
| windcharge-burst         | Deny    | Allow Windcharges to burst inside the region?                 |

