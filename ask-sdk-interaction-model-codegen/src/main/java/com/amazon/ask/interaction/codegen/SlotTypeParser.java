/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.codegen;

import com.amazon.ask.interaction.model.SlotType;
import com.amazon.ask.interaction.model.SlotTypeValue;
import com.amazon.ask.interaction.data.model.SlotTypeData;
import com.amazon.ask.interaction.definition.SlotTypeDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.amazon.ask.interaction.codegen.Utils.isAmazon;

/**
 *
 */
public class SlotTypeParser {
    public Map<SlotTypeDefinition, SlotTypeData> parse(LocalizedInteractionModel model) {
        Map<SlotTypeDefinition, SlotTypeData> results = new HashMap<>();

        Map<String, SlotTypeDefinition> seenTypes = new HashMap<>();

        Collection<SlotType> slotTypes = model.getInteractionModelEnvelope().getInteractionModel().getLanguageModel().getTypes();
        if (slotTypes != null) {
            for (SlotType slotType : slotTypes) {
                SlotTypeDefinition type = parseType(slotType);
                if (seenTypes.containsKey(type.getName())) {
                    if (!seenTypes.get(type.getName()).equals(type)) {
                        throw new IllegalArgumentException("Duplicate slot types don't have matching definitions: " + seenTypes.get(slotType.getName()) + " does not match " + slotType);
                    }
                }
                seenTypes.put(slotType.getName(), type);

                SlotTypeData data = parseData(slotType);
                if (results.containsKey(type)) {
                    data = SlotTypeData.combine(results.get(type), data);
                }
                results.put(type, data);
            }
        }
        return results;
    }


    public SlotTypeDefinition parseType(SlotType slotType) {
        return SlotTypeDefinition.builder()
            .withSlotTypeClass(Object.class)
            .withName(slotType.getName())
            .withCustom(!isAmazon(slotType.getName()))
            .build();
    }

    public SlotTypeData parseData(SlotType slotType) {
        SlotTypeData.Builder builder = SlotTypeData.builder();
        for (SlotTypeValue value : slotType.getValues()) {
            String id = value.getId();
            if (id == null) {
                id = value.getName().getValue();
            }
            builder.addValue(id, value.getName());
        }
        return builder.build();
    }
}
