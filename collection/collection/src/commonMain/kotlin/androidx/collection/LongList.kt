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
@file:Suppress("NOTHING_TO_INLINE", "RedundantVisibilityModifier")
@file:OptIn(ExperimentalContracts::class)

package androidx.collection

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmField

/**
 * [LongList] is a [List]-like collection for [Long] values. It allows retrieving
 * the elements without boxing. [LongList] is always backed by a [MutableLongList],
 * its [MutableList]-like subclass.
 *
 * This implementation is not thread-safe: if multiple threads access this
 * container concurrently, and one or more threads modify the structure of
 * the list (insertion or removal for instance), the calling code must provide
 * the appropriate synchronization. It is also not safe to mutate during reentrancy --
 * in the middle of a [forEach], for example. However, concurrent reads are safe.
 */
public sealed class LongList(initialCapacity: Int) {
    @JvmField
    @PublishedApi
    internal var content: LongArray = if (initialCapacity == 0) {
        EmptyLongArray
    } else {
        LongArray(initialCapacity)
    }

    @Suppress("PropertyName")
    @JvmField
    @PublishedApi
    internal var _size: Int = 0

    /**
     * The number of elements in the [LongList].
     */
    @get:androidx.annotation.IntRange(from = 0)
    public val size: Int
        get() = _size

    /**
     * Returns the last valid index in the [LongList]. This can be `-1` when the list is empty.
     */
    @get:androidx.annotation.IntRange(from = -1)
    public inline val lastIndex: Int get() = _size - 1

    /**
     * Returns an [IntRange] of the valid indices for this [LongList].
     */
    public inline val indices: IntRange get() = 0 until _size

    /**
     * Returns `true` if the collection has no elements in it.
     */
    public fun none(): Boolean {
        return isEmpty()
    }

    /**
     * Returns `true` if there's at least one element in the collection.
     */
    public fun any(): Boolean {
        return isNotEmpty()
    }

    /**
     * Returns `true` if any of the elements give a `true` return value for [predicate].
     */
    public inline fun any(predicate: (element: Long) -> Boolean): Boolean {
        contract { callsInPlace(predicate) }
        forEach {
            if (predicate(it)) {
                return true
            }
        }
        return false
    }

    /**
     * Returns `true` if any of the elements give a `true` return value for [predicate] while
     * iterating in the reverse order.
     */
    public inline fun reversedAny(predicate: (element: Long) -> Boolean): Boolean {
        contract { callsInPlace(predicate) }
        forEachReversed {
            if (predicate(it)) {
                return true
            }
        }
        return false
    }

    /**
     * Returns `true` if the [LongList] contains [element] or `false` otherwise.
     */
    public operator fun contains(element: Long): Boolean {
        forEach {
            if (it == element) {
                return true
            }
        }
        return false
    }

    /**
     * Returns `true` if the [LongList] contains all elements in [elements] or `false` if
     * one or more are missing.
     */
    public fun containsAll(elements: LongList): Boolean {
        for (i in elements.indices) {
            if (!contains(elements[i])) return false
        }
        return true
    }

    /**
     * Returns the number of elements in this list.
     */
    public fun count(): Int = _size

    /**
     * Counts the number of elements matching [predicate].
     * @return The number of elements in this list for which [predicate] returns true.
     */
    public inline fun count(predicate: (element: Long) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        var count = 0
        forEach { if (predicate(it)) count++ }
        return count
    }

    /**
     * Returns the first element in the [LongList] or throws a [NoSuchElementException] if
     * it [isEmpty].
     */
    public fun first(): Long {
        if (isEmpty()) {
            throw NoSuchElementException("LongList is empty.")
        }
        return content[0]
    }

    /**
     * Returns the first element in the [LongList] for which [predicate] returns `true` or
     * throws [NoSuchElementException] if nothing matches.
     * @see indexOfFirst
     */
    public inline fun first(predicate: (element: Long) -> Boolean): Long {
        contract { callsInPlace(predicate) }
        forEach { item ->
            if (predicate(item)) return item
        }
        throw NoSuchElementException("LongList contains no element matching the predicate.")
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [LongList] in order.
     * @param initial The value of `acc` for the first call to [operation] or return value if
     * there are no elements in this list.
     * @param operation function that takes current accumulator value and an element, and
     * calculates the next accumulator value.
     */
    public inline fun <R> fold(initial: R, operation: (acc: R, element: Long) -> R): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEach { item ->
            acc = operation(acc, item)
        }
        return acc
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [LongList] in order.
     */
    public inline fun <R> foldIndexed(
        initial: R,
        operation: (index: Int, acc: R, element: Long) -> R
    ): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEachIndexed { i, item ->
            acc = operation(i, acc, item)
        }
        return acc
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [LongList] in reverse order.
     * @param initial The value of `acc` for the first call to [operation] or return value if
     * there are no elements in this list.
     * @param operation function that takes an element and the current accumulator value, and
     * calculates the next accumulator value.
     */
    public inline fun <R> foldRight(initial: R, operation: (element: Long, acc: R) -> R): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEachReversed { item ->
            acc = operation(item, acc)
        }
        return acc
    }

    /**
     * Accumulates values, starting with [initial], and applying [operation] to each element
     * in the [LongList] in reverse order.
     */
    public inline fun <R> foldRightIndexed(
        initial: R,
        operation: (index: Int, element: Long, acc: R) -> R
    ): R {
        contract { callsInPlace(operation) }
        var acc = initial
        forEachReversedIndexed { i, item ->
            acc = operation(i, item, acc)
        }
        return acc
    }

    /**
     * Calls [block] for each element in the [LongList], in order.
     * @param block will be executed for every element in the list, accepting an element from
     * the list
     */
    public inline fun forEach(block: (element: Long) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in 0 until _size) {
            block(content[i])
        }
    }

    /**
     * Calls [block] for each element in the [LongList] along with its index, in order.
     * @param block will be executed for every element in the list, accepting the index and
     * the element at that index.
     */
    public inline fun forEachIndexed(block: (index: Int, element: Long) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in 0 until _size) {
            block(i, content[i])
        }
    }

    /**
     * Calls [block] for each element in the [LongList] in reverse order.
     * @param block will be executed for every element in the list, accepting an element from
     * the list
     */
    public inline fun forEachReversed(block: (element: Long) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in _size - 1 downTo 0) {
            block(content[i])
        }
    }

    /**
     * Calls [block] for each element in the [LongList] along with its index, in reverse
     * order.
     * @param block will be executed for every element in the list, accepting the index and
     * the element at that index.
     */
    public inline fun forEachReversedIndexed(block: (index: Int, element: Long) -> Unit) {
        contract { callsInPlace(block) }
        val content = content
        for (i in _size - 1 downTo 0) {
            block(i, content[i])
        }
    }

    /**
     * Returns the element at the given [index] or throws [IndexOutOfBoundsException] if
     * the [index] is out of bounds of this collection.
     */
    public operator fun get(@androidx.annotation.IntRange(from = 0) index: Int): Long {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$lastIndex")
        }
        return content[index]
    }

    /**
     * Returns the element at the given [index] or throws [IndexOutOfBoundsException] if
     * the [index] is out of bounds of this collection.
     */
    public fun elementAt(@androidx.annotation.IntRange(from = 0) index: Int): Long {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$lastIndex")
        }
        return content[index]
    }

    /**
     * Returns the element at the given [index] or [defaultValue] if [index] is out of bounds
     * of the collection.
     * @param index The index of the element whose value should be returned
     * @param defaultValue A lambda to call with [index] as a parameter to return a value at
     * an index not in the list.
     */
    public inline fun elementAtOrElse(
        @androidx.annotation.IntRange(from = 0) index: Int,
        defaultValue: (index: Int) -> Long
    ): Long {
        if (index !in 0 until _size) {
            return defaultValue(index)
        }
        return content[index]
    }

    /**
     * Returns the index of [element] in the [LongList] or `-1` if [element] is not there.
     */
    public fun indexOf(element: Long): Int {
        forEachIndexed { i, item ->
            if (element == item) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the index if the first element in the [LongList] for which [predicate]
     * returns `true`.
     */
    public inline fun indexOfFirst(predicate: (element: Long) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        forEachIndexed { i, item ->
            if (predicate(item)) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the index if the last element in the [LongList] for which [predicate]
     * returns `true`.
     */
    public inline fun indexOfLast(predicate: (element: Long) -> Boolean): Int {
        contract { callsInPlace(predicate) }
        forEachReversedIndexed { i, item ->
            if (predicate(item)) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns `true` if the [LongList] has no elements in it or `false` otherwise.
     */
    public fun isEmpty(): Boolean = _size == 0

    /**
     * Returns `true` if there are elements in the [LongList] or `false` if it is empty.
     */
    public fun isNotEmpty(): Boolean = _size != 0

    /**
     * Returns the last element in the [LongList] or throws a [NoSuchElementException] if
     * it [isEmpty].
     */
    public fun last(): Long {
        if (isEmpty()) {
            throw NoSuchElementException("LongList is empty.")
        }
        return content[lastIndex]
    }

    /**
     * Returns the last element in the [LongList] for which [predicate] returns `true` or
     * throws [NoSuchElementException] if nothing matches.
     * @see indexOfLast
     */
    public inline fun last(predicate: (element: Long) -> Boolean): Long {
        contract { callsInPlace(predicate) }
        forEachReversed { item ->
            if (predicate(item)) {
                return item
            }
        }
        throw NoSuchElementException("LongList contains no element matching the predicate.")
    }

    /**
     * Returns the index of the last element in the [LongList] that is the same as
     * [element] or `-1` if no elements match.
     */
    public fun lastIndexOf(element: Long): Int {
        forEachReversedIndexed { i, item ->
            if (item == element) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns a hash code based on the contents of the [LongList].
     */
    override fun hashCode(): Int {
        var hashCode = 0
        forEach { element ->
            hashCode += 31 * element.hashCode()
        }
        return hashCode
    }

    /**
     * Returns `true` if [other] is a [LongList] and the contents of this and [other] are the
     * same.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is LongList || other._size != _size) {
            return false
        }
        val content = content
        val otherContent = other.content
        for (i in indices) {
            if (content[i] != otherContent[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Returns a String representation of the list, surrounded by "[]" and each element
     * separated by ", ".
     */
    override fun toString(): String {
        if (isEmpty()) {
            return "[]"
        }
        val last = lastIndex
        return buildString {
            append('[')
            val content = content
            for (i in 0 until last) {
                append(content[i])
                append(',')
                append(' ')
            }
            append(content[last])
            append(']')
        }
    }
}

/**
 * [MutableLongList] is a [MutableList]-like collection for [Long] values.
 * It allows storing and retrieving the elements without boxing. Immutable
 * access is available through its base class [LongList], which has a [List]-like
 * interface.
 *
 * This implementation is not thread-safe: if multiple threads access this
 * container concurrently, and one or more threads modify the structure of
 * the list (insertion or removal for instance), the calling code must provide
 * the appropriate synchronization. It is also not safe to mutate during reentrancy --
 * in the middle of a [forEach], for example. However, concurrent reads are safe.
 *
 * @constructor Creates a [MutableLongList] with a [capacity] of `initialCapacity`.
 */
public class MutableLongList(
    initialCapacity: Int = DefaultCapacity
) : LongList(initialCapacity) {
    /**
     * Returns the total number of elements that can be held before the [MutableLongList] must
     * grow.
     *
     * @see ensureCapacity
     */
    public inline val capacity: Int
        get() = content.size

    /**
     * Adds [element] to the [MutableLongList] and returns `true`.
     */
    public fun add(element: Long): Boolean {
        ensureCapacity(_size + 1)
        content[_size] = element
        _size++
        return true
    }

    /**
     * Adds [element] to the [MutableLongList] at the given [index], shifting over any
     * elements at [index] and after, if any.
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive
     */
    public fun add(@androidx.annotation.IntRange(from = 0) index: Int, element: Long) {
        if (index !in 0.._size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$_size")
        }
        ensureCapacity(_size + 1)
        val content = content
        if (index != _size) {
            content.copyInto(
                destination = content,
                destinationOffset = index + 1,
                startIndex = index,
                endIndex = _size
            )
        }
        content[index] = element
        _size++
    }

    /**
     * Adds all [elements] to the [MutableLongList] at the given [index], shifting over any
     * elements at [index] and after, if any.
     * @return `true` if the [MutableLongList] was changed or `false` if [elements] was empty
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive.
     */
    public fun addAll(
        @androidx.annotation.IntRange(from = 0) index: Int,
        elements: LongArray
    ): Boolean {
        if (index !in 0.._size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$_size")
        }
        if (elements.isEmpty()) return false
        ensureCapacity(_size + elements.size)
        val content = content
        if (index != _size) {
            content.copyInto(
                destination = content,
                destinationOffset = index + elements.size,
                startIndex = index,
                endIndex = _size
            )
        }
        elements.copyInto(content, index)
        _size += elements.size
        return true
    }

    /**
     * Adds all [elements] to the [MutableLongList] at the given [index], shifting over any
     * elements at [index] and after, if any.
     * @return `true` if the [MutableLongList] was changed or `false` if [elements] was empty
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive
     */
    public fun addAll(
        @androidx.annotation.IntRange(from = 0) index: Int,
        elements: LongList
    ): Boolean {
        if (index !in 0.._size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$_size")
        }
        if (elements.isEmpty()) return false
        ensureCapacity(_size + elements._size)
        val content = content
        if (index != _size) {
            content.copyInto(
                destination = content,
                destinationOffset = index + elements._size,
                startIndex = index,
                endIndex = _size
            )
        }
        elements.content.copyInto(
            destination = content,
            destinationOffset = index,
            startIndex = 0,
            endIndex = elements._size
        )
        _size += elements._size
        return true
    }

    /**
     * Adds all [elements] to the end of the [MutableLongList] and returns `true` if the
     * [MutableLongList] was changed or `false` if [elements] was empty.
     */
    public fun addAll(elements: LongList): Boolean {
        return addAll(_size, elements)
    }

    /**
     * Adds all [elements] to the end of the [MutableLongList] and returns `true` if the
     * [MutableLongList] was changed or `false` if [elements] was empty.
     */
    public fun addAll(elements: LongArray): Boolean {
        return addAll(_size, elements)
    }

    /**
     * Adds all [elements] to the end of the [MutableLongList].
     */
    public operator fun plusAssign(elements: LongList) {
        addAll(_size, elements)
    }

    /**
     * Adds all [elements] to the end of the [MutableLongList].
     */
    public operator fun plusAssign(elements: LongArray) {
        addAll(_size, elements)
    }

    /**
     * Removes all elements in the [MutableLongList]. The storage isn't released.
     * @see trim
     */
    public fun clear() {
        _size = 0
    }

    /**
     * Reduces the internal storage. If [capacity] is greater than [minCapacity] and [size], the
     * internal storage is reduced to the maximum of [size] and [minCapacity].
     * @see ensureCapacity
     */
    public fun trim(minCapacity: Int = _size) {
        val minSize = maxOf(minCapacity, _size)
        if (capacity > minSize) {
            content = content.copyOf(minSize)
        }
    }

    /**
     * Ensures that there is enough space to store [capacity] elements in the [MutableLongList].
     * @see trim
     */
    public fun ensureCapacity(capacity: Int) {
        val oldContent = content
        if (oldContent.size < capacity) {
            val newSize = maxOf(capacity, oldContent.size * 3 / 2)
            content = oldContent.copyOf(newSize)
        }
    }

    /**
     * [add] [element] to the [MutableLongList].
     */
    public inline operator fun plusAssign(element: Long) {
        add(element)
    }

    /**
     * [remove] [element] from the [MutableLongList]
     */
    public inline operator fun minusAssign(element: Long) {
        remove(element)
    }

    /**
     * Removes [element] from the [MutableLongList]. If [element] was in the [MutableLongList]
     * and was removed, `true` will be returned, or `false` will be returned if the element
     * was not found.
     */
    public fun remove(element: Long): Boolean {
        val index = indexOf(element)
        if (index >= 0) {
            removeAt(index)
            return true
        }
        return false
    }

    /**
     * Removes all [elements] from the [MutableLongList] and returns `true` if anything was removed.
     */
    public fun removeAll(elements: LongArray): Boolean {
        val initialSize = _size
        for (i in elements.indices) {
            remove(elements[i])
        }
        return initialSize != _size
    }

    /**
     * Removes all [elements] from the [MutableLongList] and returns `true` if anything was removed.
     */
    public fun removeAll(elements: LongList): Boolean {
        val initialSize = _size
        for (i in 0..elements.lastIndex) {
            remove(elements[i])
        }
        return initialSize != _size
    }

    /**
     * Removes all [elements] from the [MutableLongList].
     */
    public operator fun minusAssign(elements: LongArray) {
        elements.forEach { element ->
            remove(element)
        }
    }

    /**
     * Removes all [elements] from the [MutableLongList].
     */
    public operator fun minusAssign(elements: LongList) {
        elements.forEach { element ->
            remove(element)
        }
    }

    /**
     * Removes the element at the given [index] and returns it.
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [lastIndex], inclusive
     */
    public fun removeAt(@androidx.annotation.IntRange(from = 0) index: Int): Long {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("Index $index must be in 0..$lastIndex")
        }
        val content = content
        val item = content[index]
        if (index != lastIndex) {
            content.copyInto(
                destination = content,
                destinationOffset = index,
                startIndex = index + 1,
                endIndex = _size
            )
        }
        _size--
        return item
    }

    /**
     * Removes items from index [start] (inclusive) to [end] (exclusive).
     * @throws IndexOutOfBoundsException if [start] or [end] isn't between 0 and [size], inclusive
     * @throws IllegalArgumentException if [start] is greater than [end]
     */
    public fun removeRange(
        @androidx.annotation.IntRange(from = 0) start: Int,
        @androidx.annotation.IntRange(from = 0) end: Int
    ) {
        if (start !in 0.._size || end !in 0.._size) {
            throw IndexOutOfBoundsException("Start ($start) and end ($end) must be in 0..$_size")
        }
        if (end < start) {
            throw IllegalArgumentException("Start ($start) is more than end ($end)")
        }
        if (end != start) {
            if (end < _size) {
                content.copyInto(
                    destination = content,
                    destinationOffset = start,
                    startIndex = end,
                    endIndex = _size
                )
            }
            _size -= (end - start)
        }
    }

    /**
     * Keeps only [elements] in the [MutableLongList] and removes all other values.
     * @return `true` if the [MutableLongList] has changed.
     */
    public fun retainAll(elements: LongArray): Boolean {
        val initialSize = _size
        val content = content
        for (i in lastIndex downTo 0) {
            val item = content[i]
            if (elements.indexOfFirst { it == item } < 0) {
                removeAt(i)
            }
        }
        return initialSize != _size
    }

    /**
     * Keeps only [elements] in the [MutableLongList] and removes all other values.
     * @return `true` if the [MutableLongList] has changed.
     */
    public fun retainAll(elements: LongList): Boolean {
        val initialSize = _size
        val content = content
        for (i in lastIndex downTo 0) {
            val item = content[i]
            if (item !in elements) {
                removeAt(i)
            }
        }
        return initialSize != _size
    }

    /**
     * Sets the value at [index] to [element].
     * @return the previous value set at [index]
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [lastIndex], inclusive
     */
    public operator fun set(
        @androidx.annotation.IntRange(from = 0) index: Int,
        element: Long
    ): Long {
        if (index !in 0 until _size) {
            throw IndexOutOfBoundsException("set index $index must be between 0 .. $lastIndex")
        }
        val content = content
        val old = content[index]
        content[index] = element
        return old
    }

    /**
     * Sorts the [MutableLongList] elements in ascending order.
     */
    public fun sort() {
        content.sort(fromIndex = 0, toIndex = _size)
    }

    /**
     * Sorts the [MutableLongList] elements in descending order.
     */
    public fun sortDescending() {
        content.sortDescending(fromIndex = 0, toIndex = _size)
    }
}

@Suppress("ConstPropertyName")
private const val DefaultCapacity = 16

// Empty array used when nothing is allocated
@Suppress("PrivatePropertyName")
private val EmptyLongArray = LongArray(0)

/**
 * Creates and returns an empty [MutableLongList] with the default capacity.
 */
public inline fun mutableLongListOf(): MutableLongList = MutableLongList()

/**
 * Creates and returns a [MutableLongList] with the given values.
 */
public inline fun mutableLongListOf(vararg elements: Long): MutableLongList =
    MutableLongList(elements.size).also { it.addAll(elements) }
