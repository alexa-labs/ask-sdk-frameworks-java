/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.renderer;

import com.amazon.ask.interaction.model.*;
import com.amazon.ask.interaction.data.model.IntentData;
import com.amazon.ask.interaction.data.model.IntentSlotData;
import com.amazon.ask.interaction.definition.IntentDefinition;
import com.amazon.ask.interaction.definition.SlotTypeDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntentSlotRendererTest {
    private final Set<PromptVariation> confirmations = Collections.singleton(
        PromptVariation.builder()
            .withType("PlainText")
            .withValue("confirmation")
            .build()
    );

    private final Set<PromptVariation> elicitations = Collections.singleton(
        PromptVariation.builder()
            .withType("PlainText")
            .withValue("elicit")
            .build()
    );

    @Mock
    private IntentDefinition mockIntentDefinition;

    @Mock
    private SlotTypeDefinition mockSlotTypeDefinition;

    @Mock
    private IntentSlotData mockSlotMetadata;

    IntentSlotRenderer underTest = new IntentSlotRenderer();

    @Before
    public void before() {
        when(mockIntentDefinition.getName()).thenReturn("intent_name");

        when(mockSlotTypeDefinition.getName()).thenReturn("slot_type");

        when(mockSlotMetadata.getConfirmationRequired()).thenReturn(null);
        when(mockSlotMetadata.getElicitationRequired()).thenReturn(null);
        when(mockSlotMetadata.getConfirmations()).thenReturn(Collections.emptySet());
        when(mockSlotMetadata.getElicitations()).thenReturn(Collections.emptySet());
    }

    @Test
    public void testRenderSlot_NoData() {
        Slot expected = Slot.builder()
            .withName("slot_name")
            .withType("slot_type")
            .withSamples(null)
            .build();

        Slot actual = underTest.renderSlot("slot_name", mockSlotTypeDefinition, IntentData.builder().build());

        assertEquals(expected, actual);
    }

    @Test
    public void testRenderSlot_AddSamples() {
        Slot expected = Slot.builder()
            .withName("slot_name")
            .withType("slot_type")
            .withSamples(Collections.singletonList("test"))
            .build();

        Slot actual = underTest.renderSlot("slot_name", mockSlotTypeDefinition, IntentData.builder()
            .addSlot("slot_name", IntentSlotData.builder()
                .addSamples(Collections.singletonList("test"))
                .build())
            .build());

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderDialogSlot_NullIntentDefinition() {
        underTest.renderDialogSlot(null, "slot_name", mockSlotTypeDefinition, mockSlotMetadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderDialogSlot_NullSlotName() {
        underTest.renderDialogSlot(mockIntentDefinition, null, mockSlotTypeDefinition, mockSlotMetadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderDialogSlot_NullSlotTypeDefinition() {
        underTest.renderDialogSlot(mockIntentDefinition, "slot_name", null, mockSlotMetadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderDialogSlot_NullSlotMetadata() {
        underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, null);
    }

    @Test
    public void testRenderDialogSlot_SlotName() {
        assertEquals(
            "slot_name",
            underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getName());
    }

    @Test
    public void testRenderDialogSlot_SlotType() {
        assertEquals(
            "slot_type",
            underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getType());
    }

    @Test
    public void testRenderDialogSlot_NullConfirmationRequired() {
        DialogSlot dialog = underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata);
        assertNull(dialog.getConfirmationRequired());
        assertNull(dialog.getPrompts().getConfirmation());
    }

    @Test
    public void testRenderDialogSlot_ConfirmationRequired() {
        when(mockSlotMetadata.getConfirmationRequired()).thenReturn(true);
        assertTrue(underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getConfirmationRequired());
        when(mockSlotMetadata.getConfirmationRequired()).thenReturn(false);
        assertFalse(underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getConfirmationRequired());
    }

    @Test
    public void testRenderDialogSlot_NullElicitationRequired() {
        DialogSlot dialog = underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata);
        assertNull(dialog.getElicitationRequired());
        assertNull(dialog.getPrompts().getElicitation());
    }

    @Test
    public void testRenderDialogSlot_ElicitationRequired() {
        when(mockSlotMetadata.getElicitationRequired()).thenReturn(true);
        assertTrue(underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getElicitationRequired());
        when(mockSlotMetadata.getElicitationRequired()).thenReturn(false);
        assertFalse(underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getElicitationRequired());
    }

    @Test
    public void testRenderDialogSlot_GeneratedConfirmationPromptId() {
        when(mockSlotMetadata.getConfirmations()).thenReturn(confirmations);

        assertEquals(
            "Confirm.Intent-intent_name.IntentSlot-slot_name",
            underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getPrompts().getConfirmation()
        );
    }

    @Test
    public void testRenderDialogSlot_ExplicitConfirmationPromptId() {
        when(mockSlotMetadata.getConfirmations()).thenReturn(confirmations);
        when(mockSlotMetadata.getPrompts()).thenReturn(DialogSlotPrompt.builder()
            .withConfirmation("explicit.confirmation")
            .build());

        assertEquals(
            "explicit.confirmation",
            underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getPrompts().getConfirmation()
        );
    }

    @Test
    public void testRenderDialogSlot_GeneratedElicitationPromptId() {
        when(mockSlotMetadata.getElicitations()).thenReturn(elicitations);

        assertEquals(
            "Elicit.Intent-intent_name.IntentSlot-slot_name",
            underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getPrompts().getElicitation()
        );
    }

    @Test
    public void testRenderDialogSlot_ExplicitElicitationPromptId() {
        when(mockSlotMetadata.getElicitations()).thenReturn(elicitations);
        when(mockSlotMetadata.getPrompts()).thenReturn(DialogSlotPrompt.builder()
            .withElicitation("explicit.elicitation")
            .build());

        assertEquals(
            "explicit.elicitation",
            underTest.renderDialogSlot(mockIntentDefinition, "slot_name", mockSlotTypeDefinition, mockSlotMetadata).getPrompts().getElicitation()
        );
    }

    ///

    @Test(expected = IllegalArgumentException.class)
    public void testRenderSlotPrompts_NullIntentDefinition() {
        underTest.renderSlotPrompts(null, "slot_name", mockSlotMetadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderSlotPrompts_NullSlotName() {
        underTest.renderSlotPrompts(mockIntentDefinition, null, mockSlotMetadata);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderSlotPrompts_NullSlotMetadata() {
        underTest.renderSlotPrompts(mockIntentDefinition, "slot_name", null);
    }

    @Test
    public void testRenderSlotPrompts_EmptyMetadata() {
        assertEquals(0, underTest.renderSlotPrompts(mockIntentDefinition, "slot_name", mockSlotMetadata).size());
    }

    @Test
    public void testRenderSlotPrompts_GeneratedConfirmationPromptId() {
        when(mockSlotMetadata.getConfirmations()).thenReturn(confirmations);

        List<Prompt> prompts = underTest.renderSlotPrompts(mockIntentDefinition, "slot_name", mockSlotMetadata);

        assertEquals(prompts, Collections.singletonList(
            Prompt.builder()
                .withId("Confirm.Intent-intent_name.IntentSlot-slot_name")
                .withVariations(new ArrayList<>(confirmations))
                .build()
        ));
    }

    @Test
    public void testRenderSlotPrompts_ExplicitConfirmationPromptId() {
        when(mockSlotMetadata.getConfirmations()).thenReturn(confirmations);
        when(mockSlotMetadata.getPrompts()).thenReturn(DialogSlotPrompt.builder()
            .withConfirmation("explicit.confirmation")
            .build());

        List<Prompt> prompts = underTest.renderSlotPrompts(mockIntentDefinition, "slot_name", mockSlotMetadata);

        assertEquals(prompts, Collections.singletonList(
            Prompt.builder()
                .withId("explicit.confirmation")
                .withVariations(new ArrayList<>(confirmations))
                .build()
        ));
    }

    @Test
    public void testRenderSlotPrompts_GeneratedElicitationPromptId() {
        when(mockSlotMetadata.getConfirmations()).thenReturn(confirmations);

        List<Prompt> prompts = underTest.renderSlotPrompts(mockIntentDefinition, "slot_name", mockSlotMetadata);

        assertEquals(prompts, Collections.singletonList(
            Prompt.builder()
                .withId("Confirm.Intent-intent_name.IntentSlot-slot_name")
                .withVariations(new ArrayList<>(confirmations))
                .build()
        ));
    }

    @Test
    public void testRenderSlotPrompts_ExplicitElicitationPromptId() {
        when(mockSlotMetadata.getElicitations()).thenReturn(elicitations);
        when(mockSlotMetadata.getPrompts()).thenReturn(DialogSlotPrompt.builder()
            .withElicitation("explicit.elicitation")
            .build());

        List<Prompt> prompts = underTest.renderSlotPrompts(mockIntentDefinition, "slot_name", mockSlotMetadata);

        assertEquals(prompts, Collections.singletonList(
            Prompt.builder()
                .withId("explicit.elicitation")
                .withVariations(new ArrayList<>(elicitations))
                .build()
        ));
    }
}
