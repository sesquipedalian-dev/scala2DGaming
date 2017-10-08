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

import org.sesquipedalian_dev.scala2DGaming.game.Commander
import org.sesquipedalian_dev.scala2DGaming.graphics.{BlocksBuilding, HasSingleWorldSpriteRendering, HasWorldSpriteRendering}
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistryCollection

trait HasCostToBuild extends CanBuild { this: Logging =>
  def cost: Int // cost in GMUs
  abstract override def canBuildOnInternal: PartialFunction[HasSingleWorldSpriteRendering, Boolean] = ({
    case x if Commander.gmus <= cost => false
  }: PartialFunction[HasSingleWorldSpriteRendering, Boolean]) orElse super.canBuildOnInternal
}

trait CanBuild { this: Logging =>
  def textureFile: String
  def name: String
  def buildTimeSeconds: Float  = 0 // real-time seconds needed to build this
  def userCanBuild: Boolean = true

  def canBuildOn(worldLoc: Location): Boolean = {
    val possibleConflicts = HasWorldSpriteRendering.all.collect({
      case x: HasSingleWorldSpriteRendering if x.location == worldLoc => x
    })

    val f = canBuildOnInternal.lift

    !possibleConflicts.exists(p => !f(p).getOrElse(true))
  }

  def canBuildOnInternal: PartialFunction[HasSingleWorldSpriteRendering, Boolean] = {
    case x: HasSingleWorldSpriteRendering with BlocksBuilding => false
  }

  def buildOn(location: Location): Unit = {}

  CanBuild.register(this)
}

object CanBuild extends HasRegistryCollection {
  override type ThisType = CanBuild
}


