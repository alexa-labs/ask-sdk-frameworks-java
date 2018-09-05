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
 * Utterances that combined to just a specific week (such as “this week” or “next week”), convert a date indicating the
 * week number: 2015-W49.
 */
public class WeekDate extends AmazonDate {
    private final int year;
    private final int week;

    public WeekDate(Slot slot, int year, int week) {
        super(slot);
        checkArgument(year >= 0, "year must be >= 0");
        checkArgument(week >= 0, "week must be >= 0");
        this.year = year;
        this.week = week;
    }

    public int getYear() {
        return year;
    }

    public int getWeek() {
        return week;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        WeekDate weekDate = (WeekDate) o;
        return year == weekDate.year && week == weekDate.week;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), year, week);
    }

    @Override
    public String toString() {
        return "WeekDate{" + "year=" + year + ", week=" + week + ", slot=" + getSlot() + '}';
    }
}