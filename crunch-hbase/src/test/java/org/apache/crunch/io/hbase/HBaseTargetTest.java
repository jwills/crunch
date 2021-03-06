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
package org.apache.crunch.io.hbase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.crunch.Target;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Test;

public class HBaseTargetTest {

  @Test
  public void testConfigureForMapReduce() throws IOException {
    Job job = Job.getInstance();
    // Add the test config file. We can't just call job.getConfiguration().set() because
    // setting a configuration with .set will always to precedence.
    job.getConfiguration().addResource("test-hbase-conf.xml");

    HBaseTarget target = new HBaseTarget("testTable");
    target.configureForMapReduce(job, HBaseTypes.keyValues(), new Path("/"), "name");

    assertEquals("12345", job.getConfiguration().get("hbase.client.scanner.timeout.period"));
  }

  @Test
  public void testEquality() {
    Target target = new HBaseTarget("testTable");
    Target target2 = new HBaseTarget("testTable");

    assertEquals(target, target2);
    assertEquals(target.hashCode(), target2.hashCode());
  }

  @Test
  public void testEqualityWithExtraConf() {
    Target target = new HBaseTarget("testTable").outputConf("key", "value");
    Target target2 = new HBaseTarget("testTable").outputConf("key", "value");

    assertEquals(target, target2);
    assertEquals(target.hashCode(), target2.hashCode());
  }

  @Test
  public void testInequality() {
    Target target = new HBaseTarget("testTable");
    Target target2 = new HBaseTarget("testTable2");

    assertThat(target, is(not(target2)));
    assertThat(target.hashCode(), is(not(target2.hashCode())));
  }

  @Test
  public void testInequalityWithExtraConf() {
    Target target = new HBaseTarget("testTable").outputConf("key", "value");
    Target target2 = new HBaseTarget("testTable").outputConf("key", "value2");

    assertThat(target, is(not(target2)));
    assertThat(target.hashCode(), is(not(target2.hashCode())));
  }
}
