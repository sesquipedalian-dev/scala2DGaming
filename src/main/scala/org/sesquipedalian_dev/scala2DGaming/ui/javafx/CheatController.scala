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
package org.sesquipedalian_dev.scala2DGaming.ui.javafx

import java.io.ByteArrayInputStream
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control.{Button, TextField}
import javafx.scene.text.Text

import org.antlr.v4.runtime.tree.{ErrorNode, ParseTreeWalker, TerminalNode}
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream, ParserRuleContext, RecognitionException}
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.Main.{WORLD_HEIGHT, WORLD_WIDTH}
import org.sesquipedalian_dev.scala2DGaming.cheatgrammar.{CheatGrammarBaseListener, CheatGrammarLexer, CheatGrammarListener, CheatGrammarParser}
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.entities.enemies.{BadGuy, BadGuySpawner}
import org.sesquipedalian_dev.scala2DGaming.entities.soldiers.GoodGuy
import org.sesquipedalian_dev.scala2DGaming.game.{Commander, TimeOfDay}
import org.sesquipedalian_dev.scala2DGaming.graphics.HasWorldSpriteRendering
import org.sesquipedalian_dev.util._

import scala.util.Random

class RealCheatGrammarListener extends CheatGrammarBaseListener with Logging {
  override def exitChangeGMU(ctx: CheatGrammarParser.ChangeGMUContext) = {
    Commander.setMoney(ctx.IntAmt().getText().toInt)
  }

  override def exitChangeSoldierNeed(ctx: CheatGrammarParser.ChangeSoldierNeedContext): Unit = {
    val soldierName = ctx.ID().getText()
    val needName = ctx.NeedName().getText()

    HasWorldSpriteRendering.all.foreach({
      case s: GoodGuy if s.name == soldierName => s.needs.foreach({
        case n if n.name == needName => n.degree = Math.max(0, Math.min(ctx.IntAmt().getText().toInt, 100))
        case _ =>
      })
      case _ =>
    })
  }

  override def exitSpawnBadGuy(ctx: CheatGrammarParser.SpawnBadGuyContext): Unit = {
    val amt = Option(ctx.IntAmt()).map(_.getText().toInt).getOrElse(1)
    for{i <- 1 to amt} {
      val targetY = Main.random.map(r => r.nextInt(Main.WORLD_HEIGHT)).getOrElse(25)
      new BadGuy(Location(0, 25), Some(Location(Main.WORLD_WIDTH - 1, targetY)), Location(WORLD_WIDTH, WORLD_HEIGHT), 50)
    }
  }

  override def exitSpawnWave(ctx: CheatGrammarParser.SpawnWaveContext): Unit = {
    new BadGuySpawner(Location(0, 25), 4f, Main.random.getOrElse(Random).nextInt(2) + 10)
  }

  override def exitSetTimeRate(ctx: CheatGrammarParser.SetTimeRateContext): Unit = {
    val amt = Option(ctx.FloatAmt()).map(_.getText.toFloat) orElse
    Option(ctx.IntAmt()).map(_.getText.toFloat) getOrElse
    0.0f
    TimeOfDay.singleton.foreach(_.speed = amt)
  }
}

class CheatController(cheatText: TextField, cheatButton: Button, cheatErrorText: Text) extends Logging {
  val listener = new RealCheatGrammarListener()

  cheatButton.setOnAction(new EventHandler[ActionEvent]() {
    override def handle(event: ActionEvent) = {
      val cheat = cheatText.getText()
      info"Doing cheat $cheat"
      if(cheat.nonEmpty) {
        try {
          val input = new ANTLRInputStream(new ByteArrayInputStream(cheat.getBytes))
          val lexer = new CheatGrammarLexer(input)
          val tokenStream = new CommonTokenStream(lexer)
          val parser = new CheatGrammarParser(tokenStream)
          val tree = parser.cheat()
          val walker = new ParseTreeWalker()
          walker.walk(new RealCheatGrammarListener, tree)
        } catch {
          case x: RecognitionException => {
            cheatErrorText.setText(s"Cheat parser exception: $x ${x.getMessage}")
          }
        }
      }
    }
  })
}
