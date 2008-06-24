package org.jetbrains.plugins.scala.lang.psi.impl.statements

import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiElementImpl
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns._
import org.jetbrains.plugins.scala.lang.psi.api.base._
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.TokenSet
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType;
import com.intellij.psi._
import org.jetbrains.annotations._
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.psi.api.statements._
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns._
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScBindingPattern

/** 
* @author Alexander Podkhalyuzin
* Date: 22.02.2008
* Time: 9:55:42
*/

class ScPatternDefinitionImpl(node: ASTNode) extends ScMemberImpl(node) with ScPatternDefinition {
  override def toString: String = "ScPatternDefinition"

  def bindings: Seq[ScBindingPattern] = {
    val plist = findChildByClass(classOf[ScPatternList])
    if (plist != null) plist.patterns.flatMap[ScBindingPattern]((p: ScPattern) => p.bindings) else Seq.empty
  }

  def declaredElements = bindings

  override def processDeclarations(processor: PsiScopeProcessor,
      state : ResolveState,
      lastParent: PsiElement,
      place: PsiElement): Boolean = {
    import org.jetbrains.plugins.scala.lang.resolve._

    for (b <- bindings) if (!processor.execute(b, state)) return false
    true
  }

}