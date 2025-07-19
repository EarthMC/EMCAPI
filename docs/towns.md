# Towns Endpoint
Accessed at https://api.earthmc.net/v3/aurora/towns

The following is an abridged version of the response to a **GET** request to the above URL which contains every currently registered Towny town within a JSON array.
```json5
[
  {
    "name": "Huzhou",
    "uuid": "c891767e-2992-419c-a748-7df00a25b781"
  },
  {
    "name": "Vladivostok",
    "uuid": "51c2dc6d-9a41-4091-a7ce-ece008c178fa"
  },
  {
    "name": "Roros",
    "uuid": "0fbaeb5b-bea0-46a1-b6fe-c2ed314ab03f"
  },
  {
    "name": "Gulag",
    "uuid": "2835f0bc-820a-4ef3-a2ba-8fdfd5d4e36b"
  },
  {
    "name": "Aydın",
    "uuid": "582d0ff1-10da-4ca6-99f8-97946cc776e8"
  }
]
```
<br>

To query specific towns (with more data), you can use a **POST** request, and specify an array of towns to query in the body.

Example **POST** request
```json5
{
  "query": [  // You can use both UUIDs and names
    "51c2dc6d-9a41-4091-a7ce-ece008c178fa",
    "Teslin"
  ]
}
```

Example **POST** response
```json5
[
  {
    "name": "Teslin", // Town's name
    "uuid": "969d4960-f082-4be0-9e32-f14ed0b7cd50", // Town's UUID
    "board": "Fishing every Friday! Join our Discord, link can be found on the signs!", // Town's board as seen on /t, null if none is present
    "founder": "tuzzzie", // The founder of the town as seen on /t
    "wiki": null, // The town's wiki URL as a string if set, returns null if not
    "mayor": {
      "name": "Fruitloopins",
      "uuid": "fed0ec4a-f1ad-4b97-9443-876391668b34"
    },
    "nation": { // Nested nation values will be null if the town has no nation
      "name": "Yukon",
      "uuid": "3ce63924-44f9-404a-93e0-2fe87a22edd9"
    },
    "timestamps": {
      "registered": 1660454740242, // A Unix timestamp representing when the town was created
      "joinedNationAt": 1660454906803, // A Unix timestamp representing when the town joined its nation, null if the town has no nation
      "ruinedAt": null // A Unix timestamp representing when the town fell to ruin, null if the town is not ruined
    },
    "status": {
      "isPublic": false, // True if the town is public
      "isOpen": true, // True if the town is open
      "isNeutral": false, // True if the town is neutral
      "isCapital": true, // True if the town is the nation's capital
      "isOverClaimed": true, // True if the town has more claims than it should
      "isRuined": false, // True if the town is ruined
      "isForSale": false, // True if the town is for sale
      "hasNation": true, // True if the town has a nation
      "hasOverclaimShield": false, // True if the town currently has an overclaim shield
      "canOutsidersSpawn": false, // True if the town allows outsiders to teleport
    },
    "stats": {
      "numTownBlocks": 473, // The total number of town blocks the town has
      "maxTownBlocks": 114, // The maximum town blocks the town can claim
      "numResidents": 7, // The current number of residents in the town
      "numTrusted": 5, // The total number of trusted residents in the town
      "numOutlaws": 3, // The total number of players that are outlawed in the town
      "balance": 51, // The town's balance as seen on /t
      "forSalePrice": null // The price the town is for sale at, null if the town is not for sale
    },
    "perms": {
      "build": [
        false,
        false,
        false,
        false
      ],
      "destroy": [
        false,
        false,
        false,
        false
      ],
      "switch": [
        false,
        false,
        false,
        false
      ],
      "itemUse": [
        false,
        false,
        false,
        false
      ],
      "flags": {
        "pvp": false,
        "explosion": false,
        "fire": false,
        "mobs": false
      }
    },
    "coordinates": {
      "spawn": { // The location of the town's spawn
        "world": "earth",
        "x": -25227.14828937919,
        "y": 86,
        "z": -11394.414899422554,
        "pitch": 16.419712,
        "yaw": -60.628418
      },
      "homeBlock": [ // X and Z of the town's home block
        -1577,
        -713
      ],
      "townBlocks": [ // A JSON array representing all the town blocks in this town, abridged to reduce size, first value is X, second is Z, multiply by 16 to get actual coordinate
        [
          -1551,
          -710
        ],
        [
          -1551,
          -709
        ],
        [
          -1551,
          -712
        ],
        [
          -1551,
          -711
        ],
        [
          -1551,
          -714
        ]
      ]
    },
    "residents": [ // A JSON array representing all the resident's in the town
      {
        "name": "SilentlyyJudging",
        "uuid": "00abc813-22c4-4734-88a5-609821971147"
      },
      {
        "name": "Fruitloopins",
        "uuid": "fed0ec4a-f1ad-4b97-9443-876391668b34"
      },
      {
        "name": "catgirljodie",
        "uuid": "6ff64e22-8d20-40bf-9914-a2d6172e65cc"
      },
      {
        "name": "AuroraServer",
        "uuid": "d633b540-2509-4aeb-af94-ea9ea2c481ee"
      },
      {
        "name": "tuzzzie",
        "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
      },
      {
        "name": "Thaamoth",
        "uuid": "fa61c5dc-2677-4509-b20e-d159da20c9fd"
      },
      {
        "name": "rangegirl28",
        "uuid": "6ae5516a-c5e6-488f-97a4-311aaf6ecced"
      }
    ],
    "trusted": [
      {
        "name": "Fruitloopins",
        "uuid": "fed0ec4a-f1ad-4b97-9443-876391668b34"
      },
      {
        "name": "Masrain",
        "uuid": "ea0c4d98-106d-416c-9647-06203f861e1b"
      },
      {
        "name": "CorruptedGreed",
        "uuid": "f17d77ab-aed4-44e7-96ef-ec9cd473eda3"
      },
      {
        "name": "HaisevaGreed",
        "uuid": "253dd297-cb8d-4b9a-9aae-ffe71883956f"
      },
      {
        "name": "tuzzzie",
        "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
      }
    ],
    "outlaws": [
      {
        "name": "Fruitloopins",
        "uuid": "fed0ec4a-f1ad-4b97-9443-876391668b34"
      },
      {
        "name": "Masrain",
        "uuid": "ea0c4d98-106d-416c-9647-06203f861e1b"
      },
      {
        "name": "CorruptedGreed",
        "uuid": "f17d77ab-aed4-44e7-96ef-ec9cd473eda3"
      },
      {
        "name": "HaisevaGreed",
        "uuid": "253dd297-cb8d-4b9a-9aae-ffe71883956f"
      },
      {
        "name": "tuzzzie",
        "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
      }
    ],
  "quarters": [ // The Name & UUID of every quarter in this town, can be looked up in the quarter's endpoint, empty if none are present
    {
      "name": "Quarter",
      "uuid": "b201dac7-4b7b-414a-84c6-9dd14ae69975"
    },
    {
      "name": "Quarter",
      "uuid": "2fed290a-64cc-48e1-b06d-b8bfccce0437"
    },
    {
      "name": "Inviting Suite",
      "uuid": "85335da5-fc23-41a5-b7a5-e921da99dc09"
    },
    {
      "name": "Quarter",
      "uuid": "1db79f17-9688-429c-a5a8-1d2b03ceb947"
    },
    {
      "name": "Dull Tenement",
      "uuid": "9e734449-9692-4502-96a1-51895f3a32b6"
    }
  ],
    "ranks": {
      "Councillor": [
        {
          "name": "tuzzzie",
          "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
        }
      ],
      "Builder": [],
      "Recruiter": [
        {
          "name": "Golden_Horizon",
          "uuid": "1fd49c80-8ab7-4adf-a3b9-879932bddca8"
        },
        {
          "name": "SkidgeV",
          "uuid": "9dfbd591-d86d-44a2-9eb4-22deaa88656e"
        }
      ],
      "Police": [],
      "Tax-exempt": [],
      "Treasurer": [],
      "Realtor": [],
      "Settler": []
    }
  }
]
```
