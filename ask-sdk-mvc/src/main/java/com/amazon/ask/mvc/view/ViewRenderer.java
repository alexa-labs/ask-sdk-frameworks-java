/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.mvc.view;

import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.mvc.mapper.ControllerMethodContext;
import com.amazon.ask.mvc.plugin.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Encapsulates logic for rendering the output of a controller.
 */
public class ViewRenderer {
    private static final ViewRenderer INSTANCE = new ViewRenderer();
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static ViewRenderer getInstance() {
        return INSTANCE;
    }

    protected ViewRenderer() {
    }

    /**
     * Finds a view for the request by querying the view resolvers.
     *
     * If the output is a response, or an optional response, it is directly returned.
     *
     * If a view is found, the response is rendered and returned. Otherwise empty is returned.
     *
     * @param context controller method context
     * @param output object returned from the handler
     * @param requestEnvelope envelope for the current request
     * @return final output of the rendering operation
     * @throws RuntimeException if no views are found or if the view threw an exception
     */
    @SuppressWarnings("unchecked")
    public Optional<Response> render(ControllerMethodContext context, Object output, RequestEnvelope requestEnvelope) {
        if (output == null) {
            return Optional.empty();
        } else if (output instanceof Response) {
            return Optional.of((Response) output);
        } else if (output instanceof Optional) {
            // Check to see if the result was an optional response.
            Optional o = (Optional) output;
            if (o.isPresent()) {
                if (o.get() instanceof Response) {
                    return (Optional<Response>) o;
                }
            }
        }
        return renderInternal(context, output, requestEnvelope);
    }

    protected Optional<Response> renderInternal(ControllerMethodContext context, Object output, RequestEnvelope requestEnvelope) {
        //see if there is a view renderer that can handle the output and build a response
        try {
            for (ViewResolver viewResolver : context.getSkillContext().getViewResolvers()) {
                Optional<View> view = viewResolver.resolve(output, requestEnvelope);
                if (view.isPresent()) {
                    return Optional.of(view.get().render(output, requestEnvelope));
                }
            }

        } catch (Exception e) {
            logger.error(String.format("[%s] Exception thrown when resolving views. Controller=%s, Method=%s, Output Class=%s",
                requestEnvelope.getRequest().getRequestId(), context.getController().getClass().getName(), context.getMethod(), output.getClass().getName()), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        logger.error(String.format("[%s] Found no views that could handle output: %s", requestEnvelope.getRequest().getRequestId(), output.getClass().getName()));
        throw new RuntimeException("Found no views that could handle output: " + output.getClass().getName());
    }
}
