/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit test for {@link AddOnClassnames}. */
class AddOnClassnamesUnitTest {

    @Test
    void shouldFailToCreateAddOnClassnamesWithNullAllowed() throws Exception {
        // Given
        List<String> allowed = null;
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new AddOnClassnames(allowed, Collections.emptyList()));
        // Then
        assertThat(e.getMessage(), containsString("allowedClassnames"));
    }

    @Test
    void shouldFailToCreateAddOnClassnamesWithNullRestricted() throws Exception {
        // Given
        List<String> restricted = null;
        // When
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new AddOnClassnames(Collections.emptyList(), restricted));
        // Then
        assertThat(e.getMessage(), containsString("restrictedClassnames"));
    }

    @Test
    void shouldAllowAllWithAllAllowed() {
        // Given AddOnClassnames.ALL_ALLOWED
        // When / Then
        assertThat(AddOnClassnames.ALL_ALLOWED.isAllowed("org.example.X"), is(equalTo(true)));
        assertThat(AddOnClassnames.ALL_ALLOWED.isAllowed("org.x.y.z.Class"), is(equalTo(true)));
    }

    @Test
    void shouldBeAllowedIfNothingAllowedNorRestricted() {
        // Given
        List<String> allowed = Collections.emptyList();
        List<String> restricted = Collections.emptyList();
        // When
        AddOnClassnames addOnClassnames = new AddOnClassnames(allowed, restricted);
        // Then
        assertThat(addOnClassnames.isAllowed("org.example.X"), is(equalTo(true)));
        assertThat(addOnClassnames.isAllowed("org.x.y.z.Class"), is(equalTo(true)));
    }

    @Test
    void shouldNotBeAllowedIfNotAllowed() {
        // Given
        String allowedClass = "org.example.AllowedClass";
        String allowedPackage = "org.example.allowed.";
        List<String> allowed = Arrays.asList(allowedClass, allowedPackage);
        // When
        AddOnClassnames addOnClassnames = new AddOnClassnames(allowed, Collections.emptyList());
        // Then
        assertThat(addOnClassnames.isAllowed(allowedClass), is(equalTo(true)));
        assertThat(addOnClassnames.isAllowed(allowedClass + "$1"), is(equalTo(true)));
        assertThat(addOnClassnames.isAllowed(allowedPackage + "ClassX"), is(equalTo(true)));
        assertThat(addOnClassnames.isAllowed(allowedPackage + "ClassY"), is(equalTo(true)));
        assertThat(addOnClassnames.isAllowed("org.example.ClassNotAllowed"), is(equalTo(false)));
        assertThat(
                addOnClassnames.isAllowed("org.example.package.not.allowed.Class"),
                is(equalTo(false)));
    }

    @Test
    void shouldBeAllowedIfNotRestricted() {
        // Given
        String restrictedClass = "org.example.RestrictedClass";
        String restrictedPackage = "org.example.restricted.";
        List<String> restricted = Arrays.asList(restrictedClass, restrictedPackage);
        // When
        AddOnClassnames addOnClassnames = new AddOnClassnames(Collections.emptyList(), restricted);
        // Then
        assertThat(addOnClassnames.isAllowed(restrictedClass), is(equalTo(false)));
        assertThat(addOnClassnames.isAllowed(restrictedClass + "$1"), is(equalTo(false)));
        assertThat(addOnClassnames.isAllowed(restrictedPackage + "ClassX"), is(equalTo(false)));
        assertThat(addOnClassnames.isAllowed(restrictedPackage + "ClassY"), is(equalTo(false)));
        assertThat(addOnClassnames.isAllowed("org.example.x.X"), is(equalTo(true)));
        assertThat(addOnClassnames.isAllowed("org.example.y.Y"), is(equalTo(true)));
    }

    @Test
    void shouldFailIfClassnameIsNull() throws Exception {
        // Given
        String classname = null;
        // When / Then
        assertThrows(
                NullPointerException.class, () -> AddOnClassnames.ALL_ALLOWED.isAllowed(classname));
    }
}
