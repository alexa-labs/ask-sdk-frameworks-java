package com.amazon.ask.models.data.source;

import java.io.IOException;
import java.io.InputStream;

/**
 * Read from a resource
 */
public interface Codec<T> {
    T read(InputStream resource) throws IOException;
}