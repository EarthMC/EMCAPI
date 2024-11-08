# Server-Sent Events (SSE) Endpoint
Accessed at https://api.earthmc.net/v3/aurora/events

> **Server-Sent Events** (SSEs) are a simple, one-way communication method where a server can push real-time updates to clients over HTTP. Unlike WebSockets, SSEs use a persistent HTTP connection, making them ideal for continuous data streams, such as live notifications.
> [[MDN Reference]](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events)

You can easily connect to the event stream from your terminal using

````bash
curl -H "Accept:text/event-steam" "https://api.earthmc.net/events"
````

If the connection was successful, you will receive a `open` event from the server.

---

### Example usage (in JavaScript)
Use the endpoint as an [EventSource](https://developer.mozilla.org/en-US/docs/Web/API/EventSource) to receive live events.
```javascript
const sse = new EventSource('https://api.earthmc.net/events');

/*
 * This will listen only for events
 * similar to the following:
 *
 * event: NewNation
 * data: Event data (see below)
 */
 
sse.addEventListener('NewNation', (e) => {
  console.log(e.data);
});
```
<br>Example `NewNation` event
```json5
{
  "event": "NewNation",
  
  "data": {
    "nation": {
      "name": "Egypt",
      "uuid": "e82b84fb-d3fd-4065-a43b-013d53416162"
    },
    
    "king": {
      "name": "Lumpeeh",
      "uuid": "a03f71f9-625e-419f-9d16-0e5ab50414e4"
    },
    
    "timestamp": "1651592417137"
    }
}
```

---

### Event data
Below is a list of all the events and the JSON structure of their `data` field.
<br>*(Each `data` object additionally carries a UNIX timestamp)*

**Player Connections**
- PlayerJoin (aurora)
```yaml
{
  player: {
    name: str
    uuid: str
  }
}
```
- PlayerQuit (aurora)
```yaml
{
  player: {
    name: str
    uuid: str
  }
}
```

<br>**Newday**

- NewDay
```yaml
{
  fallenTowns: str[]    // Names
  fallenNations: str[]  // Names
}
```

<br>**Nation**

- NewNation
```yaml
{
  nation: {
    name: str
    uuid: str
  }
  king: {
    name: str
    uuid: str
  }
}
```
- DeleteNation
```yaml
{
  nation: {
    name: str
    uuid: str
  }
  king: {
    name: str
    uuid: str
  }
}
```
- RenameNation
```yaml
{
  nation: {
    name: str
    uuid: str
  }
  oldName: str
}
```
- NationKingChange
```yaml
{
  nation: {
    name: str
    uuid: str
  }
  newKing: {
    name: str
    uuid: str
  }
  oldKing: {
    name: str
    uuid: str
  }
  isCapitalChange: bool
  
  # if isCapitalChange is true:
  newCapital: {
    name: str
    uuid: str
  }
  oldCapital: {
    name: str
    uuid: str
  }
}
```
- NationAddTown
```yaml
{
  nation: {
    name: str
    uuid: str
  }
  town: {
    name: str
    uuid: str
  }
}
```
- NationRemoveTown
```yaml
{
  nation: {
    name: str
    uuid: str
  }
  town: {
    name: str
    uuid: str
  }
}
```

<br>**Town**

- NewTown
```yaml
{
  town: {
    name: str
    uuid: str
  }
  mayor: {
    name: str
    uuid: str
  }
}
```
- DeleteTown
```yaml
{
  town: {
    name: str
    uuid: str
  }
  mayor: {
    name: str
    uuid: str
  }
}
```
- RenameTown
```yaml
{
  town: {
    name: str
    uuid: str
  }
  oldName: str
}
```
- TownMayorChanged
```yaml
{
  town: {
    name: str
    uuid: str
  }
  newMayor: {
    name: str
    uuid: str
  }
  oldMayor: {
    name: str
    uuid: str
  }
}
```
- TownRuined
```yaml
{
  town: {
    name: str
    uuid: str
  }
  oldMayor: {
    name: str
    uuid: str
  }
}
```
- TownReclaimed
```yaml
{
  town: {
    name: str
    uuid: str
  }
  newMayor: {
    name: str
    uuid: str
  }
}
```
- TownAddResident
```yaml
{
  town: {
    name: str
    uuid: str
  }
  resident: {
    name: str
    uuid: str
  }
}
```
- TownRemoveResident
```yaml
{
  town: {
    name: str
    uuid: str
  }
  resident: {
    name: str
    uuid: str
  }
}
```