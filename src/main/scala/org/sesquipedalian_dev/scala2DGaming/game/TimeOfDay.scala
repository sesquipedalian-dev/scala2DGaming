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
package org.sesquipedalian_dev.scala2DGaming.game

import java.awt.Color

import org.sesquipedalian_dev.scala2DGaming.graphics.{HasUiRendering, HasUiSpriteRendering, UIButtonsRenderer, UITextRenderer}
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistrySingleton

// map game time to 'day time'
class TimeOfDay()
  extends HasUiRendering
  with HasGameUpdate
  with HasUiSpriteRendering
{
  var speed: Double = TimeOfDay.MEDIUM

  def translateGameTimeToTimeOfDay(deltaTimeSeconds: Double): Double = {
    deltaTimeSeconds * speed
  }

  var currentTimeOfDay: Float = 9 * 60 * 60 // start at 9 am
  final val SECONDS_IN_DAY = 60 * 60 * 24
  def update(deltaTimeSeconds: Double): Unit = {
    currentTimeOfDay += deltaTimeSeconds.toFloat
    if (currentTimeOfDay >= SECONDS_IN_DAY) {
      currentTimeOfDay = 0
    }
  }

  override def render(uiRenderer: UITextRenderer): Unit = {
    val minutes = currentTimeOfDay / 60
    val hours = Math.floor(minutes / 60).toInt
    val minutesInHour = Math.floor(minutes - (hours * 60)).toInt
    uiRenderer.drawTextOnWorld(
      uiRenderer.uiWidth - (uiRenderer.textSizes.find(_.name == UITextRenderer.MEDIUM).get.size * 11),
      0,
      String.format("Time: %02d:%02d", new Integer(hours), new Integer(minutesInHour)),
      Color.CYAN,
      UITextRenderer.MEDIUM
    )
  }

  def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    Registry.singleton[UITextRenderer](UITextRenderer.tag).foreach(uiRenderer => {
      uiSpritesRenderer.drawTextBacking(
        uiRenderer.uiWidth - (uiRenderer.textSizes.find(_.name == UITextRenderer.MEDIUM).get.size * 11),
        0,
        11,
        UITextRenderer.MEDIUM
      )
    })
  }

  TimeOfDay.register(this)
}

object TimeOfDay extends HasRegistrySingleton {
  override type ThisType = TimeOfDay

  val PAUSE = 0.0
  val SLOW = 60.0    // 1 sec = 1 min real time
  val MEDIUM = 120.0 // 1 sec = 2 min real time
  val FAST = 360.0   // 1 sec = 5 min real time
//  val FAST = 3600.0

  def translateGameTimeToTimeOfDay(deltaTimeSeconds: Double): Double = {
    singleton.map(_.translateGameTimeToTimeOfDay(deltaTimeSeconds)).getOrElse(deltaTimeSeconds)
  }
}
