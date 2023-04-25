# coxecude

Server exposing a REST API to execute code through various VMs/interpreters

## Warning

Some VMs used as executors may not be free of privilege escalation exploit risks.  
Because of this, I highly recommend **running this server on a throwaway VPS
or hardware-based VM.**  

## Motivation

This server is meant to be used with [pcp](https://github.com/PixelSam123/pcp),
but feel free to use it for any other purposes (as long as you are aware of
the warning above.)

## Available executors

- [X] JavaScript, using [caoccao/Javet](https://github.com/caoccao/Javet)
- [ ] Python, using [ninia/jep](https://github.com/ninia/jep)
- [ ] Lua, using [luaj/luaj](https://github.com/luaj/luaj)

## Endpoints

Swagger is accessible through `/docs`

- [X] POST `/`
  Request body:
  ```json
  {
    "lang": "string",
    "code": "string"
  }
  ```
  Response body:
  ```json
  {
    "status": 0,
    "output": "string"
  }
  ```

Original README.md from Quarkus, which includes running instructions:

---

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only
> at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into
the `build/quarkus-app/lib/` directory.

The application is now runnable using
`java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using
`java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build
in a container using:

```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with:
`./build/coxecude-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please
consult https://quarkus.io/guides/gradle-tooling.
