# Server-Sent Events (SSE) Endpoint
Accessed at https://api.earthmc.net/v3/aurora/events

Use this endpoint as an [EventSource](https://developer.mozilla.org/en-US/docs/Web/API/EventSource) to receive live events.   

You can easily connect to the event stream from your terminal using

````bash
curl -H "Accept:text/event-steam" "https://api.earthmc.net/v3/aurora/events"
````


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