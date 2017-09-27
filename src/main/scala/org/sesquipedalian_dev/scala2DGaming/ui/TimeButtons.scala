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

import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.game.TimeOfDay
import org.sesquipedalian_dev.scala2DGaming.graphics.HasSingleUiSpriteRendering
import org.sesquipedalian_dev.scala2DGaming.input.UIButtonMouseListener
import org.sesquipedalian_dev.util.Logging

trait TimeButton extends UIButtonMouseListener with Logging {
  val timeToSet: Double

  override def buttonClicked(): Unit = {
    trace"button clicked $textureFile"
    TimeOfDay.singleton.foreach(_.speed = timeToSet)
  }
}

class PauseButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  var disabled: Boolean = false
  override def textureFile = "/textures/ui/buttons/pause.bmp"
  override def location = Location(2200, 30)
  val timeToSet: Double = TimeOfDay.PAUSE

  var previousSpeed: Option[Double] = None
  override def buttonClicked(): Unit = {
    if(!disabled) {
      previousSpeed match {
        case Some(prev) => TimeOfDay.singleton.foreach(tod => {
          tod.speed = prev
          previousSpeed = None
        })
        case _ => TimeOfDay.singleton.foreach(tod => {
          previousSpeed = Some(tod.speed)
          tod.speed = timeToSet
        })
      }
    }
  }
}

class SlowButton extends HasSingleUiSpriteRendering with UIButtonMouseListener with TimeButton {
  override def textureFile = "/textures/ui/buttons/slow.bmp"
  override def location = Location(2270, 30)
  val timeToSet: Double = TimeOfDay.SLOW
}

class MediumButton extends HasSingleUiSpriteRendering with UIButtonMouseListener with TimeButton {
  override def textureFile = "/textures/ui/buttons/medium.bmp"
  override def location = Location(2340, 30)
  val timeToSet: Double = TimeOfDay.MEDIUM
}

class FastButton extends HasSingleUiSpriteRendering with UIButtonMouseListener with TimeButton {
  override def textureFile = "/textures/ui/buttons/fast.bmp"
  override def location = Location(2410, 30)
  val timeToSet: Double = TimeOfDay.FAST
}
