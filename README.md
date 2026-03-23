# sysmlv2-validator

Command-line validator for SysML v2 files using the OMG Pilot Implementation.

## Prerequisites

- Java 21+
- Maven

## Build

First-time setup, and after `mvn clean`:

```sh
mvn -Psetup-dependency initialize
mvn package
```

The OMG SysML v2 Pilot Implementation is released on GitHub, not
published to Maven Central. The `setup-dependency` profile downloads
the release and installs the kernel JAR into your local Maven
repository.

Regular rebuilds without cleaning:

```sh
mvn package
```

## Install

```sh
ln -s $PWD/validate-sysml ~/bin/
```

## Usage

```sh
validate-sysml model.sysml
validate-sysml -h
```

Errors are reported in GNU format (`file:line:col: severity: message`).

## Documentation

```sh
man -l validate-sysml.1
```
