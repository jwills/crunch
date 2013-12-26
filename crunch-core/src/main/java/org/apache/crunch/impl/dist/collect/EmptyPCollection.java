/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.crunch.impl.dist.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.crunch.ReadableData;
import org.apache.crunch.impl.dist.DistributedPipeline;
import org.apache.crunch.types.PType;

import java.util.List;

public class EmptyPCollection<T> extends PCollectionImpl<T> {

  private final PType<T> ptype;

  public EmptyPCollection(DistributedPipeline pipeline, PType<T> ptype) {
    super("EMPTY", pipeline);
    this.ptype = Preconditions.checkNotNull(ptype);
  }

  @Override
  protected void acceptInternal(Visitor visitor) {
    // No-op
  }

  @Override
  public List<PCollectionImpl<?>> getParents() {
    return ImmutableList.of();
  }

  @Override
  protected ReadableData<T> getReadableDataInternal() {
    return new EmptyReadableData<T>();
  }

  @Override
  protected long getSizeInternal() {
    return 0;
  }

  @Override
  public long getLastModifiedAt() {
    return 0;
  }

  @Override
  public PType<T> getPType() {
    return ptype;
  }

}
