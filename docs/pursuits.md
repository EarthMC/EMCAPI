# Pursuit Endpoint
Accessed at https://api.earthmc.net/v4/pursuits

The pursuits endpoint provides information about pursuits' leaderboards.  
Only the top 10 entries are included.

The rate limit for this endpoint is one request per minute per API key.

Example **POST** request
```json5
{
  "query": ["TYPE"], // The pursuit type (PLAYER, TOWN, or NATION) - 'ALL' for all
  "key": "API_KEY"
}
```

Example **POST** response
```json5
[
    {
        "PLAYER": {
            "name": "mcmmo-smelting",
            "isActive": true,
            "top": {
                "1": {
                    "player": "686b1bfa-38de-4eb9-9628-43ed3b56b76e",
                    "score": 50.0
                },
                "2": {
                    "player": "5b8274bf-b162-4336-85a0-48f9d5380a78",
                    "score": 25.0
                },
                "3": {
                    "player": "be66697b-bde8-4d70-b339-4ba15b390fa9",
                    "score": 10.0
                }
            }
        },
        "NATION": {
            "name": "votes",
            "isActive": true,
            "top": {}
        },
        "TOWN": {
            "name": "votes",
            "isActive": true,
            "top": {
                "1": {
                    "town": "f6bc3242-77a6-4020-ad1c-7df4a4c0e436",
                    "score": 35.0
                },
                "2": {
                    "town": "fa14cbc6-3215-481c-b427-a411b519178f",
                    "score": 12.0
                }
            }
        }
    }
]
```
