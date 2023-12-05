// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.model.psi.PsiExternalReferenceHost
import com.intellij.psi.PsiElement
import org.angular2.codeInsight.blocks.Angular2HtmlBlockParameterSymbol
import org.angular2.lang.html.psi.Angular2HtmlBlock

interface Angular2BlockParameter : Angular2EmbeddedExpression, PsiExternalReferenceHost {

  override fun getName(): String?

  val block: Angular2HtmlBlock?

  val definition: Angular2HtmlBlockParameterSymbol?

  val isPrimaryExpression: Boolean

  val index: Int

  val nameElement: PsiElement?

  val expression: JSExpression?

  val variables: List<JSVariable>

}