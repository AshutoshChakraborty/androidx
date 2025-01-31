/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.compose.ui.focus

import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRestorerNode.Companion.FocusRestorerElement
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.Nodes
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireLayoutNode
import androidx.compose.ui.node.visitChildren
import androidx.compose.ui.platform.InspectorInfo

@Suppress("ConstPropertyName")
private const val PrevFocusedChild = "previouslyFocusedChildHash"

@ExperimentalComposeUiApi
internal fun FocusTargetNode.saveFocusedChild(): Boolean {
    if (!focusState.hasFocus) return false
    visitChildren(Nodes.FocusTarget) {
        if (it.focusState.hasFocus) {
            previouslyFocusedChildHash = it.requireLayoutNode().compositeKeyHash
            currentValueOf(LocalSaveableStateRegistry)
                ?.registerProvider(PrevFocusedChild) { previouslyFocusedChildHash }
            return true
        }
    }
    return false
}

@ExperimentalComposeUiApi
internal fun FocusTargetNode.restoreFocusedChild(): Boolean {
    if (previouslyFocusedChildHash == 0) {
        val savableStateRegistry = currentValueOf(LocalSaveableStateRegistry)
        savableStateRegistry?.consumeRestored(PrevFocusedChild)?.let {
            previouslyFocusedChildHash = it as Int
        }
    }
    if (previouslyFocusedChildHash == 0) return false
    visitChildren(Nodes.FocusTarget) {
        if (it.requireLayoutNode().compositeKeyHash == previouslyFocusedChildHash) {
            return it.restoreFocusedChild() || it.requestFocus()
        }
    }
    return false
}

// TODO: Move focusRestorer to foundation after saveFocusedChild and restoreFocusedChild are stable.
/**
 * This modifier can be uses to save and restore focus to a focus group.
 * When focus leaves the focus group, it stores a reference to the item that was previously focused.
 * Then when focus re-enters this focus group, it restores focus to the previously focused item.
 *
 * @sample androidx.compose.ui.samples.FocusRestorerSample
 */
@ExperimentalComposeUiApi
fun Modifier.focusRestorer(): Modifier = this then FocusRestorerElement

internal class FocusRestorerNode :
    FocusPropertiesModifierNode, FocusRequesterModifierNode, Modifier.Node() {
    private val onExit: (FocusDirection) -> FocusRequester = {
        @OptIn(ExperimentalComposeUiApi::class)
        saveFocusedChild()
        FocusRequester.Default
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private val onEnter: (FocusDirection) -> FocusRequester = {
        @OptIn(ExperimentalComposeUiApi::class)
        if (restoreFocusedChild()) FocusRequester.Cancel else FocusRequester.Default
    }
    override fun applyFocusProperties(focusProperties: FocusProperties) {
        @OptIn(ExperimentalComposeUiApi::class)
        focusProperties.enter = onEnter
        @OptIn(ExperimentalComposeUiApi::class)
        focusProperties.exit = onExit
    }

    companion object {
        val FocusRestorerElement = object : ModifierNodeElement<FocusRestorerNode>() {
            override fun create() = FocusRestorerNode()
            override fun update(node: FocusRestorerNode) {}
            override fun InspectorInfo.inspectableProperties() { name = "focusRestorer" }
            override fun hashCode(): Int = "focusRestorer".hashCode()
            override fun equals(other: Any?) = other === this
        }
    }
}
