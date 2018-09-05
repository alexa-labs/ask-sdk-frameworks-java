/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.mapper;

import com.amazon.ask.model.Slot;
import com.amazon.ask.interaction.mapper.slot.AmazonNumberParser;
import com.amazon.ask.interaction.types.slot.AmazonNumber;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AmazonNumberParserTest {
    private final AmazonNumberParser underTest = new AmazonNumberParser();

    @Test
    public void testParse() throws SlotValueParseException {
        AmazonNumber number = underTest.read(null, slot("1"));
        assertEquals(new AmazonNumber(slot("1"), 1), number);
    }

    @Test(expected = IntentParseException.class)
    public void testInvalid() throws IntentParseException {
        underTest.read(null, slot("value"));
    }

    private Slot slot(String value) {
        return Slot.builder().withName("test").withValue(value).build();
    }

    @Test
    public void testLong() throws IntentParseException {
        assertEquals(new AmazonNumber(slot("1"), 1L), underTest.read(null, slot("1")));
        assertEquals(new AmazonNumber(slot(Long.toString(Long.MAX_VALUE)), Long.MAX_VALUE), underTest.read(null, slot(Long.toString(Long.MAX_VALUE))));
        assertEquals(new AmazonNumber(slot(Long.toString(Long.MIN_VALUE)), Long.MIN_VALUE), underTest.read(null, slot(Long.toString(Long.MIN_VALUE))));
    }

    @Test(expected = IntentParseException.class)
    public void testLongOverflow() throws IntentParseException {
        underTest.read(null, slot(new BigDecimal(Long.MAX_VALUE).add(new BigDecimal(1)).toPlainString()));
    }

    @Test(expected = IntentParseException.class)
    public void testLongUnderflow() throws IntentParseException {
        underTest.read(null, slot(new BigDecimal(Long.MIN_VALUE).subtract(new BigDecimal(1)).toPlainString()));
    }
}
