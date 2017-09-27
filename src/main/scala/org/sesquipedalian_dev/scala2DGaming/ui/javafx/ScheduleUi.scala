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
import javafx.geometry.Insets
import javafx.scene.control.{Button, Control}
import javafx.scene.layout.{AnchorPane, HBox, Region, VBox}
import javafx.scene.text.{Font, Text}

import org.sesquipedalian_dev.scala2DGaming.entities.soldiers.{Activities, GoodGuyGroups}
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.graphics.HasSingleUiSpriteRendering
import org.sesquipedalian_dev.scala2DGaming.input.UIButtonMouseListener
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.scala2DGaming.game.{HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.util.Logging

class ScheduleUiController extends HasGameUpdate with Logging {
  final val unitWidth = 100
  final val unitHeight = 35
  var cachedSchedule: Map[String, List[Activities.Type]] = Map()

  @FXML
  var content: HBox = null

  @FXML
  var activitiesBox: VBox = null

  var lastClickedActivity: Option[String] = None
  @FXML
  def initialize(): Unit = {
    trace"initializing ScheduleUiController"
    Activities().foreach(activity => {
      val button = new Button(activity)
      button.setStyle(
        s"""
           |-fx-border-color: black;
           |-fx-border-width: 3;
           |-fx-background-color: ${Activities(activity)}
              """.stripMargin)
      button.forceWidth(unitWidth)
      button.forceHeight(unitHeight)

      button.setFont(font(12))

      button.setOnAction(new EventHandler[ActionEvent]() {
        override def handle(event: ActionEvent) = {
          trace"setting lastClickedActivity to $activity"
          lastClickedActivity = Some(activity)
        }
      })

      VBox.setMargin(button, new Insets(5, 0, 5, 0))
      trace"checking activites box $activitiesBox ${Option(activitiesBox).map(_.getChildren)}"
      activitiesBox.getChildren().add(button)
    })
  }

  override def update(deltaTimeSeconds: Double): Unit = {
    val newSchedule = GoodGuyGroups.groups.map(p => (p._1 -> p._2.schedule.activities))
    if(newSchedule != cachedSchedule) {
      cachedSchedule = newSchedule
      redraw()
    }
  }

  var loadedFont: Map[Int, Font] = Map()
  def font(size: Int): Font = {
    if(!loadedFont.contains(size)) {
      val resourceStream = getClass.getResourceAsStream("/fonts/Consolas.ttf")
      loadedFont += (size -> Font.loadFont(resourceStream, size))
    }
    loadedFont(size)
  }

  def borderStyle(color: String): String =
    s"""
      |-fx-border-color: $color;
      |-fx-border-width: 3
    """.stripMargin

  def scheduleButtonClick(groupName: String, index: Int, currentActivity: String): Unit = {
    trace"schedule update button click $groupName $index $currentActivity $lastClickedActivity"
    lastClickedActivity.foreach(newActivity => {
      GoodGuyGroups.groups.get(groupName).foreach(ggg => {
        ggg.schedule.activities = (ggg.schedule.activities.take(index) :+ newActivity) ++
          ggg.schedule.activities.drop(index + 1)
      })
    })
  }

  var currentChildren: List[VBox] = Nil
  def redraw(): Unit = {
    Platform.runLater(new Runnable() {
      def run() {
        trace"updating schedule UI $cachedSchedule"

        // remove current content
        currentChildren.foreach(c => content.getChildren.remove(c))

        // build it back up
        GoodGuyGroups.groups.foreach(p => {
          val (groupName, ggg) = p
          val schedule = ggg.schedule

          // make a header with the name of the group
          val groupNameText = new Text(groupName)
          groupNameText.setFont(font(24))

          val groupNameHolder = new AnchorPane(groupNameText)
          groupNameHolder.forceWidth(unitWidth)
          groupNameHolder.forceHeight(unitHeight)
          groupNameHolder.setStyle(borderStyle("black"))

          AnchorPane.setTopAnchor(groupNameText, 0.0)
          AnchorPane.setLeftAnchor(groupNameText, 0.0)

          // make some buttons for the each of the activities
          val buttonsToAdd = schedule.activities.zipWithIndex.map(p => {
            val (activityName, index) = p

            val button = new Button(activityName)
            button.setStyle(
              s"""
                |-fx-border-color: black;
                |-fx-border-width: 3;
                |-fx-background-color: ${Activities(activityName)}
              """.stripMargin)
            button.forceWidth(unitWidth)
            button.forceHeight(unitHeight)

            button.setFont(font(12))

            button.setOnAction(new EventHandler[ActionEvent]() {
              override def handle(event: ActionEvent) = {
                scheduleButtonClick(groupName, index, activityName)
              }
            })

            button
          })

          // make the group container
          val bundle = new VBox((groupNameHolder :: buttonsToAdd):_*)
          bundle.forceComputedHeight()
          bundle.forceComputedWidth()
          bundle.setStyle(borderStyle("gray"))

          HBox.setMargin(bundle, new Insets(0, 0, 0, 10))

          // add the group to the content
          currentChildren = currentChildren :+ bundle
          content.getChildren.add(bundle)
        })
      }
    })
  }
}

class ScheduleUi extends SeparateThreadJavaFxWindow(
  400, 400, "/fxml/schedule.fxml", "Schedules:", ScheduleUi
)

object ScheduleUi extends SeparateThreadJavaFxWindowCompanionObject {
  override def newInstance = new ScheduleUi
}


class ToggleScheduleUiButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/ui/buttons/schedule.bmp"
  override def location = Location(2270, 80)
  val timeToSet: Double = TimeOfDay.PAUSE

  var previousSpeed: Option[Double] = None
  override def buttonClicked(): Unit = {
    ScheduleUi.singleton match {
      case Some(s) => ScheduleUi.close()
      case _ => ScheduleUi.start()
    }
  }
}