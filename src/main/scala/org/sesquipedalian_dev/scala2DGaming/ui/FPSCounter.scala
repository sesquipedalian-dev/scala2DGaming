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

import java.awt.Color

import org.sesquipedalian_dev.scala2DGaming.HasGameUpdate
import org.sesquipedalian_dev.scala2DGaming.graphics.{HasUiRendering, UIRenderer}

// track fps
class FPSCounter()
extends HasGameUpdate
with HasUiRendering
{
  // only update FPS every second
  final val UPDATE_TIMER_MAX = 1d
  var updateTimer: Double = 0

  var currentFps: Double = 0d
  def update(deltaTimeSeconds: Double): Unit = {
    updateTimer -= deltaTimeSeconds
    if(updateTimer <= 0) {
      currentFps = 1 / deltaTimeSeconds
      updateTimer = UPDATE_TIMER_MAX
    }
  }

  def render(uiRenderer: UIRenderer): Unit = {
    uiRenderer.drawTextOnWorld(0, 0, String.format("FPS: %02.1f", new java.lang.Double(currentFps)), Color.RED)
  }
}
