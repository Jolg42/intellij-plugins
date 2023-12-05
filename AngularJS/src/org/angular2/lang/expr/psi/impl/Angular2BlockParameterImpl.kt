// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.parentOfType
import org.angular2.codeInsight.blocks.Angular2HtmlBlockParameterSymbol
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.impl.Angular2BindingImpl.Companion.getExpression
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockParameterImpl(elementType: IElementType?) : Angular2EmbeddedExpressionImpl(elementType), Angular2BlockParameter {
  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2ElementVisitor) {
      visitor.visitAngular2BlockParameter(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun getName(): String? = nameElement?.text

  override val block: Angular2HtmlBlock?
    get() = parentOfType<Angular2HtmlBlock>()

  override val definition: Angular2HtmlBlockParameterSymbol?
    get() = block?.definition?.parameters?.let { definitions ->
      if (isPrimaryExpression)
        definitions.find { it.isPrimaryExpression }
      else {
        val name = name
        definitions.find { it.name == name }
      }
    }

  override val index: Int
    get() = block?.parameters?.indexOf(this) ?: 0

  override val isPrimaryExpression: Boolean
    get() = firstChild is JSExpression || firstChild is JSStatement

  override val nameElement: PsiElement?
    get() = firstChild
      .let { if (it is PsiErrorElement) it.nextSibling else it }
      ?.takeIf { it !is JSExpression && it !is JSStatement }

  override val expression: JSExpression?
    get() = getExpression(this)

  override val variables: List<JSVariable>
    get() = children.mapNotNull { it as? JSVarStatement }
      .flatMap { it.variables.asSequence() }
}