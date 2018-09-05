/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.stubs;

import com.amazon.ask.model.Slot;
import com.amazon.ask.model.SlotConfirmationStatus;
import com.amazon.ask.interaction.annotation.data.AlexaIgnore;
import com.amazon.ask.interaction.annotation.data.SlotPropertyReader;
import com.amazon.ask.interaction.annotation.type.SlotType;
import com.amazon.ask.interaction.mapper.slot.*;

import java.util.Objects;

/**
 *
 */
@SlotType
public class TestCustom {
    @SlotPropertyReader(SlotNameReader.class)
    private String name;

    @SlotPropertyReader(SlotValueReader.class)
    private String value;

    // slot
    private Slot slot;

    @AlexaIgnore
    private Slot ignoredSlot;

    @SlotPropertyReader(RawSlotPropertyReader.class)
    private Object explicitSlot;

    // slot confirmation status
    private SlotConfirmationStatus slotConfirmationStatus;

    @SlotPropertyReader(SlotConfirmationStatusReader.class)
    private Object explicitSlotConfirmationStatus;

    // resolutions
    private com.amazon.ask.model.slu.entityresolution.Resolutions resolutions;

    @SlotPropertyReader(ResolutionsReader.class)
    private Object explicitResolutions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Slot getIgnoredSlot() {
        return ignoredSlot;
    }

    public void setIgnoredSlot(Slot ignoredSlot) {
        this.ignoredSlot = ignoredSlot;
    }

    public Object getExplicitSlot() {
        return explicitSlot;
    }

    public void setExplicitSlot(Object explicitSlot) {
        this.explicitSlot = explicitSlot;
    }

    public SlotConfirmationStatus getSlotConfirmationStatus() {
        return slotConfirmationStatus;
    }

    public void setSlotConfirmationStatus(SlotConfirmationStatus slotConfirmationStatus) {
        this.slotConfirmationStatus = slotConfirmationStatus;
    }

    public Object getExplicitSlotConfirmationStatus() {
        return explicitSlotConfirmationStatus;
    }

    public void setExplicitSlotConfirmationStatus(Object explicitSlotConfirmationStatus) {
        this.explicitSlotConfirmationStatus = explicitSlotConfirmationStatus;
    }

    public com.amazon.ask.model.slu.entityresolution.Resolutions getResolutions() {
        return resolutions;
    }

    public void setResolutions(com.amazon.ask.model.slu.entityresolution.Resolutions resolutions) {
        this.resolutions = resolutions;
    }

    public Object getExplicitResolutions() {
        return explicitResolutions;
    }

    public void setExplicitResolutions(Object explicitResolutions) {
        this.explicitResolutions = explicitResolutions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCustom that = (TestCustom) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(value, that.value) &&
            Objects.equals(slot, that.slot) &&
            Objects.equals(ignoredSlot, that.ignoredSlot) &&
            Objects.equals(explicitSlot, that.explicitSlot) &&
            slotConfirmationStatus == that.slotConfirmationStatus &&
            Objects.equals(explicitSlotConfirmationStatus, that.explicitSlotConfirmationStatus) &&
            Objects.equals(resolutions, that.resolutions) &&
            Objects.equals(explicitResolutions, that.explicitResolutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, slot, ignoredSlot, explicitSlot, slotConfirmationStatus, explicitSlotConfirmationStatus, resolutions, explicitResolutions);
    }

    @Override
    public String toString() {
        return "TestCustom{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", slot=" + slot +
            ", ignoredSlot=" + ignoredSlot +
            ", explicitSlot=" + explicitSlot +
            ", slotConfirmationStatus=" + slotConfirmationStatus +
            ", explicitSlotConfirmationStatus=" + explicitSlotConfirmationStatus +
            ", resolutions=" + resolutions +
            ", explicitResolutions=" + explicitResolutions +
            '}';
    }
}
