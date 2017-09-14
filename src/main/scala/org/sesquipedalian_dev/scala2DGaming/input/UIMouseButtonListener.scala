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
package org.sesquipedalian_dev.scala2DGaming.input

import org.lwjgl.glfw.GLFW._
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.graphics.UIButtonsRenderer
import org.sesquipedalian_dev.scala2DGaming.{Main, TimeOfDay}


trait UIButtonMouseListener extends MouseInputHandler {
  def textureFile: String
  def location: Location
  def handleAction(windowHandle: Long, button: Int, action: Int): Boolean = {
    if(action == GLFW_RELEASE && button == GLFW_MOUSE_BUTTON_LEFT) {
      val screenLeft = location.x * Main.SCREEN_WIDTH / Main.UI_WIDTH
      val screenRight = screenLeft + UIButtonsRenderer.singleton.map(_.textureSize).getOrElse(0).toFloat / Main.UI_WIDTH * Main.SCREEN_WIDTH
      val aspectRatio = Main.SCREEN_WIDTH.toFloat / Main.SCREEN_HEIGHT
      val aspectRatioUiHeight = Main.UI_HEIGHT / aspectRatio
      val screenTop = location.y * Main.SCREEN_HEIGHT / aspectRatioUiHeight
      val screenBottom = screenTop + UIButtonsRenderer.singleton.map(_.textureSize).getOrElse(0).toFloat / aspectRatio / aspectRatioUiHeight * Main.SCREEN_HEIGHT

      //      println(s"checking click in button bounds $location $currentX / $currentY $screenLeft $screenRight $screenTop $screenBottom")
      if(
        (screenLeft <= currentX) &&
          (currentX <= screenRight) &&
          (screenTop <= currentY) &&
          (currentY <= screenBottom)
      ) {
        buttonClicked
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  val timeToSet: Double
  def buttonClicked(): Unit = {
    //    println(s"button clicked $textureFile")
    TimeOfDay.instance.foreach(_.speed = timeToSet)
  }
}