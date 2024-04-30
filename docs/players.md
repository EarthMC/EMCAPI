# Players Endpoint
Accessed at https://api.earthmc.net/v3/aurora/players

The following is an abridged version of the response from the above URL which contains every currently registered Towny resident within a JSON array
```json5
[
  {
    "name": "deb0rahah",
    "uuid": "eca37341-fb75-4776-a7c6-b5b785e736a8"
  },
  {
    "name": "MiniBird675",
    "uuid": "9ac5f867-59a4-485e-b239-51da7155e367"
  },
  {
    "name": "MrZioX",
    "uuid": "bf055853-9510-4541-ba2f-753b293c0600"
  },
  {
    "name": "_EukaliptUs",
    "uuid": "013dddcc-5406-470d-bb69-c2fcdb5f8f2c"
  },
  {
    "name": "4O4Lucas",
    "uuid": "9c5a1c2e-76e9-4e09-81ff-e975f0ea1b3c"
  }
]
```

Look up the specified usernames to get player data, for example https://api.earthmc.net/v3/aurora/players?query=eca37341-fb75-4776-a7c6-b5b785e736a8,9ac5f867-59a4-485e-b239-51da7155e367
```json5
[
  {
    "name": "Fruitloopins", // Player's username
    "uuid": "fed0ec4a-f1ad-4b97-9443-876391668b34", // Player's UUID
    "title": "<rainbow>King", // Title set through /n set title, null if no title is set
    "surname": "of Yukon", // Surname set through /n set surname, null if no surname is set
    "formattedName": "<rainbow>King Fruitloopins of Yukon", // Formatted name combining title, username and surname
    "about": "CEO of this feature", // About section of /res set with /res set about, null if there is no about set
    "town": { // Child values are null if no town or nation
      "name": "Teslin",
      "uuid": "969d4960-f082-4be0-9e32-f14ed0b7cd50"
    },
    "nation": {
      "name": "Yukon",
      "uuid": "3ce63924-44f9-404a-93e0-2fe87a22edd9"
    },
    "timestamps": {
      "registered": 1651299304371, // A Unix timestamp representing the time the player joined the server
      "joinedTownAt": 1701704799323, // A Unix timestamp representing when the player joined their town, null if the player has no town
      "lastOnline": 1711116371655 // A Unix timestamp representing when the player was lastOnline, can be null if the player is an NPC
    },
    "status": {
      "isOnline": true, // True if the player is currently online
      "isNPC": false, // True if the player is a Towny NPC
      "isMayor": false, // True if the player is a mayor of a town
      "isKing": false, // True if the player is the king of a nation
      "hasTown": true, // True if the player is currently in a town
      "hasNation": true // True if the player is currently in a nation
    },
    "stats": {
      "balance": 2838, // The player's current gold balance as seen on /res
      "numFriends": 1 // The amount of friends this player has
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
    "ranks": {
      "townRanks": [ // A JSON array representing the town ranks this player holds, empty if the player holds no ranks
        "Councillor"
      ],
      "nationRanks": [ // A JSON array representing the nation ranks this player holds, empty if the player holds no ranks
        "Chancellor"
      ]
    },
    "friends": [ // A JSON array representing the res friends this player has, empty if the player has no friends
      {
        "name": "tuzzzie",
        "uuid": "8391474f-4b57-412a-a835-96bd2c253219"
      }
    ]
  }
]
```
