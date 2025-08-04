# Server Endpoint
Accessed at https://api.earthmc.net/v3/aurora/

The following is an example of the response to a **GET** request to the above URL. 
```json5
{
  "version": "1.21.4", // The server's current version as a string
  "moonPhase": "LAST_QUARTER", // The moon's current phase (https://jd.papermc.io/paper/1.20/io/papermc/paper/world/MoonPhase.html)
  "timestamps": {
    "newDayTime": 43200, // Time the new day occurs at
    "serverTimeOfDay": 15235 // The time of day in seconds in the server's timezone
  },
  "status": {
    "hasStorm": true, // True if the server is currently raining
    "isThundering": false // True if the server is currently thundering
  },
  "stats": {
    "time": 21068, // The amount of ticks that have passed within the current day
    "fullTime": 896133068, // The amount of ticks that have ever passed
    "maxPlayers": 200, // The total amount of players that can connect to the server
    "numOnlinePlayers": 184, // The current amount of online players
    "numOnlineNomads": 7, // The current amount of online players with no town
    "numResidents": 27858, // The total amount of currently registered Towny residents
    "numNomads": 14711, // The total amount of registered Towny residents who have no town
    "numTowns": 3007, // The total amount of currently registered Towny towns
    "numTownBlocks": 273417, // The total amount of town blocks across all towns
    "numNations": 513, // The total amount of currently registered Towny nations
    "numQuarters": 5875, // The total amount of Quarters on the server
    "numCuboids": 9773 // The total amount of cuboids within all quarters
  },
  "voteParty": {
    "target": 5000, // The total votes required to trigger a vote party
    "numRemaining": 4141 // The votes remaining before a vote party
  }
}
```