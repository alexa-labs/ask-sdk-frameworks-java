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

import com.amazon.ask.interaction.model.InteractionModel;
import com.amazon.ask.interaction.model.InteractionModelEnvelope;

import java.util.Locale;

import static com.amazon.ask.util.ValidationUtils.assertNotNull;

/**
 * Associate a {@link InteractionModel} add a {@link Locale}
 */
public class LocalizedInteractionModel {
    private final InteractionModelEnvelope interactionModelEnvelope;
    private final Locale locale;

    public LocalizedInteractionModel(InteractionModelEnvelope interactionModelEnvelope, Locale locale) {
        this.interactionModelEnvelope = assertNotNull(interactionModelEnvelope, "skillModel");
        this.locale = assertNotNull(locale, "locale");
    }

    public InteractionModelEnvelope getInteractionModelEnvelope() {
        return interactionModelEnvelope;
    }

    public Locale getLocale() {
        return locale;
    }
}
