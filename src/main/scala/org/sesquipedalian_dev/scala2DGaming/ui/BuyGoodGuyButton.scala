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
import org.sesquipedalian_dev.scala2DGaming.entities.soldiers.{GoodGuy, GoodGuyGroups}
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.game.Commander
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.UIButtonMouseListener
import org.sesquipedalian_dev.scala2DGaming.util.Logging


class BuyGoodGuyButton
  extends HasUiSpriteRendering
  with UIButtonMouseListener
  with HasUiRendering
  with Logging
{
  final val GoodGuyNames = List(
    "Washington", "Adams", "Jefferson", "Madison", "Monroe", "Quincy", "Jackson", "VanBuren",
    "Harrison", "Tyler", "Polk", "Taylor", "Fillmore", "Pierce", "Buchanan", "Lincoln",
    "Johnson", "Grant", "Hayes", "Garfield", "Arthur", "Cleveland",
    "McKinley", "Roosevelt", "Taft", "Wilson", "Harding", "Coolidge", "Hoover",
    "Truman", "Eisenhower", "Kennedy", "Nixon", "Ford", "Carter", "Regan",
    "Bush", "Clinton", "Obama", "Trump"
  )

  final val GuySpawnLocation = Location(25, 0)

  var textureIndex: Option[Int] = None
  var goldCost: Int = 50
  override def textureFile = "/textures/ui/buttons/buy_guy.bmp"
  override def location = Location(0, (Math.pow(Main.UI_HEIGHT, 2) / Main.UI_WIDTH - UITextRenderer.sizeToInt(UITextRenderer.MEDIUM) * 3).toFloat)

  override def buttonClicked(): Unit = {
    trace"Buy Good Guy button clicked $textureIndex"

    if(goldCost < Commander.gmus) {
      info"Buying a new guy $goldCost ${Commander.gmus}"

      // pick out name for new dude
      val selectedName = Main.random.flatMap(r => {
        r.shuffle(GoodGuyNames diff HasWorldSpriteRendering.all.collect({ case x: GoodGuy => x.name })).headOption
      }).getOrElse(GoodGuyNames.head)

      // make new dude
      val newGuy = new GoodGuy(selectedName, GuySpawnLocation.copy())

      // put the guy into the 'first' group?
      GoodGuyGroups.addNewGuy(newGuy)

      // charge the player for the cost of getting the guy
      Commander.changeMoney(-goldCost)

      // bump the cost of buying new guys
      goldCost *= 2
    } else {
      trace"attempt to buy a guy with too little money $goldCost ${Commander.gmus}"
    }
  }

  override def render(uiRenderer: UITextRenderer): Unit = {
    val textLoc = Location(location.x + UIButtonsRenderer.singleton.map(_.textureSize).getOrElse(64), location.y)
    uiRenderer.drawTextOnWorld(textLoc.x, textLoc.y, goldCost.toString, Color.YELLOW, UITextRenderer.MEDIUM)
  }

  override def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    // draw button
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

    // draw text backing
    val textLoc = Location(location.x + UIButtonsRenderer.singleton.map(_.textureSize).getOrElse(64), location.y)
    uiSpritesRenderer.drawTextBacking(textLoc.x, textLoc.y, goldCost.toString.size, UITextRenderer.MEDIUM)
  }
}

