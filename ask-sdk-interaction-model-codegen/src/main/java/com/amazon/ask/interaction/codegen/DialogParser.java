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

import com.amazon.ask.interaction.model.*;
import com.amazon.ask.interaction.data.model.IntentData;
import com.amazon.ask.interaction.data.model.IntentSlotData;
import com.amazon.ask.interaction.definition.IntentDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class DialogParser {
    public Map<IntentDefinition, IntentData> parse(LocalizedInteractionModel model, Collection<IntentDefinition> intents) {
        InteractionModel interactionModel = model.getInteractionModelEnvelope().getInteractionModel();
        Map<String, IntentDefinition> intentDefinitions = intents.stream().collect(Collectors.toMap(
            IntentDefinition::getName,
            Function.identity()));

        Map<IntentDefinition, IntentData> results = new HashMap<>();
        if (interactionModel.getDialog() != null && interactionModel.getPrompts() != null) {
            Dialog dialog = interactionModel.getDialog();
            Map<String, Prompt> prompts = interactionModel.getPrompts().stream().collect(Collectors.toMap(
                Prompt::getId,
                prompt -> prompt));

            for (DialogIntent intent : dialog.getIntents()) {
                IntentDefinition intentDefinition = intentDefinitions.get(intent.getName());
                if (intentDefinition == null) {
                    throw new IllegalArgumentException("Dialog intent points to a non-existent intent: " + intent.getName());
                }

                IntentData data = parseDialog(intent, prompts, intentDefinitions.get(intent.getName()));
                if (results.containsKey(intentDefinition)) {
                    data = IntentData.combine(results.get(intentDefinition), data);
                }
                results.put(intentDefinition, data);
            }
        }
        return results;
    }

    protected IntentData parseDialog(DialogIntent intent, Map<String, Prompt> prompts, IntentDefinition intentDefinition) {
        if (intentDefinition == null) {
            throw new IllegalArgumentException("Dialog Intent '" + intent.getName() + "' is not defined in the Interation Model");
        }
        Prompt prompt = prompts.get(intent.getPrompts().getConfirmation());
        if (prompt == null) {
            throw new IllegalArgumentException("Dialog Intent '" + intent.getName() + "' references missing confirmation prompt '" + intent.getPrompts().getConfirmation() + "'");
        }

        IntentData.Builder builder = IntentData.builder()
            .withPrompts(intent.getPrompts())
            .withConfirmationRequired(intent.getConfirmationRequired())
            .addConfirmations(prompt.getVariations());

        if (intent.getSlots() != null) {
            for (DialogSlot slot : intent.getSlots()) {
                Prompt confirmation = prompts.get(slot.getPrompts().getConfirmation());
                Prompt elicitation = prompts.get(slot.getPrompts().getElicitation());

                IntentSlotData.Builder slotBuilder = IntentSlotData.builder()
                    .withConfirmationRequired(slot.getConfirmationRequired())
                    .withElicitationRequired(slot.getElicitationRequired())
                    .withPrompts(slot.getPrompts());

                if (confirmation != null) {
                    slotBuilder.addConfirmations(confirmation.getVariations());
                }
                if (elicitation != null) {
                    slotBuilder.addElicitations(elicitation.getVariations());
                }

                builder.addSlot(slot.getName(), slotBuilder.build());
            }
        }

        return builder.build();
    }
}
