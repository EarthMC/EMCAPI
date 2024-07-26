# Discord Endpoint
Accessed at https://api.earthmc.net/v3/aurora/discord?query=

Example POST request
```json5
{
  "query": [
    {
      "type": "minecraft",
      "target": "fed0ec4a-f1ad-4b97-9443-876391668b34"
    },
    {
      "type": "discord",
      target: "160374716928884736"
    }
  ]
}
```

Example POST response
```json5
[
  {
    "fed0ec4a-f1ad-4b97-9443-876391668b34": {
      "id": "160374716928884736",
    }
  },
  {
    "160374716928884736": {
      "uuid": "f17d77ab-aed4-44e7-96ef-ec9cd473eda3"
    }
  }
]
```