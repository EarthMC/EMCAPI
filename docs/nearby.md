# Nearby Endpoint
Accessed at https://api.earthmc.net/v4/nearby

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
      "search_type": "TOWN", // Returns a list of towns within the specified radius
      "radius": 100,
      "strict": true // Optional field. Default is false. 
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
      "search_type": "NATION", // Returns a list of nations whose capitals fall within the specified radius
      "radius": 10000
    }
  ]
}
```

Note: When checking if a town is in range, the distance between the center and the town's homeblock is checked. If the distance is smaller than the radius, the town is added to the response.  
If it's out of range but only by <=300 blocks, and `strict` is set to false (or not specified), all the town's townblocks are checked

Example **POST** response for a nearby query with `search_type` set to `TOWN`
```json5
[
  [
    {
      "name":"Jyväskylä",
      "uuid":"0b69c00d-c112-4ca0-a16c-ce551120e464"
    },
    {
      "name":"Watson",
      "uuid":"5851f859-0c4e-49bb-9e5c-5a2f9121585c"
    }
  ]
]
```