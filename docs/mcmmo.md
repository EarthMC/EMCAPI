# mcMMO Endpoints
## Personal endpoint
Accessed at https://api.earthmc.net/v4/mcmmo

The mcMMO endpoint provides information about a player's mcMMO levels.
It is important to note that the information here is not public, and players can only access their own information, using their API key.

The rate limit for this endpoint is one request per hour per API key (and thus per player).

Example **POST** request
```json5
{
  "query": ["PLAYER_UUID"],
  "key": "API_KEY"
}
```
The player UUID must match the API key's owner.

Example **POST** response
```json5
[
  {
    "name": "Veyronity",
    "ACROBATICS": 21,
    "ALCHEMY": 0,
    "ARCHERY": 0,
    "AXES": 0,
    "CROSSBOWS": 0,
    "EXCAVATION": 0,
    "FISHING": 0,
    "HERBALISM": 0,
    "MACES": 0,
    "MINING": 1,
    "REPAIR": 0,
    "SALVAGE": 0,
    "SMELTING": 0,
    "SPEARS": 0,
    "SWORDS": 0,
    "TAMING": 0,
    "TRIDENTS": 0,
    "UNARMED": 0,
    "WOODCUTTING": 50
  }
]
```
The player's name and their level in each primary skill. The overall power level can be calculated by adding all the values.

## Top endpoint
Accessed at https://api.earthmc.net/v4/mcmmo-top

The mcMMO-top endpoint provides the leaderboard of a specified skill.  

The rate limit for this endpoint is one request per 2 minutes per API key.

Example **POST** request
```json5
{
  "query": ["SKILL"], // Skill name (E.g. 'FISHING', 'MINING'), or 'POWER' for the overall power leaderboard
  "key": "API_KEY"
}
```

Example **POST** response
```json5
[
  {
    "skill": "power",
    "1": {
      "player": "K1kimor",
      "level": 12078
    },
    "2": {
      "player": "Veyronity",
      "level": 72
    },
    "3": {
      "player": "Andre1098",
      "level": 29
    },
    "lastUpdated": 1779619278
  }
]
```
The information is not live, it is cached & updated every 15 minutes. The field `lastUpdated` provides the epoch second that the information was last updated at.
