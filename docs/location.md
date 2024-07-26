# Location Endpoint
Accessed at https://api.earthmc.net/v3/aurora/location

Example POST request
```json5
{
  "query": [
    [
      0,
      0
    ],
    [
      100,
      100
    ]
  ]
}
```

Example POST response
```json5
[
  {
    "location": {
      "x":0,
      "z":0
    },
    "isWilderness":false,
    "town": {
      "name":"Jyväskylä",
      "uuid":"0b69c00d-c112-4ca0-a16c-ce551120e464"
    },
    "nation": {
      "name":"Finland",
      "uuid":"ae16c3c0-f8ab-4715-8553-019168008c49"
    }
  }
]
```