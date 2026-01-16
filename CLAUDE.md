# Claude Code Instructions

Command-line SysML v2 validator using the OMG Pilot Implementation.

## Build

```sh
mvn package    # downloads dependency, builds JAR, runs tests
```

The SysML v2 Pilot Implementation (not in Maven Central) is
auto-downloaded from GitHub releases during build.

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
