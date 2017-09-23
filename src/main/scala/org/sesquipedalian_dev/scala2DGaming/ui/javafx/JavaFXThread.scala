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

import javafx.application.{Application, Platform}
import javafx.stage.Stage


class JavaFXManager extends Application with Runnable {
  override def start(primaryStage: Stage) = {
    JavaFXManager.primaryStage = Some(primaryStage)
  }

  def run = {
    Application.launch()
  }
}

object JavaFXManager {
  // not really used by wtf not
  var primaryStage: Option[Stage] = None

  def myInit(): Unit = {
    // the wumpus doesn't leave when the last stage is hidden.
    Platform.setImplicitExit(false)

    val groupsUi = new JavaFXManager
    val uiThread = new Thread(groupsUi)
    uiThread.start()
  }

  def myCleanup(): Unit = {
    Platform.exit()
  }
}
