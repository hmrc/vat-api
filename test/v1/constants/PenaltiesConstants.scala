/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.constants

import config.AppConfig
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import v1.controllers.UserRequest
import v1.models.auth.UserDetails
import v1.models.domain.Vrn
import v1.models.errors.{ErrorWrapper, MtdError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.penalties.{PenaltiesRawData, PenaltiesRequest}
import v1.models.response.penalties._

object PenaltiesConstants {

  implicit val correlationId: String = "abc123-789xyz"
  val userDetails: UserDetails = UserDetails("Individual", None, "client-Id")
  implicit val userRequest: UserRequest[AnyContentAsEmpty.type] = UserRequest(userDetails,FakeRequest())

  val vrn: String = "123456789"
  val rawData: PenaltiesRawData = PenaltiesRawData(vrn)
  val penaltiesRequest: PenaltiesRequest = PenaltiesRequest(Vrn(vrn))
  val invalidVrn = "fakeVRN"
  val invalidRawData: PenaltiesRawData = PenaltiesRawData(invalidVrn)

  def penaltiesURl(vrn: String = vrn)(implicit appConfig: AppConfig) = s"/penalties/penalty-details/VAT/VRN/$vrn"
  def penaltiesURlWithConfig(vrn: String = vrn)(implicit appConfig: AppConfig) = appConfig.penaltiesBaseUrl + s"/penalties/penalty-details/VAT/VRN/$vrn"


  val testTotalisationMin: Totalisations = Totalisations(
    LSPTotalValue = None,
    penalisedPrincipalTotal = None,
    LPPPostedTotal = None,
    LPPEstimatedTotal = None,
  )

  val testPenaltiesTotalisationDataJsonMin: JsObject = Json.obj()

  val testTotalisationMax: Totalisations = Totalisations(
    LSPTotalValue = Some(10),
    penalisedPrincipalTotal = Some(11),
    LPPPostedTotal = Some(12),
    LPPEstimatedTotal = Some(13),
  )

  val testPenaltiesTotalisationDataJsonMax: JsObject = Json.obj(
    "LSPTotalValue" -> 10,
    "penalisedPrincipalTotal" -> 11,
    "LPPPostedTotal" -> 12,
    "LPPEstimatedTotal" -> 13,

  )

  def testlateSubmissionPenaltyDetails(testChargeRef: String): LateSubmissionPenaltyDetails = {
    LateSubmissionPenaltyDetails(
      penaltyNumber = "123",
      penaltyOrder = "123",
      penaltyCategory = LateSubmissionPenaltyCategoryUpstream.`point`,
      penaltyStatus = LateSubmissionPenaltyStatusUpstream.`active`,
      FAPIndicator = Some("123"),
      penaltyCreationDate = "123",
      triggeringProcess = "123",
      penaltyExpiryDate = "123",
      expiryReason = Some(ExpiryReasonUpstream.`appeal`),
      communicationsDate = "123",
      lateSubmissions = Some(List(LateSubmissions(
        lateSubmissionID = "123",
        taxPeriod = Some("123"),
        taxReturnStatus = TaxReturnStatus.`Open`,
        taxPeriodStartDate = Some("123"),
        taxPeriodEndDate = Some("2022-10-11"),
        taxPeriodDueDate = Some("2022-10-11"),
        returnReceiptDate = Some("2022-10-11")
      ))),
      appealInformation = Some(List(
        AppealInformation(
          appealStatus = AppealStatusUpstream.`under appeal`,
          appealLevel = AppealLevelUpstream.`statutory-review`
        )
      )),
      chargeReference = Some(testChargeRef),
      chargeAmount = Some(123),
      chargeOutstandingAmount = Some(123),
      chargeDueDate = Some("2022-10-11")
    )
  }

  def testLatePaymentPenaltyDetails(testPrincipalChargeReference: String): LatePaymentPenaltyDetails = {
    LatePaymentPenaltyDetails(
      principalChargeReference = testPrincipalChargeReference,
      penaltyCategory = "123",
      penaltyStatus = LatePaymentPenaltyStatusUpstream.`posted`,
      penaltyAmountAccruing = 123,
      penaltyAmountPosted = 123,
      penaltyAmountPaid = Some(123),
      penaltyAmountOutstanding = Some(123),
      LPP1LRCalculationAmount = Some(123),
      LPP1LRPercentage = Some(123),
      LPP1HRCalculationAmount = Some(123),
      LPP1HRPercentage = Some(123),
      LPP2Days = Some("123"),
      LPP2Percentage = Some(123),
      penaltyChargeCreationDate = "2022-10-11",
      communicationsDate = "2022-10-11",
      penaltyChargeReference = Some("123"),
      penaltyChargeDueDate = "2022-10-11",
      appealInformation = Some(Seq(
        AppealInformation(
          appealStatus = AppealStatusUpstream.`under appeal`,
          appealLevel = AppealLevelUpstream.`statutory-review`
        )
      )),
      principalChargeDocNumber = "123",
      principalChargeMainTransaction = "123",
      principalChargeSubTransaction = "123",
      principalChargeBillingFrom = "2022-10-11",
      principalChargeBillingTo = "2022-10-11",
      principalChargeDueDate = "2022-10-11",
      principalChargeLatestClearing = Some("123"),
      timeToPay = Some(Seq(
        TimeToPay(
          TTPStartDate = Some("2022-10-11"),
          TTPEndDate = Some("2022-10-11")
        )
      ))
    )
  }

  val testLateSubmissionPenalty: LateSubmissionPenalty = LateSubmissionPenalty(
    summary = LateSubmissionPenaltySummary(
      activePoints = 2,
      inactivePenaltyPoints = 2,
      PoCAchievementDate = "2022-10-11",
      regimeThreshold = 2,
      penaltyChargeAmount = 123
    ),
    details = List(
      testlateSubmissionPenaltyDetails("123"),
      testlateSubmissionPenaltyDetails("1234")
    )
  )

  val downstreamTestLateSubmissionDetailsJson: JsValue = {
    Json.parse(
    """|[{
      |            "penaltyNumber":"123",
      |            "penaltyOrder":"123",
      |            "penaltyCategory":"P",
      |            "penaltyStatus":"ACTIVE",
      |            "FAPIndicator":"123",
      |            "penaltyCreationDate":"123",
      |            "triggeringProcess":"123",
      |            "penaltyExpiryDate":"123",
      |            "expiryReason": "APP",
      |            "communicationsDate":"123",
      |            "lateSubmissions":[
      |               {
      |                  "lateSubmissionID":"123",
      |                  "taxPeriod":"123",
      |                  "taxReturnStatus":"Open",
      |                  "taxPeriodStartDate":"123",
      |                  "taxPeriodEndDate":"2022-10-11",
      |                  "taxPeriodDueDate":"2022-10-11",
      |                  "returnReceiptDate":"2022-10-11"
      |               }
      |            ],
      |            "appealInformation":[
      |               {
      |                  "appealStatus":"A",
      |                  "appealLevel":"01"
      |               }
      |            ],
      |            "chargeReference":"123",
      |            "chargeAmount":123,
      |            "chargeOutstandingAmount":123,
      |            "chargeDueDate":"2022-10-11"
      |         },
      |         {
       |            "penaltyNumber":"123",
       |            "penaltyOrder":"123",
       |            "penaltyCategory":"P",
       |            "penaltyStatus":"ACTIVE",
       |            "FAPIndicator":"123",
       |            "penaltyCreationDate":"123",
       |            "triggeringProcess":"123",
       |            "penaltyExpiryDate":"123",
       |            "expiryReason":"APP",
       |            "communicationsDate":"123",
       |            "lateSubmissions":[
       |               {
       |                  "lateSubmissionID":"123",
       |                  "taxPeriod":"123",
       |                  "taxReturnStatus":"Open",
       |                  "taxPeriodStartDate":"123",
       |                  "taxPeriodEndDate":"2022-10-11",
       |                  "taxPeriodDueDate":"2022-10-11",
       |                  "returnReceiptDate":"2022-10-11"
       |               }
       |            ],
       |            "appealInformation":[
       |               {
       |                  "appealStatus":"A",
       |                  "appealLevel":"01"
       |               }
       |            ],
       |            "chargeReference":"1234",
       |            "chargeAmount":123,
       |            "chargeOutstandingAmount":123,
       |            "chargeDueDate":"2022-10-11"
       |         }
      |      ]
      |""".stripMargin)
  }

  val upstreamTestLateSubmissionDetailsJson: JsValue = {
    Json.parse(
    """|[{
      |            "penaltyNumber":"123",
      |            "penaltyOrder":"123",
      |            "penaltyCategory":"point",
      |            "penaltyStatus":"active",
      |            "FAPIndicator":"123",
      |            "penaltyCreationDate":"123",
      |            "triggeringProcess":"123",
      |            "penaltyExpiryDate":"123",
      |            "expiryReason": "appeal",
      |            "communicationsDate":"123",
      |            "lateSubmissions":[
      |               {
      |                  "lateSubmissionID":"123",
      |                  "taxPeriod":"123",
      |                  "taxReturnStatus":"Open",
      |                  "taxPeriodStartDate":"123",
      |                  "taxPeriodEndDate":"2022-10-11",
      |                  "taxPeriodDueDate":"2022-10-11",
      |                  "returnReceiptDate":"2022-10-11"
      |               }
      |            ],
      |            "appealInformation":[
      |               {
      |                  "appealStatus":"under appeal",
      |                  "appealLevel":"statutory-review"
      |               }
      |            ],
      |            "chargeReference":"123",
      |            "chargeAmount":123,
      |            "chargeOutstandingAmount":123,
      |            "chargeDueDate":"2022-10-11"
      |         },
      |         {
       |            "penaltyNumber":"123",
       |            "penaltyOrder":"123",
       |            "penaltyCategory":"point",
       |            "penaltyStatus":"active",
       |            "FAPIndicator":"123",
       |            "penaltyCreationDate":"123",
       |            "triggeringProcess":"123",
       |            "penaltyExpiryDate":"123",
       |            "expiryReason":"appeal",
       |            "communicationsDate":"123",
       |            "lateSubmissions":[
       |               {
       |                  "lateSubmissionID":"123",
       |                  "taxPeriod":"123",
       |                  "taxReturnStatus":"Open",
       |                  "taxPeriodStartDate":"123",
       |                  "taxPeriodEndDate":"2022-10-11",
       |                  "taxPeriodDueDate":"2022-10-11",
       |                  "returnReceiptDate":"2022-10-11"
       |               }
       |            ],
       |            "appealInformation":[
       |               {
       |                  "appealStatus":"under appeal",
       |                  "appealLevel":"statutory-review"
       |               }
       |            ],
       |            "chargeReference":"1234",
       |            "chargeAmount":123,
       |            "chargeOutstandingAmount":123,
       |            "chargeDueDate":"2022-10-11"
       |         }
      |      ]
      |""".stripMargin)
  }


  val downstreamTestLatePaymentPenaltyDetailsJson: JsValue = {
    Json.parse(
      """
        |[{
        |"principalChargeReference": "123",
        |    "penaltyCategory": "123",
        |    "penaltyStatus": "P",
        |    "penaltyAmountAccruing": 123,
        |    "penaltyAmountPosted": 123,
        |    "penaltyAmountPaid": 123,
        |    "penaltyAmountOutstanding": 123,
        |    "LPP1LRCalculationAmount": 123,
        |    "LPP1LRDays": "123",
        |    "LPP1LRPercentage": 123,
        |    "LPP1HRCalculationAmount": 123,
        |    "LPP1HRPercentage": 123,
        |    "LPP2Days": "123",
        |    "LPP2Percentage": 123,
        |    "penaltyChargeCreationDate": "2022-10-11",
        |    "communicationsDate": "2022-10-11",
        |    "penaltyChargeReference": "123",
        |    "penaltyChargeDueDate": "2022-10-11",
        |    "appealInformation": [{
        |        "appealStatus":  "A",
        |        "appealLevel": "01"
        |      }],
        |    "principalChargeDocNumber": "123",
        |    "principalChargeMainTransaction": "123",
        |    "principalChargeSubTransaction": "123",
        |    "principalChargeBillingFrom": "2022-10-11",
        |    "principalChargeBillingTo": "2022-10-11",
        |    "principalChargeDueDate": "2022-10-11",
        |    "principalChargeLatestClearing": "123",
        |    "timeToPay": [{
        |        "TTPStartDate": "2022-10-11",
        |        "TTPEndDate": "2022-10-11"
        |        }]
        |},
        |{
        |"principalChargeReference": "1234",
        |    "penaltyCategory": "123",
        |    "penaltyStatus": "P",
        |    "penaltyAmountAccruing": 123,
        |    "penaltyAmountPosted": 123,
        |    "penaltyAmountPaid": 123,
        |    "penaltyAmountOutstanding": 123,
        |    "LPP1LRCalculationAmount": 123,
        |    "LPP1LRDays": "123",
        |    "LPP1LRPercentage": 123,
        |    "LPP1HRCalculationAmount": 123,
        |    "LPP1HRPercentage": 123,
        |    "LPP2Days": "123",
        |    "LPP2Percentage": 123,
        |    "penaltyChargeCreationDate": "2022-10-11",
        |    "communicationsDate": "2022-10-11",
        |    "penaltyChargeReference": "123",
        |    "penaltyChargeDueDate": "2022-10-11",
        |    "appealInformation": [{
        |        "appealStatus":  "A",
        |        "appealLevel": "01"
        |      }],
        |    "principalChargeDocNumber": "123",
        |    "principalChargeMainTransaction": "123",
        |    "principalChargeSubTransaction": "123",
        |    "principalChargeBillingFrom": "2022-10-11",
        |    "principalChargeBillingTo": "2022-10-11",
        |    "principalChargeDueDate": "2022-10-11",
        |    "principalChargeLatestClearing": "123",
        |    "timeToPay": [{
        |        "TTPStartDate": "2022-10-11",
        |        "TTPEndDate": "2022-10-11"
        |        }]
        |}
        | ]
        |""".stripMargin
    )
  }

  val upstreamTestLatePaymentPenaltyDetailsJson: JsValue = {
    Json.parse(
      """
        |[{
        |"principalChargeReference": "123",
        |    "penaltyCategory": "123",
        |    "penaltyStatus": "posted",
        |    "penaltyAmountAccruing": 123,
        |    "penaltyAmountPosted": 123,
        |    "penaltyAmountPaid": 123,
        |    "penaltyAmountOutstanding": 123,
        |    "LPP1LRCalculationAmount": 123,
        |    "LPP1LRPercentage": 123,
        |    "LPP1HRCalculationAmount": 123,
        |    "LPP1HRPercentage": 123,
        |    "LPP2Days": "123",
        |    "LPP2Percentage": 123,
        |    "penaltyChargeCreationDate": "2022-10-11",
        |    "communicationsDate": "2022-10-11",
        |    "penaltyChargeReference": "123",
        |    "penaltyChargeDueDate": "2022-10-11",
        |    "appealInformation": [{
        |        "appealStatus":  "under appeal",
        |        "appealLevel": "statutory-review"
        |      }],
        |    "principalChargeDocNumber": "123",
        |    "principalChargeMainTransaction": "123",
        |    "principalChargeSubTransaction": "123",
        |    "principalChargeBillingFrom": "2022-10-11",
        |    "principalChargeBillingTo": "2022-10-11",
        |    "principalChargeDueDate": "2022-10-11",
        |    "principalChargeLatestClearing": "123",
        |    "timeToPay": [{
        |        "TTPStartDate": "2022-10-11",
        |        "TTPEndDate": "2022-10-11"
        |        }]
        |},
        |{
        |"principalChargeReference": "1234",
        |    "penaltyCategory": "123",
        |    "penaltyStatus": "posted",
        |    "penaltyAmountAccruing": 123,
        |    "penaltyAmountPosted": 123,
        |    "penaltyAmountPaid": 123,
        |    "penaltyAmountOutstanding": 123,
        |    "LPP1LRCalculationAmount": 123,
        |    "LPP1LRPercentage": 123,
        |    "LPP1HRCalculationAmount": 123,
        |    "LPP1HRPercentage": 123,
        |    "LPP2Days": "123",
        |    "LPP2Percentage": 123,
        |    "penaltyChargeCreationDate": "2022-10-11",
        |    "communicationsDate": "2022-10-11",
        |    "penaltyChargeReference": "123",
        |    "penaltyChargeDueDate": "2022-10-11",
        |    "appealInformation": [{
        |        "appealStatus":  "under appeal",
        |        "appealLevel": "statutory-review"
        |      }],
        |    "principalChargeDocNumber": "123",
        |    "principalChargeMainTransaction": "123",
        |    "principalChargeSubTransaction": "123",
        |    "principalChargeBillingFrom": "2022-10-11",
        |    "principalChargeBillingTo": "2022-10-11",
        |    "principalChargeDueDate": "2022-10-11",
        |    "principalChargeLatestClearing": "123",
        |    "timeToPay": [{
        |        "TTPStartDate": "2022-10-11",
        |        "TTPEndDate": "2022-10-11"
        |        }]
        |}
        | ]
        |""".stripMargin
    )
  }

  val testPenaltiesResponseMin: PenaltiesResponse = PenaltiesResponse(
    totalisations = None,
    lateSubmissionPenalty = None,
    latePaymentPenalty = None
  )

  val testLatePaymentPenalty: LatePaymentPenalty = LatePaymentPenalty(
    details = Some(List(
      testLatePaymentPenaltyDetails("123"),
      testLatePaymentPenaltyDetails("1234")

    ))
  )

  val testLateSubmissionPenaltySummaryJson: JsObject = Json.obj(
    "activePoints" -> 2,
    "inactivePenaltyPoints" -> 2,
    "PoCAchievementDate" -> "2022-10-11",
    "regimeThreshold" -> 2,
    "penaltyChargeAmount" -> 123
  )

  val upstreamTestLatePaymentPenaltyJson: JsObject = Json.obj(
    "details" -> upstreamTestLatePaymentPenaltyDetailsJson
  )

  val downstreamTestLatePaymentPenaltyJson: JsObject = Json.obj(
    "details" -> downstreamTestLatePaymentPenaltyDetailsJson
  )

  val downstreamTestLateSubmissionPenaltyJson: JsObject = Json.obj(
    "summary" -> testLateSubmissionPenaltySummaryJson,
    "details" -> downstreamTestLateSubmissionDetailsJson
  )

  val upstreamTestLateSubmissionPenaltyJson: JsObject = Json.obj(
    "summary" -> testLateSubmissionPenaltySummaryJson,
    "details" -> upstreamTestLateSubmissionDetailsJson
  )


  val testPenaltiesResponseMax: PenaltiesResponse = PenaltiesResponse(
    totalisations = Some(testTotalisationMax),
    lateSubmissionPenalty = Some(testLateSubmissionPenalty),
    latePaymentPenalty = Some(testLatePaymentPenalty)
  )

  val testPenaltiesResponseJsonMin: JsObject = Json.obj()

  val upstreamTestPenaltiesResponseJsonMax: JsObject = Json.obj(
    "totalisations" -> Some(testPenaltiesTotalisationDataJsonMax),
    "lateSubmissionPenalty" -> Some(upstreamTestLateSubmissionPenaltyJson),
    "latePaymentPenalty" -> Some(upstreamTestLatePaymentPenaltyJson)
  )

  val downstreamTestPenaltiesResponseJsonMax: JsObject = Json.obj(
    "totalisations" -> Some(testPenaltiesTotalisationDataJsonMax),
    "lateSubmissionPenalty" -> Some(downstreamTestLateSubmissionPenaltyJson),
    "latePaymentPenalty" -> Some(downstreamTestLatePaymentPenaltyJson)
  )

  def wrappedPenaltiesResponse(penaltiesResponse: PenaltiesResponse = testPenaltiesResponseMin): ResponseWrapper[PenaltiesResponse] = {
    ResponseWrapper(correlationId, penaltiesResponse)
  }

  def errorWrapper(error: MtdError): ErrorWrapper = ErrorWrapper(correlationId, error)
  def errorWrapperMulti(error: Seq[MtdError]): ErrorWrapper = ErrorWrapper(correlationId, error.head, Some(error.tail))
}
