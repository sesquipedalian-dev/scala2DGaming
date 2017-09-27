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
import org.sesquipedalian_dev.scala2DGaming.entities.equipment.Bed

// soldiers need sleep
// target should be 8hr sleep / 24 hr period for max effectiveness -
// so increase at rate of (100 / 16 / 60 / 60 per second) and
// 'sleep off' (when using Bed equipment) at rate of (100 / 8 / 60 / 60)
//
case class SleepNeed(target: GoodGuy) extends Need(target) {
  override val name: String = "Sleep"
  override def update(deltaTimeSeconds: Double): Unit = {
    val tickRate: Float = if(target.equipmentImUsing.collect{case x: Bed => x} nonEmpty) {
      -100.toFloat / 7 / 60 / 60  // sleeping for 8 hours fully sets you up, but give 'em a few minutes to get to the beds
    } else {
      100.toFloat / 16 / 60 / 60 // being awake for 16 hours fully exacerbates this need
    }
    adjustByRate(deltaTimeSeconds, tickRate)
  }
}
