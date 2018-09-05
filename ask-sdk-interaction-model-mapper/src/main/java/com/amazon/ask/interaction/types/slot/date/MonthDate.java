/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.types.slot.date;


import com.amazon.ask.model.Slot;

import java.util.Objects;

import static com.amazon.ask.interaction.Utils.checkArgument;

/**
 * Utterances that combined to a month, but not a specific day (such as “next month”, or “december”) convert to a date
 * add just the year and month: 2015-12.
 */
public class MonthDate extends AmazonDate {
    private final int year;
    private final int month;

    public MonthDate(Slot slot, int year, int month) {
        super(slot);
        checkArgument(year >= 0, "year must be >= 0");
        checkArgument(month >= 0, "month must be >= 0");
        this.year = year;
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        MonthDate monthDate = (MonthDate) o;
        return year == monthDate.year && month == monthDate.month;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), year, month);
    }

    @Override
    public String toString() {
        return "MonthDate{" + "year=" + year + ", month=" + month + ", slot=" + getSlot() + '}';
    }
}
