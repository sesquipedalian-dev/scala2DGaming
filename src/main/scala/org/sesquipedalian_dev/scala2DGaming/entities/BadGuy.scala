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
      val newLocation = Location(newX.toFloat, newY.toFloat)
      val newLocInt = Location(Math.floor(newX).toFloat, Math.floor(newY).toFloat)
      val currentLocInt = Location(Math.floor(location.x).toFloat, Math.floor(location.y).toFloat)

      if(newLocInt != currentLocInt) {
        // if we move into a new grid location, check if we can CONTINUE moving in the same direction
        // if not, we have to move around an obstacle

        val xDir = Math.cos(direction)
        val yDir = Math.sin(direction)
        val evenNewerX = newLocation.x + (xDir / Math.abs(xDir))
        val evenNewerY = newLocation.y + (yDir / Math.abs(yDir))
        val evenNewerLocation = Location(Math.floor(evenNewerX).toFloat, Math.floor(evenNewerY).toFloat)
        val traversable = WorldMap.instance.flatMap(world => {
          world.worldLocations.get(evenNewerLocation).map(_.traversable)
        }).getOrElse(false)

        if(!traversable) {
          // ok, we can't go that way - move the closest direction to where we were trying to go that is traversable

          val directions = List( // cardinal directions we could move
            0f,             // +x
            Math.PI / 2,    // +y
            Math.PI,        // -x
            3 * Math.PI / 2 // -y
          )
          val (d, _) = directions.map(d => { // figure out whether each direction is traversable
            val newLocX = newLocInt.x + Math.cos(d)
            val newLocY = newLocInt.y + Math.sin(d)
            val newLocCombined = Location(Math.floor(newLocX).toFloat, Math.floor(newLocY).toFloat)
            val traversable = WorldMap.instance.flatMap(world => {
              world.worldLocations.get(newLocCombined).map(_.traversable)
            }).getOrElse(false)
            (d, traversable)
          })
            .filter(p => p._2) // only pick a direction that is traversable
            .sortBy(p => Math.abs(direction - p._1)) // pick the direction that is closest to where we were already trying to go
            .head

          // go new direction instead
          val newX = location.x + (Math.cos(d) * speed * deltaTimeSeconds)
          val newY = location.y + (Math.sin(d) * speed * deltaTimeSeconds)
          location = Location(newX.toFloat, newY.toFloat)
        } else {
          location = Location(newX.toFloat, newY.toFloat)
        }
      } else {
        location = Location(newX.toFloat, newY.toFloat)
      }
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
