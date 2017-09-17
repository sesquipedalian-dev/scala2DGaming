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
package org.sesquipedalian_dev.scala2DGaming.ui

import javafx.application.Application
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ScrollPane
import javafx.stage.Stage

import org.sesquipedalian_dev.scala2DGaming.HasGameUpdate

import collection.JavaConverters._

class GroupsUiController extends HasGameUpdate {
  @FXML
  var scrollPane: ScrollPane = null

  def onAddGroup(e: ActionEvent): Unit = {
//    println(s"GroupsUiController add button clicked $e")
  }

  override def update(deltaTimeSeconds: Double): Unit = {
//    println(s"GroupsUiController update has scrollPane? $scrollPane")


  }


}

class GroupsUi extends Application with Runnable {
  val WINDOW_WIDTH = 400
  val WINDOW_HEIGHT = 400
  var primaryStage: Option[Stage] = None

  override def start(primaryStage: Stage): Unit = {
    val fxmlFile = getClass.getResource("/fxml/groups.fxml")
    val loader = new javafx.fxml.FXMLLoader()
    val root: javafx.scene.Parent = loader.load(fxmlFile.openStream())
    val theScene = new javafx.scene.Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT)

    this.primaryStage = Some(primaryStage)
    primaryStage.setTitle("Groups:")

    primaryStage.setScene(theScene)
    primaryStage.show()
  }

  override def run() = {
    Application.launch()
  }

  def requestStop() = {
    primaryStage.foreach(_.close())
  }
}
