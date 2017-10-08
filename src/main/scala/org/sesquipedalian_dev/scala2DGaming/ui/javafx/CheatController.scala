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
import org.sesquipedalian_dev.scala2DGaming.cheatgrammar.{CheatGrammarBaseListener, CheatGrammarLexer, CheatGrammarListener, CheatGrammarParser}
import org.sesquipedalian_dev.scala2DGaming.game.Commander
import org.sesquipedalian_dev.util._

class RealCheatGrammarListener extends CheatGrammarBaseListener with Logging {
  override def exitChangeGMU(ctx: CheatGrammarParser.ChangeGMUContext) = {
    Commander.setMoney(ctx.IntAmt().getText().toInt)
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
