# Nearby Endpoint
Accessed at https://api.earthmc.net/v3/aurora/nearby

Example POST request
```json5
{
  "query": [
    {
      "target_type": "TOWN",
      "target": "melbourne",
      "search_type": "TOWN",
      "radius": 100
    }
  ]
}
```

Example POST request
```json5
{
  "query": [
    {
      "target_type": "COORDINATE",
      "target": [
        2000,
        10000
      ],
      "search_type": "TOWN",
      "radius": 10000
    }
  ]
}
```

Example POST response
```json5
[
  [
    {
      "name":"Jyväskylä",
      "uuid":"0b69c00d-c112-4ca0-a16c-ce551120e464"},
    {
      "name":"Watson",
      "uuid":"5851f859-0c4e-49bb-9e5c-5a2f9121585c"
    }
  ]
]
```