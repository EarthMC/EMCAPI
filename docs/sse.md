# Server Sent Events (SSE)
Accessed at https://api.earthmc.net/v4/aurora/events

Server sent events allow clients to connect & listen to specific events sent by the server.

Connecting requires an API key in the Authorization header, which can be generated on EarthMC in-game using `/api key create`
Only one client may connect per API key.

Format: `Authorization Bearer <key>`

## Events
These are the current events available:
```
"NewDay",
"NationCreated", "NationDeleted", "NationRenamed", "NationKingChanged", "NationMerged",
"TownCreated", "TownDeleted", "TownRenamed", "TownMayorChanged", "TownMerged", "TownRuined", "TownReclaimed",
"TownJoinedNation", "TownLeftNation",
"ResidentJoinedTown", "ResidentLeftTown",
"ShopSoldItem", "ShopBoughtItem", "ShopOutOfStock", "ShopOutOfSpace", "ShopOutOfGold"
```
Clients must specify which events to listen to by specifying a `?listen=` query parameter in the URL.
Example: `https://api.earthmc.net/v4/aurora/events?listen=NewDay,TownDeleted,NationRenamed`. This would make it so only these events are sent to the client.

Most Town events include a `town` field with the name & UUID of the town. The same applies to nations with a `nation` field.
### Authorized player events
Some events are only sent to the relevant player. These are:
- TownJoinedNation, TownLeftNation - Sent to the leader of the nation
- ResidentJoinedTown, ResidentLeftTown - Sent to the mayor of the town
- ShopSoldItem, ShopBoughtItem, ShopOutOfStock, ShopOutOfSpace, ShopOutOfGold - Sent to the shop owner

