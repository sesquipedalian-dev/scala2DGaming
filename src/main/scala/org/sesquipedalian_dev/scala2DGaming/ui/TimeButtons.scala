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

import org.sesquipedalian_dev.scala2DGaming.TimeOfDay
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.graphics.HasSingleUiSpriteRendering
import org.sesquipedalian_dev.scala2DGaming.input.UIButtonMouseListener

class PauseButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/pause.bmp"
  override def location = Location(2200, 30)
  val timeToSet: Double = TimeOfDay.PAUSE
}

class SlowButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/slow.bmp"
  override def location = Location(2270, 30)
  val timeToSet: Double = TimeOfDay.SLOW
}

class MediumButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/medium.bmp"
  override def location = Location(2340, 30)
  val timeToSet: Double = TimeOfDay.MEDIUM
}

class FastButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  override def textureFile = "/textures/fast.bmp"
  override def location = Location(2410, 30)
  val timeToSet: Double = TimeOfDay.FAST
}
