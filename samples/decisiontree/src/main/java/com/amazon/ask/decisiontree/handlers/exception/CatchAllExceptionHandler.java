/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package com.amazon.ask.decisiontree.handlers.exception;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import java.util.Optional;

public class CatchAllExceptionHandler implements ExceptionHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput, Throwable throwable) {
        return true; //catch all
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, Throwable throwable) {
        System.out.println("Exception: " + throwable.toString());
        System.out.println("Exception thrown while receiving: " + handlerInput.getRequestEnvelope().getRequest().getType());
        return handlerInput.getResponseBuilder()
            .withSpeech("Sorry. I have problems answering your request. Please try again")
            .withShouldEndSession(true)
            .build();
    }
}
