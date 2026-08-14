package com.graphinout.base.cj.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Instead of depending on Guava just for {@link #setsDifference(Set, Set)} and on IOUtils just for
 * {@link #toString(InputStream, Charset)} we re-implemented both methods here.
 */
public class Utils {

    /** As in IOUtils: 8 KiB. */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /** Return value of {@link Reader#read(char[])} at end of stream. */
    private static final int EOF = -1;

    /**
     * Modelled after Guavas impl: an unmodifiable, lazily evaluated <em>view</em> of the elements contained in
     * {@code setA} but not in {@code setB}. The view reflects later changes of the backing sets; iteration order is
     * that of {@code setA}.
     *
     * @param setA elements to keep, unless they also occur in {@code setB}
     * @param setB elements to subtract
     * @return a live view, never null
     */
    public static <T> Set<T> setsDifference(Set<T> setA, Set<T> setB) {
        Objects.requireNonNull(setA, "setA");
        Objects.requireNonNull(setB, "setB");
        return new AbstractSet<T>() {

            @Override
            public boolean contains(Object element) {
                return setA.contains(element) && !setB.contains(element);
            }

            @Override
            public boolean isEmpty() {
                return setB.containsAll(setA);
            }

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    private final Iterator<T> backing = setA.iterator();
                    private boolean hasNext;
                    private T next;

                    {
                        advance();
                    }

                    @Override
                    public boolean hasNext() {
                        return hasNext;
                    }

                    @Override
                    public T next() {
                        if (!hasNext) {
                            throw new NoSuchElementException();
                        }
                        T result = next;
                        advance();
                        return result;
                    }

                    private void advance() {
                        while (backing.hasNext()) {
                            T candidate = backing.next();
                            if (!setB.contains(candidate)) {
                                next = candidate;
                                hasNext = true;
                                return;
                            }
                        }
                        next = null;
                        hasNext = false;
                    }
                };
            }

            @Override
            public int size() {
                int size = 0;
                for (T element : setA) {
                    if (!setB.contains(element)) {
                        size++;
                    }
                }
                return size;
            }
        };
    }

    /**
     * Modelled after Apache Commons IOUtils.toString(InputStream, Charset): reads the stream to its end and decodes it
     * with the given charset. The stream is <em>not</em> closed, that stays the caller's job.
     *
     * @param inputStream the stream to read, must not be null
     * @param charset     the charset to decode with, null means {@link Charset#defaultCharset()}
     * @return the stream content as a string, never null
     * @throws IOException if reading fails
     */
    public static String toString(InputStream inputStream, Charset charset) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Reader reader = new InputStreamReader(inputStream, charset == null ? Charset.defaultCharset() : charset);
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = reader.read(buffer)) != EOF) {
            result.append(buffer, 0, read);
        }
        return result.toString();
    }

}
