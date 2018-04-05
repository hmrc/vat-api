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