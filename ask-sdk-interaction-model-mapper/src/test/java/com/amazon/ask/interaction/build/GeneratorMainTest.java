/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.interaction.build;

import com.amazon.ask.interaction.Locales;
import com.amazon.ask.interaction.definition.Model;
import com.amazon.ask.interaction.definition.SkillModel;
import com.amazon.ask.interaction.types.intent.StopIntent;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class GeneratorMainTest {
    @Test(expected = RuntimeException.class)
    public void testInvalidArgs() throws GeneratorException {
        GeneratorMain.main(new String[]{"--invalid"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullClassname() throws GeneratorException {
        GeneratorMain.main(new String[]{
            "-d", "destdir",
            "-l", "en_US"
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDestDir() throws GeneratorException {
        GeneratorMain.main(new String[]{
            "-c", TestApplication.class.getName(),
            "-l", "en_US"
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLocale() throws GeneratorException {
        GeneratorMain.main(new String[]{
            "-c", TestApplication.class.getName(),
            "-d", "destdir",
            "-l", "invalid"
        });
    }

    @Test(expected = GeneratorException.class)
    public void testClassNotFound() throws GeneratorException {
        GeneratorMain.main(new String[]{
            "-c", "ClassNotFound",
            "-d", "destdir",
            "-l", "en_US"
        });
    }

    @Test
    public void testGenerate() throws GeneratorException {
        new GeneratorMain(new String[]{
            "-c", TestApplication.class.getName(),
            "-d", "destdir",
            "-l", "en_US",
            "-l", "fr_FR"
        }) {
            @Override
            protected void generate(SkillModelSupplier skillModelSupplier, File destdir, List<Locale> locales) throws GeneratorException {
                assertEquals(TestApplication.class, skillModelSupplier.getClass());
                assertEquals(new File("destdir"), destdir);
                assertEquals(Arrays.asList(Locales.en_US, Locales.fr_FR), locales);
            }
        }.run();
    }

    public static class TestApplication implements SkillModelSupplier {

        @Override
        public SkillModel getSkillModel() {
            return SkillModel.builder()
                .withInvocationName(Locales.en_US, "en_US")
                .withInvocationName(Locales.fr_FR, "fr_FR")
                .addModel(Model.builder()
                    .intent(StopIntent.class)
                    .build())
                .build();
        }
    }
}
