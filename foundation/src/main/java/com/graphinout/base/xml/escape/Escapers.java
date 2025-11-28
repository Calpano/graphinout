/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.graphinout.base.xml.escape;

import org.jspecify.annotations.Nullable;

import static com.graphinout.base.xml.escape.Preconditions.checkNotNull;


/**
 * Static utility methods pertaining to {@link Escaper} instances.
 *
 * @author Sven Mawson
 * @author David Beaumont
 * @since 15.0
 */
@GwtCompatible
public final class Escapers {
  private Escapers() {}

  /**
   * Returns an {@link Escaper} that does no escaping, passing all character data through unchanged.
   */
  public static Escaper nullEscaper() {
    return NULL_ESCAPER;
  }

  // An Escaper that efficiently performs no escaping.
  // Extending CharEscaper (instead of Escaper) makes Escapers.compose() easier.
  private static final Escaper NULL_ESCAPER =
      new CharEscaper() {
        @Override
        public String escape(String string) {
          return checkNotNull(string);
        }

        @Override
        @Nullable
        protected char[] escape(char c) {
          // TODO: Fix tests not to call this directly and make it throw an error.
          return null;
        }
      };

  /**
   * Returns a builder for creating simple, fast escapers. A builder instance can be reused and each
   * escaper that is created will be a snapshot of the current builder state. Builders are not
   * thread safe.
   *
   * <p>The initial state of the builder is such that:
   *
   * <ul>
   *   <li>There are no replacement mappings
   *   <li>{@code safeMin == Character.MIN_VALUE}
   *   <li>{@code safeMax == Character.MAX_VALUE}
   *   <li>{@code unsafeReplacement == null}
   * </ul>
   *
   * <p>For performance reasons escapers created by this builder are not Unicode aware and will not
   * validate the well-formedness of their input.
   */
  public static EscaperBuilder builder() {
    return new EscaperBuilder();
  }



  /**
   * Returns a string that would replace the given character in the specified escaper, or {@code
   * null} if no replacement should be made. This method is intended for use in tests through the
   * {@code EscaperAsserts} class; production users of {@link CharEscaper} should limit themselves
   * to its public interface.
   *
   * @param c the character to escape if necessary
   * @return the replacement string, or {@code null} if no escaping was needed
   */
  @Nullable
  public static String computeReplacement(CharEscaper escaper, char c) {
    return stringOrNull(escaper.escape(c));
  }



  @Nullable
  private static String stringOrNull(@Nullable char[] in) {
    return (in == null) ? null : new String(in);
  }

}
