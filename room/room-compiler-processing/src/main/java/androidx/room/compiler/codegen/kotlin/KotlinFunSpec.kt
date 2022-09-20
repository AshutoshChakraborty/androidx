/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.compiler.codegen.kotlin

import androidx.room.compiler.codegen.VisibilityModifier
import androidx.room.compiler.codegen.XAnnotationSpec
import androidx.room.compiler.codegen.XCodeBlock
import androidx.room.compiler.codegen.XFunSpec
import androidx.room.compiler.codegen.toKTypeName
import androidx.room.compiler.processing.KnownTypeNames.KOTLIN_UNIT
import androidx.room.compiler.processing.XNullability
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.javapoet.JTypeName

internal class KotlinFunSpec(
    internal val actual: FunSpec
) : KotlinLang(), XFunSpec {

    internal class Builder(
        internal val actual: com.squareup.kotlinpoet.FunSpec.Builder
    ) : KotlinLang(), XFunSpec.Builder {

        override fun addCode(code: XCodeBlock) = apply {
            require(code is KotlinCodeBlock)
            actual.addCode(code.actual)
        }

        override fun addParameter(
            typeName: JTypeName,
            name: String,
            nullability: XNullability,
            annotations: List<XAnnotationSpec>
        ) = apply {
            actual.addParameter(
                ParameterSpec.builder(name, typeName.toKTypeName(nullability)).apply {
                    // TODO(b/247247439): Add other annotations
                }.build()
            )
        }

        override fun callSuperConstructor(vararg args: XCodeBlock) = apply {
            actual.callSuperConstructor(
                args.map {
                    check(it is KotlinCodeBlock)
                    it.actual
                }
            )
        }

        override fun returns(typeName: JTypeName, nullability: XNullability) = apply {
            if (typeName == com.squareup.javapoet.TypeName.VOID || typeName == KOTLIN_UNIT) {
                return@apply
            }
            actual.returns(typeName.toKTypeName(nullability))
        }

        override fun build() = KotlinFunSpec(actual.build())
    }
}

internal fun VisibilityModifier.toKotlinVisibilityModifier() = when (this) {
    VisibilityModifier.PUBLIC -> KModifier.PUBLIC
    VisibilityModifier.PROTECTED -> KModifier.PROTECTED
    VisibilityModifier.PRIVATE -> KModifier.PRIVATE
}