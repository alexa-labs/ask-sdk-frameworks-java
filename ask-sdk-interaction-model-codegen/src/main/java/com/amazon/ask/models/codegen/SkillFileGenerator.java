package com.amazon.ask.models.codegen;

import com.amazon.ask.Skill;
import com.amazon.ask.models.SkillApplication;
import com.amazon.ask.models.annotation.type.Intent;
import com.amazon.ask.models.data.model.IntentData;
import com.amazon.ask.models.data.model.SlotTypeData;
import com.amazon.ask.models.definition.IntentDefinition;
import com.amazon.ask.models.definition.Model;
import com.amazon.ask.models.definition.SkillModel;
import com.amazon.ask.models.definition.SlotTypeDefinition;
import com.amazon.ask.models.types.intent.StandardIntent;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.Locale;
import java.util.Map;

/**
 * Generates a {@link SkillApplication} {@link JavaFile} for a set of intents.
 */
public class SkillFileGenerator {

    /**
     * @param invocationNames localized invocation names
     * @param intents
     * @param slots
     * @param namespace java namespace of generated skill
     * @param skillName name of skill - corresponds to generated java class name
     * @return generated java file representing the skill
     */
    public JavaFile generate(Map<Locale, String> invocationNames,
                             Map<IntentDefinition, Map<Locale, IntentData>> intents,
                             Map<SlotTypeDefinition, Map<Locale, SlotTypeData>> slots,
                             String namespace,
                             String skillName) {
        CodeBlock.Builder defineSkillBlock = CodeBlock.builder()
            .add("return $T.builder()\n", SkillModel.class)
            .indent()
            .indent();
        for (Map.Entry<Locale, String> invocationName: invocationNames.entrySet()) {
            defineSkillBlock.add(".withInvocationName($T.forLanguageTag($S), $S)\n", Locale.class, invocationName.getKey().toLanguageTag(), invocationName.getValue());
        }
        if (!intents.isEmpty()) {
            defineSkillBlock.add(".addModel($T.builder()\n", Model.class).indent();
            for (IntentDefinition intentDefinition : intents.keySet()) {
                if (intentDefinition.isCustom()) {
                    defineSkillBlock.add(".intent($T.class)\n", ClassName.get(namespace + ".intents", intentDefinition.getName()));
                } else {
                    TypeName intentClass = TypeNames.get(intentDefinition.getName());

                    if (intents.get(intentDefinition).values().stream().anyMatch(i -> !i.isEmpty())) {
                        defineSkillBlock.add(".intent($T.class, $T.resource()\n" +
                            "    .withResourceClass(getClass())\n" +
                            "    .withName($S)\n" +
                            "    .build())\n", intentClass, IntentData.class, "intents/data/" + builtInIntentName(intentClass));
                    } else {
                        try {
                            defineSkillBlock.add(".intent($T.class)\n", intentClass);
                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            for (SlotTypeDefinition slotType : slots.keySet()) {
                if (!slotType.isCustom()) {
                    if (slots.get(slotType).values().stream().anyMatch(s -> !s.isEmpty())) {
                        defineSkillBlock.add(".slotType($T.class, $T.resource()\n" +
                            "    .withResourceClass(getClass())\n" +
                            "    .withName($S)\n" +
                            "    .build())", TypeNames.get(slotType.getName()), SlotTypeData.class, "slots/data/" + slotType.getName());
                    }
                }
            }

            defineSkillBlock.add(".build())\n").unindent();
        }
        defineSkillBlock
            .addStatement(".build()")
            .unindent()
            .unindent();

        TypeSpec skillType = TypeSpec
            .classBuilder(ClassName.get(namespace, skillName))
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(SkillApplication.class)
            .addMethod(MethodSpec
                .methodBuilder("getSkillModel")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(SkillModel.class)
                .addCode(defineSkillBlock.build())
                .build())
            .addMethod(MethodSpec.methodBuilder("getSkill")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(Skill.class)
                .addCode(CodeBlock.builder()
                    .addStatement("throw new $T($S)", RuntimeException.class, "TODO")
                    .build())
                .build())
            .build();

        return JavaFile.builder(namespace, skillType).build();
    }

    private static String builtInIntentName(TypeName typeName) {
        try {
            return Class.forName(typeName.toString()).getAnnotation(Intent.class).value();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isBuiltIn(TypeName typeName) {
        try {
            return StandardIntent.class.isAssignableFrom(Class.forName(typeName.toString()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}