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

import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.game.TimeOfDay
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.{MouseInputHandler, UIButtonMouseListener}
import org.sesquipedalian_dev.scala2DGaming.util.Logging

class Dialog(text: String, image: String) extends HasUiSpriteRendering with HasUiRendering with Logging {
  var currentDialog: Option[String] = None
  var backingTexIndex: Option[Int] = None
  var characterTexIndex: Option[Int] = None

  override def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    if(characterTexIndex.isEmpty) {
      uiSpritesRenderer.textureArray.foreach(ta => {
        val currentLoc = ta.textureFiles.indexOf(image)
        if(currentLoc == -1) {
          characterTexIndex = ta.addTextureResource(image)
        } else {
          characterTexIndex = Some(currentLoc)
        }
      })
    }

    // render UI textures
    trace"start render my button 1"
    uiSpritesRenderer.drawAButton(320, 590, characterTexIndex.get, 256, 256)
    trace"stop render my button 1"

    if(backingTexIndex.isEmpty) {
      uiSpritesRenderer.textureArray.foreach(ta => {
        val currentLoc = ta.textureFiles.indexOf("/textures/ui/dialog_backer.bmp")
        if(currentLoc == -1) {
          backingTexIndex = ta.addTextureResource("/textures/ui/dialog_backer.bmp")
        } else {
          backingTexIndex = Some(currentLoc)
        }
      })
    }

    // render UI textures
    trace"start render my button 2"
    uiSpritesRenderer.drawAButton(586, 590, backingTexIndex.get, 1280, 256)
    trace"stop render my button 2"
  }

  override def render(uiRenderer: UITextRenderer): Unit = {
    // render text
    val lines = text.grouped(1200 / UITextRenderer.sizeToInt(UITextRenderer.SMALL))
    lines.zipWithIndex.foreach(p => {
      val (line, index) = p
      val targetY = 600 + (index * UITextRenderer.sizeToInt(UITextRenderer.SMALL))
      val maxY = 720
      if(targetY < maxY) {
        uiRenderer.drawTextOnWorld(
          616, targetY,
          line, Color.GREEN, UITextRenderer.SMALL
        )
      }
    })
  }
}

object Dialog {
  var singleton: Option[Dialog] = None
  var okButton: Option[OkButton] = None
  var prevSpeed: Option[Double] = None

  def open(text: String, image: String): Unit = {
    if(singleton.isEmpty) {
      singleton = Some(new Dialog(text, image))
      okButton = Some(new OkButton)
      TimeOfDay.instance.foreach(tod => {
        prevSpeed = Some(tod.speed)
        tod.speed = TimeOfDay.PAUSE
      })
    }
  }

  def close(): Unit = {
    singleton.foreach(s => {
      HasUiRendering.all = HasUiRendering.all.filterNot(_ == s)
      HasUiSpriteRendering.all = HasUiSpriteRendering.all.filterNot(_ == s)
      singleton = None
    })

    okButton.foreach(s => {
      HasUiSpriteRendering.all = HasUiSpriteRendering.all.filterNot(_ == s)
      val index = MouseInputHandler.all.indexOf(s)
      MouseInputHandler.all.remove(index)
      okButton = None
    })

    prevSpeed.foreach(speed => {
      TimeOfDay.instance.foreach(tod => {
        prevSpeed = None
        tod.speed = speed
      })
    })
  }
}

class OkButton extends HasSingleUiSpriteRendering with UIButtonMouseListener {
  var disabled: Boolean = false
  override def textureFile = "/textures/ui/buttons/OK.bmp"
  override def location = Location(1886, 700)

  var previousSpeed: Option[Double] = None
  override def buttonClicked(): Unit = {
    if(!disabled) {
      Dialog.close()
    }
  }
}
