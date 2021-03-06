package org.jetbrains.plugins.scala.codeInsight.intentions.controlflow

import org.jetbrains.plugins.scala.codeInsight.intentions.ScalaIntentionTestBase
import org.jetbrains.plugins.scala.codeInsight.intention.controlflow.MergeIfToOrIntention

/**
 * @author Ksenia.Sautina
 * @since 6/6/12
 */

class MergeIfToOrIntentionTest extends ScalaIntentionTestBase {
  val familyName = MergeIfToOrIntention.familyName

  def testMergeIfToOr() {
    val text  = """
                  |class MergeIfToOr {
                  |  def mthd {
                  |    val a: Int = 0
                  |    i<caret>f (a == 9) {
                  |    } else if (a == 7) {
                  |    }
                  |  }
                  |}
                """.stripMargin.replace("\r", "").trim
    val resultText = """
                       |class MergeIfToOr {
                       |  def mthd {
                       |    val a: Int = 0
                       |    <caret>if (a == 9 || a == 7) {
                       |    }
                       |  }
                       |}
                     """.stripMargin.replace("\r", "").trim

    doTest(text, resultText)
  }

  def testMergeIfToOr2() {
    val text  = """
                  |class MergeIfToOr {
                  |  def mthd {
                  |    val a: Int = 0
                  |    i<caret>f (a == 9) {
                  |      System.out.println("if")
                  |    } else if (a == 7) {
                  |      System.out.println("if")
                  |    } else {
                  |      System.out.println("else")
                  |    }
                  |  }
                  |}
                """.stripMargin.replace("\r", "").trim
    val resultText = """
                       |class MergeIfToOr {
                       |  def mthd {
                       |    val a: Int = 0
                       |    <caret>if (a == 9 || a == 7) {
                       |      System.out.println("if")
                       |    } else {
                       |      System.out.println("else")
                       |    }
                       |  }
                       |}
                     """.stripMargin.replace("\r", "").trim

    doTest(text, resultText)
  }

  def testMergeIfToOr3() {
    val text  = """
                  |class MergeIfToOr {
                  |  def mthd {
                  |    val a: Int = 0
                  |      i<caret>f (a == 9)
                  |        System.out.println("if")
                  |      else if (a == 7)
                  |        System.out.println("if")
                  |  }
                  |}
                """.stripMargin.replace("\r", "").trim
    val resultText = """
                       |class MergeIfToOr {
                       |  def mthd {
                       |    val a: Int = 0
                       |    <caret>if (a == 9 || a == 7) System.out.println("if")
                       |  }
                       |}
                     """.stripMargin.replace("\r", "").trim

    doTest(text, resultText)
  }

  def testMergeIfToOr4() {
    val text  = """
                  |class MergeIfToOr {
                  |  def mthd {
                  |    val a: Int = 0
                  |      i<caret>f (a == 9)
                  |        System.out.println("if")
                  |      else if (a == 7)
                  |        System.out.println("if")
                  |      else if (a == 19)
                  |        System.out.println("if")
                  |      else
                  |        System.out.println("if")
                  |  }
                  |}
                """.stripMargin.replace("\r", "").trim
    val resultText = """
                       |class MergeIfToOr {
                       |  def mthd {
                       |    val a: Int = 0
                       |    <caret>if (a == 9 || a == 7) System.out.println("if")
                       |    else if (a == 19)
                       |      System.out.println("if")
                       |    else
                       |      System.out.println("if")
                       |  }
                       |}
                     """.stripMargin.replace("\r", "").trim

    doTest(text, resultText)
  }
}