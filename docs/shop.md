# Shop Endpoint
Accessed at https://api.earthmc.net/v4/shop

The shop endpoint provides information about player-owned QuickShops.
It is important to note that the information here is not public, and players can only access their own shops' information, using their API key.

Each shop object has the following properties:
- `item` - The Material/name of the item being traded
- `price` - The price of one transaction
- `amount` - The amount of items in one transaction
- `type` - Whether it is selling or buying items
- `stock` - The remaining stock if it's a selling shop, otherwise the remaining space in the buying shop.

The JSON element returned carries a dictionary of numbers (counter/shop id) and their respective shop objects.
The counter begins at 1.

Example **POST** request
```json5
{
  "query": ["PLAYER_UUID"],
  "key": "API_KEY"
}
```
The player UUID must match the API key's owner, otherwise an empty list is returned.

Example **POST** response
```json5
[
  {
    "1": {
      "item": "COPPER_BLOCK",
      "price": 2,
      "amount": 4,
      "type": "selling",
      "stock": 5
    }
  }
]
```
This shop is selling 4 copper blocks for 2 gold, and there are 5 of these transactions in stock (A total of 20 copper blocks)
