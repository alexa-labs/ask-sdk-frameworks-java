/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.mvc.view.nashorn;

import com.amazon.ask.mvc.view.View;
import com.amazon.ask.mvc.view.resolver.ScriptEngineViewResolver;
import com.amazon.ask.mvc.view.resolver.cache.ViewCache;
import com.amazon.ask.mvc.view.resolver.candidate.ViewCandidateEnumerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.apache.commons.io.IOUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Compiles a script using the {@link NashornScriptEngine} and constructs a {@link NashornView} to execute it.
 */
public class NashornViewResolver extends ScriptEngineViewResolver {
    protected NashornViewResolver(ObjectMapper mapper, ViewCache cache, List<ViewCandidateEnumerator> viewCandidateEnumerators,
                                  Collection<Pattern> viewNamePatterns, String prefix, String suffix, Class<?> resourceClass,
                                  ScriptEngineManager scriptEngineManager, String scriptEngineName, String renderObject, String renderFunction) {
        super(mapper, cache, viewCandidateEnumerators, viewNamePatterns, prefix, suffix, resourceClass, scriptEngineManager, scriptEngineName, renderObject, renderFunction);
    }

    @Override
    protected View createView(ScriptEngine scriptEngine, Reader scriptReader) throws Exception {
        return new NashornView(scriptEngine, IOUtils.toString(scriptReader), renderObject, renderFunction, mapper);
    }

    public static Builder builder() {
        return new Builder()
            .withScriptEngineName("nashorn")
            .withSuffix(".js");
    }

    public static class Builder extends ScriptEngineViewResolver.Builder<Builder> {
        public NashornViewResolver build() {
            return new NashornViewResolver(mapper, cache, viewCandidateEnumerators, viewNamePatterns, prefix, suffix,
                resourceClass, scriptEngineManager, scriptEngineName, renderObject, renderFunction);
        }
    }
}
