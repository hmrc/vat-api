<hr>

## All changes after 12<sup>nd</sup> Feb 2019 can be found within the Wiki page located [here](https://github.com/hmrc/vat-api/wiki/Changelog).

<hr>

## [1.76.0](https://github.com/hmrc/vat-api/releases/tag/v1.76.0) 24-Jan-2018
* Added additional error for VAT Return submission when made too early

## [1.18.0](https://github.com/hmrc/vat-api/releases/tag/v1.18.0) 24-Aug-2018
* Updated response headers for VAT Return submission

## [1.0.1](https://github.com/hmrc/vat-api/releases/tag/v1.0.1) 26-Apr-2018
* Updated libraries and scoverage settings
* Updated documentation to remove test only tags for endpoints ready for production

## [0.22.0](https://github.com/hmrc/vat-api/releases/tag/v0.22.0) 24-Apr-2018

* Removed excessive retrievals where NRS is not needed
* Added auth logic for agents

## [0.20.0](https://github.com/hmrc/vat-api/releases/tag/v0.20.0) 23-Apr-2018

* Prefixed URIs with `/organisations/vat`
* Added auth logic for organisations and individuals

## [0.13.0](https://github.com/hmrc/vat-api/releases/tag/v0.13.0) 05-Apr-2018

* Modified VAT Returns (Submit 9 Box) to accept whole values ending in '.00' for boxes 7-10.
* Modified VAT Returns (Submit 9 Box) to accept negative values for boxes 6-9.
* Modified VAT Obligations dueDate response to be one Calendar month and 7 days after the PeriodEnd
* Documentation changes for the above.
* General improvements to documentation.

## [0.12.0](https://github.com/hmrc/vat-api/releases/tag/v0.12.0) 04-Apr-2018

* Added functionality to call out to the Non Repudiation service after a VAT 9 box return has been submitted. New headers
  returned in the response as a result: Receipt-ID, Receipt-Timestamp, Receipt-Signature

## [0.7.0](https://github.com/hmrc/vat-api/releases/tag/v0.7.0) 25-Jan-2018

* New API to retrieve VAT returns (Submit 9 Box) which they submitted earlier for their VAT registered business.
