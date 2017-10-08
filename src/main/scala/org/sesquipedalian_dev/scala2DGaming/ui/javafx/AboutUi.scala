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
import javafx.fxml.FXML
import javafx.scene.control.{Button, ScrollPane, TextField, TitledPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{ClipboardContent, DragEvent, MouseEvent, TransferMode}
import javafx.scene.layout.FlowPane
import javafx.scene.text.Text

import org.sesquipedalian_dev.scala2DGaming.entities.{ Location}
import org.sesquipedalian_dev.scala2DGaming.graphics.{ HasSingleUiSpriteRendering}
import org.sesquipedalian_dev.scala2DGaming.input.UIButtonMouseListener
import org.sesquipedalian_dev.scala2DGaming.game.{HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.util.Logging

class AboutUiController extends HasGameUpdate with Logging {
  // TODO multiple textures
  //  var texture: Option[Image] = Some(new Image("/textures/entities/MilitaryMan.bmp", 40.0, 40.0, false, true))

  var textureImages: Map[String, Image] = Map()
  var cachedAbout: List[String] = Nil
  var cachedActiveAbouter: Option[String] = None

  @FXML
  var logText: Text = null
  @FXML
  var logLayoutText: TextField = null
  @FXML
  var logLayoutButton: Button = null

  var logsUiController: Option[LogsUiController] = None

  override def update(deltaTimeSeconds: Double): Unit = {
    trace"AboutUiController update has $logsUiController $logText"
    if(logText != null && logsUiController.isEmpty) {
      logsUiController = Some(new LogsUiController(logText, logLayoutText, logLayoutButton))
    }
  }

  def redraw(): Unit = {
    Platform.runLater(new Runnable() {
      def run() {
        trace"updating build UI $cachedAbout"
      }
    })
  }
}

class AboutUi extends SeparateThreadJavaFxWindow(
  400, 400, "/fxml/about.fxml", "About:", AboutUi
)

object AboutUi extends SeparateThreadJavaFxWindowCompanionObject {
  override def newInstance = new AboutUi
}

class ToggleAboutUiButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/ui/buttons/about_ui.bmp"
  override def location = Location(2410, 80)

  override def buttonClicked(): Unit = {
    AboutUi.singleton match {
      case Some(s) => AboutUi.close()
      case _ => AboutUi.start()
    }
  }
}



