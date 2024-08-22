# Use Case: Managing EV Charging Stations

## Overview

### System

The system is an IoT device management workflow specifically designed for Electric Vehicle (EV) charging stations. It utilizes the Serverless Workflow DSL to automate the monitoring, management, and maintenance of EV charging units, ensuring they operate efficiently and are available for users.

### Actors

- **EV Drivers:** Individuals who use electric vehicles and need charging services.
- **Charging Station Operators:** Businesses or entities managing the charging stations.
- **IoT Devices:** Sensors and controllers in the charging stations that provide real-time data.
- **Cloud Services:** External systems for storing data and providing analytics.

### Goals

- **Automate Charging Management:** Streamline the management of charging sessions, including starting, stopping, and monitoring.
- **Monitor Station Health:** Enable real-time monitoring of the charging stations to detect and address issues proactively.
- **Enhance User Experience:** Provide seamless experiences for EV drivers while charging their vehicles.

### Preconditions

- The workflow assumes that the charging stations are equipped with IoT devices that can send and receive events.
- An appropriate cloud infrastructure is in place to handle the data from charging sessions and device statuses.

## Scenario

### Triggers

The workflow is triggered when:

- An EV charging session starts.
- An EV charging session ends.
- The charging station reports an error.

### Flow Breakdown


### Visualization

The following diagram represents the high-level flow of the workflow:

![managing-ev-charging-stations-diagram](diagram.png)

### Example

```yaml
document:
  dsl: '1.0.0'
  namespace: default
  name: managing-ev-charging-stations
  version: '0.1.0'
schedule:
  on:
    any:
      - with:
          type: com.ev-power-supplier.charging-station.card-scanned.v1
      - with:
          type: com.ev-power-supplier.charging-station.faulted.v1
do:

  - initialize:
      set:
        event: ${ $workflow.input[0].data }
      export:
        as: .event

  - handleStationEvents:
      switch:
        - sessionStarted:
            when: .event.type == "com.ev-power-supplier.charging-station.card-scanned.v1"
            then: tryGetActiveSession
        - stationError:
            when: .event.type == "com.ev-power-supplier.charging.station-faulted.v1"
            then: handleError
      then: raiseUnsupportedEventError

  - tryGetActiveSession:
      try:
        - getSessionForCard:
            call: http
            with:
              method: get
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/session/{cardId}
        - setSessionInfo:
            set:
              session: ${ .session }
      catch:
        errors:
          with:
            status: 404
      
  - handleActiveSession:
      switch:
        - sessionInProgress:
            when: .session != null
            then: endSession
        - noActiveSession:
            then: tryAquireSlot

  - tryAquireSlot:
      try:
        - acquireSlot:
            call: http
            with:
              method: post
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}
              body:
                card: ${ $context.card }
            export:
              as: '$context + { slot: .slot }'
      catch:
        errors:
          with:
            status: 400
        when: .detail == "No charging slots available"
        do:
          - noSlotsAvailable:
              call: http
              with:
                method: post
                endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/leds/main
                body:
                  action: flicker
                  color: red
                  duration: 3000
              then: end
      
  - startSession:
      do:
        - initialize:
            set:
              session:
                card: ${ $context.card }
                slotNumber: ${ $context.slot.number }
            export:
              as: '$context + { session: . }'
        - feedBack:
            call: http
            with:
              method: post
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/leds/{slotNumber}
              body:
                action: 'on'
                color: blue
        - lockSlot:
            call: http
            with:
              method: put
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/slot/{slotNumber}/lock
        - start:
            call: http
            with:
              method: put
              endpoint: https://ev-power-supplier.com/api/v2/sessions/{sessionId}/start
        - notify:
            emit:
              event:
                with:
                  source: https://ev-power-supplier.com
                  type: com.ev-power-supplier.charging-station.session-started.v1
                  data: ${ $context.session }

  - endSession:
      do:
        - end:
            call: http
            with:
              method: put
              endpoint: https://ev-power-supplier.com/api/v2/sessions/{sessionId}/end
        - processPayment:
            call: http
            with:
              method: put
              endpoint: https://ev-power-supplier.com/api/v2/sessions/{sessionId}/pay
        - unlockSlot:
            call: http
            with:
              method: put
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/slot/{slotNumber}/unlock
        - feedBack:
            call: http
            with:
              method: post
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/leds/{slotNumber}
              body:
                action: flicker
                color: white
                duration: 3000
        - notify:
            emit:
              event:
                with:
                  source: https://ev-power-supplier.com
                  type: com.ev-power-supplier.charging-station.session-ended.v1
                  data: ${ $context.session }
      then: end

  - handleError:
      do:
        - contactSupport:
            call: http
            with:
              method: post
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/support
              body: 
                error: ${ $context.event.data.error }
        - feedBack:
            call: http
            with:
              method: post
              endpoint: https://ev-power-supplier.com/api/v2/stations/{stationId}/leds/main
              body:
                action: 'on'
                color: red
        - notify:
            emit:
              event:
                with:
                  source: https://ev-power-supplier.com
                  type: com.ev-power-supplier.charging-station.out-of-order.v1
                  data: ${ $context.event.data.error }
      then: end

  - raiseUnsupportedEventError:
      raise:
        error:
          type: https://serverlessworkflow.io/spec/1.0.0/errors/runtime
          status: 400
          title: Unsupported Event
          detail: ${ "The specified station event '\($context.event.type)' is not supported in this context" }
      then: end
```

## Conclusion

This use case highlights the capabilities of Serverless Workflow in managing EV charging stations effectively. By automating charging sessions and monitoring station health, organizations can enhance user experiences for EV drivers while ensuring that their charging infrastructure operates efficiently. Leveraging Serverless Workflow enables responsive and scalable management solutions in the evolving landscape of electric vehicle infrastructure.