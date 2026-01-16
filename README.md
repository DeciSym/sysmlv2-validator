# sysmlv2-validator

Command-line validator for SysML v2 files using the OMG Pilot Implementation.

## Prerequisites

- Java 21+
- Maven

## Build

```sh
mvn package
```

The SysML v2 Pilot Implementation is auto-downloaded from GitHub releases.

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
