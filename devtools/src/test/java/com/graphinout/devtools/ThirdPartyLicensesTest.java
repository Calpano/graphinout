package com.graphinout.devtools;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThirdPartyLicensesTest {

    @Test
    void parsesPluginLinesIncludingNestedParentheses() {
        List<ThirdPartyLicenses.Dep> deps = ThirdPartyLicenses.parsePluginOutput(List.of(
                "",
                "Lists of 2 third-party dependencies.",
                "     (The MIT License (MIT)) ClassGraph (io.github.classgraph:classgraph:4.8.199 - https://github.com/classgraph/classgraph)",
                "     (EPL-2.0) (LGPL-2.1-only) Logback Classic Module (ch.qos.logback:logback-classic:1.6.7 - http://logback.qos.ch/logback-classic)"));
        assertEquals(2, deps.size());
        ThirdPartyLicenses.Dep cg = deps.get(0);
        assertEquals(List.of("The MIT License (MIT)"), cg.licenses());
        assertEquals("ClassGraph", cg.name());
        assertEquals("io.github.classgraph:classgraph", cg.ga());
        assertEquals("4.8.199", cg.version());
        assertEquals("classgraph-4.8.199", cg.folder());
        assertEquals(List.of("EPL-2.0", "LGPL-2.1-only"), deps.get(1).licenses());
    }

    @Test
    void rejectsUnparsableLine() {
        assertThrows(IllegalArgumentException.class, () -> ThirdPartyLicenses.parsePluginOutput(List.of("(MIT) no coordinates")));
    }

    @Test
    void mapsLicenseNamesToLabels() {
        assertEquals("Apache-2.0", ThirdPartyLicenses.label(List.of("The Apache Software License, Version 2.0")));
        assertEquals("BSD-3-Clause", ThirdPartyLicenses.label(List.of("New BSD License")));
        assertEquals("EPL-2.0 / LGPL-2.1", ThirdPartyLicenses.label(List.of("EPL-2.0", "LGPL-2.1-only")));
        assertEquals("EPL-2.0 / GPL-2.0+CPE",
                ThirdPartyLicenses.label(List.of("Eclipse Public License 2.0", "GNU General Public License, version 2 with the GNU Classpath Exception")));
        assertEquals("CDDL-1.1 / GPL-2.0+CPE", ThirdPartyLicenses.label(List.of("CDDL 1.1", "GPL2 w/ CPE")));
        assertEquals("EPL-1.0", ThirdPartyLicenses.label(List.of("Eclipse Public License 1.0")));
        assertEquals("MIT", ThirdPartyLicenses.label(List.of("The MIT License")));
        assertTrue(ThirdPartyLicenses.label(List.of("Some Custom License")).startsWith("??"));
    }

    @Test
    void parsesExistingRows() {
        String adoc = """
                | https://example.org[Example] | `org.example:ex` | `1.0` | link:LICENSES/ex-1.0/[MIT] | [link:https://raw.githubusercontent.com/x/y/HEAD/LICENSE[1]] | ✓
                | https://example.org[Other] | `org.example:other` | `2.0` | link:LICENSES/other-2.0/[Apache-2.0] | [link:https://repo1.maven.org/x.jar[1]] |
                """;
        Map<String, ThirdPartyLicenses.Row> rows = ThirdPartyLicenses.parseRows(adoc);
        assertEquals(4, rows.size()); // keyed by g:a and g:a:v
        ThirdPartyLicenses.Row ex = rows.get("org.example:ex");
        assertEquals(ex, rows.get("org.example:ex:1.0"));
        assertEquals("ex-1.0", ex.dir());
        assertEquals("MIT", ex.label());
        assertEquals("example", ex.sortKey());
        assertEquals("Apache-2.0", rows.get("org.example:other").label());
    }

}
