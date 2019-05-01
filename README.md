# vat-api

[![Build Status](https://travis-ci.org/hmrc/vat-api.svg)](https://travis-ci.org/hmrc/vat-api) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-api/images/download.svg) ](https://bintray.com/hmrc/releases/vat-api/_latestVersion)

vat-api has five endpoints:
  - GET obligations
  - POST returns
  - GET returns
  - GET payments
  - GET liabilities

### Prerequisites 
- Scala 2.11.x
- Java 8
- sbt > 0.13.17
- [Service Manager](https://github.com/hmrc/service-manager)

### Development Setup

Run from the console using: `sbt "~run 9675"`

## Highlighted SBT Tasks
Task | Description | Command
:-------|:------------|:-----
test | Runs the standard unit tests | ```$ sbt test```
func:test  | Runs the functional tests | ```$ sbt func:test ```
dependencyCheck | Runs dependency-check against the current project. It aggregates dependencies and generates a report | ```$ sbt dependencyCheck```
dependencyUpdates |  Shows a list of project dependencies that can be updated | ```$ sbt dependencyUpdates```
dependencyUpdatesReport | Writes a list of project dependencies to a file | ```$ sbt dependencyUpdatesReport```

### Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/vat-api/issues).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

