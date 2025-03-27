# POST Templates

> ###### Templates are supported for Player, Town, Nation, Quarter and Location endpoints.

*Only get what you asked for.*

POST Templates are **optional** JSON objects you can provide in POST requests bodies to specify which fields the requested objects should feature.

In a new `template` JSON object in the body, simply recreate each field of the object you are requesting, and set its value to `true` if you want it to be present in the response.

You do not have to specifically set the other fields to `false`, everything that is not present or not `true` will not be included.

> ⚠️ Templates do not support nested components. You can only select top-level fields, and all their nested contents will be added.

Here is an example of a POST request to `/towns` with a template:

```json5
{
  "query": ["Mojo", "JavaScript"],
  "template": {
    "name": true,
    "mayor": true,
    "board": false,   // This is optional
    "nation": 839,    // 839 is not a valid value. This key will be skipped despite existing in the Town object.
    "jacko": true     // This key does not exist in the Town object, so despite being true, it will not be included
  }
}
```

And here is the response:

```json5
[
  {
    "name": "Mojo",
    "mayor": {
      "name": "12BarBruise",
      "uuid": "e17c3b1d-1c20-41ec-928a-6f7f375c42f9"
    }
  },
  {
    "name": "JavaScript",
    "mayor": {
      "name": "Raurok",
      "uuid": "ea0d3135-b9c1-4236-8886-ff489851e69d"
    }
  }
]
```