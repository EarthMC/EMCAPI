# Discord Endpoint
Accessed at https://api.earthmc.net/v3/aurora/discord?query=

Determine a player's Discord ID from their Minecraft UUID and vice versa using DiscordSRV's link feature.
The player needs to have linked their account beforehand (`/discord link` in game).

Use a **POST** request, and specify an array of discord queries in the body.
Use `type` to specify the nature of your `target`.

<br>

Example **POST** request
```json5
{
  "query": [
    {
      "type": "minecraft",
      "target": "fed0ec4a-f1ad-4b97-9443-876391668b34" // <- Minecraft UUID
    },
    {
      "type": "discord",
      "target": "160374716928884736" // <- Discord ID 
    }
  ]
}
```

Example **POST** response
```json5
[
  {
    "id": "160374716928884736", // Discord ID 
    "uuid": "fed0ec4a-f1ad-4b97-9443-876391668b34" // Minecraft UUID
  },
  {
    "id": "160374716928884736",
    "uuid": "f17d77ab-aed4-44e7-96ef-ec9cd473eda3"
  }
]
```