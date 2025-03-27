# Nearby Endpoint
Accessed at https://api.earthmc.net/v3/aurora/nearby

Returns an array of all the elements of type `search_type` (`TOWN`) in a given `radius` of a target location of type `target_type` (`TOWN`|`COORDINATE`).

Use a **POST** request, and specify an array of nearby queries in the body.

<br>

Example **POST** request with a nearby query based on a town
```json5
{
  "query": [
    {
      "target_type": "TOWN",
      "target": "Melbourne",
      "search_type": "TOWN",
      "radius": 100
    }
  ]
}
```

Example **POST** request with a nearby query based on coordinates
```json5
{
  "query": [
    {
      "target_type": "COORDINATE",
      "target": [
        2000,
        10000
      ],
      "search_type": "TOWN",
      "radius": 10000
    }
  ]
}
```

Example **POST** response for a nearby query with `search_type` set to `TOWN` (the only type currently available)
```json5
[
  [
    {
      "name": "Jyväskylä",
      "uuid": "0b69c00d-c112-4ca0-a16c-ce551120e464"
    },
    {
      "name": "Watson",
      "uuid": "5851f859-0c4e-49bb-9e5c-5a2f9121585c"
    }
  ]
]
```