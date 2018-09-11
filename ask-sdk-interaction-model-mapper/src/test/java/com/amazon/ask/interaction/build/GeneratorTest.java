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

import com.amazon.ask.interaction.model.InteractionModel;
import com.amazon.ask.interaction.definition.SkillModel;
import com.amazon.ask.interaction.model.InteractionModelEnvelope;
import com.amazon.ask.interaction.renderer.SkillModelRenderer;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.amazon.ask.interaction.Locales.en_US;
import static com.amazon.ask.interaction.Locales.fr_FR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorTest {

    @Mock
    private SkillModelSupplier mockApplication;

    @Mock
    private File mockDir;

    @Mock
    private File mockFile;

    @Mock
    private ObjectWriter mockWriter;

    private SkillModel mockSkill = SkillModel.builder()
        .withInvocationName(en_US, "en_US")
        .withInvocationName(fr_FR, "fr_FR")
        .build();

    @Mock
    private SkillModelRenderer mockRenderer;

    private InteractionModelEnvelope mockModel = InteractionModelEnvelope.builder().build();

    private Generator underTest;

    @Before
    public void before() {
        underTest = new Generator(mockWriter, mockRenderer, mockApplication, mockDir, Collections.singletonList(en_US));
        Mockito.when(mockApplication.getSkillModel()).thenReturn(mockSkill);
        Mockito.when(mockRenderer.render(mockSkill, en_US)).thenReturn(mockModel);

        underTest = new Generator(mockWriter, mockRenderer, mockApplication, mockDir, Collections.singletonList(en_US)) {
            @Override
            protected File getDestFile(String name) {
                assertEquals("en-US.json", name);
                return mockFile;
            }
        };
    }

    @Test
    public void testWriteToEmptyDir() throws GeneratorException, IOException {
        Mockito.when(mockDir.exists()).thenReturn(false);
        underTest.generate();

        Mockito.verify(mockDir).mkdirs();
        Mockito.verify(mockWriter).writeValue(mockFile, mockModel);
    }

    @Test
    public void testWriteToDir() throws GeneratorException, IOException {
        Mockito.when(mockDir.exists()).thenReturn(true);
        underTest.generate();
        Mockito.verify(mockDir, Mockito.never()).mkdirs();
        Mockito.verify(mockWriter).writeValue(mockFile, mockModel);
    }

    @Test
    public void testLocales() throws GeneratorException, IOException {
        File mockEnFile = Mockito.mock(File.class);
        File mockFrFile = Mockito.mock(File.class);
        Generator generator = new Generator(mockWriter, mockRenderer, mockApplication, mockDir, Arrays.asList(en_US, fr_FR)) {
            @Override
            protected File getDestFile(String name) {
                if (name.equals("en-US.json")) return mockEnFile;
                if (name.equals("fr-FR.json")) return mockFrFile;
                fail("expected en-US.json or fr-FR.json");
                return null;
            }
        };

        InteractionModelEnvelope mockEnModel = InteractionModelEnvelope.builder()
            .withInteractionModel(InteractionModel.builder().build())
            .build();
        InteractionModelEnvelope mockFrModel = InteractionModelEnvelope.builder().build();

        Mockito.when(mockRenderer.render(mockSkill, en_US)).thenReturn(mockEnModel);
        Mockito.when(mockRenderer.render(mockSkill, fr_FR)).thenReturn(mockFrModel);
        Mockito.when(mockDir.exists())
            .thenReturn(true)
            .thenReturn(false);

        generator.generate();

        Mockito.verify(mockWriter).writeValue(mockEnFile, mockEnModel);
        Mockito.verify(mockWriter).writeValue(mockFrFile, mockFrModel);
    }

    @Test
    public void testDefaultToAllInvocationNameLocales() throws GeneratorException, IOException {
        File mockEnFile = Mockito.mock(File.class);
        File mockFrFile = Mockito.mock(File.class);
        Generator generator = new Generator(mockWriter, mockRenderer, mockApplication, mockDir, null) {
            @Override
            protected File getDestFile(String name) {
                if (name.equals("en-US.json")) return mockEnFile;
                if (name.equals("fr-FR.json")) return mockFrFile;
                fail("expected en-US.json or fr-FR.json");
                return null;
            }
        };

        InteractionModelEnvelope mockEnModel = InteractionModelEnvelope.builder()
            .withInteractionModel(InteractionModel.builder().build())
            .build();
        InteractionModelEnvelope mockFrModel = InteractionModelEnvelope.builder().build();

        Mockito.when(mockRenderer.render(mockSkill, en_US)).thenReturn(mockEnModel);
        Mockito.when(mockRenderer.render(mockSkill, fr_FR)).thenReturn(mockFrModel);
        Mockito.when(mockDir.exists())
            .thenReturn(true)
            .thenReturn(false);

        generator.generate();

        Mockito.verify(mockWriter).writeValue(mockEnFile, mockEnModel);
        Mockito.verify(mockWriter).writeValue(mockFrFile, mockFrModel);
    }

    @Test(expected = GeneratorException.class)
    public void testIOExceptionOnWrite() throws GeneratorException, IOException {
        Mockito.doThrow(new IOException()).when(mockWriter).writeValue(mockFile, mockModel);
        Mockito.when(mockDir.exists()).thenReturn(true);
        underTest.generate();
        Mockito.verify(mockWriter).writeValue(mockFile, mockModel);
    }

    @Test(expected = GeneratorException.class)
    public void testRuntimeExceptionOnRender() throws GeneratorException, IOException {
        Mockito.when(mockRenderer.render(mockSkill, en_US)).thenThrow(new IllegalArgumentException("test"));
        Mockito.when(mockDir.exists()).thenReturn(true);
        underTest.generate();
        Mockito.verify(mockWriter).writeValue(mockFile, mockModel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullApplication() {
        new Generator(null, mockDir, Collections.singletonList(en_US));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullFile() {
        new Generator(mockApplication, null, Collections.singletonList(en_US));
    }

    @Test
    public void testGetDestFile() {
        File dir = new File(".");
        Generator generator = new Generator(mockApplication, dir, Collections.singletonList(en_US));
        assertEquals(new File(dir, "test.json"), generator.getDestFile("test.json"));
    }
}
