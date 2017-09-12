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
import org.sesquipedalian_dev.scala2DGaming.entities.needs.{Need, SleepNeed}
import org.sesquipedalian_dev.scala2DGaming.graphics.HasSingleWorldSpriteRendering

class GoodGuy(
  var location: Location
) extends HasSingleWorldSpriteRendering
  with HasGameUpdate
{
  // TODO make needs init more flexible - some good guys could have traits that adjust how their needs work,
  // or what needs they even have
  var needs: List[Need] = List(
    SleepNeed(this)
  )

  var equipmentImUsing: Option[Equipment] = None
  override def textureFile: String = "/textures/MilitaryMan.bmp"

  override def update(deltaTimeSeconds: Double): Unit = {

  }

  def use(equipment: Equipment): Unit = {
    // unman whatever we were already manning
    equipmentImUsing.foreach(e => e.user = None)

    // man the new thing
    equipment.user = Some(this)
    equipmentImUsing = Some(equipment)
  }

  def needEffectiveness: Double = {
    val needsGraphBase = Math.pow(100, 1.toFloat / 100)
    if(needs.nonEmpty) {
      val result = needs.map(need => Math.max(0, 100 - Math.pow(needsGraphBase, need.degree))).sum / needs.size / 100
//      println(s"need effectiveness: $needsGraphBase, ${needs.map(_.degree)}, $result")
      result
    } else {
      1f
    }
  }
}