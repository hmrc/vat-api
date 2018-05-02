package uk.gov.hmrc.assets.nrs

import play.api.libs.json.{JsValue, Json}

  object NRS {
    def success(): JsValue =
      Json.parse(
        s"""
           |{
           |  "nrSubmissionId":"2dd537bc-4244-4ebf-bac9-96321be13cdc",
           |  "cadesTSignature":"30820b4f06092a864886f70111111111c0445c464",
           |  "timestamp":"2018-02-14T09:32:15Z"
           |}
         """.stripMargin)
  }
