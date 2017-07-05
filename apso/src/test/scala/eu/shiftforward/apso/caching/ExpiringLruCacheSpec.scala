/*
 * Copyright © 2011-2015 the spray project <http://spray.io>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.shiftforward.apso.caching

import java.util.Random
import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.duration._

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

class ExpiringLruCacheSpec(implicit env: ExecutionEnv) extends Specification {
  implicit val system = ActorSystem()
  import system.dispatcher

  "An LruCache" should {
    "be initially empty" in {
      lruCache().store.toString === "{}"
      lruCache().size === 0
      lruCache().keys === Set()
    }
    "store uncached values" in {
      val cache = lruCache[String]()
      cache(1)("A") must beEqualTo("A").await
      cache.store.toString === "{1=A}"
      cache.size === 1
      cache.keys === Set(1)
    }
    "return stored values upon cache hit on existing values" in {
      val cache = lruCache[String]()
      cache(1)("A") must beEqualTo("A").await
      cache(1)(failure("Cached expression was evaluated despite a cache hit").asInstanceOf[String]) must beEqualTo("A").await
      cache.store.toString === "{1=A}"
      cache.size === 1
    }
    "return Futures on uncached values during evaluation and replace these with the value afterwards" in {
      val cache = lruCache[String]()
      val latch = new CountDownLatch(1)
      val future1 = cache(1) { (promise: Promise[String]) ⇒
        Future {
          latch.await()
          promise.success("A")
        }
      }
      val future2 = cache(1)("")
      cache.store.toString === "{1=pending}"
      latch.countDown()
      future1 must beEqualTo("A").await
      future2 must beEqualTo("A").await
      cache.store.toString === "{1=A}"
      cache.size === 1
    }
    "properly limit capacity" in {
      val cache = lruCache[String](maxCapacity = 3)
      cache(1)("A") must beEqualTo("A").await
      cache(2)(Future.successful("B")) must beEqualTo("B").await
      cache(3)("C") must beEqualTo("C").await
      cache.store.toString === "{1=A, 2=B, 3=C}"
      cache(4)("D")
      Thread.sleep(10)
      cache.store.toString === "{2=B, 3=C, 4=D}"
      cache.size === 3
      cache.keys === Set(2, 3, 4)
    }
    "expire old entries" in {
      val cache = lruCache[String](timeToLive = 75 millis span)
      cache(1)("A") must beEqualTo("A").await
      cache(2)("B") must beEqualTo("B").await
      Thread.sleep(50)
      cache(3)("C") must beEqualTo("C").await
      cache.size === 3
      Thread.sleep(50)
      cache.get(2) must beNone // removed on request
      cache.size === 2 // expired entry 1 still there
      cache.get(1) must beNone // but not retrievable anymore
    }
    "not cache exceptions" in {
      val cache = lruCache[String]()
      cache(1)((throw new RuntimeException("Naa")): String) must throwA[RuntimeException]("Naa").await
      cache(1)("A") must beEqualTo("A").await
    }
    "refresh an entries expiration time on cache hit" in {
      val cache = lruCache[String]()
      cache(1)("A") must beEqualTo("A").await
      cache(2)("B") must beEqualTo("B").await
      cache(3)("C") must beEqualTo("C").await
      cache(1)("") must beEqualTo("A").await // refresh
      cache.store.toString === "{1=A, 2=B, 3=C}"
    }
    "be thread-safe" in {
      val cache = lruCache[Int](maxCapacity = 1000)
      // exercise the cache from 10 parallel "tracks" (threads)
      val views = Await.result({
        Future.traverse(Seq.tabulate(10)(identity)) { track ⇒
          Future {
            val array = Array.fill(1000)(0) // our view of the cache
            val rand = new Random(track)
            (1 to 10000) foreach { i ⇒
              val ix = rand.nextInt(1000) // for a random index into the cache
              val value = Await.result(cache(ix) { // get (and maybe set) the cache value
                Thread.sleep(0)
                rand.nextInt(1000000) + 1
              }, 3.seconds)
              if (array(ix) == 0) array(ix) = value // update our view of the cache
              else if (array(ix) != value) failure("Cache view is inconsistent (track " + track + ", iteration " + i +
                ", index " + ix + ": expected " + array(ix) + " but is " + value)
            }
            array
          }
        }
      }, 3.seconds)
      val beConsistent: Matcher[Seq[Int]] = (
        (ints: Seq[Int]) ⇒ ints.filter(_ != 0).reduceLeft((a, b) ⇒ if (a == b) a else 0) != 0,
        (_: Seq[Int]) ⇒ "consistency check")
      forall(views.transpose)(_ must beConsistent)
    }
  }

  step(system.terminate())

  def lruCache[T](maxCapacity: Int = 500, initialCapacity: Int = 16,
    timeToLive: Duration = Duration.Inf, timeToIdle: Duration = Duration.Inf) =
    new ExpiringLruCache[T](maxCapacity, initialCapacity, timeToLive, timeToIdle)

}