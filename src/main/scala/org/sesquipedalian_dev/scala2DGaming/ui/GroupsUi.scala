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

import javafx.application.{Application, Platform}
import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.control.{Button, ScrollPane, TextField, TitledPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{ClipboardContent, DragEvent, MouseEvent, TransferMode}
import javafx.scene.layout.{AnchorPane, Border, FlowPane}
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text}
import javafx.stage.Stage

import org.sesquipedalian_dev.scala2DGaming.HasGameUpdate
import org.sesquipedalian_dev.scala2DGaming.entities.GoodGuyGroups

import collection.JavaConverters._

class GroupsUiController extends HasGameUpdate {
  var texture: Option[Image] = Some(new Image("/textures/MilitaryMan.bmp", 40.0, 40.0, false, true))

  var cachedGroups: Map[String, List[String]] = Map()

  @FXML
  var groupInput: TextField = null

  @FXML
  var scrollPane: ScrollPane = null

  def onAddGroup(e: ActionEvent): Unit = {
    println(s"GroupsUiController add button clicked $e")
    GoodGuyGroups.add(groupInput.getText)
  }

  override def update(deltaTimeSeconds: Double): Unit = {
//    println(s"GroupsUiController update has scrollPane? $scrollPane")
    val newGroups = GoodGuyGroups.groups.map(p => (p._1 -> p._2.map(_.name)))
    if(newGroups != cachedGroups) {
      cachedGroups = newGroups
      redraw()
    }
  }

  def redraw(): Unit = {
    Platform.runLater(new Runnable() {
      def run() {
        println(s"updating groups UI $cachedGroups")

        // clear the current content of the group UI
        val contentPane = scrollPane.getContent.asInstanceOf[FlowPane]
        contentPane.getChildren.clear()

        cachedGroups.foreach(group => {
          val (groupName, memberNames) = group

          // create a flow pane to hold the men inside
          val namesPane = new FlowPane()
          namesPane.setHgap(5)

          // create UI for each man in the unit
          memberNames.foreach(n => {
            val guyPane = new FlowPane()
            guyPane.setPrefHeight(50)
            guyPane.setPrefWidth(50)
            guyPane.setOnDragDetected(new EventHandler[MouseEvent] {
              override def handle(event: MouseEvent) = {
                println(s"on drag start $n")
                // create the 'dragboard' - analogous to clipbaord I guess
                val db = guyPane.startDragAndDrop(TransferMode.LINK)

                val content = new ClipboardContent()
                content.putString(n)
                db.setContent(content)

                event.consume()
              }
            })

            texture.foreach(t => {
              val tex = new ImageView(t)
              guyPane.getChildren().add(tex)
            })

            val nameText = new Text(n)
            guyPane.getChildren().add(nameText)

            namesPane.getChildren.add(guyPane)
          })

          // create a button to delete the group
          val deleteMeButton = new Button("Delete Group")
          deleteMeButton.setOnAction(new EventHandler[ActionEvent] {
            override def handle(event: ActionEvent) = GoodGuyGroups.tryRemove(groupName)
          })
          namesPane.getChildren().add(deleteMeButton)

          // create title pane using the name of the group and the flow pane as the content
          val groupPane = new TitledPane(groupName, namesPane)

          // allow group pane to receive drag events
          groupPane.setOnDragOver(new EventHandler[DragEvent] {
            override def handle(event: DragEvent) = {
//              println(s"drag over $groupName")
              if(event.getDragboard.hasString) {
                event.acceptTransferModes(TransferMode.LINK)
              }

              event.consume()
            }
          })

          // provide some interactivity help for drag
          groupPane.setOnDragEntered(new EventHandler[DragEvent] {
            override def handle(event: DragEvent) = {
//              println(s"drag enter $groupName")
              if(event.getDragboard.hasString) {
                groupPane.setOpacity(.5)
              }
            }
          })
          groupPane.setOnDragExited(new EventHandler[DragEvent] {
            override def handle(event: DragEvent) = {
//              println(s"drag leave $groupName")
              if(event.getDragboard.hasString) {
                groupPane.setOpacity(1.0)
              }
            }
          })

          groupPane.setOnDragDropped(new EventHandler[DragEvent] {
            override def handle(event: DragEvent) = {
              val dragboard = event.getDragboard
//              println(s"drag handle entry $groupName")
              if(dragboard.hasString) {
                val targetName = dragboard.getString
                GoodGuyGroups.moveToGroup(targetName, groupName)
              }
//              println(s"drag handle exit $groupName")
            }
          })

          contentPane.getChildren.add(groupPane)
        })
      }
    })
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
