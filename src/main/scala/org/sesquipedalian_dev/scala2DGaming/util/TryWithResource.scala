/**
  * Copyright 2017 sesquipedalian.dev@gmail.com
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
package org.sesquipedalian_dev.scala2DGaming.util

import scala.util.{Failure, Success, Try}

// source: https://www.phdata.io/try-with-resources-in-scala/
object TryWithResource {
  def apply[A, B](resource: => A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
    var r: Option[A] = None
    try {
      r = Some(resource)
      r.map(rsc => Success(doWork(rsc))).getOrElse(Failure(new Exception("probably making rsc")))
    } catch {
      case e: Exception => Failure(e)
    }
    finally {
      try {
        r.foreach(cleanup)
      } catch {
        case e: Exception => println(e) // should be logged
      }
    }
  }
}
