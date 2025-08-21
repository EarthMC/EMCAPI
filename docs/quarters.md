# Quarters Endpoint
Accessed at https://api.earthmc.net/v3/aurora/quarters

The following is an abridged version of the response to a **GET** request to the above URL which contains every currently registered quarter within a JSON array.
```json5
[
  {
    "name": null,
    "uuid": "412c7c07-eaa4-4870-bbb0-a8eaa205f925"
  },
  {
    "name": null,
    "uuid": "4399a284-ded4-4fd8-8966-126d6e489c84"
  },
  {
    "name": null,
    "uuid": "cc3189e3-87d1-4d42-81c6-e9033cf644b3"
  },
  {
    "name": null,
    "uuid": "5572325c-9716-48ae-ab16-7294282e50f0"
  },
  {
    "name": null,
    "uuid": "8fd1f7be-f0fb-4da5-b82c-d4dcdd6644ec"
  }
]
```
<br>

To query specific quarters (with more data), you can use a **POST** request, and specify an array of quarters to query in the body.\
Every quarter in the query that is not found will skipped and therefore not included in the response.

Example **POST** request
```json5
{
  "query": [
    "f21cb87e-26e1-4434-baa6-e99eda6892d5",
    "3a30a7dc-4892-4de4-b3ed-d2768ae5ae26"
  ]
}
```

Example **POST** response
```json5
[
  {
    "name": "Luxurious Tenement", // The colloquial name for this quarter
    "uuid": "5fb3b17a-c67e-476e-b8ad-f030955ef8ea", // The quarter's UUID
    "type": "APARTMENT", // The quarter's type (https://github.com/Fruitloopins/Quarters/wiki/Quarter-Types)
    "creator": "f17d77ab-aed4-44e7-96ef-ec9cd473eda3", // The UUID of the player who created this quarter
    "owner": { // Nested values are null if the quarter has no owner
      "name": "Mcplayer21",
      "uuid": "57bcd815-d93a-4e4a-904f-d01b3459ce17"
    },
    "town": {
      "name": "Venice",
      "uuid": "83c9eb49-40e8-4588-98d3-e2c25b174694"
    },
    "nation": { // Nested values are null if the quarter's town is not in a nation
      "name": "Venice",
      "uuid": "5341d292-28bb-4c8e-a404-0076cfa2f478"
    },
    "timestamps": {
      "registered": 1727747211374, // The time when this quarter was created (as a Unix timestamp)
      "claimedAt": 1710726377127 // The time when this quarter was claimed (as a Unix timestamp). null if the quarter has no owner
    },
    "status": {
      "isEmbassy": true, // True if the quarter is an embassy
      "isForSale": false // True if the quarter is for sale
    },
    "stats": {
      "price": null, // An integer representing the quarter's sale price, null if the quarter is not for sale
      "volume": 7714, // The total number of blocks within the quarter's bounds
      "numCuboids": 1, // The total amount of cuboids this quarter is made up of
      "particleSize": 10 // The size of the particles in this quarter, null if particle size is not set
    },
    "colour": [ // A JSON array representing the colour of this quarter
      64,   // Red
      194,  // Green
      121,  // Blue
      255   // Alpha
    ],
    "trusted": [ // A JSON array representing all the trusted players in this quarter, empty if nobody is trusted
      {
        "name": "Mcplayer21",
        "uuid": "57bcd815-d93a-4e4a-904f-d01b3459ce17"
      }
    ],
    "cuboids": [ // A JSON array representing every cuboid in this quarter
      {
        "cornerOne": [
          2189,
          62,
          -8649
        ],
        "cornerTwo": [
          2176,
          80,
          -8621
        ]
      }
    ]
  }
]
```