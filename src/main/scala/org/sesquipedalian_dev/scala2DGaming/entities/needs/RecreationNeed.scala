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
package org.sesquipedalian_dev.scala2DGaming.entities.needs

import org.sesquipedalian_dev.scala2DGaming.entities.soldiers.GoodGuy
import org.sesquipedalian_dev.scala2DGaming.entities.equipment.{Bed, NeedFulfillingEquipment}

// soldiers need to recreate every now and again - 48 hrs w/out recreating is enough to
// heighten this to dangerous levels
case class RecreationNeed(target: GoodGuy) extends Need(target) {
  override val name: String = RecreationNeed.name
  override def update(deltaTimeSeconds: Double): Unit = {
    val equipmentFixRate = target.equipmentImUsing.collect{
      case x: NeedFulfillingEquipment if x.associatedNeed == name => x.fulfillmentRateHours
    }
    val tickRate: Float = if(equipmentFixRate.nonEmpty) {
      -100.toFloat / equipmentFixRate.get / 60 / 60
    } else {
      100.toFloat / 48 / 60 / 60 // no recreating for 48 hours fully exacerbates this need
    }
    adjustByRate(deltaTimeSeconds, tickRate)
  }
}

object RecreationNeed {
  val name: String = "Recreation"
}
