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

package uk.gov.hmrc.vatapi

import com.mongodb.BasicDBObject
import com.mongodb.casbah.MongoClient
import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodProcess, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.Logger
import uk.gov.hmrc.mongo.MongoConnector

trait MongoEmbeddedDatabase extends UnitSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  import MongoEmbeddedDatabase._

  implicit val mongo = MongoConnector(mongoUri).db

  lazy private val mongoClient =
    MongoClient("localhost", if (useEmbeddedMongo) embeddedPort else diskPort)("vat-api")

  override def beforeEach(): Unit =
    List("vat").foreach { coll =>
      mongoClient.getCollection(coll).remove(new BasicDBObject())
    }

  startEmbeddedMongo()
  System.setProperty("RELEASED_ROUTES", "prod.Routes")
}

object MongoEmbeddedDatabase {
  private val diskPort = 27017
  private val embeddedPort = 12345
  private val localhost = "127.0.0.1"
  private val mongoUri = sys.env.getOrElse("MONGO_TEST_URI", s"mongodb://$localhost:$embeddedPort/vat-api")
  private val useEmbeddedMongo = mongoUri.contains(embeddedPort.toString)
  private val runtimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent)
    .build()

  private var mongodExe: MongodExecutable = _
  @volatile private var mongod: MongodProcess = _

  private def startEmbeddedMongo() = this.synchronized {
    if (mongod == null && useEmbeddedMongo) {
      Logger.info("Starting embedded mongo")
      mongodExe = MongodStarter
        .getInstance(runtimeConfig)
        .prepare(new MongodConfigBuilder()
          .version(Version.Main.PRODUCTION)
          .net(new Net(localhost, embeddedPort, Network.localhostIsIPv6()))
          .build())
      mongod = mongodExe.start()
    }
  }
}
