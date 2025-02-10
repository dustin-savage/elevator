# Elevator

The elevator app implements an elevator in Java with REST access to elevator cars, building floors, and requests for service.
This project also includes an angular UI to simulate and test the service logic.

## Prerequisites

* Java (JDK 17)
* Maven
* Docker
* Docker Compose
* GNU Make (optional)

## Build the app

Run the maven build, which produces a service docker and UI docker.

```
# Runs mvn clean install
make mvn
```

## Run the app

Run the docker compose app. This runs `docker-compose up -d` and tails the server logs

```
make up
```

Access the UI at [http://localhost:8210/app/elevator](http://localhost:8210/app/elevator)

## Service Details



## UI Details

The UI simulates an elevator, and leverages the Java services via REST for building state (i.e. number of floors),
car state (i.e. how many cars, what floor they're on), and managing requests (i.e. calling the elevator to a floor.).

![UI](./ui.png "UI")

## Future work

