import sbt._

object Dependencies {
  object Versions {
    val Akka                    = "2.6.15"
    val AkkaHttp                = "10.2.4"
    val AkkaTestkitSpecs2       = "0.3.0"
    val Aws                     = "1.12.17"
    val BouncyCastle            = "1.69"
    val Cats                    = "2.6.1"
    val Circe                   = "0.14.1"
    val CommonsCodec            = "1.15"
    val ConcurrentLinkedHashMap = "1.4.2"
    val Elastic4s               = "7.10.0"
    val FastMd5                 = "2.7.1"
    val JodaTime                = "2.10.10"
    val JUnit                   = "4.13.2"
    val Log4jCore               = "2.14.1"
    val NscalaTime              = "2.28.0"
    val ScalaCheck              = "1.15.4"
    val ScalaLogging            = "3.9.4"
    val ScalaPool               = "0.4.3"
    val ScalaTest               = "3.2.9"
    val Shapeless               = "2.3.7"
    val SimpleJmx               = "1.19"
    val Specs2                  = "4.12.1"
    val Squants                 = "1.8.0"
    val SshJ                    = "0.31.0"
    val TypesafeConfig          = "1.4.1"
    val UnirestJava             = "1.4.9"
  }

  val AkkaActor                  = "com.typesafe.akka"        %% "akka-actor"               % Versions.Akka
  val AkkaActorTyped             = "com.typesafe.akka"        %% "akka-actor-typed"         % Versions.Akka
  val AkkaActorTestkitTyped      = "com.typesafe.akka"        %% "akka-actor-testkit-typed" % Versions.Akka
  val AkkaHttp                   = "com.typesafe.akka"        %% "akka-http"                % Versions.AkkaHttp
  val AkkaHttpCore               = "com.typesafe.akka"        %% "akka-http-core"           % Versions.AkkaHttp
  val AkkaHttpTestkit            = "com.typesafe.akka"        %% "akka-http-testkit"        % Versions.AkkaHttp
  val AkkaStream                 = "com.typesafe.akka"        %% "akka-stream"              % Versions.Akka
  val AkkaStreamTestkit          = "com.typesafe.akka"        %% "akka-stream-testkit"      % Versions.Akka
  val AkkaTestkit                = "com.typesafe.akka"        %% "akka-testkit"             % Versions.Akka
  val AkkaTestkitSpecs2          = "net.ruippeixotog"         %% "akka-testkit-specs2"      % Versions.AkkaTestkitSpecs2
  val ApacheHttpAsyncClient      = "org.apache.httpcomponents" % "httpasyncclient"          % "4.1.4"
  val ApacheHttpClient           = "org.apache.httpcomponents" % "httpclient"               % "4.5.13"
  val ApacheHttpCore             = "org.apache.httpcomponents" % "httpcore"                 % "4.4.14"
  val AwsJavaSdkCore             = "com.amazonaws"             % "aws-java-sdk-core"        % Versions.Aws
  val AwsJavaSdkS3               = "com.amazonaws"             % "aws-java-sdk-s3"          % Versions.Aws
  val BouncyCastlePkix           = "org.bouncycastle"          % "bcpkix-jdk15on"           % Versions.BouncyCastle
  val BouncyCastleProvider       = "org.bouncycastle"          % "bcprov-jdk15on"           % Versions.BouncyCastle
  val CatsCore                   = "org.typelevel"            %% "cats-core"                % Versions.Cats
  val CirceCore                  = "io.circe"                 %% "circe-core"               % Versions.Circe
  val CirceGeneric               = "io.circe"                 %% "circe-generic"            % Versions.Circe
  val CirceLiteral               = "io.circe"                 %% "circe-literal"            % Versions.Circe
  val CirceParser                = "io.circe"                 %% "circe-parser"             % Versions.Circe
  val CommonsCodec               = "commons-codec"             % "commons-codec"            % Versions.CommonsCodec
  val ConcurrentLinkedHashMapLru =
    "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % Versions.ConcurrentLinkedHashMap
  val Elastic4sClientEsJava      = "com.sksamuel.elastic4s"     %% "elastic4s-client-esjava"      % Versions.Elastic4s
  val Elastic4sCore              = "com.sksamuel.elastic4s"     %% "elastic4s-core"               % Versions.Elastic4s
  val Elastic4sTestkit           = "com.sksamuel.elastic4s"     %% "elastic4s-testkit"            % Versions.Elastic4s
  val Elasticsearch              = "org.elasticsearch"           % "elasticsearch"                % "7.10.2"
  val ElasticsearchClusterRunner = "org.codelibs"                % "elasticsearch-cluster-runner" % "7.10.2.0"
  val ElasticsearchRestClient    = "org.elasticsearch.client"    % "elasticsearch-rest-client"    % "7.10.1"
  val FastMd5                    = "com.joyent.util"             % "fast-md5"                     % Versions.FastMd5
  val JodaTime                   = "joda-time"                   % "joda-time"                    % Versions.JodaTime
  val JUnit                      = "junit"                       % "junit"                        % Versions.JUnit
  val Log4jCore                  = "org.apache.logging.log4j"    % "log4j-core"                   % Versions.Log4jCore
  val NscalaTime                 = "com.github.nscala-time"     %% "nscala-time"                  % Versions.NscalaTime
  val ScalaCheck                 = "org.scalacheck"             %% "scalacheck"                   % Versions.ScalaCheck
  val ScalaLogging               = "com.typesafe.scala-logging" %% "scala-logging"                % Versions.ScalaLogging
  val ScalaPool                  = "io.github.andrebeat"        %% "scala-pool"                   % Versions.ScalaPool
  val ScalaTestCore              = "org.scalatest"              %% "scalatest-core"               % Versions.ScalaTest
  val Shapeless                  = "com.chuusai"                %% "shapeless"                    % Versions.Shapeless
  val SimpleJmx                  = "com.j256.simplejmx"          % "simplejmx"                    % Versions.SimpleJmx
  val Specs2Common               = "org.specs2"                 %% "specs2-common"                % Versions.Specs2
  val Specs2Core                 = "org.specs2"                 %% "specs2-core"                  % Versions.Specs2
  val Specs2JUnit                = "org.specs2"                 %% "specs2-junit"                 % Versions.Specs2
  val Specs2Matcher              = "org.specs2"                 %% "specs2-matcher"               % Versions.Specs2
  val Specs2ScalaCheck           = "org.specs2"                 %% "specs2-scalacheck"            % Versions.Specs2
  val Squants                    = "org.typelevel"              %% "squants"                      % Versions.Squants
  val SshJ                       = "com.hierynomus"              % "sshj"                         % Versions.SshJ
  val TypesafeConfig             = "com.typesafe"                % "config"                       % Versions.TypesafeConfig
  val UnirestJava                = "com.mashape.unirest"         % "unirest-java"                 % Versions.UnirestJava
}
