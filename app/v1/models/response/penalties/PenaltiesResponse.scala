/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json._

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
                                      latePaymentPenalty1LowerRateCalculationAmount: Option[BigDecimal],
                                      latePaymentPenalty1LowerRatePercentage: Option[BigDecimal],
                                      latePaymentPenalty1HigherRateCalculationAmount: Option[BigDecimal],
                                      latePaymentPenalty1HigherRatePercentage: Option[BigDecimal],
                                      latePaymentPenalty2Days: Option[String],
                                      latePaymentPenalty2Percentage: Option[BigDecimal],
                                      penaltyChargeCreationDate: Option[String],
                                      communicationsDate: Option[String],
                                      penaltyChargeReference: Option[String],
                                      penaltyChargeDueDate: Option[String],
                                      appealInformation: Option[Seq[AppealInformation]],
                                      principalChargeDocNumber: String,
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
    penaltyChargeCreationDate       <- (JsPath \ "penaltyChargeCreationDate").readNullable[String]
    communicationsDate              <- (JsPath \ "communicationsDate").readNullable[String]
    penaltyChargeReference          <- (JsPath \ "penaltyChargeReference").readNullable[String]
    penaltyChargeDueDate            <- (JsPath \ "penaltyChargeDueDate").readNullable[String]
    appealInformation               <- (JsPath \ "appealInformation").readNullable[Seq[AppealInformation]]
    principalChargeDocNumber        <- (JsPath \ "principalChargeDocNumber").read[String]
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
      latePaymentPenalty1LowerRateCalculationAmount = lPP1LRCalculationAmount,
      latePaymentPenalty1LowerRatePercentage = lPP1LRPercentage,
      latePaymentPenalty1HigherRateCalculationAmount = lPP1HRCalculationAmount,
      latePaymentPenalty1HigherRatePercentage = lPP1HRPercentage,
      latePaymentPenalty2Days = lPP2Days,
      latePaymentPenalty2Percentage = lPP2Percentage,
      penaltyChargeCreationDate = penaltyChargeCreationDate,
      communicationsDate = communicationsDate,
      penaltyChargeReference = penaltyChargeReference,
      penaltyChargeDueDate = penaltyChargeDueDate,
      appealInformation = appealInformation,
      principalChargeDocNumber = principalChargeDocNumber,
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
          "latePaymentPenalty1LowerRateCalculationAmount" -> Json.toJson(o.latePaymentPenalty1LowerRateCalculationAmount),
          "latePaymentPenalty1LowerRatePercentage" -> Json.toJson(o.latePaymentPenalty1LowerRatePercentage),
          "latePaymentPenalty1HigherRateCalculationAmount" -> Json.toJson(o.latePaymentPenalty1HigherRateCalculationAmount),
          "latePaymentPenalty1HigherRatePercentage" -> Json.toJson(o.latePaymentPenalty1HigherRatePercentage),
          "latePaymentPenalty2Days" -> Json.toJson(o.latePaymentPenalty2Days),
          "latePaymentPenalty2Percentage" -> Json.toJson(o.latePaymentPenalty2Percentage),
          "penaltyChargeCreationDate" -> Json.toJson(o.penaltyChargeCreationDate),
          "communicationsDate" -> Json.toJson(o.communicationsDate),
          "penaltyChargeReference" -> Json.toJson(o.penaltyChargeReference),
          "penaltyChargeDueDate" -> Json.toJson(o.penaltyChargeDueDate),
          "appealInformation" -> Json.toJson(o.appealInformation),
          "principalChargeDocNumber" -> Json.toJson(o.principalChargeDocNumber),
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
                      timeToPayStartDate: Option[String],
                      timeToPayEndDate: Option[String]
                    )

object TimeToPay {
  implicit val reads: Reads[TimeToPay] = (
    (JsPath \ "TTPStartDate").readNullable[String] and
      (JsPath \ "TTPEndDate").readNullable[String]
    )(TimeToPay.apply _)

  implicit val writes: OWrites[TimeToPay] = Json.writes[TimeToPay]
}

case class Totalisations (
                           lateSubmissionPenaltyTotalValue: Option[BigDecimal],
                           penalisedPrincipalTotal: Option[BigDecimal],
                           latePaymentPenaltyPostedTotal: Option[BigDecimal],
                           latePaymentPenaltyEstimateTotal: Option[BigDecimal]
                         )

object Totalisations {
  implicit val reads: Reads[Totalisations] = (
    (JsPath \ "LSPTotalValue").readNullable[BigDecimal] and
      (JsPath \ "penalisedPrincipalTotal").readNullable[BigDecimal] and
      (JsPath \ "LPPPostedTotal").readNullable[BigDecimal] and
      (JsPath \ "LPPEstimatedTotal").readNullable[BigDecimal]
    )(Totalisations.apply _)

  implicit val writes: OWrites[Totalisations] = Json.writes[Totalisations]
}


case class LateSubmissionPenalty (
                                 summary: LateSubmissionPenaltySummary,
                                 details: Seq[LateSubmissionPenaltyDetails]
                                 )

object LateSubmissionPenalty {
  implicit val format: OFormat[LateSubmissionPenalty] = Json.format[LateSubmissionPenalty]
}

case class LateSubmissionPenaltySummary(
                                         activePenaltyPoints: BigDecimal,
                                         inactivePenaltyPoints: Int,
                                         periodOfComplianceAchievement: String,
                                         regimeThreshold: Int,
                                         penaltyChargeAmount: BigDecimal
                                       )

object LateSubmissionPenaltySummary {
  implicit val reads: Reads[LateSubmissionPenaltySummary] = (
    (JsPath \ "activePenaltyPoints").read[BigDecimal] and
      (JsPath \ "inactivePenaltyPoints").read[Int] and
      ((JsPath \ "PoCAchievementDate").read[String] orElse Reads.pure("9999-12-31")) and
      (JsPath \ "regimeThreshold").read[Int] and
      (JsPath \ "penaltyChargeAmount").read[BigDecimal]
    )(LateSubmissionPenaltySummary.apply _)

  implicit val writes: OWrites[LateSubmissionPenaltySummary] = Json.writes[LateSubmissionPenaltySummary]
}

case class LateSubmissionPenaltyDetails(
                                         penaltyNumber: String,
                                         penaltyOrder: String,
                                         penaltyCategory: LateSubmissionPenaltyCategoryUpstream,
                                         penaltyStatus: LateSubmissionPenaltyStatusUpstream,
                                         frequencyAdjustmentPointIndicator: Option[String],
                                         penaltyCreationDate: String,
                                         penaltyExpiryDate: String,
                                         expiryReason: Option[ExpiryReasonUpstream],
                                         communicationsDate: Option[String],
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
    penaltyOrder            <- (JsPath \ "penaltyOrder").read[String] orElse Reads.pure("NA")
    penaltyCategory         <- (JsPath \ "penaltyCategory").read[LateSubmissionPenaltyCategoryDownstream].map(_.toUpstreamPenaltyCategory) orElse
                                            (Reads.pure(LateSubmissionPenaltyCategoryDownstream.`P`).map(_.toUpstreamPenaltyCategory))

    penaltyStatus           <- (JsPath \ "penaltyStatus").read[LateSubmissionPenaltyStatusDownstream].map(_.toUpstreamPenaltyStatus)
    fAPIndicator            <- (JsPath \ "FAPIndicator").readNullable[String]
    penaltyCreationDate     <- (JsPath \ "penaltyCreationDate").read[String]
    penaltyExpiryDate       <- (JsPath \ "penaltyExpiryDate").read[String]
    expiryReason            <- (JsPath \ "expiryReason").readNullable[ExpiryReasonDownstream]
    communicationsDate      <- (JsPath \ "communicationsDate").readNullable[String]
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
      frequencyAdjustmentPointIndicator = fAPIndicator,
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

  implicit val reads: Reads[LateSubmissions] = (
    (JsPath \ "lateSubmissionID").read[String] and
      ((JsPath \ "taxReturnStatus").read[TaxReturnStatus] orElse Reads.pure(TaxReturnStatus.`Reversed`)) and
      (JsPath \ "taxPeriodStartDate").readNullable[String] and
      (JsPath \ "taxPeriodEndDate").readNullable[String] and
      (JsPath \ "taxPeriodDueDate").readNullable[String] and
      (JsPath \ "returnReceiptDate").readNullable[String]
    )(LateSubmissions.apply _)

  implicit val writes: OWrites[LateSubmissions] = Json.writes[LateSubmissions]

}


case class AppealInformation (
                               appealStatus: AppealStatusUpstream,
                               appealLevel: AppealLevelUpstream
                             )

object AppealInformation {
  implicit val reads: Reads[AppealInformation] = (
    (JsPath \ "appealStatus").read[AppealStatusDownstream].map(_.toUpstreamAppealStatus) and
      ((JsPath \ "appealLevel").read[AppealLevelDownstream].map(_.toUpstreamAppealLevel) orElse
                                     (Reads.pure(AppealLevelDownstream.`01`).map(_.toUpstreamAppealLevel)))
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

