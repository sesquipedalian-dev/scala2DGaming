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

import org.sesquipedalian_dev.scala2DGaming.entities.equipment.Equipment
import org.sesquipedalian_dev.scala2DGaming.game.HasGameUpdate
import org.sesquipedalian_dev.scala2DGaming.graphics.{BlocksBuilding, HasSingleWorldSpriteRendering}
import org.sesquipedalian_dev.util._

class PendingBuild(
  val location: Location,
  var buildTime: Float,
  val realBuilder: CanBuild
) extends Equipment
  with HasGameUpdate
  with BlocksBuilding
  with HasSingleWorldSpriteRendering
{
  override def useRange = 1.0f
  override def name = "PendingBuild"

  override def update(deltaTimeSeconds: Double): Unit = {
    if(user.nonEmpty) {
      buildTime -= deltaTimeSeconds.toFloat
      if (buildTime <= 0) {
        Registry.unregister(this)
        realBuilder.buildOn(location)
        user.get.equipmentImUsing = None
      }
    }
  }

  override def textureFile = "/textures/entities/Pending.bmp"
}

//object PendingBuild extends CanBuild {
//  override def userCanBuild: Boolean = false
//  override def textureFile = ""
//  override def name = "pending"
//}
