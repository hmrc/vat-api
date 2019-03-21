[Making Tax Digital](https://www.gov.uk/government/publications/making-tax-digital/overview-of-making-tax-digital) introduces digital record-keeping for VAT-registered businesses. Those that are above the VAT threshold will be mandated to use this service from April 2019. Those that are below the threshold can use this service voluntarily. HMRC customers (and their agents) will use digital record-keeping software to interact directly with our systems via the MTD APIs.

The MTD VAT API allows software to supply business financial data to HMRC, so that clients can fulfil their obligations.

For more background on the VAT API, see the <a href="https://developer.service.hmrc.gov.uk/guides/vat-mtd-end-to-end-service-guide/">MTDfB VAT Guide for Vendors</a>.



### How it works 


* The client (or their agent) enters their VAT account information into the software.
* The software updates HMRC via the API.
* The API submits this information to an HMRC systems database, where the client’s liability is stored to establish whether the client has met their obligations.


### Fraud prevention

We expect it will become compulsory to supply header information for this API from April 2019 to <a href="/api-documentation/docs/reference-guide#fraud-prevention">prevent fraud</a>. We recommend that you include this now.
