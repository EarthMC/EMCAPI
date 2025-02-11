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
    "quarters": [ // The UUID of every quarter in this town, can be looked up in the quarter's endpoint, empty if none are present
      "8a7c56d1-d45a-4f5f-ac42-751c2e8c1b3d",
      "aeac5e83-62e5-421d-9737-9f33825479ec",
      "49a4da60-4589-45ff-a0ba-0fd54c45ba9f",
      "54177617-694d-43b1-b2d9-2bb553fd8775",
      "fc8a7c21-2992-4584-970c-e0b98c2233cd",
      "47cf0463-dd50-4b2b-b533-5b57b3a51c09",
      "853908a4-2d76-4055-b83a-cee9cda714ce",
      "95d00017-845f-4ccd-883a-9104aabc1391",
      "473c2da9-e0b1-467a-87c8-98a59b62e386",
      "7645cc85-0d54-4ce0-800e-d358ae647aac",
      "0253c695-15aa-48a6-9592-d558224dcff7",
      "66ec664a-7eb9-47a6-aa53-1b692ef37e5e",
      "3c130442-4521-47f2-9eb9-2f61db1c3b58",
      "7fc3351c-42b0-4d55-b309-5ea7325519d4",
      "0624ab20-19c8-4277-a900-5f336417ad67",
      "93bd12da-9854-41eb-ad35-fb7789e47811",
      "c5190a18-2001-475a-a9cc-b584998e6d91",
      "9d966cc2-61c4-45e6-a792-45cd6a5affb6",
      "fed851f7-09f7-49c4-9a12-51b22834145f",
      "e8e594bd-2651-4559-a717-dbd75c125de6",
      "439b01d5-a2b7-4a3c-8cb5-3e8389e1215e",
      "f244d142-a0e0-4bf5-bb28-d497024203a1",
      "d9fbef25-ce0e-4675-91ea-4b1e969a80c2",
      "12303d82-e59d-4f5e-bdfe-20e8c5f8b758",
      "738ed341-b3c8-4f60-b1dc-daee44adcb96",
      "283feb0d-9db9-404e-bb4e-139377502d25",
      "93a839ea-0f69-4e36-adcc-869439b22070",
      "7f139395-5117-4937-80b2-3e3ab6c8aa11",
      "bf765fbc-c41c-4dd5-b796-51dd1ebd0d6b",
      "a112b649-e104-4aab-930b-5ef186eeed18",
      "8ab5df2e-0201-47de-b342-c013cc14b0d2",
      "0ef3bab8-e78f-4527-a0b6-2a1fa192a7d8",
      "1717528c-4b1e-4e9e-8775-47def673a192",
      "12de6b21-7376-4d5b-9fa3-1c95a693ccbc",
      "41372449-d667-4567-97a3-7fe769ddb82f",
      "74b89914-f80e-41f5-918c-8961767caff4",
      "c50e1d12-a125-46a2-b6bb-a88972a017b7",
      "0ba51d36-57d7-4a6a-b3fb-a3127c2fb3da",
      "609fc31e-2e54-480e-aa7f-313c8eee351b",
      "f0248886-ebad-4dcc-bc22-53e3b3ef6a92",
      "e54aa634-0939-43b7-b0ec-9486d5d2c12d",
      "da15b4bd-1182-41a0-8ff1-c6ca3c04508f",
      "998dc903-8605-4be0-bc75-46cc4438b1f2",
      "32157422-7b06-4299-9e04-0668c2b18b7b",
      "f29944ef-4b60-4145-a848-27d7fb879e1c",
      "02cad4e4-00fc-4f8e-a3fb-9d9981f25ae1",
      "ff737a85-24c6-4abf-a656-c2c01a4ebe70"
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
