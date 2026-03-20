# API Keys
API keys are an extra layer of authorization, granting a higher level of access to players & API users.

They can be generated in-game using `/api key create`. They are persistent & linked to the player until deleted using `/api key delete`.
You may at anytime copy your existing API key with `/api key copy`

Keys currently serve 2 purposes:
1. Allowing access to the server's SSE endpoint (`/events`)
2. Allowing players to query their own data - Players can query their own resident information even if they have opted out, and they may view information about their shops in the `/shop` endpoint.
