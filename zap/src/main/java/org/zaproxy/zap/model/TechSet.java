/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class TechSet {

    /** @deprecated Not for public use. Replaced by {@link #getAllTech()}. */
    @Deprecated public static final TechSet AllTech = new TechSet(Tech.getTopLevel());

    private TreeSet<Tech> includeTech = new TreeSet<>();
    private TreeSet<Tech> excludeTech = new TreeSet<>();

    public TechSet() {}

    public TechSet(Tech... include) {
        this(include, (Tech[]) null);
    }

    public TechSet(Tech[] include, Tech[] exclude) {
        if (include != null) {
            for (Tech tech : include) {
                this.include(tech);
            }
        }
        if (exclude != null) {
            for (Tech tech : exclude) {
                this.exclude(tech);
            }
        }
    }

    public TechSet(Set<Tech> include) {
        this.includeTech.addAll(include);
    }

    public TechSet(TechSet techSet) {
        this.includeTech.addAll(techSet.includeTech);
        this.excludeTech.addAll(techSet.excludeTech);
    }

    public void include(Tech tech) {
        excludeTech.remove(tech);
        includeTech.add(tech);
    }

    public void exclude(Tech tech) {
        includeTech.remove(tech);
        excludeTech.add(tech);
    }

    public boolean includes(Tech tech) {
        if (tech == null) {
            return false;
        }
        if (excludeTech.contains(tech)) {
            return false;
        } else if (includeTech.contains(tech)) {
            return true;
        } else {
            return this.includes(tech.getParent());
        }
    }

    /**
     * Tells whether or not any of the given technologies is included.
     *
     * @param techs the technologies that will be checked.
     * @return {@code true} if any of the technologies is included, {@code false} otherwise.
     * @since 2.8.0
     * @see #includes(Tech)
     */
    public boolean includesAny(Tech... techs) {
        if (techs == null || techs.length == 0) {
            return false;
        }
        for (Tech tech : techs) {
            if (includes(tech)) {
                return true;
            }
        }
        return false;
    }

    public TreeSet<Tech> getIncludeTech() {
        TreeSet<Tech> copy = new TreeSet<>();
        copy.addAll(this.includeTech);
        return copy;
    }

    public TreeSet<Tech> getExcludeTech() {
        TreeSet<Tech> copy = new TreeSet<>();
        copy.addAll(this.excludeTech);
        return copy;
    }

    // Useful for debugging ;)
    public void print() {
        System.out.println("TechSet: " + this.hashCode());
        for (Tech tech : includeTech) {
            System.out.println("\tInclude: " + tech);
        }
        for (Tech tech : excludeTech) {
            System.out.println("\tExclude: " + tech);
        }
    }

    /**
     * Get a TechSet including all Tech currently known
     *
     * <p><b>Info</b>: always returns a new TechSet containing the currently known Tech
     *
     * @return TechSet including all Tech
     */
    public static TechSet getAllTech() {
        return new TechSet(Tech.getTopLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(excludeTech, includeTech);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TechSet)) {
            return false;
        }
        TechSet other = (TechSet) obj;
        return Objects.equals(excludeTech, other.excludeTech)
                && Objects.equals(includeTech, other.includeTech);
    }
}
