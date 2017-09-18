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

trait HasRangeOverlayRendering {
  HasRangeOverlayRendering.all :+= this
  def render(renderer: RangeOverlay): Unit
}

object HasRangeOverlayRendering {
  var all: List[HasRangeOverlayRendering] = Nil
  def render(renderer: RangeOverlay): Unit = {
    all.foreach(_.render(renderer))
  }
  def unregister(x: HasRangeOverlayRendering): Unit = {
    all = all.filterNot(_ == x)
  }
}