[Making Tax Digital](https://www.gov.uk/government/publications/making-tax-digital/overview-of-making-tax-digital) introduces digital record-keeping for VAT-registered businesses. For businesses that are above the VAT threshold, it is mandatory to use this service. Those that are below the threshold can use this service voluntarily. HMRC customers (and their agents) will use digital record-keeping software to interact directly with our systems via the MTD APIs.

The MTD VAT API allows software to supply business financial data to HMRC, so that clients can fulfil their obligations.

For more background on the VAT API, see the [MTDfB VAT Guide for Vendors](https://developer.service.hmrc.gov.uk/guides/vat-mtd-end-to-end-service-guide).



### How it works 


* The client (or their agent) enters their VAT account information into the software.
* The software updates HMRC via the API.
* The API submits this information to an HMRC systems database, where the client’s liability is stored to establish whether the client has met their obligations.


### Fraud prevention

It is compulsory to supply fraud prevention header information for this API. You can use the <a href="/api-documentation/docs/fraud-prevention">Test Fraud Prevention Headers API</a>. to make sure your application can produce correctly formatted fraud prevention headers.   You can use this API during development and as part of your regular quality assurance checks.
