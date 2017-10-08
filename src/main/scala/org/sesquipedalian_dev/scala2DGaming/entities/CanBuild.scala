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
import org.sesquipedalian_dev.scala2DGaming.graphics.{BlocksBuilding, HasSingleWorldSpriteRendering}
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistryCollection

trait HasCostToBuild extends CanBuild {
  def cost: Int // cost in GMUs
  abstract override def canBuildOn: PartialFunction[HasSingleWorldSpriteRendering, Boolean] = ({
    case x if Commander.gmus <= cost => false
  }: PartialFunction[HasSingleWorldSpriteRendering, Boolean]) orElse super.canBuildOn

  abstract override def buildOn(location: Location): Unit = {
    super.buildOn(location)
    Commander.changeMoney(-cost)
  }
}

trait CanBuild {
  def textureFile: String
  def name: String

  def canBuildOn: PartialFunction[HasSingleWorldSpriteRendering, Boolean] = {
    case x: HasSingleWorldSpriteRendering with BlocksBuilding => false
  }

  def buildOn(location: Location): Unit = {}

  CanBuild.register(this)
}

object CanBuild extends HasRegistryCollection {
  override type ThisType = CanBuild
}


