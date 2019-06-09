/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap;

import com.github.zafarkhaja.semver.expr.ExpressionParser;
import org.apache.commons.lang.Validate;

/**
 * A semantic version.
 *
 * @since 2.4.0
 * @see <a href="http://semver.org/">Semantic Versioning</a>
 */
public final class Version implements Comparable<Version> {

    /** The actual implementation of semantic version, never {@code null}. */
    private final com.github.zafarkhaja.semver.Version impl;

    /**
     * Constructs a {@code Version} from the given {@code version}.
     *
     * @param version the semantic version
     * @throws IllegalArgumentException if the given {@code version} is {@code null} or empty, or is
     *     not a valid semantic version.
     */
    public Version(String version) throws IllegalArgumentException {
        Validate.notEmpty(version, "Parameter version must not be null nor empty.");

        try {
            impl = new com.github.zafarkhaja.semver.Version.Builder(version).build();
        } catch (com.github.zafarkhaja.semver.ParseException e) {
            throw new IllegalArgumentException(
                    "Parameter version [" + version + "] is not valid: " + e.getMessage());
        }
    }

    /**
     * Returns the major version.
     *
     * @return the major version
     */
    public int getMajorVersion() {
        return impl.getMajorVersion();
    }

    /**
     * Returns the minor version.
     *
     * @return the minor version
     */
    public int getMinorVersion() {
        return impl.getMinorVersion();
    }

    /**
     * Returns the patch version.
     *
     * @return the patch version
     */
    public int getPatchVersion() {
        return impl.getPatchVersion();
    }

    /**
     * Tells whether or not the given version range matches this version.
     *
     * @param versionRange the range version
     * @return {@code true} if this version matches the given version range, {@code false} otherwise
     * @throws IllegalArgumentException if {@code versionRange} is null or empty, or not valid range
     *     version
     * @see #isValidVersionRange(String)
     */
    public boolean matches(String versionRange) throws IllegalArgumentException {
        Validate.notEmpty(versionRange, "Parameter versionRange must not be null nor empty.");

        try {
            return impl.satisfies(versionRange);
        } catch (com.github.zafarkhaja.semver.ParseException e) {
            throw new IllegalArgumentException(
                    "Parameter versionRange ["
                            + versionRange
                            + "] is not valid: "
                            + e.getMessage());
        }
    }

    /**
     * Tells whether or not the given version range is valid.
     *
     * @param versionRange the version range to test.
     * @return {@code true} if the version range is valid, {@code false} otherwise.
     * @since 2.7.0
     * @see #matches(String)
     */
    public static boolean isValidVersionRange(String versionRange) {
        try {
            ExpressionParser.newInstance().parse(versionRange);
            return true;
        } catch (com.github.zafarkhaja.semver.ParseException ignore) {
            // Ignore.
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 + ((impl == null) ? 0 : impl.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Version other = (Version) obj;
        if (impl == null) {
            if (other.impl != null) {
                return false;
            }
        } else if (!impl.equals(other.impl)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }
        return impl.compareTo(other.impl);
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
