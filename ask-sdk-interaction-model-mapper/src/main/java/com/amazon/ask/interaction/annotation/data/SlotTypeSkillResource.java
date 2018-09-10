/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.annotation.data;

import com.amazon.ask.interaction.Utils;
import com.amazon.ask.interaction.annotation.plugin.AutoSlotTypeData;
import com.amazon.ask.interaction.annotation.type.SlotType;
import com.amazon.ask.interaction.data.model.SlotTypeData;
import com.amazon.ask.interaction.data.source.Codec;
import com.amazon.ask.interaction.data.source.JsonCodec;
import com.amazon.ask.interaction.definition.SlotTypeDefinition;
import com.amazon.ask.interaction.model.InteractionModel;
import com.amazon.ask.interaction.model.InteractionModelEnvelope;
import com.amazon.ask.interaction.model.SlotTypeValue;
import com.amazon.ask.interaction.model.SlotValue;
import com.amazon.ask.interaction.renderer.RenderContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.*;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Associates a resource file containing {@link InteractionModelEnvelope} with a type annotated with {@link SlotType}.
 *
 * Using the slot type's name, its {@link SlotTypeData} is cherry-picked from the skill model file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@AutoSlotTypeData(SlotTypeSkillResource.Plugin.class)
@Repeatable(SlotTypeSkillResource.Container.class)
public @interface SlotTypeSkillResource {
    /**
     * @return resource name containing data.
     */
    String value();

    /**
     * @return suffix of resource file - defaults to .json.
     */
    String suffix() default ".json";

    /**
     * @return class to load resources from, defaults to the annotated class.
     */
    Class<?> resourceClass() default Object.class;

    /**
     * @return class of {@link Codec} to read {@link InteractionModelEnvelope} data
     */
    Class<? extends Codec<InteractionModelEnvelope>> codec() default DefaultCodec.class;

    /**
     * Parse the standard Skill Model JSON Schema, defined by {@link InteractionModelEnvelope}.
     */
    class DefaultCodec extends JsonCodec<InteractionModelEnvelope> {
        private static final ObjectMapper MAPPER = new ObjectMapper();
        public DefaultCodec() {
            super(MAPPER.readerFor(InteractionModelEnvelope.class));
        }
    }

    /**
     * Reads {@link SlotTypeData} from an {@link InteractionModel}
     *
     * Information for this slot type is cherry-picked from the interaction model.
     */
    class Plugin implements AutoSlotTypeData.Plugin<SlotTypeSkillResource> {
        @Override
        public Stream<SlotTypeData> apply(RenderContext<SlotTypeDefinition> context, SlotTypeSkillResource annotation) {
            /*
                Read this intent's data from the skill model
             */
            Codec<SlotTypeData> transformingCodec = stream -> read(context, stream, Utils.instantiate(annotation.codec()));

            SlotTypeData.Resource.Builder intentData = SlotTypeData.resource()
                .withName(annotation.value())
                .withSuffix(annotation.suffix())
                .withCodec(transformingCodec)
                .withResourceClass(annotation.resourceClass() == Object.class ? context.getValue().getSlotTypeClass() : annotation.resourceClass());

            return Stream.of(intentData.build()).map(s -> s.apply(context));
        }

        protected SlotTypeData read(RenderContext<SlotTypeDefinition> input, InputStream resource, Codec<InteractionModelEnvelope> codec) throws IOException {
            InteractionModel model = codec.read(resource).getInteractionModel();
            SlotTypeData.Builder slotMetadataBuilder = SlotTypeData.builder();

            if (model.getLanguageModel().getTypes() != null) {
                model
                    .getLanguageModel().getTypes().stream()
                    .filter(slotType -> slotType.getName().equals(input.getValue().getName()))
                    .findFirst()
                    .ifPresent(slotType -> {
                        for (SlotTypeValue slotTypeValue: slotType.getValues()) {
                            slotMetadataBuilder.addValue(
                                slotTypeValue.getId(),
                                SlotValue.builder()
                                    .withValue(slotTypeValue.getName().getValue())
                                    .withSynonyms(slotTypeValue.getName().getSynonyms())
                                    .build());

                        }
                    });
            }

            return slotMetadataBuilder.build();
        }
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @AutoSlotTypeData(Container.Plugin.class)
    @interface Container {
        SlotTypeSkillResource[] value();

        class Plugin implements AutoSlotTypeData.Plugin<Container> {
            private static final SlotTypeSkillResource.Plugin SINGLE = new SlotTypeSkillResource.Plugin();

            @Override
            public Stream<SlotTypeData> apply(RenderContext<SlotTypeDefinition> input, Container container) {
                return Arrays.stream(container.value()).flatMap(a -> SINGLE.apply(input, a));
            }
        }
    }
}
