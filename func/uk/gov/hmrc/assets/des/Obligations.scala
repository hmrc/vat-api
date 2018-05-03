package uk.gov.hmrc.assets.des

object Obligations {

  object Obligations {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |  "obligations": [
         |        {
         |        "identification": {
         |          "incomeSourceType": "A",
         |          "referenceNumber": "$id",
         |          "referenceType": "VRN"
         |        },
         |      "obligationDetails": [
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDateReceived": "2017-08-01",
         |          "inboundCorrespondenceDueDate": "2017-08-05",
         |          "periodKey": "#001"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "periodKey": "#002"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "periodKey": "#003"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "#004"
         |        }
         |      ]
         |    }
         |  ]
         |}
         """.stripMargin
    }
  }

  object ObligationsWithNoIncomeSourceType {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |  "obligations": [
         |        {
         |        "identification": {
         |          "referenceNumber": "$id",
         |          "referenceType": "VRN"
         |        },
         |      "obligationDetails": [
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDateReceived": "2017-08-01",
         |          "inboundCorrespondenceDueDate": "2017-08-05",
         |          "periodKey": "#001"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "periodKey": "#002"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "periodKey": "#003"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "#004"
         |        }
         |      ]
         |    }
         |  ]
         |}
         """.stripMargin
    }
  }

  object ObligationsWithoutIdentification {
    def apply(id: String = "abc"): String = {
      s"""
         |{
         |  "obligations": [
         |  {
         |      "obligationDetails": [
         |        {
         |          "status": "F",
         |          "inboundCorrespondenceFromDate": "2017-04-06",
         |          "inboundCorrespondenceToDate": "2017-07-05",
         |          "inboundCorrespondenceDateReceived": "2017-08-01",
         |          "inboundCorrespondenceDueDate": "2017-08-05",
         |          "periodKey": "#001"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-07-06",
         |          "inboundCorrespondenceToDate": "2017-10-05",
         |          "inboundCorrespondenceDueDate": "2017-11-05",
         |          "periodKey": "#002"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2017-10-06",
         |          "inboundCorrespondenceToDate": "2018-01-05",
         |          "inboundCorrespondenceDueDate": "2018-02-05",
         |          "periodKey": "#003"
         |        },
         |        {
         |          "status": "O",
         |          "inboundCorrespondenceFromDate": "2018-01-06",
         |          "inboundCorrespondenceToDate": "2018-04-05",
         |          "inboundCorrespondenceDueDate": "2018-05-06",
         |          "periodKey": "#004"
         |        }
         |      ]
         |    }
         |  ]
         |}
         """.stripMargin
    }
  }

}
