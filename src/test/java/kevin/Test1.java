/*
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

package kevin;

import org.junit.Test;

import hu.rxd.kevin.mirobo.MiroboClient;
import hu.rxd.kevin.mirobo.MiroboClient.MiRoboStatus;
import hu.rxd.kevin.mirobo.MiroboClient.StateKey;
import hu.rxd.kevin.prometheus.PromMetricsServer;

public class Test1 {
  @Test
  public void asd() throws Exception {

    MiRoboStatus st = MiroboClient.status();
    System.out.println(st);

    PromMetricsServer pms = new PromMetricsServer(16701);
    st.getVals().put(StateKey.State, null);
    pms.pushValues(st);

    try {
      Thread.sleep(100000);
    } catch (InterruptedException e) {
      throw new RuntimeException();

    }
    throw new RuntimeException("Unimplemented!");

  }

}
