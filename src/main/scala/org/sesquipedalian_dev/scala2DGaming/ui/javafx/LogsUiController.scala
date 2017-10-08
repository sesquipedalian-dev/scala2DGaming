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
package org.sesquipedalian_dev.scala2DGaming.ui.javafx

import javafx.application.Platform
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control.{Button, TextField}
import javafx.scene.text.Text

import ch.qos.logback.classic.{Level, PatternLayout}
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.{Appender, AppenderBase, Context}
import ch.qos.logback.core.status.Status
import org.sesquipedalian_dev.scala2DGaming.game.{Commander, HasGameUpdate}
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistrySingleton
import org.slf4j.LoggerFactory

class LogsUiController(
  logText: Text,
  layoutText: TextField,
  layoutUpdate: Button,
  filterText: TextField,
  filterButton: Button
) extends HasGameUpdate with Logging {
  final val maxLogFileEntries = 1000
  override def update(deltaTimeSeconds: Double): Unit = {
    UIAppender.singleton.foreach(uia => {
      if(uia.dirty) {
        Platform.runLater(new Runnable {
          def run(): Unit = {
            logText.setText(uia.makeString())
          }
        })
      }
    })
  }

  layoutUpdate.setOnAction(new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent) = {
      val newPattern = layoutText.getText()
      trace"updating log file pattern $newPattern"
      UIAppender.singleton.foreach(uia => {
        uia.updateLayout(newPattern)
        uia.dirty = true
      })
    }
  })
  UIAppender.singleton.foreach(uia => {
    uia.updateLayout(layoutText.getText())
    logText.setText(uia.makeString())
  })

  var lastDetailName: Option[String] = None
  filterButton.setOnAction(new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent) = {
      val filter = filterText.getText()
      trace"adding filter appender $filter"
      lastDetailName.foreach(ldn => {
        // reset previous to default logging level of info
        val newLogger = LoggerFactory.getLogger(ldn).asInstanceOf[ch.qos.logback.classic.Logger]
        newLogger.setLevel(Level.INFO)
      })
      if(filter.nonEmpty) {
        // set the desired filter to trace level
        val newLogger = LoggerFactory.getLogger(filter).asInstanceOf[ch.qos.logback.classic.Logger]
        newLogger.setLevel(Level.TRACE)
        lastDetailName = Some(filter)
      }
    }
  })
}


class UIAppender() extends AppenderBase[ILoggingEvent] {
  var layout: Option[PatternLayout] = None
  final val maxLogFileEntries = 1000

  var logFileEntries: List[ILoggingEvent] = Nil
  var dirty: Boolean = false
  override def append(eventObject: ILoggingEvent) = {
    logFileEntries = (eventObject +: logFileEntries).take(maxLogFileEntries)
    dirty = true
  }

  def makeString(): String = {
    val r = layout.map(l => {
      val s = logFileEntries.reverse.map(i => l.doLayout(i)).mkString("\n")
      dirty = false
      s
    }).getOrElse("")
    r
  }

  def updateLayout(pattern: String): Unit = {
    val newL = new PatternLayout()
    newL.setPattern(pattern)
    newL.setContext(context)
    newL.start()
    layout = Some(newL)
  }

  UIAppender.register(this)
}

object UIAppender extends HasRegistrySingleton {
  override type ThisType = UIAppender
}