# Nations Endpoint
Accessed at https://api.earthmc.net/v3/aurora/nations

The following is an abridged version of the response to a **GET** request to the above URL which contains every currently registered Towny nation within a JSON array.
```json5
[
  {
    "name": "North_Pole",
    "uuid": "f8553418-202f-431a-8127-6e4eb0bb258e"
  },
  {
    "name": "Guinea",
    "uuid": "6a458663-16ff-49c9-a27e-3ad5b3b9caf5"
  },
  {
    "name": "severnaya",
    "uuid": "ceff7448-5b6d-4494-a91d-b555a97a96e9"
  },
  {
    "name": "Texas",
    "uuid": "873d49aa-f086-4525-aef5-f36897037412"
  },
  {
    "name": "Emirates",
    "uuid": "b1d39d3f-8723-4d8b-916f-3de0d2492e4d"
  }
]
```
<br>

To query specific nations (with more data), you can use a **POST** request, and specify an array of nations to query in the body.

Example **POST** request
```json5
{
  "query": [  // You can use both UUIDs and names
    "b1d39d3f-8723-4d8b-916f-3de0d2492e4d",
    "Yukon"
  ]
}
```

Example **POST** response
```json5
[
  {
    "name": "Yukon", // Nation's name
    "uuid": "3ce63924-44f9-404a-93e0-2fe87a22edd9", // Nation's UUID
    "board": "Larger than Life", // Nation's board as seen on /n, null if no board is set
    "dynmapColour": "FFA500", // Nation's hex Dynmap colour
    "dynmapOutline": "FFFF00", // Nation's hex Dynmap outline
    "wiki": null, // The nation's wiki URL as a string if set, returns null if not
    "king": {
      "name": "tuzzzie",
      "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
    },
    "capital": {
      "name": "Teslin",
      "uuid": "969d4960-f082-4be0-9e32-f14ed0b7cd50"
    },
    "timestamps": {
      "registered": 1660454906803 // A Unix timestamp representing when the nation was created
    },
    "status": {
      "isPublic": true, // True if the nation is public
      "isOpen": true, // True if the nation is open
      "isNeutral": true // True if the nation is neutral
    },
    "stats": {
      "numTownBlocks": 1764, // The total number of town blocks the nation has
      "numResidents": 50, // The current number of residents in the nation
      "numTowns": 20, // The current number of towns in the nation
      "numAllies": 5, // The current number of allies the nation has
      "numEnemies": 2, // The current number of enemies the nation has
      "balance": 1790 // The nation's balance as seen on /n
    },
    "coordinates": {
      "spawn": { // The location of the nation's spawn
        "world": "earth",
        "x": -24932.5,
        "y": 99.1875,
        "z": -11442.5,
        "pitch": 0.010773102,
        "yaw": 179.99843
      }
    },
    "residents": [ // A JSON array representing all the residents in this nation, abridged to reduce size
      {
        "name": "TheGrimReaper_34",
        "uuid": "33cfd874-1554-4cbb-b54a-9ff93667a954"
      },
      {
        "name": "kundumji",
        "uuid": "44861aef-8d09-4a16-8654-a5db565cf0bb"
      },
      {
        "name": "Dandelion23062",
        "uuid": "66c71f8c-1b06-4719-9f64-39d1b8dbb4e2"
      },
      {
        "name": "Madlykeanu",
        "uuid": "36ad238b-e8e4-40c5-9be3-feb97e9fb5db"
      },
      {
        "name": "cletustturtle",
        "uuid": "36ff784a-234b-4a0f-a5bf-c1d76ef3a2c5"
      }
    ],
    "towns": [ // A JSON array representing all the towns in this nation, abridged to reduce size
      {
        "name": "Corlu",
        "uuid": "d77e7fc1-aed9-4402-9000-4ed2f5a07eab"
      },
      {
        "name": "Ram_Ranch",
        "uuid": "78db92c0-e222-42b7-865f-1ede376503ef"
      },
      {
        "name": "Kruzof",
        "uuid": "6f10a165-cbd1-481d-935c-3e1d9cd6d643"
      },
      {
        "name": "Aduna",
        "uuid": "089e67b0-0cd6-4d2e-8348-34a0d603ee06"
      },
      {
        "name": "Slushberg",
        "uuid": "3b58b9c4-3124-4f06-8a1f-439b122325f0"
      }
    ],
    "allies": [ // A JSON array representing all the allies this nation has, empty if the nation has no allies
      {
        "name": "Venice",
        "uuid": "5341d292-28bb-4c8e-a404-0076cfa2f478"
      },
      {
        "name": "Aland",
        "uuid": "bc7d0767-7c81-4090-87be-6ee61d840410"
      },
      {
        "name": "Kalaallit_Nunaat",
        "uuid": "18fc4239-9a69-48a2-ad0b-5cd45880d04b"
      },
      {
        "name": "Tassie",
        "uuid": "2e36d7c8-5ea7-49d8-bd9a-d99b033be83f"
      },
      {
        "name": "Germany",
        "uuid": "d7833c7a-2fe6-4f26-9120-00e0759c555f"
      }
    ],
    "enemies": [ // A JSON array representing all the enemies this nation has, empty if the nation has no enemies
      {
        "name": "Mongolia",
        "uuid": "becfc4fd-6cc6-4f9c-aa3c-2ea542ce6236"
      },
      {
        "name": "Russia",
        "uuid": "a43c4aff-f472-4fe2-9f77-c307dda1defc"
      }
    ],
    "sanctioned": [], // A JSON array representing all the town's this nation has sanctioned, empty if the nation has sanctioned no towns
    "ranks": {
      "Chancellor": [
        {
          "name": "tuzzzie",
          "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
        }
      ],
      "Colonist": [],
      "Diplomat": []
    }
  }
]
```
