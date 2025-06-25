package com.calpano.graphinout.base.cj;

import com.calpano.graphinout.base.cj.impl.CjLabelImpl;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CjLabelImplTest {

    @Test
    void testConnectedJsonSpecificationExamples() {
        // Test simple label example from specification
        CjLabelImpl simpleLabel = CjLabelImpl.of("Hello, World");
        assertNotNull(simpleLabel);
        assertTrue(simpleLabel.isSimple());
        assertEquals("Hello, World", simpleLabel.asString());

        // Test multi-lingual label example from specification
        Map<String, String> multiLingual = new HashMap<>();
        multiLingual.put("de", "Hallo, Welt");
        multiLingual.put("en", "Hello, World");

        CjLabelImpl multiLabel = CjLabelImpl.of(multiLingual);
        assertNotNull(multiLabel);
        assertTrue(multiLabel.isMultiLingual());

        Map<String, String> result = multiLabel.asMultiLingual();
        assertEquals("Hallo, Welt", result.get("de"));
        assertEquals("Hello, World", result.get("en"));
    }

    @Test
    void testEmptyMultiLingualLabel() {
        Map<String, String> emptyMap = new HashMap<>();
        CjLabelImpl label = CjLabelImpl.of(emptyMap);

        assertTrue(label.isMultiLingual());
        assertFalse(label.isSimple());
        assertTrue(label.asMultiLingual().isEmpty());
    }

    @Test
    void testEqualityMixedTypes() {
        CjLabelImpl simpleLabel = CjLabelImpl.of("Test");
        Map<String, String> map = new HashMap<>();
        map.put("en", "Test");
        CjLabelImpl multiLabel = CjLabelImpl.of(map);

        assertNotEquals(simpleLabel, multiLabel);
    }

    @Test
    void testEqualityMultiLingualLabels() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("en", "Hello");
        map1.put("de", "Hallo");

        Map<String, String> map2 = new HashMap<>();
        map2.put("en", "Hello");
        map2.put("de", "Hallo");

        Map<String, String> map3 = new HashMap<>();
        map3.put("en", "Hello");
        map3.put("fr", "Bonjour");

        CjLabelImpl label1 = CjLabelImpl.of(map1);
        CjLabelImpl label2 = CjLabelImpl.of(map2);
        CjLabelImpl label3 = CjLabelImpl.of(map3);

        assertEquals(label1, label2);
        assertNotEquals(label1, label3);
    }

    @Test
    void testEqualitySimpleLabels() {
        CjLabelImpl label1 = CjLabelImpl.of("Test");
        CjLabelImpl label2 = CjLabelImpl.of("Test");
        CjLabelImpl label3 = CjLabelImpl.of("Different");

        assertEquals(label1, label2);
        assertNotEquals(label1, label3);
    }

    @Test
    void testFromMapObject() {
        Map<String, String> multiLingual = new HashMap<>();
        multiLingual.put("en", "English");
        multiLingual.put("fr", "Français");

        CjLabelImpl label = CjLabelImpl.of(multiLingual);

        assertNotNull(label);
        assertTrue(label.isMultiLingual());
        Map<String, String> result = label.asMultiLingual();
        assertEquals("English", result.get("en"));
        assertEquals("Français", result.get("fr"));
    }

    @Test
    void testFromStringObject() {
        CjLabelImpl label = CjLabelImpl.of("Simple Label");

        assertNotNull(label);
        assertTrue(label.isSimple());
        assertEquals("Simple Label", label.asString());
    }

    @Test
    void testHashCode() {
        CjLabelImpl label1 = CjLabelImpl.of("Test");
        CjLabelImpl label2 = CjLabelImpl.of("Test");

        assertEquals(label1.hashCode(), label2.hashCode());

        Map<String, String> map1 = new HashMap<>();
        map1.put("en", "Hello");
        Map<String, String> map2 = new HashMap<>();
        map2.put("en", "Hello");

        CjLabelImpl multiLabel1 = CjLabelImpl.of(map1);
        CjLabelImpl multiLabel2 = CjLabelImpl.of(map2);

        assertEquals(multiLabel1.hashCode(), multiLabel2.hashCode());
    }

    @Test
    void testMultiLingualLabel() {
        Map<String, String> multiLingual = new HashMap<>();
        multiLingual.put("en", "Hello, World");
        multiLingual.put("de", "Hallo, Welt");
        multiLingual.put("es", "Hola, Mundo");

        CjLabelImpl label = CjLabelImpl.of(multiLingual);

        assertFalse(label.isSimple());
        assertTrue(label.isMultiLingual());
        assertThrows(IllegalStateException.class, label::asString);

        Map<String, String> result = label.asMultiLingual();
        assertEquals(3, result.size());
        assertEquals("Hello, World", result.get("en"));
        assertEquals("Hallo, Welt", result.get("de"));
        assertEquals("Hola, Mundo", result.get("es"));

        // Verify the returned map is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> result.put("fr", "Bonjour"));
    }

    @Test
    void testSimpleStringLabel() {
        CjLabelImpl label = CjLabelImpl.of("Hello, World");

        assertTrue(label.isSimple());
        assertFalse(label.isMultiLingual());
        assertEquals("Hello, World", label.asString());
        assertThrows(IllegalStateException.class, label::asMultiLingual);
    }

    @Test
    void testToString() {
        CjLabelImpl simpleLabel = CjLabelImpl.of("Simple");
        assertEquals("Simple", simpleLabel.asString());

        Map<String, String> map = new HashMap<>();
        map.put("en", "Hello");
        map.put("de", "Hallo");
        CjLabelImpl multiLabel = CjLabelImpl.of(map);

        String result = multiLabel.toString();
        assertTrue(result.contains("en"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("de"));
        assertTrue(result.contains("Hallo"));
    }

}
