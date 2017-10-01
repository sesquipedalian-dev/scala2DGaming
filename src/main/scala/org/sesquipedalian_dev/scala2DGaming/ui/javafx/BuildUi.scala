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

import org.sesquipedalian_dev.scala2DGaming.entities.{CanBuild, Location}
import org.sesquipedalian_dev.scala2DGaming.graphics.{BuildOverlay, HasSingleUiSpriteRendering}
import org.sesquipedalian_dev.scala2DGaming.input.UIButtonMouseListener
import org.sesquipedalian_dev.scala2DGaming.game.{HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.util.Logging

class BuildUiController extends HasGameUpdate with Logging {
  // TODO multiple textures
//  var texture: Option[Image] = Some(new Image("/textures/entities/MilitaryMan.bmp", 40.0, 40.0, false, true))

  var textureImages: Map[String, Image] = Map()
  var cachedBuild: List[String] = Nil
  var cachedActiveBuilder: Option[String] = None

  @FXML
  var contentPane: FlowPane = null

  @FXML
  var scrollPane: ScrollPane = null


  override def update(deltaTimeSeconds: Double): Unit = {
    trace"BuildUiController update has"
    val newBuild = CanBuild.all.map(_.name)
    val newSelectedBuild = BuildOverlay.singleton.flatMap(_.currentBuilder).map(_.name)

    if((newBuild != cachedBuild) || (newSelectedBuild != cachedActiveBuilder)) {
      cachedActiveBuilder = newSelectedBuild
      cachedBuild = newBuild
      redraw()
    }
  }

  def redraw(): Unit = {
    Platform.runLater(new Runnable() {
      def run() {
        trace"updating build UI $cachedBuild"

        contentPane.getChildren().clear()

        cachedBuild.foreach(builderName => {
          val _builder = CanBuild.all.find(b => b.name == builderName)
          _builder.foreach(builder => {
            if(!textureImages.contains(builderName)) {
              textureImages = textureImages + (builderName -> (new Image(builder.textureFile, 40.0, 40.0, false, true)))
            }

            val image = textureImages(builderName)

            val itemPane = new FlowPane()

            itemPane.setPrefHeight(50)
            itemPane.setPrefWidth(50)

            val tex = new ImageView(image)
            itemPane.getChildren().add(tex)

            val nameText = new Text(builderName)
            itemPane.getChildren().add(nameText)

            if(Some(builderName) == cachedActiveBuilder) {
              itemPane.setStyle(
                s"""
                   |-fx-border-width: 3;
                   |-fx-border-color: black;
                 """.stripMargin)
            }

            itemPane.setOnMouseClicked(new EventHandler[MouseEvent](){
              override def handle(event: MouseEvent) = {
                if(Some(builderName) == cachedActiveBuilder) {
                  BuildOverlay.disable()
                } else {
                  BuildOverlay.enable(builder)
                }
              }
            })

            contentPane.getChildren().add(itemPane)
          })
        })
      }
    })
  }
}

class BuildUi extends SeparateThreadJavaFxWindow(
  400, 400, "/fxml/build.fxml", "Build:", BuildUi
)

object BuildUi extends SeparateThreadJavaFxWindowCompanionObject {
  override def newInstance = new BuildUi
}

class ToggleBuildUiButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/ui/buttons/build_ui.bmp"
  override def location = Location(2340, 80)

  override def buttonClicked(): Unit = {
    BuildUi.singleton match {
      case Some(s) => BuildUi.close()
      case _ => BuildUi.start()
    }
  }
}



