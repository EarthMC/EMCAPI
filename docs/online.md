# Online Players Endpoint
Accessed at https://api.earthmc.net/v3/aurora/online

Returns a JSON object with a count of how many players are online and their names and uuid. Players that have opted themselves out of appearing in the API will not
appear in the players array, but are included in the count.

Example response (GET request):

```json5
{
  "count": 1,
  "players": [
    {
      "name": "Warriorrr",
      "uuid": "e25ad129-fe8a-4306-b1af-1dee1ff59841"
    }
  ]
}
```
