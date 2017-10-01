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
package org.sesquipedalian_dev.util

import org.joml.{Matrix4f, Vector3f}
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.graphics.WorldSpritesRenderer

trait ConvertsScreenAndWorldCoords {
  var lastProjectionMatrix: Option[Matrix4f] = None

  def invalidateLastProjectionMatrix(): Unit = {
  }

  def toWorld(screenLoc: Location): Option[Location] = {
    val lastProjectionMatrix = WorldSpritesRenderer.singleton.flatMap(_.camera).flatMap(_.lastProjectionMatrix)
    lastProjectionMatrix.map(lpm => {
      val (_, _, matrix) = lpm
      val worldLoc = new Vector3f()

      val mouseProjectedX = (screenLoc.x / Main.SCREEN_WIDTH * 2) - 1
      val mouseProjectedY = -((screenLoc.y / Main.SCREEN_HEIGHT * 2) - 1)

      matrix.unproject(mouseProjectedX, mouseProjectedY, 0.0f, Array[Int](-1, -1, 2, 2), worldLoc)
      worldLoc.mul(1.toFloat / Main.TEXTURE_SIZE) // divided by texture size

      new Location(worldLoc.x, worldLoc.y)
    })
  }

  def toScreen(worldLoc: Location, offset: Location = Location(0, 0)): Option[Location] = {
    val lastProjectionMatrix = WorldSpritesRenderer.singleton.flatMap(_.camera).flatMap(_.lastProjectionMatrix)
    if(this.lastProjectionMatrix != lastProjectionMatrix.map(_._3)) {
      invalidateLastProjectionMatrix()
    }
    this.lastProjectionMatrix = lastProjectionMatrix.map(_._3)

    lastProjectionMatrix.map(lpm => {
      val (_, _, matrix) = lpm
      val screenLoc: Vector3f = new Vector3f()
      matrix.project(
        new Vector3f(
          Math.floor(worldLoc.x + offset.x).toFloat * Main.TEXTURE_SIZE,
          Math.floor(worldLoc.y + offset.y).toFloat * Main.TEXTURE_SIZE,
          0.0f
        ),
        Array[Int](-1, -1, 2, 2),
        screenLoc
      )
      Location(screenLoc.x, screenLoc.y)
    })
  }
}
