# AKKA 2.6 By examples

I'm starting to migrate a few projects to AKKA 2.6 from AKKA 2.*. The idea
is to start using the new (and looks like now more mature) Akka Typed.

So I will use this repo to try the migration in a small project before
to go ahead with the others.

> This is a continuation of the [Migrating/Comparision from Akka Classic to Typed ](https://www.acervera.com/blog/2019/11/akka-typed-grpc-persistence/)
> but removing the Classic implementation part. 

# Current state of this project covers:

- [x] Akka gRPC
- [x] Akka Typed
- [x] Akka Event Sourcing
- [x] Akka Persistence
- [ ] Akka Stream Typed

# Posts covering every part:
- [Migrating/Comparision from Akka Classic to Typed ](https://www.acervera.com/blog/2019/11/akka-typed-grpc-persistence/)
- [Integrating Akka Streams with Akka Actors Typed ](https://www.acervera.com/blog/2020/04/akka-stream-typed-actors/)

# Services description
Remind that the idea of this service is not to implement something cool
but something to play with *Akka Typed* style.

So the functionality is really simple:

## Server.
The server is a **Counter** service. It is going to keep the counter in the service along with the number of
events created.

This is the list of services to expose:
- **Inc** will increment the counter the X times and increment one time
  the events counter.
- **Incs** like **Inc** service but in a streaming way.
- **Get** will return the counter and the number of events generated.

The definition is in the [submodule protobuf-api](protobuf-api/src/main/resources/example.proto)

## Client.
The client will stimulate the counter in different ways to play with, for example, back-pressure.

# Running
From the project root:

Assembly the project.
```
sbt clean universal:packageZipTarball
```
## Server side

The server is using typesafe config configuration system, so it is
possible to override all configuration:
- server.interface = interface binding. Default *0.0.0.0* so listening
  from everywhere.
- server.port = Port listening. Default 8080 

Running setting, for example, the server interface to listen:
```
bin/server -Dserver.interface=localhost
```

## Client side

The implementation is using the Akka gRPC like the server, so it is
using typesafe config configuration system.
- *akka.grpc.client.example.api.CounterService.host* = Server host.
  Default locahost
- *akka.grpc.client.example.api.CounterService.port* = Server port.
  Default 8080

Three different behaviors depending on the number of parameters:
- No parameters: Return the counter and the number of events generated.
- One parameter: Increment the counter X times.
- Two parameters: Execute the previous one as a stream, calling the
  service one time every value in the range.
  
Increment 10 with defaults
```
bin/client 10
```

## Example:

In on terminal, execute the server:

```
$ bin/server
[2019-11-16 17:14:23,252] [INFO] [akka.event.slf4j.Slf4jLogger] [CounterServer-akka.actor.default-dispatcher-6] [] - Slf4jLogger started
[2019-11-16 17:14:23,366] [WARN] [akka.persistence.Persistence] [CounterServer-akka.actor.default-dispatcher-9] [Persistence(akka://CounterServer)] - No default snapshot store configured! To configure a default snapshot-store plugin set the `akka.persistence.snapshot-store.plugin` key. For details see 'reference.conf'
Counter server typed online at http://0:0:0:0:0:0:0:0:8080/

```

In other terminal, execute the client:

```
$ bin/client
Current state: Accumulator [0] / Events executed [0].

$ bin/client 10
Successfully incremented.

$ bin/client
Current state: Accumulator [10] / Events executed [1].

$ bin/client 5 10
Incremented Done()
Incremented Done()
Incremented Done()
Incremented Done()
Incremented Done()
Incremented Done()
Incremented range [Range 5 to 10].

$ bin/client
Current state: Accumulator [55] / Events executed [7].

```

# Future

In the future, I will use this project to do add:

- [ ] Clustering 
- [ ] Sharding

