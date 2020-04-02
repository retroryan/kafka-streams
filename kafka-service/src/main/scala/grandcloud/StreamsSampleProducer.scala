
package grandcloud

import akka.{Done, NotUsed}
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object StreamsSampleProducer extends App with StrictLogging {

  object RootBehavior {
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] {
      case _ => Behaviors.same
    }
  }

  case class ProducerSetup(config: Config, as: ActorSystem[Nothing], producerSettings: ProducerSettings[String, String], transactionTopic:String)
  val producerSetup = createProducerSetup()

  testDivertTo(producerSetup)
  runSampleProducer(producerSetup)

  def createProducerSetup() = {
    val maybeEnvStr = sys.env.get("CONF_ENV")
    val config: Config = maybeEnvStr.fold(ConfigFactory.load()) { env =>
      ConfigFactory.load(s"application-$env").withFallback(ConfigFactory.load())
    }
    implicit val as: ActorSystem[Nothing] =
      ActorSystem[Nothing](RootBehavior(), "stream-test", config)

    val producerConfig   = config.getConfig("akka.kafka.producer")
    val transactionTopic = producerConfig.getString("transaction-topic")
    val bootstrapServers = producerConfig.getString("bootstrapServers")

    val producerSettings: ProducerSettings[String, String] =
      ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServers)

    ProducerSetup(config, as, producerSettings, transactionTopic)
  }



  type EitherInt = Either[Exception, Int]

  val flow: Flow[EitherInt, ProducerRecord[String, String], NotUsed] = Flow[EitherInt]
    .collect {
      case Left(exception) => exception
    }
    .map(value => new ProducerRecord[String, String]("topic", value.getMessage))

  /** How can we sink the exceptions from the Either Int to the Kafka Transaction Topic?
   **/
  def specialHandler(
      logger: Logger,
      producerSetup: ProducerSetup): Sink[EitherInt, Future[Done]] = {

    logger.info(s"Topic: ${producerSetup.transactionTopic}")

    Sink.foreach { eitherInt: EitherInt =>
      logger.info(s"ODD: $eitherInt")
    }
  }

  def testDivertTo(producerSetup: ProducerSetup) = {

    implicit val materializer: Materializer = Materializer.matFromSystem(producerSetup.as)

    Source(1 to 10)
      .map(nxt =>
        if (nxt % 2 == 0) {
          val downStreamVal: EitherInt = Right(nxt)
          downStreamVal
        } else {
          val downStreamVal: EitherInt = Left(new Exception("Not Even"))
          downStreamVal
        })
        //How can we divert the errors to a kafka producer sink?
      .divertTo(specialHandler(producerSetup.as.log, producerSetup), _.isLeft)
      .to(Sink.foreach { eitherInt: EitherInt =>
        println(s"ODD:  $eitherInt")
      })
      .run()
  }

  def runSampleProducer(producerSetup: ProducerSetup) = {

    implicit val materializer: Materializer = Materializer.matFromSystem(producerSetup.as)

    val done: Future[Done] =
      Source(200 to 250)
        .map(_.toString)
        .map { value =>
            producerSetup.as.log.info(s"Producing: $value")
            new ProducerRecord[String, String](producerSetup.transactionTopic, value)
        }
        .runWith(Producer.plainSink(producerSetup.producerSettings))

     val isReallyDone = Await.result(done, 30.seconds)
     logger.info(s"Producer Is Done? $isReallyDone")
  }
}
