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
package org.sesquipedalian_dev.scala2DGaming.entities.soldiers

import java.awt.Color

import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.{needs, _}
import org.sesquipedalian_dev.scala2DGaming.entities.equipment.{Bed, Equipment, GunTurret, NeedFulfillingEquipment}
import org.sesquipedalian_dev.scala2DGaming.entities.needs.{Need, RecreationNeed, SleepNeed}
import org.sesquipedalian_dev.scala2DGaming.entities.terrain.Terrain
import org.sesquipedalian_dev.scala2DGaming.game.{HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.WorldMouseListener
import org.sesquipedalian_dev.scala2DGaming.ui.Dialog
import org.sesquipedalian_dev.util.Logging

class GoodGuy(
  val name: String,
  var location: Location
) extends HasSingleWorldSpriteRendering
  with HasGameUpdate
  with WorldMouseListener
  with HasUiRendering
  with HasUiSpriteRendering
  with HasMovingToward
  with Logging
{
  override val speed = 1.0f / TimeOfDay.SLOW.toFloat
  var overrideActivity: Option[Activities.Type] = None

  // TODO make needs init more flexible - some good guys could have traits that adjust how their needs work,
  // or what needs they even have
  var needs: List[Need] = List(
    SleepNeed(this),
    RecreationNeed(this)
  )

  var equipmentImUsing: Option[Equipment] = None
  override def textureFile: String = "/textures/entities/MilitaryMan.bmp"

  override def update(deltaTimeSeconds: Double): Unit = {
    // determine anything to do based on activity
    val currentActivity = overrideActivity orElse {
      GoodGuyGroups.groupForGuy(this).map(group => {
        group.schedule.get()
      })
    }

    currentActivity.foreach(activity => {
      trace"Good guy deciding based on activity? $name $activity"
      activity match {
        // IDLE - do nothing - maybe eventually we should play some sort of frustrated animation
        case Activities.IDLE =>
        // we found some equipment, keep it up
        case Activities.RECREATE if equipmentImUsing.collect({case x: NeedFulfillingEquipment if x.associatedNeed == RecreationNeed.name => x}).nonEmpty =>
        // if guarding and on wrong equipment, drop it
        case Activities.RECREATE if equipmentImUsing.nonEmpty => {
          equipmentImUsing.foreach(drop)
          direction = Nil
        }
        // not using a gun && have a cached direction - go towards it
        case Activities.RECREATE if direction.nonEmpty =>
        // not using a gun, move towards one
        case Activities.RECREATE => moveTowardsEquipment[NeedFulfillingEquipment](use, _.associatedNeed == RecreationNeed.name)
        // we found some equipment, keep it up
        case Activities.GUARD if equipmentImUsing.collect({case x: GunTurret => x}).nonEmpty =>
        // if guarding and on wrong equipment, drop it
        case Activities.GUARD if equipmentImUsing.nonEmpty => {
          equipmentImUsing.foreach(drop)
          direction = Nil
        }
        // not using a gun && have a cached direction - go towards it
        case Activities.GUARD if direction.nonEmpty =>
        // not using a gun, move towards one
        case Activities.GUARD => moveTowardsEquipment[GunTurret](use)
        // if we usin' a bed keep it up
        case Activities.SLEEP if equipmentImUsing.collect({case x: Bed => x}).nonEmpty =>
        // put down the equipment
        case Activities.SLEEP if equipmentImUsing.nonEmpty => {
          equipmentImUsing.foreach(drop)
          direction = Nil
        }
        // not using a bed && Have a cachred direction - nop
        case Activities.SLEEP if direction.nonEmpty =>
        // we not usin' a bed, move on towards one
        case Activities.SLEEP => moveTowardsEquipment[Bed](use)
      }
    })

    trace"Good Guy preupdate"
    super.update(deltaTimeSeconds)
  }

  def use(equipment: Equipment): Unit = {
    info"$name using equipment $equipment"

    // unman whatever we were already manning
    equipmentImUsing.foreach(e => e.user = None)

    // man the new thing
    equipment.user = Some(this)
    equipmentImUsing = Some(equipment)
  }

  def drop(equipment: Equipment): Unit = {
    info"$name dropping equipment $equipment"

    // unman whatever we were already manning
    equipmentImUsing.foreach(e => e.user = None)
    equipmentImUsing = None
  }

  var hasTriggeredNeedTutorial: Boolean = false
  def needEffectiveness: Double = {
    val needsGraphBase = Math.pow(100, 1.toFloat / 100)
    if(needs.nonEmpty) {
      val result = needs.map(need => Math.max(0, 100 - Math.pow(needsGraphBase, need.degree))).sum / needs.size / 100
      trace"need effectiveness: $needsGraphBase, ${needs.map(_.degree)}, $result"
      if(result <= .5 && !hasTriggeredNeedTutorial) {
        hasTriggeredNeedTutorial = true
        Dialog.open("Looks like your Guards are getting tired!  Try recruiting some more guards in the lower left." +
          "Then, in the upper right open the Group and Schedule UIs with those buttons.  Try to assign some " +
          "fresh soldiers to guard duty to replace these knuckleheads.", "/textures/ui/sarge_dialog.bmp"
        )
      }
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
      needs.zipWithIndex.foreach(p => {
        val (need, needIndex) = p
        val needTexts = (1 to 10).map(i => if(need.degree > i * 10) { "+" } else { " " }).reduce(_ + _) + need.name
                val color = need.degree match {
          case x if x > 90 => Color.RED
          case x if x > 80 => Color.YELLOW
          case x if x < 20 => Color.GREEN
          case _ => Color.WHITE
        }
        uiRenderer.drawTextOnWorld(uiLoc.x, uiLoc.y + smallYSize + medYSize + (smallYSize * needIndex), needTexts, color, UITextRenderer.SMALL)
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
      needs.zipWithIndex.foreach(p => {
        val (need, needIndex) = p
        val needTexts = (0 to 10).map(i => if(need.degree > i * 10) { "+" } else { " " }).reduce(_ + _) + need.name
        uiSpritesRenderer.drawTextBacking(uiLoc.x, uiLoc.y + smallYSize + medYSize + (smallYSize * needIndex), needTexts.size, UITextRenderer.SMALL)
      })
    }
  }

  override def toString: String = {
    s"GoodGuy($name)"
  }

  override def noPathToEquipment(): Unit = {
    overrideActivity = Some(Activities.IDLE)
    Terrain.onTerrainChanged(name, () => {
      Terrain.stopOnTerrainChanged(name)
      overrideActivity = None
      // try again when terrain updated again
      moveTowardsEquipment(use)
    })
  }
}