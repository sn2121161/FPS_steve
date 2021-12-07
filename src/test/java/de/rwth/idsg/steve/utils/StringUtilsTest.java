/*
 * SteVe - SteckdosenVerwaltung - https://github.com/RWTH-i5-IDSG/steve
 * Copyright (C) 2013-2021 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.utils;

import de.rwth.idsg.steve.ocpp.task.CancelReservationTask;
import de.rwth.idsg.steve.ocpp.task.ClearCacheTask;
import de.rwth.idsg.steve.ocpp.task.GetCompositeScheduleTask;
import de.rwth.idsg.steve.web.dto.ocpp.CancelReservationParams;
import de.rwth.idsg.steve.web.dto.ocpp.GetCompositeScheduleParams;
import de.rwth.idsg.steve.web.dto.ocpp.MultipleChargePointSelect;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 05.10.2021
 */
public class StringUtilsTest {

    @Test
    public void testOperationName_ocpp12andMultiple() {
        var operationName = StringUtils.getOperationName(new ClearCacheTask(null, new MultipleChargePointSelect()));
        Assert.assertEquals("Clear Cache", operationName);
    }

    @Test
    public void testOperationName_ocpp15andSingle() {
        var operationName = StringUtils.getOperationName(new CancelReservationTask(null, new CancelReservationParams(), null));
        Assert.assertEquals("Cancel Reservation", operationName);
    }

    @Test
    public void testOperationName_ocpp16() {
        var operationName = StringUtils.getOperationName(new GetCompositeScheduleTask(null, new GetCompositeScheduleParams()));
        Assert.assertEquals("Get Composite Schedule", operationName);
    }

    @Test
    public void testJoinByComma_inputNull() {
        String val = StringUtils.joinByComma(null);
        Assert.assertNull(val);
    }

    @Test
    public void testJoinByComma_inputEmpty() {
        String val = StringUtils.joinByComma(new ArrayList<>());
        Assert.assertNull(val);
    }

    @Test
    public void testJoinByComma_inputOneElement() {
        String val = StringUtils.joinByComma(Arrays.asList("hey"));
        Assert.assertEquals("hey", val);
    }

    @Test
    public void testJoinByComma_inputTwoElements() {
        String val = StringUtils.joinByComma(Arrays.asList("hey", "ho"));
        Assert.assertEquals("hey,ho", val);
    }

    @Test
    public void testJoinByComma_inputDuplicateElements() {
        String val = StringUtils.joinByComma(Arrays.asList("hey", "ho", "hey"));
        Assert.assertEquals("hey,ho", val);
    }


    @Test
    public void testSplitByComma_inputNull() {
        List<String> val = StringUtils.splitByComma(null);
        Assert.assertNotNull(val);
        Assert.assertTrue(val.isEmpty());
    }

    @Test
    public void testSplitByComma_inputEmpty() {
        List<String> val = StringUtils.splitByComma("");
        Assert.assertNotNull(val);
        Assert.assertTrue(val.isEmpty());
    }

    @Test
    public void testSplitByComma_inputOneElement() {
        List<String> val = StringUtils.splitByComma("1one");
        Assert.assertTrue(val.size() == 1);
        Assert.assertEquals("1one", val.get(0));
    }

    @Test
    public void testSplitByComma_inputTwoElements() {
        List<String> val = StringUtils.splitByComma("1one,2two");
        Assert.assertTrue(val.size() == 2);

        List<String> sortedVal = val.stream().sorted().collect(Collectors.toList());
        Assert.assertEquals("1one", sortedVal.get(0));
        Assert.assertEquals("2two", sortedVal.get(1));
    }
}
