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

import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.graphics.{HasUiRendering, HasUiSpriteRendering, UIButtonsRenderer, UITextRenderer}
import org.sesquipedalian_dev.scala2DGaming.ui.{Dialog, PauseButton}
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistrySingleton

// entity to hold global info about the game player
class Commander(
  var gmus: Int
) extends HasUiSpriteRendering
  with HasUiRendering
{
  override def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    val textWidth = String.format("GMUs: %d", new java.lang.Integer(gmus)).size
    val yPos = Math.pow(Main.UI_HEIGHT, 2) / (Main.UI_WIDTH) - UITextRenderer.sizeToInt(UITextRenderer.MEDIUM)
    uiSpritesRenderer.drawTextBacking(0, yPos.toFloat, textWidth, UITextRenderer.MEDIUM)
  }

  override def render(uiRenderer: UITextRenderer): Unit = {
    val yPos = Math.pow(Main.UI_HEIGHT, 2) / (Main.UI_WIDTH) - UITextRenderer.sizeToInt(UITextRenderer.MEDIUM)
    uiRenderer.drawTextOnWorld(0, yPos.toFloat, String.format("GMUs: %d", new java.lang.Integer(gmus)), Color.YELLOW, UITextRenderer.MEDIUM)
  }

  Commander.register(this)
}

object Commander extends HasRegistrySingleton {
  override type ThisType = Commander

  // amt can be + or -
  def changeMoney(amt: Int): Unit = singleton.foreach(s => {
    s.gmus = Math.max(0, s.gmus + amt)
    if(s.gmus <= 0) {
      Dialog.open("You let too many bad guys through, Fool!  We'll try to salvage this situation with a new commander." +
        "  Get back down to the Gun Turrets, private!", "/textures/ui/sarge_dialog.bmp"
      )
      Dialog.okButton.foreach(_.disabled = true)
      Registry.objects[HasUiSpriteRendering](HasUiSpriteRendering.tag).foreach({
        case x: PauseButton => x.disabled = true
        case _ =>
      })
    }
  })

  def setMoney(amt: Int): Unit = singleton.foreach(s => {
    changeMoney(amt - s.gmus)
  })

  def gmus: Int = singleton.map(_.gmus).getOrElse(0)
}
