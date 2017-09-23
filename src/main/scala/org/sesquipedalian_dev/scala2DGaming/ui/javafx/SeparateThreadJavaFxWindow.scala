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
import javafx.event.EventHandler
import javafx.stage.{Stage, WindowEvent}

trait SeparateThreadJavaFxWindowCompanionObject {
  def newInstance: SeparateThreadJavaFxWindow

  var primaryStage: Option[Stage] = None

  var singleton: Option[SeparateThreadJavaFxWindow] = None
  def start(): Unit = {
    if(singleton.isEmpty) {
      // fork off JavaFX UI thread
      val instance = newInstance
      Platform.runLater(instance)
      singleton = Some(instance)
    }
  }

  def close(): Unit = {
    if(singleton.nonEmpty) {
      primaryStage.foreach(ps => Platform.runLater(new Runnable {
        override def run() = ps.close()
      }))

      singleton = None
      primaryStage = None
    }
  }
}

abstract class SeparateThreadJavaFxWindow(
  width: Double,
  height: Double,
  fxmlPath: String,
  windowTitle: String,
  companion: SeparateThreadJavaFxWindowCompanionObject
) extends Runnable {
  override def run(): Unit = {

    val fxmlFile = getClass.getResource(fxmlPath)
    val loader = new javafx.fxml.FXMLLoader()
    val root: javafx.scene.Parent = loader.load(fxmlFile.openStream())
    val theScene = new javafx.scene.Scene(root, width, height)

    val theStage = new Stage()
    companion.primaryStage = Some(theStage)
    theStage.setTitle(windowTitle)

    // also intercept alt-f4 and 'x' button on app
    theStage.setOnCloseRequest(new EventHandler[WindowEvent]() {
      override def handle(event: WindowEvent): Unit = {
        companion.close()
        event.consume()
      }
    })

    theStage.setScene(theScene)
    theStage.show()
  }
}