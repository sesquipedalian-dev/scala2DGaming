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

import org.joml.Vector3f
import org.lwjgl.glfw.GLFW._
import org.sesquipedalian_dev.scala2DGaming.{Main, TimeOfDay}
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.graphics.{UIButtonsRenderer, WorldSpritesRenderer}


trait WorldButtonMouseListener extends MouseInputHandler {
  def textureFile: String
  def location: Location

  var hovered: Boolean = false
  def hoverEnter()
  def hoverLeave()

  override def handleMove(windowHandle: Long, xPos: Double, yPos: Double, lbState: Int, rbState: Int): Boolean = {
    super.handleMove(windowHandle, xPos, yPos, lbState, rbState)

    val lastProjectionMatrix = WorldSpritesRenderer.singleton.flatMap(_.camera).flatMap(_.lastProjectionMatrix)

    lastProjectionMatrix.map(lpm => {
      val (_, _, matrix) = lpm
      val topLeft: Vector3f = new Vector3f()
      matrix.project(
        new Vector3f(
          Math.floor(location.x).toFloat * Main.TEXTURE_SIZE,
          Math.floor(location.y).toFloat * Main.TEXTURE_SIZE,
          0.0f
        ),
        Array[Int](-1, -1, 2, 2),
        topLeft
      )

      val bottomRight: Vector3f = new Vector3f()
      matrix.project(
        new Vector3f(
          (Math.floor(location.x).toFloat + 1) * Main.TEXTURE_SIZE,
          (Math.floor(location.y).toFloat + 1) * Main.TEXTURE_SIZE,
          0.0f
        ),
        Array[Int](-1, -1, 2, 2),
        bottomRight
      )
      val screenLeft = topLeft.x
      val screenRight = bottomRight.x
      val screenTop = bottomRight.y // y axis inverted
      val screenBottom = topLeft.y

      val mouseProjectedX = (currentX / Main.SCREEN_WIDTH * 2) - 1
      val mouseProjectedY = -((currentY / Main.SCREEN_HEIGHT * 2) - 1)

//      println(s"mouse check for game obj $location $screenLeft $screenRight $screenTop $screenBottom $mouseProjectedX $mouseProjectedY")

      val in = (screenLeft <= mouseProjectedX) &&
        (mouseProjectedX <= screenRight) &&
        (screenTop <= mouseProjectedY) &&
        (mouseProjectedY <= screenBottom)
      if(!hovered && in) {
        hovered = true
        hoverEnter()
      } else if (hovered && !in) {
        hovered = false
        hoverLeave()
      }
    }).getOrElse(false)

    false
  }

  def handleAction(windowHandle: Long, button: Int, action: Int): Boolean = {
    if(
      (action == GLFW_RELEASE) &&
        (button == GLFW_MOUSE_BUTTON_LEFT) &&
        hovered
    ) {
      clicked
      true
    } else {
      false
    }
  }

  def clicked(): Unit
}