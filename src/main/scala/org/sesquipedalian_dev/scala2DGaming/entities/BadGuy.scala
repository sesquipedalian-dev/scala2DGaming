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
package org.sesquipedalian_dev.scala2DGaming.entities

import org.sesquipedalian_dev.scala2DGaming.HasGameUpdate
import org.sesquipedalian_dev.scala2DGaming.graphics.{HasWorldSpriteRendering, WorldSpritesRenderer}

class BadGuy(
  var location: Location,
  var directionRadians: Option[Float], // radians
  textureFile: String
) extends HasGameUpdate with HasWorldSpriteRendering {
  final val speed: Float = 1 // blocks / sec
  var textureIndex: Option[Int] = None


  override def update(deltaTimeSeconds: Double): Unit = {
    directionRadians.foreach(direction => {
      val newX = location.x + (Math.cos(direction) * speed * deltaTimeSeconds)
      val newY = location.y + (Math.sin(direction) * speed * deltaTimeSeconds)
      location = Location(newX.toFloat, newY.toFloat)
    })
  }

  override def render(worldSpritesRenderer: WorldSpritesRenderer): Unit = {
    if(textureIndex.isEmpty) {
      worldSpritesRenderer.textureArray.foreach(ta => {
        val currentLoc = ta.textureFiles.indexOf(textureFile)
        if(currentLoc == -1) {
          textureIndex = ta.addTextureResource(textureFile)
        } else {
          textureIndex = Some(currentLoc)
        }
      })
    }
    textureIndex.foreach(ti => worldSpritesRenderer.drawAGuyWorld(location.x, location.y, ti))
  }
}
