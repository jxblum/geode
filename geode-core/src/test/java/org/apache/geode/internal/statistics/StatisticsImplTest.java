/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.statistics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Unit tests for {@link StatisticsImpl}.
 */
public class StatisticsImplTest {

  private Logger originalLogger;
  private StatisticsImpl stats;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void createStats() {
    originalLogger = StatisticsImpl.logger;

    StatisticsTypeImpl type = mock(StatisticsTypeImpl.class);
    when(type.getIntStatCount()).thenReturn(5);
    when(type.getDoubleStatCount()).thenReturn(5);
    when(type.getLongStatCount()).thenReturn(5);

    String textId = "";
    long numbericId = 0;
    long uniqueId = 0;
    int osStatFlags = 0;
    boolean atomicIncrements = false;
    StatisticsManager system = mock(StatisticsManager.class);

    stats = new LocalStatisticsImpl(type, textId, numbericId, uniqueId, atomicIncrements,
        osStatFlags, system);
  }

  @After
  public void tearDown() {
    StatisticsImpl.logger = originalLogger;
  }

  @Test
  public void invokeIntSuppliersShouldUpdateStats() {
    IntSupplier supplier1 = mock(IntSupplier.class);
    when(supplier1.getAsInt()).thenReturn(23);
    stats.setIntSupplier(4, supplier1);
    assertEquals(0, stats.invokeSuppliers());

    verify(supplier1).getAsInt();
    assertEquals(23, stats.getInt(4));
  }

  @Test
  public void invokeLongSuppliersShouldUpdateStats() {
    LongSupplier supplier1 = mock(LongSupplier.class);
    when(supplier1.getAsLong()).thenReturn(23L);
    stats.setLongSupplier(4, supplier1);
    assertEquals(0, stats.invokeSuppliers());

    verify(supplier1).getAsLong();
    assertEquals(23L, stats.getLong(4));
  }

  @Test
  public void invokeDoubleSuppliersShouldUpdateStats() {
    DoubleSupplier supplier1 = mock(DoubleSupplier.class);
    when(supplier1.getAsDouble()).thenReturn(23.3);
    stats.setDoubleSupplier(4, supplier1);
    assertEquals(0, stats.invokeSuppliers());

    verify(supplier1).getAsDouble();
    assertEquals(23.3, stats.getDouble(4), 0.1f);
  }

  @Test
  public void getSupplierCountShouldReturnCorrectCount() {
    IntSupplier supplier1 = mock(IntSupplier.class);
    stats.setIntSupplier(4, supplier1);
    assertEquals(1, stats.getSupplierCount());
  }

  @Test
  public void invokeSuppliersShouldCatchSupplierErrorsAndReturnCount() {
    IntSupplier supplier1 = mock(IntSupplier.class);
    when(supplier1.getAsInt()).thenThrow(NullPointerException.class);
    stats.setIntSupplier(4, supplier1);
    assertEquals(1, stats.invokeSuppliers());

    verify(supplier1).getAsInt();
  }

  @Test
  public void invokeSuppliersShouldLogErrorOnlyOnce() {
    Logger logger = mock(Logger.class);
    StatisticsImpl.logger = logger;
    IntSupplier supplier1 = mock(IntSupplier.class);
    when(supplier1.getAsInt()).thenThrow(NullPointerException.class);
    stats.setIntSupplier(4, supplier1);
    assertEquals(1, stats.invokeSuppliers());

    // String message, Object p0, Object p1, Object p2
    verify(logger, times(1)).warn(anyString(), isNull(), anyInt(), isA(NullPointerException.class));

    assertEquals(1, stats.invokeSuppliers());

    // Make sure the logger isn't invoked again
    verify(logger, times(1)).warn(anyString(), isNull(), anyInt(), isA(NullPointerException.class));
  }

  @Test
  public void badSupplierParamShouldThrowError() {
    IntSupplier supplier1 = mock(IntSupplier.class);
    when(supplier1.getAsInt()).thenReturn(23);
    thrown.expect(IllegalArgumentException.class);
    stats.setIntSupplier(23, supplier1);
  }
}
