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
package org.sesquipedalian_dev.scala2DGaming.entities.equipment

import org.sesquipedalian_dev.scala2DGaming.entities.needs.{Need, RecreationNeed, SleepNeed}
import org.sesquipedalian_dev.scala2DGaming.entities.{CanBuild, Location}
import org.sesquipedalian_dev.scala2DGaming.graphics.HasSingleWorldSpriteRendering
import org.sesquipedalian_dev.util.Logging

class GameConsole(
  var location: Location
) extends HasSingleWorldSpriteRendering
  with Equipment
  with NeedFulfillingEquipment
{
  override val textureFile: String = GameConsole.textureFile
  override val name: String = GameConsole.name
  override val useRange: Float = 0.5f
  override val fulfillmentRateHours: Float = 2f
  override val associatedNeed: String = RecreationNeed.name
}

object GameConsole extends CanBuild with Logging {
  override def buildOn(location: Location): Unit = new GameConsole(location)
  override def textureFile = "/textures/entities/game_console.bmp"
  override def name = "GameConsole"
  override def buildTimeSeconds: Float = 30 * 60
}
