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
package org.sesquipedalian_dev.scala2DGaming.entities.terrain

import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.{CanBuild, Location}
import org.sesquipedalian_dev.scala2DGaming.graphics.{HasSingleWorldSpriteRendering, HasWorldSpriteRendering}
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.pathfinding.Pathfinder.NodeID
import org.sesquipedalian_dev.util.pathfinding.{Adapter, Pathfinder}

trait Terrain extends HasSingleWorldSpriteRendering {
  def traversable: Boolean
}

trait CanBuildTerrain extends CanBuild {
  abstract override def buildOn(location: Location): Unit = {
    super.buildOn(location)
    val existingTerrain = HasWorldSpriteRendering.all.collect({
      case x: HasSingleWorldSpriteRendering with Terrain if x.location == location => x
    })
    existingTerrain.foreach(t => HasWorldSpriteRendering.unregister(t))

    Terrain.informTerrainChanged()
  }
}

class TerrainPathfinderAdapter(val terrain: List[Terrain]) extends Adapter {
  override def getNodeCount() = terrain.size

  override def distance(n1: NodeID, n2: NodeID) = {
    val loc1 = terrain(n1).location
    val loc2 = terrain(n2).location

    Math.sqrt(Math.pow(Math.abs(loc1.x - loc2.x), 2) + Math.pow(Math.abs(loc1.y - loc2.y), 2)).toFloat
  }

  // adapted from https://github.com/lapinozz/Lazy-Theta-with-optimization-any-angle-pathfinding/blob/master/tileadaptor.hpp
  override def lineOfSight(n1: NodeID, n2: NodeID): Boolean = {
    // This line of sight check uses only integer values. First it checks whether the movement along the x or the y axis is longer and moves along the longer
    // one cell by cell. dx and dy specify how many cells to move in each direction. Suppose dx is longer and we are moving along the x axis. For each
    // cell we pass in the x direction, we increase variable f by dy, which is initially 0. When f >= dx, we move along the y axis and set f -= dx. This way,
    // after dx movements along the x axis, we also move dy moves along the y axis.
    var l1: Location = terrain(n1).location
    var l2: Location = terrain(n2).location

    var diff: Location = Location(l2.x - l1.x, l2.y - l2.y)

    var f: Int = 0

    var dir: Location = Location(0, 0) // Direction of movement. Value can be either 1 or -1.

    // The x and y locations correspond to nodes, not cells. We might need to check different surrounding cells depending on the direction we do the
    // line of sight check. The following values are used to determine which cell to check to see if it is unblocked.
    var offset: Location = Location(0, 0)

    if(diff.y < 0) {
      diff = diff.copy(y = -diff.y)
      dir = dir.copy(y = -1)
      offset = offset.copy(y = 0) // Cell is to the North
    } else {
      dir = dir.copy(y = 1)
      offset = offset.copy(y = 1) // Cell is to the South
    }

    if(dir.x < 0) {
      diff = diff.copy(x = -diff.x)
      dir = dir.copy(x = -1)
      offset = offset.copy(x = 0)
    } else {
      dir = dir.copy(x = 1)
      offset = offset.copy(x = 1)
    }


    // Move along the x axis and increment/decrement y when f >= diff.x.
    if(diff.x >= diff.y) {
      while(l1.x != l2.x) {
        f += Math.round(diff.y)
        if(f >= diff.x) {  // We are changing rows, we might need to check two cells this iteration.
          if(!isTraversable(Location(l1.x + offset.x, l1.y + offset.y))) {
            return false
          }

          l1 = l1.copy(y = l1.y + dir.y)
          f -= Math.round(diff.x)
        }

        if(f != 0 && !isTraversable(Location(l1.x + offset.x, l1.y + offset.y))) {
          return false
        }

        // If we are moving along a horizontal line, either the north or the south cell should be unblocked.
        if(diff.y == 0 && !isTraversable(Location(l1.x + offset.x, l1.y)) && !isTraversable(Location(l1.x + offset.x, l1.y + 1))) {
          return false
        }

        l1 = l1.copy(x = l1.x + dir.x)
      }
    } else { //if (diff.x < diff.y). Move along the y axis and increment/decrement x when f >= diff.y.
      while(l1.y != l2.y) {
        f += Math.round(diff.x)
        if(f >= diff.y) {
          if(!isTraversable(Location(l1.x + offset.x, l1.y + offset.y))) {
            return false
          }

          l1 = l1.copy(x = l1.x + dir.x)
          f -= Math.round(diff.y)
        }

        if(f != 0 && !isTraversable(Location(l1.x + offset.x, l1.y + offset.y))) {
          return false
        }

        if(diff.x == 0 && !isTraversable(l1.copy(y = l1.y + offset.y)) && !isTraversable(l1.copy(x = l1.x + 1, l1.y + offset.y))) {
          return false
        }

        l1 = l1.copy(y = l1.y + dir.y)
      }
    }

    // we made it to the other node!
    true
  }

  override def getNodeNeighbors(n: NodeID) = {
    val loc = terrain(n).location

    var neighborLocs: List[Location] = Nil

    if(loc.x != 0) {
      neighborLocs :+= Location(loc.x - 1, loc.y)
    }

    if(loc.x != Main.WORLD_WIDTH - 1) {
      neighborLocs :+= Location(loc.x + 1, loc.y)
    }

    if(loc.y != 0) {
      neighborLocs :+= Location(loc.x, loc.y - 1)
    }

    if(loc.y != Main.WORLD_HEIGHT - 1) {
      neighborLocs :+= Location(loc.x, loc.y + 1)
    }

    val validLocs = neighborLocs.filter(isTraversable)

    neighborLocs.map(l => terrain.indexWhere(_.location == l))
      .filter(_ >= 0)
      .map(index => (index -> 1.0f)) // everything only costs 1 to go between since it's a grid
  }

  def isTraversable(location: Location): Boolean = {
    // only the top left corners of non-traversable tiles are non-traversable
    terrain.find(t => t.location == location).map(_.traversable).getOrElse(false)
  }
}

object Terrain {
  var adapter: Option[TerrainPathfinderAdapter] = None
  var pathfinder: Option[Pathfinder] = None
  var suppressPathfinderUpdate: Boolean = false
  def informTerrainChanged(): Unit = {
    if(!suppressPathfinderUpdate) {
      val terrainNodes = HasWorldSpriteRendering.all.collect({
        case x: Terrain => x
      })

      adapter = Some(new TerrainPathfinderAdapter(terrainNodes))
      pathfinder = Some(new Pathfinder(adapter.get))
    }
  }

  def bulkUpdateTerrain(block: () => Unit): Unit = {
    suppressPathfinderUpdate = true
    block()
    suppressPathfinderUpdate = false
    informTerrainChanged()
  }

  def idToLocation(id: NodeID): Location = {
    adapter.map(_.terrain(id).location).getOrElse(Location(0, 0))
  }

  def locationToId(location: Location): NodeID = {
    adapter.map(_.terrain.indexWhere(_.location == location)).filter(_ >= 0).getOrElse(0)
  }

  def findPath(start: Location, goal: Location): Option[List[Location]] = {
    pathfinder.flatMap(p => {
      p.search[Location](start, goal, idToLocation _, locationToId _)
    })
  }
}