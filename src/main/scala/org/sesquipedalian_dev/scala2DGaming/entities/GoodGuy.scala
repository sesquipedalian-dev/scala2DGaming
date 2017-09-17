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
package org.sesquipedalian_dev.scala2DGaming.entities

import java.awt.Color

import org.joml.Vector3f
import org.sesquipedalian_dev.scala2DGaming.{HasGameUpdate, Main}
import org.sesquipedalian_dev.scala2DGaming.entities.needs.{Need, SleepNeed}
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.WorldMouseListener

class GoodGuy(
  val name: String,
  var location: Location
) extends HasSingleWorldSpriteRendering
  with HasGameUpdate
  with WorldMouseListener
  with HasUiRendering
  with HasUiSpriteRendering
{
  // TODO make needs init more flexible - some good guys could have traits that adjust how their needs work,
  // or what needs they even have
  var needs: List[Need] = List(
    SleepNeed(this)
  )

  var equipmentImUsing: Option[Equipment] = None
  override def textureFile: String = "/textures/MilitaryMan.bmp"

  override def update(deltaTimeSeconds: Double): Unit = {
    // determine anything to do based on activity
    GoodGuyGroups.groupForGuy(this).foreach(group => {
      val activity = group.schedule.get()
//      println(s"Good guy deciding based on activity? $name $activity")
      activity match {
        case Activities.GUARD if equipmentImUsing.nonEmpty => // we found some equipment, keep it up
        case Activities.GUARD => {

        }
        case Activities.SLEEP if equipmentImUsing.exists(_ => false /* TODO set up bed equipment */) => // if we usin' a bed keep it up
        case Activities.SLEEP if equipmentImUsing.nonEmpty => { // put down the equipment
          equipmentImUsing.foreach(drop)
        }
        case Activities.SLEEP => {
          // we not usin' a bed, move on towards one
        }
      }
    })
  }


  def use(equipment: Equipment): Unit = {
    // unman whatever we were already manning
    equipmentImUsing.foreach(e => e.user = None)

    // man the new thing
    equipment.user = Some(this)
    equipmentImUsing = Some(equipment)
  }

  def drop(equipment: Equipment): Unit = {
    // unman whatever we were already manning
    equipmentImUsing.foreach(e => e.user = None)
    equipmentImUsing = None
  }

  def needEffectiveness: Double = {
    val needsGraphBase = Math.pow(100, 1.toFloat / 100)
    if(needs.nonEmpty) {
      val result = needs.map(need => Math.max(0, 100 - Math.pow(needsGraphBase, need.degree))).sum / needs.size / 100
//      println(s"need effectiveness: $needsGraphBase, ${needs.map(_.degree)}, $result")
      result
    } else {
      1f
    }
  }

  override def hoverEnter(): Unit = {}
  override def hoverLeave(): Unit = {}
  override def clicked(): Unit = {}

  def toUiLoc(screenLoc: Location): Option[Location] = {
    val x = (screenLoc.x + 1) * Main.UI_WIDTH / 2
    val totalY = 1.toFloat / (Main.UI_WIDTH.toFloat / Main.UI_HEIGHT)
    val y = (-screenLoc.y + 1) / 2 * Math.pow(Main.UI_HEIGHT, 2) / Main.UI_WIDTH

    Some(Location(x, y.toFloat))
  }

  override def render(uiRenderer: UITextRenderer): Unit = {
    if(hovered) {
      val screenLoc = toScreen(location, Location(1, 0))
      val uiLoc = screenLoc.flatMap(toUiLoc).getOrElse(Location(0, 0))

      uiRenderer.drawTextOnWorld(uiLoc.x, uiLoc.y, name, Color.ORANGE, UITextRenderer.SMALL)
      val medYSize = UITextRenderer.sizeToInt(UITextRenderer.MEDIUM)
      val smallYSize = UITextRenderer.sizeToInt(UITextRenderer.SMALL)

      uiRenderer.drawTextOnWorld(uiLoc.x, uiLoc.y + smallYSize, "=[Needs]=", Color.WHITE, UITextRenderer.MEDIUM)
      needs.foreach(need => {
        val needTexts = (1 to 10).map(i => if(need.degree > i * 10) { "+" } else { " " }).reduce(_ + _) + need.name
                val color = need.degree match {
          case x if x > 90 => Color.RED
          case x if x > 80 => Color.YELLOW
          case x if x < 20 => Color.GREEN
          case _ => Color.WHITE
        }
        uiRenderer.drawTextOnWorld(uiLoc.x, uiLoc.y + smallYSize + medYSize, needTexts, color, UITextRenderer.SMALL)
      })
    }
  }

  def render(uiSpritesRenderer: UIButtonsRenderer): Unit = {
    if(hovered) {
      val screenLoc = toScreen(location, Location(1, 0))
      val uiLoc = screenLoc.flatMap(toUiLoc).getOrElse(Location(0, 0))
      val medYSize = UITextRenderer.sizeToInt(UITextRenderer.MEDIUM)
      val smallYSize = UITextRenderer.sizeToInt(UITextRenderer.SMALL)

      uiSpritesRenderer.drawTextBacking(uiLoc.x, uiLoc.y, name.size, UITextRenderer.SMALL)
      uiSpritesRenderer.drawTextBacking(uiLoc.x, uiLoc.y + smallYSize, 9, UITextRenderer.MEDIUM)
      needs.foreach(need => {
        val needTexts = (0 to 10).map(i => if(need.degree > i * 10) { "+" } else { " " }).reduce(_ + _) + need.name
        uiSpritesRenderer.drawTextBacking(uiLoc.x, uiLoc.y + smallYSize + medYSize, needTexts.size, UITextRenderer.SMALL)
      })
    }
  }

  override def toString: String = {
    s"GoodGuy($name)"
  }
}