package com.velocidi.apso.akka.http

import java.net.InetAddress

import scala.concurrent.Future
import scala.concurrent.duration._

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.RemoteAddress.IP
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.stream.scaladsl.Flow
import net.ruippeixotog.akka.testkit.specs2.mutable.AkkaSpecificationLike
import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.Scope

import com.velocidi.apso.NetUtils._

class ProxySupportSpec(implicit ee: ExecutionEnv) extends Specs2RouteTest with AkkaSpecificationLike with ProxySupport {

  trait MockServer extends Scope {
    def serverResponse(req: HttpRequest) = HttpResponse(entity = req.uri.toRelative.toString)

    val (interface, port) = ("localhost", availablePort())

    // Server that replies with the request relative URI and ignores DELETE requests
    val boundFuture = {
      val serverFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Flow.apply[HttpRequest]
        .filter(_.method != HttpMethods.DELETE)
        .map(serverResponse)
      Http().bindAndHandle(serverFlow, interface, port)
    }

    val proxy = new Proxy(interface, port)
    val strictProxy = new Proxy(interface, port, strictTimeout = Some(10.seconds))

    val routes = {
      // format: OFF
      (get | post) {
        path("get-path") {
          complete("get-reply")
        } ~
        path("get-path-proxied-single-strict") {
          strictProxySingleTo(Uri(s"http://$interface:$port/remote-proxy"), 10.seconds)
        } ~
        path("get-path-proxied-single") {
          proxySingleTo(Uri(s"http://$interface:$port/remote-proxy"))
        } ~
        pathPrefix("get-path-proxied-single-unmatched-strict") {
          strictProxySingleToUnmatchedPath(Uri(s"http://$interface:$port/remote-proxy"), 10.seconds)
        } ~
        pathPrefix("get-path-proxied-single-unmatched") {
          proxySingleToUnmatchedPath(Uri(s"http://$interface:$port/remote-proxy"))
        } ~
        pathPrefix("get-path-proxied-strict") {
          strictProxy.proxyTo(Uri(s"http://$interface:$port/remote-proxy"))
        } ~
        pathPrefix("get-path-proxied") {
          proxy.proxyTo(Uri(s"http://$interface:$port/remote-proxy"))
        }
      }
      // format: ON
    }

    boundFuture.map(_.localAddress.isUnresolved) must beFalse.awaitFor(10.seconds)
  }

  val localIp1 = IP(InetAddress.getByName("127.0.0.1"))
  val localIp2 = IP(InetAddress.getByName("127.0.0.2"))

  implicit val timeout = RouteTestTimeout(5.seconds)

  "An akka-http proxy support directive" should {

    "proxy single requests" in new MockServer {
      Get("/get-path") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("get-reply")
      }

      Get("/get-path-proxied-single") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }
      Post("/get-path-proxied-single") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }

      Get("/get-path-proxied-single-strict") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }
      Post("/get-path-proxied-single-strict") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }
    }

    "proxy single requests using unmatched path" in new MockServer {
      Get("/get-path") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("get-reply")
      }

      Get("/get-path-proxied-single-unmatched/other/path/parts") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy/other/path/parts")
      }
      Post("/get-path-proxied-single-unmatched/other/path/parts") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy/other/path/parts")
      }
      Get("/get-path-proxied-single-unmatched/other/path/parts?foo=bar") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy/other/path/parts?foo=bar")
      }

      Get("/get-path-proxied-single-unmatched-strict/other/path/parts") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy/other/path/parts")
      }
      Post("/get-path-proxied-single-unmatched-strict/other/path/parts") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy/other/path/parts")
      }
      Get("/get-path-proxied-single-unmatched-strict/other/path/parts?foo=bar") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy/other/path/parts?foo=bar")
      }
    }

    "proxy requests using a dedicated Proxy object" in new MockServer {
      Get("/get-path") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("get-reply")
      }

      Get("/get-path-proxied") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }
      Post("/get-path-proxied") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }

      Get("/get-path-proxied-strict") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }
      Post("/get-path-proxied-strict") ~> routes ~> check {
        status == OK
        responseAs[String] must be_==("/remote-proxy")
      }

      def parseResult(result: RouteResult): Future[String] = result match {
        case Complete(res) if res.status.intValue() == 200 =>
          res.entity.toStrict(10.seconds).map { r => r.data.utf8String }
        case Complete(res) => Future.successful(res.status.intValue().toString)
        case _ => Future.failed(new Exception("Failed to parse result"))
      }

      proxy.sendRequest(Get("/proxied"), failOnDrop = false)
        .flatMap(parseResult) must be_==("/proxied").awaitFor(10.seconds)

      val badProxy = new Proxy(interface, port, reqQueueSize = 1)
      // Fill the bad proxy
      (0 to 10).foreach(x => badProxy.sendRequest(Delete("/proxied-" + x), failOnDrop = false))

      badProxy.sendRequest(Get("/proxied"), failOnDrop = true)
        .failed.map(_.getMessage) must be_==("Dropping request (Queue is full)").awaitFor(10.seconds)

      badProxy.sendRequest(Get("/proxied"), failOnDrop = false)
        .flatMap(parseResult) must be_==("503").awaitFor(10.seconds)
    }

    "do not send unwanted headers" in new MockServer {
      override def serverResponse(req: HttpRequest) = HttpResponse(entity = req.headers.mkString("\n"))
      Get("/get-path-proxied").withHeaders(Host("expecteddomain.com"), `Remote-Address`(localIp1), `Raw-Request-URI`("somedomain.com")) ~> routes ~> check {
        responseAs[String] must not(contain("Remote-Address"))
        responseAs[String] must not(contain("Raw-Request-URI"))
        responseAs[String] must contain("Host: expecteddomain.com")
      }
    }

    "Modify the `X-Forwarded-For` header" in {
      trait CollectHeadersAndForwardedForMockServer extends MockServer {
        override def serverResponse(req: HttpRequest) = {
          val forwardedForIps = req.headers.collectFirst {
            case `X-Forwarded-For`(ips) => ips
          }.getOrElse(Seq.empty)
          HttpResponse(entity = forwardedForIps.mkString(", "), headers = req.headers)
        }
      }

      "add `X-Forwarded-For` if request has `Remote-Address`" in new CollectHeadersAndForwardedForMockServer {
        Get("/get-path-proxied").withHeaders(`Remote-Address`(localIp1)) ~> routes ~> check {
          responseAs[String] must be_==("127.0.0.1")
        }
      }

      "update existing `X-Forwarded-For`" in new CollectHeadersAndForwardedForMockServer {
        Get("/get-path-proxied").withHeaders(`Remote-Address`(localIp1), `X-Forwarded-For`(localIp2)) ~> routes ~> check {
          status == OK
          responseAs[String] must be_==("127.0.0.2, 127.0.0.1")
        }

        Get("/get-path-proxied").withHeaders(`X-Forwarded-For`(localIp2)) ~> routes ~> check {
          status == OK
          responseAs[String] must be_==("127.0.0.2")
        }
      }

      "do not add `X-Forwarded-For` if no `Remote-Address`" in new CollectHeadersAndForwardedForMockServer {
        Get("/get-path-proxied") ~> routes ~> check {
          status == OK
          responseAs[String] must beEmpty
        }
      }
    }
  }
}
