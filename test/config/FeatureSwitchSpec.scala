/*
 * Copyright 2023 HM Revenue & Customs
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

package config

import com.typesafe.config.ConfigFactory
import config.FeatureSwitch.{AuthFeature, Version1Feature, featureSwitches}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import support.UnitSpec

class FeatureSwitchSpec extends UnitSpec with FeatureToggleSupport with GuiceOneAppPerSuite {
  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  "version enabled" when {
    "doesnt pass regex" must {

      "return false" in {
        isVersionEnabled("test") shouldBe false
      }
    }

    "no config value" must {
      "return false" in {
        isVersionEnabled("10.0") shouldBe false
      }
    }

    "config set" must {

      "return true for enabled versions" in {
        isVersionEnabled("1.0") shouldBe true
      }

      "return false for disabled versions" in {
        disable(Version1Feature)
        isVersionEnabled("2.0") shouldBe false
      }
    }
  }

  "isEnabled" when {

    "feature is enabled in config" must {
      "return true" in {
        isEnabled(AuthFeature) shouldBe true
      }
    }

    "feature is disabled in config" must {
      "return false" in {
        disable(AuthFeature)
        isEnabled(AuthFeature) shouldBe false
      }
    }
  }

}
