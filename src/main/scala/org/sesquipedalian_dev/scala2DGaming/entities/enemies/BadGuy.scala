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
package org.sesquipedalian_dev.scala2DGaming.entities.enemies

import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.terrain.Terrain
import org.sesquipedalian_dev.scala2DGaming.entities.{HasMovingToward, Location}
import org.sesquipedalian_dev.scala2DGaming.game.{Commander, HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.scala2DGaming.graphics.{HasSingleWorldSpriteRendering, HasWorldSpriteRendering, WorldSpritesRenderer}
import org.sesquipedalian_dev.util._

import scala.util.Random

class BadGuy(
  var location: Location,
  destination: Option[Location], // expected to be +/- 1 (or 0) in each axis
  worldSize: Location,
  var health: Int
) extends HasGameUpdate
  with HasSingleWorldSpriteRendering
  with HasMovingToward
{
  // path find to destination

  val name: String = "BadGuy"
  override val textureFile: String = "/textures/entities/badguy.bmp"
  final val speed: Float = 1f / TimeOfDay.SLOW.toFloat // blocks / sec

  override def update(deltaTimeSeconds: Double): Unit = {
    if(direction.isEmpty && destination.nonEmpty) {
      val newDirection = Terrain.findPath(location, destination.get)
      trace"BadGuy moving in new direction $location $destination $newDirection"
      if(newDirection.isEmpty) {
        direction = destination.get :: Nil // if user has blocked our path, just go straight through
      } else {
        direction = newDirection.get
        val key = Main.random.getOrElse(Random).nextInt().toString
        Terrain.onTerrainChanged(key, () => {
          Terrain.stopOnTerrainChanged(key)
          direction = Nil
        })
      }
    }

    // check for killed
    if(health <= 0) {
      despawn(Some(1))
    }

    // if bad guys get all the way to the right, despawn 'em and lose money
    if(location.x >= 49) {
      despawn(Some(-5))
    }

    super.update(deltaTimeSeconds)
  }

  var despawnCallbacks: List[(BadGuy) => Unit] = List()

  def onDespawn(callback: (BadGuy) => Unit): Unit = {
    despawnCallbacks :+= callback
  }

  def despawn(bounty: Option[Int]): Unit = {
    // unregister us
    Registry.unregister(this)

    // bounty per kill
    bounty.foreach(b => Commander.changeMoney(b))

    // any other projectiles that may have been chasing us can go away
    Registry.objects[HasGameUpdate](HasGameUpdate.tag).foreach({
      case x: Projectile if x.target == this => {
        Registry.unregister(this)
      }
      case _ =>
    })

    despawnCallbacks.foreach(cb => cb(this))
    despawnCallbacks = Nil
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
