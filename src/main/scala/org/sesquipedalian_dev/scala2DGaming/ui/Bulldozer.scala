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
package org.sesquipedalian_dev.scala2DGaming.ui

import org.sesquipedalian_dev.scala2DGaming.entities.equipment.Equipment
import org.sesquipedalian_dev.scala2DGaming.entities.terrain.{Grass, Terrain}
import org.sesquipedalian_dev.scala2DGaming.entities.{CanBuild, HasCostToBuild, Location}
import org.sesquipedalian_dev.scala2DGaming.graphics.{BlocksBuilding, HasSingleWorldSpriteRendering, HasWorldSpriteRendering}
import org.sesquipedalian_dev.util._

object Bulldozer extends CanBuild with HasCostToBuild with Logging {
  override def textureFile = "/textures/ui/bulldozer.bmp"
  override def name = "Bulldozer"

  override def canBuildOn: PartialFunction[HasSingleWorldSpriteRendering, Boolean] = ({
    case x: HasSingleWorldSpriteRendering with BlocksBuilding => true // can only bulldoze things that would block building
    case x: Equipment => true
    case _ => false
  }: PartialFunction[HasSingleWorldSpriteRendering, Boolean]) orElse super.canBuildOn

  override def buildOn(location: Location): Unit = {
    super.buildOn(location)
    val existingEquipment = HasWorldSpriteRendering.all.collectFirst({
      case x: Equipment if x.location == location => x
    })
    val existingTerrain = HasWorldSpriteRendering.all.collectFirst({
      case x: HasSingleWorldSpriteRendering with Terrain if x.location == location => x
    })
    val preferredDeleteTarget = (existingEquipment orElse existingTerrain)
    trace"Bulldozer preferred delete? $existingEquipment $existingTerrain $preferredDeleteTarget"
    preferredDeleteTarget.foreach(t => HasWorldSpriteRendering.unregister(t))

    if(preferredDeleteTarget == existingTerrain) {
      new Grass(location)
    }
  }

  override def cost = 20
}
