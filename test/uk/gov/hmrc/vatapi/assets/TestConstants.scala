/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatapi.assets

import nrs.models._
import org.joda.time.{DateTime, LocalDate}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.vatapi.httpparsers.NRSData
import uk.gov.hmrc.vatapi.models.des.PaymentIndicator
import uk.gov.hmrc.vatapi.models.{VatReturnDeclaration, des}

object TestConstants {

  object VatReturn {
    val vatReturnDeclaration = VatReturnDeclaration(
      periodKey = "#001",
      vatDueSales = 50.00,
      vatDueAcquisitions = 100.30,
      totalVatDue = 150.30,
      vatReclaimedCurrPeriod = 40.00,
      netVatDue = 110.30,
      totalValueSalesExVAT = 1000,
      totalValuePurchasesExVAT = 200.00,
      totalValueGoodsSuppliedExVAT = 100.00,
      totalAcquisitionsExVAT = 540.00,
      finalised = true
    )

    val desVatReturnDeclaration: DateTime => des.VatReturnDeclaration = time => vatReturnDeclaration.toDes().copy(receivedAt = time)

    val vatReturnsDes = des.VatReturnsDES(
      processingDate = DateTime.parse("2018-06-30T01:20"),
      paymentIndicator = PaymentIndicator.DirectDebit,
      formBundleNumber = "123456789012",
      chargeRefNumber = Some("SKDJGFH9URGT")
    )
  }

  object NRSResponse {

    val timestamp: DateTime = DateTime.parse("2018-02-14T09:32:15Z")

    //Examples taken from NRS Spec
    val nrsSubmission: NRSSubmission = NRSSubmission(
      payload = "payload",
      metadata = Metadata(
        businessId = "",
        notableEvent = "",
        payloadContentType = "",
        payloadSha256Checksum = Some(""),
        userSubmissionTimestamp = DateTime.parse("2018-06-30T01:20"),
        identityData = IdentityData(

        ),
        userAuthToken = "",
        headerData = HeaderData(

        ),
        searchKeys = SearchKeys(
          vrn = Vrn("123456789"),
          companyName = "",
          taxPeriodEndDate = LocalDate.parse("2018-06-30")
        )
      )
    )

    val nrsData: NRSData = NRSData(
      nrSubmissionId = "2dd537bc-4244-4ebf-bac9-96321be13cdc",
      cadesTSignature = "30820b4f06092a864886f70111111111c0445c464",
      timestamp = timestamp.toString
    )
  }
}
