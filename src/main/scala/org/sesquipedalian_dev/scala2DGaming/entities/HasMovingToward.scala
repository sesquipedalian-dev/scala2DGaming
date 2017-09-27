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

import org.sesquipedalian_dev.scala2DGaming.graphics.HasWorldSpriteRendering
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.equipment.Equipment
import org.sesquipedalian_dev.scala2DGaming.game.HasGameUpdate
import org.sesquipedalian_dev.util._

trait HasMovingToward extends HasGameUpdate with Logging {
  def name: String
  val speed: Float // units / sec
  var location: Location
  var direction: Option[Location] = None

  def worldSize: Location = Location(Main.WORLD_WIDTH, Main.WORLD_HEIGHT)

  def update(deltaTimeSeconds: Double): Unit = {
    trace"HasMovingToward update entry $deltaTimeSeconds $direction"
    direction.foreach(targetDirection => {
      val deltaX = (targetDirection.x * speed * deltaTimeSeconds.toFloat)
      val deltaY = (targetDirection.y * speed * deltaTimeSeconds.toFloat)

      if(deltaX > 1 || deltaY > 1) {
        // don't allow jumping multi grid locations on one update - we probably had a graphics hitch
        return
      }

      // check if moving 1 in the direction would be traversable
      val oneTileX = if(targetDirection.x < 0) {
        Math.ceil(location.x).toFloat + targetDirection.x
      } else {
        Math.floor(location.x).toFloat + targetDirection.x
      }
      val oneTileY = if(targetDirection.y < 0) {
        Math.ceil(location.y).toFloat + targetDirection.y
      } else {
        Math.floor(location.y).toFloat + targetDirection.y
      }
      val oneTileInDirection = Location(oneTileX, oneTileY)

      // if we move into a new grid location, check if we can move into that block
      // if not, we have to move around an obstacle
      val traversable = WorldMap.singleton.flatMap(world => {
        val tile = world.worldLocations.get(oneTileInDirection)
        trace"tile is traversable $tile"
        tile.map(_.traversable)
      }).getOrElse(false)

      trace"moving a guy! $name $location $oneTileInDirection $traversable"

      if(!traversable) {
        // ok, we can't go that way - move the closest direction to where we were trying to go that is traversable

        val directions = List( // cardinal directions we could move
          Location(1, 0),  // +x
          Location(0, 1),  // +y
          Location(-1, 0), // -x
          Location(0, -1)  // -y
        ).filter(_ != targetDirection)
        val (d, _) = directions
          .map(d => { // figure out whether each direction is traversable
            // check if moving 1 in the direction would be traversable
            val oneTileNewDX = if(d.x < 0) {
              Math.ceil(location.x).toFloat + d.x
            } else {
              Math.floor(location.x).toFloat + d.x
            }
            val oneTileNewDY = if(d.y < 0) {
              Math.ceil(location.y).toFloat + d.y
            } else {
              Math.floor(location.y).toFloat + d.y
            }
            val oneTileInNewDirection = Location(oneTileNewDX, oneTileNewDY)
            val traversable = WorldMap.singleton.flatMap(world => {
              world.worldLocations.get(oneTileInNewDirection).map(_.traversable)
            }).getOrElse(false)
            (d, traversable)
          })
          .filter(p => {
            trace"valid alternate direction? $p"
            p._2
          }) // only pick a direction that is traversable
          .sortBy(p => {
          val angle = Math.atan2(p._1.y, p._1.x)
          val angleDiff = Math.abs(p._1.x - targetDirection.x) + Math.abs(p._1.y - targetDirection.y)
          trace"sorting alternate direction $p by $angleDiff"
          angleDiff
        }) // pick the direction that is closest to where we were already trying to go
          .head

        // go new direction instead
        val newX = location.x + (d.x * speed * deltaTimeSeconds)
        val newY = location.y + (d.y * speed * deltaTimeSeconds)
        trace"going diff direction: $d $speed $deltaTimeSeconds $location $newX $newY"
        location = Location(Math.max(0, Math.min(worldSize.x - 1, newX)).toFloat, Math.max(0, Math.min(worldSize.y - 1, newY)).toFloat)
      } else {
        location = Location(
          Math.max(0, Math.min(worldSize.x - 1, location.x + deltaX)),
          Math.max(0, Math.min(worldSize.y - 1, location.y + deltaY))
        )
      }
    })
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
        val targetTurret = turretsByRange.head._1.location
        val targetDir = Location(targetTurret.x - location.x, targetTurret.y - location.y)
        val angle = Math.atan2(targetDir.y, targetDir.x)
        val normalX = Math.cos(angle).toFloat
        val normalY = Math.sin(angle).toFloat
        direction = Some(Location(normalX, normalY))
      }
    }
  }
}
