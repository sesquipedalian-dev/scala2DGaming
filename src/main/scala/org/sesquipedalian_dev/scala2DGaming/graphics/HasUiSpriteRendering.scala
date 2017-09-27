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
package org.sesquipedalian_dev.scala2DGaming.graphics

import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistryCollection

trait HasSingleUiSpriteRendering extends HasUiSpriteRendering {
  var textureIndex: Option[Int] = None
  def textureFile: String
  def location: Location
  def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    if(textureIndex.isEmpty) {
      uiSpritesRenderer.textureArray.foreach(ta => {
        val currentLoc = ta.textureFiles.indexOf(textureFile)
        if(currentLoc == -1) {
          textureIndex = ta.addTextureResource(textureFile)
        } else {
          textureIndex = Some(currentLoc)
        }
      })
    }
    textureIndex.foreach(ti => uiSpritesRenderer.drawAButton(location.x, location.y, ti))
  }
}

trait HasUiSpriteRendering {
  def render(uiSpritesRenderer: UIButtonsRenderer): Unit

  HasUiSpriteRendering.register(this)
}

object HasUiSpriteRendering extends HasRegistryCollection {
  override type ThisType = HasUiSpriteRendering

  def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    all.foreach(_.render(uiSpritesRenderer))
  }
}