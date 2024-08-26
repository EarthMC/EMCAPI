# Server-Sent Events (SSE) Endpoint
Accessed at https://api.earthmc.net/v3/aurora/events

Use this endpoint as an [EventSource](https://developer.mozilla.org/en-US/docs/Web/API/EventSource) to receive live events.   

You can easily connect to the event stream from your terminal using

````bash
curl -H "Accept:text/event-steam" "https://api.earthmc.net/v3/aurora/events"
````

<br>

### Here is a list of all the events and their JSON structure:
*(Each event carries a UNIX timestamp)*

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

<br>

- NewDay
```yaml
{
  fallenTowns: str[]
  fallenNations: str[]
}
```

<br>

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

<br>

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

<br>

Example `NewNation` event
```json5
{
  "event": "NewNation",
  
  "data": {
    "nation": {
      "name": "Guinea",
      "uuid": "6a458663-16ff-49c9-a27e-3ad5b3b9caf5"
    },
    
    "king": {
      "name": "Czipsu35",
      "uuid": "12a19eee-6539-4634-89bc-4398ab8de870"
    },
    
    "timestamp": "1656352097939"
    }
}
```