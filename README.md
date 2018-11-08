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

Run from the console using: `$ sbt "run 9675"`

### Testing

Run unit tests: `$ sbt test`

Run integration tests: `$ sbt func:test`

### Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/vat-api/issues).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

