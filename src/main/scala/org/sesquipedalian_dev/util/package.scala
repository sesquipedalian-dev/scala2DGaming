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
package org.sesquipedalian_dev

import org.sesquipedalian_dev.util.registry.ObjectRegistry

import scala.util.Try

package object util extends JavaFXExtensions {
  implicit def cleanly[A <% AutoCloseable](rsc: => A)(doWork: (A) => Unit): Try[Unit] = {
    TryWithResource[A, Unit](rsc)(_.close())(doWork)
  }

  val Registry = new ObjectRegistry
}