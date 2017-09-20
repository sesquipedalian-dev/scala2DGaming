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

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  lazy val logging: Logger = LoggerFactory.getLogger(getClass())

  implicit class LoggingHelper(val sc: StringContext) {
    def trace(args: Any*): Unit = if (logging.isTraceEnabled()) {
      logging.trace(sc.parts.mkString("{}"), args.map(_.asInstanceOf[AnyRef]):_*)
    }
    def debug(args: Any*): Unit = if (logging.isDebugEnabled()) {
      logging.debug(sc.parts.mkString("{}"), args.map(_.asInstanceOf[AnyRef]):_*)
    }
    def info(args: Any*): Unit = if (logging.isInfoEnabled()) {
      logging.info(sc.parts.mkString("{}"), args.map(_.asInstanceOf[AnyRef]):_*)
    }
    def warn(args: Any*): Unit = if (logging.isWarnEnabled()) {
      logging.warn(sc.parts.mkString("{}"), args.map(_.asInstanceOf[AnyRef]):_*)
    }
    def error(args: Any*): Unit = if (logging.isErrorEnabled()) {
      logging.error(sc.parts.mkString("{}"), args.map(_.asInstanceOf[AnyRef]):_*)
    }
  }
}
