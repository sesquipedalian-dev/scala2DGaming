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
import org.sesquipedalian_dev.scala2DGaming.entities.{CanBuild, HasCostToBuild, Location, PendingBuild}
import org.sesquipedalian_dev.scala2DGaming.game.Commander
import org.sesquipedalian_dev.scala2DGaming.graphics.{BlocksBuilding, HasSingleWorldSpriteRendering, HasWorldSpriteRendering}
import org.sesquipedalian_dev.util._

object Bulldozer extends CanBuild with HasCostToBuild with Logging {
  override def textureFile = "/textures/ui/bulldozer.bmp"
  override def name = "Bulldozer"

  override def canBuildOn(worldLoc: Location): Boolean = {
    val possibleConflicts = HasWorldSpriteRendering.all.collect({
      case x: HasSingleWorldSpriteRendering if x.location == worldLoc => x
    })

    val f = canBuildOnInternal.lift

    possibleConflicts.exists(pc => f(pc).getOrElse(false))
  }

  override def canBuildOnInternal: PartialFunction[HasSingleWorldSpriteRendering, Boolean] = ({
    case x: PendingBuild => true
    case x: HasSingleWorldSpriteRendering with BlocksBuilding if Commander.gmus > cost => true // can only bulldoze things that would block building
    case x: Equipment if Commander.gmus > cost => true
    case _ => false
  }: PartialFunction[HasSingleWorldSpriteRendering, Boolean])

  override def buildOn(location: Location): Unit = {
    super.buildOn(location)
    val existingEquipment = HasWorldSpriteRendering.all.collectFirst({
      case x: PendingBuild if x.location == location => x
      case x: Equipment if x.location == location => x
    })
    val existingTerrain = HasWorldSpriteRendering.all.collectFirst({
      case x: HasSingleWorldSpriteRendering with Terrain if x.location == location => x
    })
    val preferredDeleteTarget = (existingEquipment orElse existingTerrain)
    trace"Bulldozer preferred delete? $existingEquipment $existingTerrain $preferredDeleteTarget"
    preferredDeleteTarget.foreach(t => {
      if(t.isInstanceOf[PendingBuild]) {
        val builder = t.asInstanceOf[PendingBuild].realBuilder
        builder match {
          case x: HasCostToBuild => Commander.changeMoney(x.cost) // refund build cost of underlying builder if deleted a PendingBuild
          case _ =>
        }
      }
      HasWorldSpriteRendering.unregister(t)
    })

    if(preferredDeleteTarget == existingTerrain) {
      new Grass(location)
    }
  }

  override def cost = 1
}
