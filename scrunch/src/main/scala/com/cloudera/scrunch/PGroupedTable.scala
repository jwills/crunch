/**
 * Copyright (c) 2011, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.scrunch

import com.cloudera.crunch.{DoFn, Emitter, FilterFn, MapFn}
import com.cloudera.crunch.{CombineFn, PGroupedTable => JGroupedTable, PTable => JTable, Pair => JPair}
import java.lang.{Iterable => JIterable}
import scala.collection.{Iterable, Iterator}
import Conversions._

class PGroupedTable[K, V](val native: JGroupedTable[K, V])
    extends PCollectionLike[JPair[K, JIterable[V]], PGroupedTable[K, V], JGroupedTable[K, V]] {
  import PGroupedTable._

  def filter(f: (K, Iterable[V]) => Boolean) = {
    ClosureCleaner.clean(f)
    parallelDo(filterFn[K, V](f), native.getPType())
  }

  def map[T, To](f: (K, Iterable[V]) => T)
      (implicit pt: PTypeH[T], b: CanParallelTransform[T, To]): To = {
    b(this, mapFn[K, V, T](f), pt.getPType(getTypeFamily()))
  }

  def flatMap[T, To](f: (K, Iterable[V]) => Traversable[T])
      (implicit pt: PTypeH[T], b: CanParallelTransform[T, To]): To = {
    b(this, flatMapFn[K, V, T](f), pt.getPType(getTypeFamily()))
  }

  def combine(f: Iterable[V] => V) = combineValues(new IterableCombineFn[K, V](f))

  def combineValues(fn: CombineFn[K, V]) = new PTable[K, V](native.combineValues(fn))

  def ungroup() = new PTable[K, V](native.ungroup())
  
  def wrap(newNative: AnyRef): PGroupedTable[K, V] = {
    new PGroupedTable[K, V](newNative.asInstanceOf[JGroupedTable[K, V]])
  }
}

class IterableCombineFn[K, V](f: Iterable[V] => V) extends CombineFn[K, V] {
  ClosureCleaner.clean(f)
  override def process(input: JPair[K, JIterable[V]], emitfn: Emitter[JPair[K, V]]) = {
    val v = s2c(f(new ConversionIterable[V](input.second()))).asInstanceOf[V]
    emitfn.emit(JPair.of(input.first(), v))
  }
}

trait SFilterGroupedFn[K, V] extends FilterFn[JPair[K, JIterable[V]]] with Function2[K, Iterable[V], Boolean] {
  override def accept(input: JPair[K, JIterable[V]]): Boolean = {
    apply(c2s(input.first()).asInstanceOf[K], new ConversionIterable[V](input.second()))
  }
}

trait SDoGroupedFn[K, V, T] extends DoFn[JPair[K, JIterable[V]], T] with Function2[K, Iterable[V], Traversable[T]] {
  override def process(input: JPair[K, JIterable[V]], emitter: Emitter[T]): Unit = {
    for (v <- apply(c2s(input.first()).asInstanceOf[K], new ConversionIterable[V](input.second()))) {
      emitter.emit(s2c(v).asInstanceOf[T])
    }
  }
}

trait SMapGroupedFn[K, V, T] extends MapFn[JPair[K, JIterable[V]], T] with Function2[K, Iterable[V], T] {
  override def map(input: JPair[K, JIterable[V]]): T = {
    s2c(apply(c2s(input.first()).asInstanceOf[K], new ConversionIterable[V](input.second()))).asInstanceOf[T]
  }
}

object PGroupedTable {
  def filterFn[K, V](fn: (K, Iterable[V]) => Boolean) = {
    ClosureCleaner.clean(fn)
    new SFilterGroupedFn[K, V] { def apply(k: K, v: Iterable[V]) = fn(k, v) }
  }

  def flatMapFn[K, V, T](fn: (K, Iterable[V]) => Traversable[T]) = {
    ClosureCleaner.clean(fn)
    new SDoGroupedFn[K, V, T] { def apply(k: K, v: Iterable[V]) = fn(k, v) }
  }

  def mapFn[K, V, T](fn: (K, Iterable[V]) => T) = {
    ClosureCleaner.clean(fn)
    new SMapGroupedFn[K, V, T] { def apply(k: K, v: Iterable[V]) = fn(k, v) }
  }
}