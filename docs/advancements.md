# Advancements Endpoint
Accessed at https://api.earthmc.net/v4/advancements

The advancements endpoint provides information about the first time an advancement was completed. The information here is live.  

Example **GET** response
```json5
{
  "minecraft:story/mine_diamond": { // Advancement name
    "player": "e25ad129-fe8a-4306-b1af-1dee1ff59841", // UUID of the player who completed it first
    "date": "2026-05-10" // The date it was completed
  },
  "minecraft:adventure/revaulting": {
    "player": "e25ad129-fe8a-4306-b1af-1dee1ff59841",
    "date": "2026-05-10"
  },
  "minecraft:adventure/root": {
    "player": "0bacd488-bc41-4f76-ba8b-50dc843efe49",
    "date": "2026-04-29"
  },
  "minecraft:adventure/under_lock_and_key": {
    "player": "e25ad129-fe8a-4306-b1af-1dee1ff59841",
    "date": "2026-05-10"
  },
  "minecraft:adventure/trade_at_world_height": {
    "player": "5b8274bf-b162-4336-85a0-48f9d5380a78",
    "date": "2026-04-12"
  },
  "minecraft:story/smelt_iron": {
    "player": "e25ad129-fe8a-4306-b1af-1dee1ff59841",
    "date": "2026-05-10"
  },
  "minecraft:adventure/sleep_in_bed": {
    "player": "5b8274bf-b162-4336-85a0-48f9d5380a78",
    "date": "2026-04-12"
  },
  "minecraft:adventure/fall_from_world_height": {
    "player": "5b8274bf-b162-4336-85a0-48f9d5380a78",
    "date": "2026-04-07"
  }
}
```
