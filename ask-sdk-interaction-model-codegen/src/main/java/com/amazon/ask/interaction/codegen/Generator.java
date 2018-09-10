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

import com.amazon.ask.interaction.data.model.IntentData;
import com.amazon.ask.interaction.data.model.SlotTypeData;
import com.amazon.ask.interaction.definition.IntentDefinition;
import com.amazon.ask.interaction.definition.SlotTypeDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BinaryOperator;

import static com.amazon.ask.interaction.Utils.stringifyLocale;
import static com.amazon.ask.interaction.codegen.Utils.validateNamespace;

/**
 * Generates and writes {@link JavaFile}s and JSON files for a skill's localized interaction models.
 */
public class Generator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Set<Locale> locales = new LinkedHashSet<>();
    private final Map<Locale, String> invocationNames = new LinkedHashMap<>();

    private final Map<IntentDefinition, Map<Locale, IntentData>> intents = new HashMap<>();
    private final Map<SlotTypeDefinition, Map<Locale, SlotTypeData>> slots = new HashMap<>();

    private final String namespace;
    private final String skillName;

    private final IntentParser intentParser;
    private final SlotTypeParser slotTypeParser;
    private final DialogParser dialogParser;

    private final SlotTypeGenerator slotTypeGenerator;
    private final IntentFileGenerator intentFileGenerator;
    private final SkillFileGenerator skillFileGenerator;

    public Generator(String namespace, String skillName) {
        this.namespace = validateNamespace(namespace);
        this.skillName = skillName;

        this.slotTypeParser = new SlotTypeParser();
        this.intentParser = new IntentParser(slotTypeParser);
        this.dialogParser = new DialogParser();

        this.intentFileGenerator = new IntentFileGenerator();
        this.slotTypeGenerator = new SlotTypeGenerator();
        this.skillFileGenerator = new SkillFileGenerator();
    }

    /**
     * Parse a localized interaction model's intents, types and dialogs.
     *
     * @param model localized interaction model
     */
    public void parseModel(LocalizedInteractionModel model) {
        Locale locale = model.getLocale();
        locales.add(model.getLocale());
        invocationNames.put(model.getLocale(), model.getInteractionModelEnvelope().getInteractionModel().getLanguageModel().getInvocationName());
        slotTypeParser.parse(model).forEach((key, value) -> update(this.slots, locale, key, value, SlotTypeData::combine));
        intentParser.parse(model).forEach((key, value) -> update(this.intents, locale, key, value, IntentData::combine));
        dialogParser.parse(model, this.intents.keySet()).forEach((key, value) -> update(this.intents, locale, key, value, IntentData::combine));
    }

    private static <K, V> void update(Map<K, Map<Locale, V>> map, Locale locale, K key, V value, BinaryOperator<V> combiner) {
        if (map.containsKey(key)) {
            Map<Locale, V> localizedValues = map.get(key);
            if (localizedValues.containsKey(locale)) {
                value = combiner.apply(value, localizedValues.get(locale));
            }
            localizedValues.put(locale, value);
            map.put(key, localizedValues);
        } else {
            Map<Locale, V> localizedValues = new HashMap<>();
            localizedValues.put(locale, value);
            map.put(key, localizedValues);
        }
    }

    public void generate(File path) throws IOException {
        File javaPath = new File(path, "src/main/java/");
        File resourcesPath = new File(path, "src/main/resources/");

        makeDirectory(path);
        makeDirectory(javaPath);
        makeDirectory(resourcesPath);

        List<TypeName> intentClasses = new ArrayList<>();

        for (IntentDefinition intentDefinition : intents.keySet()) {
            if (intentDefinition.isCustom()) {
                JavaFile intentFile = intentFileGenerator.generate(intentDefinition, namespace);
                intentFile.writeTo(javaPath);

                intentClasses.add(ClassName.get(intentFile.packageName, intentFile.typeSpec.name));
            } else {
                intentClasses.add(TypeNames.get(intentDefinition.getName()));
            }
        }

        for (SlotTypeDefinition slotType : slots.keySet()) {
            if (slotType.isCustom()) {
                slotTypeGenerator.generate(slotType, namespace).writeTo(javaPath);
            }
        }

        if (skillName != null) {
            skillFileGenerator.generate(invocationNames, intents, slots, namespace, skillName).writeTo(javaPath);
        }

        for (IntentDefinition intentDefinition : intents.keySet()) {
            Map<Locale, IntentData> data = new HashMap<>();
            for (Locale locale : locales) {
                IntentData intentData = intents.get(intentDefinition).get(locale);
                data.put(locale, intentData);
            }

            if (data.values().stream().anyMatch(d -> !d.isEmpty())) {
                for (Map.Entry<Locale, IntentData> entry : data.entrySet()) {
                    File dir = new File(resourcesPath, (namespace + ".intents").replaceAll("\\.", "/"));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File dataFile = new File(dir, intentDefinition.getName() + "_" + stringifyLocale(entry.getKey()) + ".json");
                    MAPPER.writer(com.amazon.ask.interaction.Utils.PRETTY_PRINTER).writeValue(dataFile, entry.getValue());
                }
            }
        }

        for (SlotTypeDefinition slotType : slots.keySet()) {
            if (slotType.isCustom() || slots.get(slotType).values().stream().anyMatch(s -> !s.isEmpty())) {
                Map<Locale, SlotTypeData> data = new HashMap<>();
                for (Locale locale : locales) {
                    SlotTypeData slotData = this.slots.get(slotType).get(locale);
                    if (slotData == null) {
                        slotData = SlotTypeData.empty();
                    }
                    data.put(locale, slotData);
                }
                if (data.values().stream().anyMatch(d -> !d.isEmpty())) {
                    for (Locale locale : locales) {
                        File dir = new File(resourcesPath, (namespace + ".slots").replaceAll("\\.", "/"));
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        File dataFile = new File(dir, slotType.getName() + "_" + stringifyLocale(locale) + ".json");

                        SlotTypeData slotData = this.slots.get(slotType).get(locale);
                        if (slotData == null) {
                            slotData = SlotTypeData.empty();
                        }
                        MAPPER.writerWithDefaultPrettyPrinter().writeValue(dataFile, slotData);
                    }
                }
            }
        }
    }

    private static void makeDirectory(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Path must be a directory: " + file.getPath());
        }
    }
}
