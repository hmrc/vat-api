package v1.models.request.submit

import play.api.libs.json.{Json, OWrites, Reads}

case class SubmitRequestBody(periodKey: String,
                             vatDueSales: BigDecimal,
                             vatDueAcquisitions: BigDecimal,
                             totalVatDue: BigDecimal,
                             vatReclaimedCurrPeriod: BigDecimal,
                             netVatDue: BigDecimal,
                             totalValueSalesExVAT: BigDecimal,
                             totalValuePurchasesExVAT: BigDecimal,
                             totalValueGoodsSuppliedExVAT: BigDecimal,
                             totalAcquisitionsExVAT: BigDecimal,
                             finalised: Boolean)

object SubmitRequestBody {

  implicit val reads: Reads[SubmitRequestBody] = Json.reads[SubmitRequestBody]
  implicit val writes: OWrites[SubmitRequestBody] = Json.writes[SubmitRequestBody]

}