# AGENTS.md

Command-line SysML v2 validator using the OMG Pilot Implementation.

## Build

```sh
mvn -Psetup-dependency initialize
mvn package
```

The SysML v2 Pilot Implementation is released on GitHub, not published
to Maven Central. Bootstrap it once with the `setup-dependency`
profile, and rerun it after `mvn clean`. Use `mvn package` for normal
rebuilds without cleaning.

## Run

```sh
./validate-sysml model.sysml
```

## Requirements

- Java 21+ (required by SysML Pilot Implementation)
- Maven

## Conventions

- GNU error format: `filename:line:column: severity: message`
- mandoc man page, lint with `mandoc -Wwarning`
