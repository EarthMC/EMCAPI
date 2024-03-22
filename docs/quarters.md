# Quarters Endpoint
Accessed at https://api.earthmc.net/v3/aurora/quarters

The following is an abridged version of the response from the above URL which contains every currently registered quarter within a JSON array
```json5
[
  "5fb3b17a-c67e-476e-b8ad-f030955ef8ea",
  "8c30b12d-ede6-41bb-9bfa-31d0523b9e80",
  "92f36e1d-1f31-4f4f-8a58-5f22a0c92c29",
  "3a30a7dc-4892-4de4-b3ed-d2768ae5ae26",
  "0b0dc879-2fee-45eb-a73f-30463ec92677"
]
```

Look up the specified quarter UUIDs to get quarter data, for example https://api.earthmc.net/v3/aurora/quarters?query=5fb3b17a-c67e-476e-b8ad-f030955ef8ea,8c30b12d-ede6-41bb-9bfa-31d0523b9e80
```json5
[
  {
    "uuid": "5fb3b17a-c67e-476e-b8ad-f030955ef8ea", // The quarter's UUID
    "type": "APARTMENT", // The quarter's type (https://github.com/Fruitloopins/Quarters/wiki/Quarter-Types)
    "owner": { // Nested values are null if the quarter has no owner
      "name": "Mcplayer21",
      "uuid": "57bcd815-d93a-4e4a-904f-d01b3459ce17"
    },
    "town": {
      "name": "Washington_DC",
      "uuid": "065caae9-5b75-4f2e-aabe-826a209e8247"
    },
    "timestamps": {
      "registered": 1697904112328, // A Unix timestamp representing the time the quarter was created
      "claimedAt": 1710726377127 // A Unix timestamp representing the time the quarter was claimed at, null if the quarter has no owner
    },
    "status": {
      "isEmbassy": true // True if the quarter is an embassy
    },
    "stats": {
      "price": null, // An integer representing the quarter's sale price, null if the quarter is not for sale
      "volume": 1890, // The total number of blocks within the quarter's bounds
      "numCuboids": 1 // The total amount of cuboids this quarter is made up of
    },
    "colour": [ // A JSON array representing the RGB values of the quarter
      64,
      194,
      121
    ],
    "trusted": [], // A JSON array representing all the trusted players in this quarter, empty if nobody is trusted
    "cuboids": [ // A JSON array representing every cuboid in this quarter
      {
        "pos1": [
          -18395,
          93,
          -8215
        ],
        "pos2": [
          -18381,
          88,
          -8235
        ]
      }
    ]
  }
]
```
