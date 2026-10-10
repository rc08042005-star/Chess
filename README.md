# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
Phase 2: Chess Server Design

#Register endpoint
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=FAZwDghgxgpsAuBLeAbGACASjA5ok8MATugKIB2AJmAPaLnzDDTw0kDCKiMDwkRSKIkgN0AZWIA3YnwgDEQkfCy58hIgAkIVNEVnzF25djwFiEopIVx+g4UfQBVEMQAiAQQDy+u0vTuAV3gACw9vWwV7UVcIeAgAIwgXJk5uBgBaAD4LaSIALnQABU8xABV0AHoAlxIACmricggAWxgAGnRIEBAAdzZKDphmiEQUAEpgHOIskzViLR1iAuDtSjRZsyJaohgARwCYAgmN9QW16cyT8ylrAp3TdXqaptaOrt7+weHRieArohy1iyzjcXgKOBg8BBWwaRBeMAm0LCWRicUSLgKADN6JR0LD0PEAJ5454tOCohJJGDpYE1MIFJGxCDoNjocgBFAoYBIrw0y6qTaA2AMulMlkkdmcpgQFDKaHw9AynYQSjEmAADzUIGA6F1Kge10sQJmAtOq10BXcKGVqtKEAA1jxSOrYGAkDRyDq9f8zro+VN8uhiERWTtwB7knrxFJpllUjx4AUNKVSoV0AAWAAMAGZgDAUC4nKTWorJCMUAk0F7df8hdTMjzPAUoMrCNCnm4mRMo42UUz0TACvQaspYdX0BSB3zGwUQAEoLBuuPeyaDQCbsL0HOF4dtePaxv64EQvT0C2YLEYMfgh24WTu3rr8jMpOqbPWDtFUFgqUaI7PVGr4uHyT5glu86LtqUagZ4fIHkam5PkyTBRvBViwHBprzOaSwko0ZIdBA36-v++5YZoOFEP6MaBgAUmIngAHLoGGtDkJGeoBny8YMEmKZpgATJmmboLe8KEcRf48BMPCUMAQA

#LOgin endpoint
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=FAFwliA2CmAEAyB7A5mAdrAomgJgB0XRGGAEMBjERAJ1gGFIxo1i9Trxyw2XYBlaNQBug4Gw5guPEAhToAEqVwxqY9p25KZSVGgHDJ0NRKlbYAVQDOggCIBBAPLGN02HYCuIABb2n4l2Y2pCCkAEak1iQMTCwAtAB8+iLUAFywAAoOfAAqsAD01paWYIgYABTu1tRopAC20AA0sGxFAO40OACUwEmCCToKSjgqaV5DMANoZdTQAI7u0JYg3ZOKyn3xk0mGaZByU5WCNfVNLZbt1F0kW4JChglWto5pyNAgj9QVVcfQ3R++CSCIXC1jSADN0DhYIdaKEAJ7Q751IxAsIRaCxB5VXxpf7BUiwGiwNDuSCQYD-RyYzb7bbkaC47H4wm0ElkkikSAyACSaCEnLAUJhPxZzQi5w6wFg0tkujpGP6+zWw0EuJqni8NDAAC9oDhMAAPel4cClKUy1bjPoJXqpWCCahEmaWAhoSIy-i3a3xaLMEBpeTZbLpWAAFgADABGYDQSDWWAANQFUPIMxwfrAnMs5ulNwM9ISHm8ONgqegwWgRa8XyOyO6HqrAPiqJBDNgSxocFIGuyiAA1swc7AW+jqY3nu33OR6UUh+OHNS83d6WlG-iSB6l-dFbplSNEbWTrBu95ewO0EPLetqNTbWkAFJ8BwAOVgztd7pltupvpYAaDIYAEzhuGsA1tUyJNCeXhnsw3TMDgwBAA

