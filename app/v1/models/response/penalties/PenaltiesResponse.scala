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

package v1.models.response.penalties

import play.api.libs.json._
import v1.models.errors.MtdError
import play.api.libs.functional.syntax._

case class LatePaymentPenalty(
                             details: Option[Seq[LatePaymentPenaltyDetails]]
                             )

object LatePaymentPenalty {
  implicit val format: OFormat[LatePaymentPenalty] = Json.format[LatePaymentPenalty]
}



case class LatePaymentPenaltyDetails(
                                      principalChargeReference: String,
                                      penaltyCategory: String,
                                      penaltyStatus: LatePaymentPenaltyStatusUpstream,
                                      penaltyAmountAccruing: BigDecimal,
                                      penaltyAmountPosted: BigDecimal,
                                      penaltyAmountPaid: Option[BigDecimal],
                                      penaltyAmountOutstanding: Option[BigDecimal],
                                      LPP1LRCalculationAmount: Option[BigDecimal],
                                      LPP1LRPercentage: Option[BigDecimal],
                                      LPP1HRCalculationAmount: Option[BigDecimal],
                                      LPP1HRPercentage: Option[BigDecimal],
                                      LPP2Days: Option[String],
                                      LPP2Percentage: Option[BigDecimal],
                                      penaltyChargeCreationDate: String,
                                      communicationsDate: String,
                                      penaltyChargeReference: Option[String],
                                      penaltyChargeDueDate: String,
                                      appealInformation: Option[Seq[AppealInformation]],
                                      principalChargeDocNumber: String,
                                      principalChargeMainTransaction: String,
                                      principalChargeSubTransaction: String,
                                      principalChargeBillingFrom: String,
                                      principalChargeBillingTo: String,
                                      principalChargeDueDate: String,
                                      principalChargeLatestClearing: Option[String],
                                      timeToPay: Option[Seq[TimeToPay]]
                                    )

object LatePaymentPenaltyDetails {

  implicit val reads: Reads[LatePaymentPenaltyDetails] = for {
    principalChargeReference        <- (JsPath \ "principalChargeReference").read[String]
    penaltyCategory                 <- (JsPath \ "penaltyCategory").read[String]
    penaltyStatus                   <- (JsPath \ "penaltyStatus").read[LatePaymentPenaltyStatusDownstream].map(_.toUpstreamPenaltyStatus)
    penaltyAmountAccruing           <- (JsPath \ "penaltyAmountAccruing").read[BigDecimal]
    penaltyAmountPosted             <- (JsPath \ "penaltyAmountPosted").read[BigDecimal]
    penaltyAmountPaid               <- (JsPath \ "penaltyAmountPaid").readNullable[BigDecimal]
    penaltyAmountOutstanding        <- (JsPath \ "penaltyAmountOutstanding").readNullable[BigDecimal]
    lPP1LRCalculationAmount         <- (JsPath \ "LPP1LRCalculationAmount").readNullable[BigDecimal]
    lPP1LRPercentage                <- (JsPath \ "LPP1LRPercentage").readNullable[BigDecimal]
    lPP1HRCalculationAmount         <- (JsPath \ "LPP1HRCalculationAmount").readNullable[BigDecimal]
    lPP1HRPercentage                <- (JsPath \ "LPP1HRPercentage").readNullable[BigDecimal]
    lPP2Days                        <- (JsPath \ "LPP2Days").readNullable[String]
    lPP2Percentage                  <- (JsPath \ "LPP2Percentage").readNullable[BigDecimal]
    penaltyChargeCreationDate       <- (JsPath \ "penaltyChargeCreationDate").read[String]
    communicationsDate              <- (JsPath \ "communicationsDate").read[String]
    penaltyChargeReference          <- (JsPath \ "penaltyChargeReference").readNullable[String]
    penaltyChargeDueDate            <- (JsPath \ "penaltyChargeDueDate").read[String]
    appealInformation               <- (JsPath \ "appealInformation").readNullable[Seq[AppealInformation]]
    principalChargeDocNumber        <- (JsPath \ "principalChargeDocNumber").read[String]
    principalChargeMainTransaction  <- (JsPath \ "principalChargeMainTransaction").read[String]
    principalChargeSubTransaction   <- (JsPath \ "principalChargeSubTransaction").read[String]
    principalChargeBillingFrom      <- (JsPath \ "principalChargeBillingFrom").read[String]
    principalChargeBillingTo        <- (JsPath \ "principalChargeBillingTo").read[String]
    principalChargeDueDate          <- (JsPath \ "principalChargeDueDate").read[String]
    principalChargeLatestClearing   <- (JsPath \ "principalChargeLatestClearing").readNullable[String]
    timeToPay                       <- (JsPath \ "timeToPay").readNullable[Seq[TimeToPay]]
  } yield {
    LatePaymentPenaltyDetails(
      principalChargeReference = principalChargeReference,
      penaltyCategory = penaltyCategory,
      penaltyStatus = penaltyStatus,
      penaltyAmountAccruing = penaltyAmountAccruing,
      penaltyAmountPosted = penaltyAmountPosted,
      penaltyAmountPaid = penaltyAmountPaid,
      penaltyAmountOutstanding = penaltyAmountOutstanding,
      LPP1LRCalculationAmount = lPP1LRCalculationAmount,
      LPP1LRPercentage = lPP1LRPercentage,
      LPP1HRCalculationAmount = lPP1HRCalculationAmount,
      LPP1HRPercentage = lPP1HRPercentage,
      LPP2Days = lPP2Days,
      LPP2Percentage = lPP2Percentage,
      penaltyChargeCreationDate = penaltyChargeCreationDate,
      communicationsDate = communicationsDate,
      penaltyChargeReference = penaltyChargeReference,
      penaltyChargeDueDate = penaltyChargeDueDate,
      appealInformation = appealInformation,
      principalChargeDocNumber = principalChargeDocNumber,
      principalChargeMainTransaction = principalChargeMainTransaction,
      principalChargeSubTransaction = principalChargeSubTransaction,
      principalChargeBillingFrom = principalChargeBillingFrom,
      principalChargeBillingTo = principalChargeBillingTo,
      principalChargeDueDate = principalChargeDueDate,
      principalChargeLatestClearing = principalChargeLatestClearing,
      timeToPay = timeToPay
    )
  }

  implicit val writes: Writes[LatePaymentPenaltyDetails] = new Writes[LatePaymentPenaltyDetails] {
    override def writes(o: LatePaymentPenaltyDetails): JsValue = {
      JsObject(
        Map(
          "principalChargeReference" -> Json.toJson(o.principalChargeReference),
          "penaltyCategory" -> Json.toJson(o.penaltyCategory),
          "penaltyStatus" -> Json.toJson(o.penaltyStatus),
          "penaltyAmountAccruing" -> Json.toJson(o.penaltyAmountAccruing),
          "penaltyAmountPosted" -> Json.toJson(o.penaltyAmountPosted),
          "penaltyAmountPaid" -> Json.toJson(o.penaltyAmountPaid),
          "penaltyAmountOutstanding" -> Json.toJson(o.penaltyAmountOutstanding),
          "LPP1LRCalculationAmount" -> Json.toJson(o.LPP1LRCalculationAmount),
          "LPP1LRPercentage" -> Json.toJson(o.LPP1LRPercentage),
          "LPP1HRCalculationAmount" -> Json.toJson(o.LPP1HRCalculationAmount),
          "LPP1HRPercentage" -> Json.toJson(o.LPP1HRPercentage),
          "LPP2Days" -> Json.toJson(o.LPP2Days),
          "LPP2Percentage" -> Json.toJson(o.LPP2Percentage),
          "penaltyChargeCreationDate" -> Json.toJson(o.penaltyChargeCreationDate),
          "communicationsDate" -> Json.toJson(o.communicationsDate),
          "penaltyChargeReference" -> Json.toJson(o.penaltyChargeReference),
          "penaltyChargeDueDate" -> Json.toJson(o.penaltyChargeDueDate),
          "appealInformation" -> Json.toJson(o.appealInformation),
          "principalChargeDocNumber" -> Json.toJson(o.principalChargeDocNumber),
          "principalChargeMainTransaction" -> Json.toJson(o.principalChargeMainTransaction),
          "principalChargeSubTransaction" -> Json.toJson(o.principalChargeSubTransaction),
          "principalChargeBillingFrom" -> Json.toJson(o.principalChargeBillingFrom),
          "principalChargeBillingTo" -> Json.toJson(o.principalChargeBillingTo),
          "principalChargeDueDate" -> Json.toJson(o.principalChargeDueDate),
          "principalChargeLatestClearing" -> Json.toJson(o.principalChargeLatestClearing),
          "timeToPay" -> Json.toJson(o.timeToPay),
      ).filterNot(_._2 == JsNull)
      )
    }
  }
}


case class TimeToPay(
                      TTPStartDate: Option[String],
                      TTPEndDate: Option[String]
                    )

object TimeToPay {
  implicit val format: OFormat[TimeToPay] = Json.format[TimeToPay]
}

case class Totalisations (
                           LSPTotalValue: Option[BigDecimal],
                           penalisedPrincipalTotal: Option[BigDecimal],
                           LPPPostedTotal: Option[BigDecimal],
                           LPPEstimatedTotal: Option[BigDecimal]
                         )

object Totalisations {
  implicit val format: OFormat[Totalisations] = Json.format[Totalisations]
}


case class LateSubmissionPenalty (
                                 summary: LateSubmissionPenaltySummary,
                                 details: Seq[LateSubmissionPenaltyDetails]
                                 )

object LateSubmissionPenalty {
  implicit val format: OFormat[LateSubmissionPenalty] = Json.format[LateSubmissionPenalty]
}

case class LateSubmissionPenaltySummary(
                                         activePoints: BigDecimal,
                                         inactivePenaltyPoints: Int,
                                         PoCAchievementDate: String,
                                         regimeThreshold: Int,
                                         penaltyChargeAmount: BigDecimal
                                       )

object LateSubmissionPenaltySummary {
  implicit val format: OFormat[LateSubmissionPenaltySummary] = Json.format[LateSubmissionPenaltySummary]
}

case class LateSubmissionPenaltyDetails(
                                         penaltyNumber: String,
                                         penaltyOrder: String,
                                         penaltyCategory: LateSubmissionPenaltyCategoryUpstream,
                                         penaltyStatus: LateSubmissionPenaltyStatusUpstream,
                                         FAPIndicator: Option[String],
                                         penaltyCreationDate: String,
                                         penaltyExpiryDate: String,
                                         expiryReason: Option[ExpiryReasonUpstream],
                                         communicationsDate: String,
                                         lateSubmissions: Option[Seq[LateSubmissions]],
                                         appealInformation: Option[Seq[AppealInformation]],
                                         chargeReference: Option[String],
                                         chargeAmount: Option[BigDecimal],
                                         chargeOutstandingAmount: Option[BigDecimal],
                                         chargeDueDate: Option[String]
                  )

object LateSubmissionPenaltyDetails {
  implicit val reads: Reads[LateSubmissionPenaltyDetails] = for {
    penaltyNumber           <- (JsPath \ "penaltyNumber").read[String]
    penaltyOrder            <- (JsPath \ "penaltyOrder").read[String]
    penaltyCategory         <- (JsPath \ "penaltyCategory").read[LateSubmissionPenaltyCategoryDownstream].map(_.toUpstreamPenaltyCategory)
    penaltyStatus           <- (JsPath \ "penaltyStatus").read[LateSubmissionPenaltyStatusDownstream].map(_.toUpstreamPenaltyStatus)
    fAPIndicator            <- (JsPath \ "FAPIndicator").readNullable[String]
    penaltyCreationDate     <- (JsPath \ "penaltyCreationDate").read[String]
    penaltyExpiryDate       <- (JsPath \ "penaltyExpiryDate").read[String]
    expiryReason            <- (JsPath \ "expiryReason").readNullable[ExpiryReasonDownstream]
    communicationsDate      <- (JsPath \ "communicationsDate").read[String]
    lateSubmissions         <- (JsPath \ "lateSubmissions").readNullable[Seq[LateSubmissions]]
    appealInformation       <- (JsPath \ "appealInformation").readNullable[Seq[AppealInformation]]
    chargeReference         <- (JsPath \ "chargeReference").readNullable[String]
    chargeAmount            <- (JsPath \ "chargeAmount").readNullable[BigDecimal]
    chargeOutstandingAmount <- (JsPath \ "chargeOutstandingAmount").readNullable[BigDecimal]
    chargeDueDate           <- (JsPath \ "chargeDueDate").readNullable[String]
  } yield {
      LateSubmissionPenaltyDetails(
      penaltyNumber = penaltyNumber,
      penaltyOrder = penaltyOrder,
      penaltyCategory = penaltyCategory,
      penaltyStatus = penaltyStatus,
      FAPIndicator = fAPIndicator,
      penaltyCreationDate = penaltyCreationDate,
      penaltyExpiryDate = penaltyExpiryDate,
      expiryReason = if (expiryReason.isDefined) Some(expiryReason.get.toUpstreamExpiryReason) else None,
      communicationsDate = communicationsDate,
      lateSubmissions = lateSubmissions,
      appealInformation = appealInformation,
      chargeReference = chargeReference,
      chargeAmount = chargeAmount,
      chargeOutstandingAmount = chargeOutstandingAmount,
      chargeDueDate = chargeDueDate
    )
  }

  implicit val writes: OWrites[LateSubmissionPenaltyDetails] = Json.writes[LateSubmissionPenaltyDetails]
}

case class LateSubmissions(
                            lateSubmissionID: String,
                            taxReturnStatus: TaxReturnStatus,
                            taxPeriodStartDate: Option[String],
                            taxPeriodEndDate: Option[String],
                            taxPeriodDueDate: Option[String],
                            returnReceiptDate: Option[String]
                          )

object LateSubmissions {
  implicit val format: OFormat[LateSubmissions] = Json.format[LateSubmissions]
}


case class AppealInformation (
                               appealStatus: AppealStatusUpstream,
                               appealLevel: AppealLevelUpstream
                             )

object AppealInformation {
  implicit val reads: Reads[AppealInformation] = (
    (JsPath \ "appealStatus").read[AppealStatusDownstream].map(_.toUpstreamAppealStatus) and
      (JsPath \ "appealLevel").read[AppealLevelDownstream].map(_.toUpstreamAppealLevel)
    )(AppealInformation.apply _)

  implicit val writes: OWrites[AppealInformation] = Json.writes[AppealInformation]
}


case class PenaltiesResponse(
                              totalisations: Option[Totalisations],
                              lateSubmissionPenalty: Option[LateSubmissionPenalty],
                              latePaymentPenalty: Option[LatePaymentPenalty]
                            )

object PenaltiesResponse {
  implicit val format: OFormat[PenaltiesResponse] = Json.format[PenaltiesResponse]
}


case class PenaltiesErrors(
                         failures: Seq[PenaltyError]
                         )

object PenaltiesErrors {
  implicit val format: OFormat[PenaltiesErrors] = Json.format[PenaltiesErrors]
}

case class PenaltyError(
                        code: String,
                        reason: String
                        )

object PenaltyError {
  implicit val format: OFormat[PenaltyError] = Json.format[PenaltyError]
}

