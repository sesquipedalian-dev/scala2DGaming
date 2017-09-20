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

import org.joml.{Matrix4f, Vector3f}
import org.lwjgl.glfw.GLFW._
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.graphics.WorldSpritesRenderer
import org.sesquipedalian_dev.scala2DGaming.util.Logging


trait WorldMouseListener extends MouseInputHandler with Logging {
  def textureFile: String
  def location: Location

  // TODO hover state needs to be invalidated when camera's projection matrix changes
  var hovered: Boolean = false
  def hoverEnter()
  def hoverLeave()
  var lastProjectionMatrix: Option[Matrix4f] = None

  def toScreen(worldLoc: Location, offset: Location = Location(0, 0)): Option[Location] = {
    val lastProjectionMatrix = WorldSpritesRenderer.singleton.flatMap(_.camera).flatMap(_.lastProjectionMatrix)
    if(this.lastProjectionMatrix != lastProjectionMatrix.map(_._3)) {
      hovered = false
    }
    this.lastProjectionMatrix = lastProjectionMatrix.map(_._3)

    lastProjectionMatrix.map(lpm => {
      val (_, _, matrix) = lpm
      val screenLoc: Vector3f = new Vector3f()
      matrix.project(
        new Vector3f(
          Math.floor(location.x + offset.x).toFloat * Main.TEXTURE_SIZE,
          Math.floor(location.y + offset.y).toFloat * Main.TEXTURE_SIZE,
          0.0f
        ),
        Array[Int](-1, -1, 2, 2),
        screenLoc
      )
      Location(screenLoc.x, screenLoc.y)
    })
  }

  override def handleMove(windowHandle: Long, xPos: Double, yPos: Double, lbState: Int, rbState: Int): Boolean = {
    super.handleMove(windowHandle, xPos, yPos, lbState, rbState)

    val lastProjectionMatrix = WorldSpritesRenderer.singleton.flatMap(_.camera).flatMap(_.lastProjectionMatrix)
    if(this.lastProjectionMatrix != lastProjectionMatrix.map(_._3)) {
      hovered = false
    }
    this.lastProjectionMatrix = lastProjectionMatrix.map(_._3)

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

      val topLeft2 = toScreen(location)
      val bottomRight2 = toScreen(location, Location(1, 1))
      trace"new function for points? $topLeft $topLeft2 $bottomRight $bottomRight2"

      val screenLeft = topLeft.x
      val screenRight = bottomRight.x
      val screenTop = bottomRight.y // y axis inverted
      val screenBottom = topLeft.y

      val mouseProjectedX = (currentX / Main.SCREEN_WIDTH * 2) - 1
      val mouseProjectedY = -((currentY / Main.SCREEN_HEIGHT * 2) - 1)

      trace"mouse check for game obj $location $screenLeft $screenRight $screenTop $screenBottom $mouseProjectedX $mouseProjectedY"

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