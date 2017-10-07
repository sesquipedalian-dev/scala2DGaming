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

import org.sesquipedalian_dev.scala2DGaming.graphics.{HasSingleWorldSpriteRendering, HasWorldSpriteRendering}
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.equipment.Equipment
import org.sesquipedalian_dev.scala2DGaming.entities.terrain.Terrain
import org.sesquipedalian_dev.scala2DGaming.game.HasGameUpdate
import org.sesquipedalian_dev.util._

trait HasMovingToward extends HasGameUpdate with Logging {
  def name: String
  val speed: Float // units / sec
  var location: Location
  var direction: List[Location] = Nil // list of way points we're moving to

  def worldSize: Location = Location(Main.WORLD_WIDTH, Main.WORLD_HEIGHT)

  def update(deltaTimeSeconds: Double): Unit = {
    trace"HasMovingToward update entry $deltaTimeSeconds $direction"
    direction.headOption.foreach(d => {
      // get vector from here to waypoint
      val targetX = d.x - location.x
      val targetY = d.y - location.y

      // normalize direction to unit length
      val vLength = Math.sqrt(Math.pow(targetX, 2) + Math.pow(targetY, 2))
      val deltaX = (targetX * speed * deltaTimeSeconds / vLength).toFloat
      val deltaY = (targetY * speed * deltaTimeSeconds / vLength).toFloat

      if(deltaX > 1 || deltaY > 1) {
        // don't allow jumping multi grid locations on one update - we probably had a graphics hitch
        return
      }

      // go new direction instead
      val newX = location.x + deltaX
      val newY = location.y + deltaY
      trace"going diff direction: $d $speed $deltaTimeSeconds $location $newX $newY"
      location = Location(Math.max(0, Math.min(worldSize.x - 1, newX)), Math.max(0, Math.min(worldSize.y - 1, newY)))

      // checking if we got to way point
      trace"checking HasMovingToward got there $location $d"
      if(location == d) {
        // got there
        direction = direction.tail
      }
    })
  }

  def noPathToEquipment(): Unit = {

  }

  def moveTowardsEquipment[A <: Equipment](use: (A) => Unit)(implicit mf: Manifest[A]): Unit = {
    val turretsByRange = Registry.objects[HasWorldSpriteRendering](HasWorldSpriteRendering.tag).collect({case gun: A => {
      gun -> (Math.abs(gun.location.x - location.x) + Math.abs(gun.location.y - location.y))
    }}).sortBy(_._2)

    if(turretsByRange.nonEmpty) {
      val turretCloseEnoughToUse = turretsByRange.find(p => p._2 <= p._1.useRange && p._1.user.isEmpty)
      if(turretCloseEnoughToUse.nonEmpty) {
        use(turretCloseEnoughToUse.get._1)
      } else {
        val selectedEquipment = turretsByRange.head._1
        val targetTurret = turretsByRange.head._1.location
        // adjust this by the usable range
        selectedEquipment.useRange
        // first location is pathfinder target
        // second location is getting ever so slightly closer to the thing to make sure we can use it
        var potentialUseLocs: List[(Location, Location)] = Nil

        if(Terrain.isTraversable(Location(Math.ceil(targetTurret.x - selectedEquipment.useRange).toFloat, targetTurret.y))) {
          potentialUseLocs :+= (
            Location(Math.ceil(targetTurret.x - selectedEquipment.useRange).toFloat, targetTurret.y),
            Location(targetTurret.x - selectedEquipment.useRange + .1f, targetTurret.y)
          )
        }
        if(Terrain.isTraversable(Location(Math.floor(targetTurret.x + selectedEquipment.useRange).toFloat, targetTurret.y))) {
          potentialUseLocs :+= (
            Location(Math.floor(targetTurret.x + selectedEquipment.useRange).toFloat, targetTurret.y),
            Location(targetTurret.x + selectedEquipment.useRange - .1f, targetTurret.y)
          )
        }
        if(Terrain.isTraversable(Location(targetTurret.x, Math.ceil(targetTurret.y - selectedEquipment.useRange).toFloat))) {
          potentialUseLocs :+= (
            Location(targetTurret.x, Math.ceil(targetTurret.y - selectedEquipment.useRange).toFloat),
            Location(targetTurret.x, targetTurret.y - selectedEquipment.useRange + .1f)
          )
        }
        if(Terrain.isTraversable(Location(targetTurret.x, Math.floor(targetTurret.y + selectedEquipment.useRange).toFloat))) {
          potentialUseLocs :+= (
            Location(targetTurret.x, Math.floor(targetTurret.y + selectedEquipment.useRange).toFloat),
            Location(targetTurret.x, targetTurret.y + selectedEquipment.useRange - .1f)
          )
        }

        if(potentialUseLocs.isEmpty) {
          info"no path to destination 1 for $name ${turretsByRange.head._1} $targetTurret"
          noPathToEquipment()
        } else {
          val targetUseLoc = Main.random.map(_.shuffle(potentialUseLocs).head).getOrElse(potentialUseLocs.head)

          val path = Terrain.findPath(location, targetUseLoc._1)
          trace"testing pathfinding algo: $location to $targetUseLoc = $path"
          path match {
            case Some(p) => {
              direction = p :+ targetUseLoc._2
              Terrain.onTerrainChanged(name, () => {
                direction = Nil // re-trigger pathfinding behaviour
                Terrain.stopOnTerrainChanged(name)
              })
            }
            case _ => {
              info"no path to destination for $name ${turretsByRange.head._1} $targetUseLoc"
              noPathToEquipment()
            }
          }
        }
      }
    }
  }
}
