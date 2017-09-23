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

import org.sesquipedalian_dev.scala2DGaming.graphics.{HasSingleWorldSpriteRendering, HasWorldSpriteRendering}
import org.sesquipedalian_dev.scala2DGaming.{HasGameUpdate, TimeOfDay}

class Projectile(
  var location: Location,
  val target: BadGuy,
  damage: Int,
  speed: Float // units / second
) extends HasSingleWorldSpriteRendering
  with HasGameUpdate
{
  override val textureFile: String = "/textures/entities/bullet.bmp"

  override def update(deltaTimeSeconds: Double): Unit = {
    // move toward the target - homing style lol
    val xDiff = target.location.x - location.x
    val yDiff = target.location.y - location.y
    val angle = Math.atan2(yDiff, xDiff)
    val normalX = Math.cos(angle)
    val normalY = Math.sin(angle)

    val direction = Location(normalX.toFloat, normalY.toFloat)
    val deltaX = (direction.x * speed * deltaTimeSeconds.toFloat / TimeOfDay.SLOW.toFloat)
    val deltaY = (direction.y * speed * deltaTimeSeconds.toFloat / TimeOfDay.SLOW.toFloat)
    location = Location(location.x + deltaX, location.y + deltaY)

    // if we've gotten to the target, hit them and delete us
    if((Math.floor(location.x) == Math.floor(target.location.x)) &&
       (Math.floor(location.y) == Math.floor(target.location.y))) {
      target.health -= damage
      HasWorldSpriteRendering.unregister(this)
      HasGameUpdate.unregister(this)
    }
  }
}
