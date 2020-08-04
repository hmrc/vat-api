/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.filters

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import uk.gov.hmrc.play.bootstrap.filters._

case class VatApiFilters @Inject()(
                                    emptyResponseFilter: EmptyResponseFilter,
                                    headerValidatorFilter: HeaderValidatorFilter,
                                    setContentTypeFilter: SetContentTypeFilter,
                                    setXContentTypeOptionsFilter: SetXContentTypeOptionsFilter,
                                    logging: LoggingFilter,
                                    defaultFilters: MicroserviceFilters
                                  ) extends DefaultHttpFilters(
  defaultFilters.filters :+
    emptyResponseFilter :+
    headerValidatorFilter :+
    setContentTypeFilter :+
    logging :+
    setXContentTypeOptionsFilter: _*)




