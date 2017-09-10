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

import org.sesquipedalian_dev.scala2DGaming.{HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.scala2DGaming.entities.GoodGuy

// Needs - soldiers have needs that adjust their effectiveness at manning equipment
// all needs range in degree from 0 (no problem) to 100 (need to do something about it)
//   ** TODO ** it's likely we want a non-linear affectiveness modifier - being ready to sleep is MUCH worse than being at 50% sleep need
// the need defines how it updates over time - examples include:
// - simple ticking one direction or another over time
// - ticking based on equipment being used
//
abstract class Need(target: GoodGuy) extends HasGameUpdate {
  def name: String
  var degree: Double = 0
  def update(deltaTimeSeconds: Double): Unit
  def adjustByRate(deltaTimeSeconds: Double, rate: Double): Unit = {
    degree = Math.max(0, Math.min(100, degree + (deltaTimeSeconds * rate / TimeOfDay.SLOW)))
  }
}
