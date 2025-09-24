package com.calpano.graphinout.base.cj.element;

import com.calpano.graphinout.base.cj.element.impl.CjLabelElement;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CjLabelTest {

    @Test
    void test() {
        CjLabelElement label = new CjLabelElement();
        label.addEntry(entry->{
            entry.language("de");
            entry.value("Gummibärchen");
        });
        label.addEntry(entry-> entry.value("gummy bear"));
        String json = label.toJsonString();
        assertNotNull(json);
        assertThat(json).isEqualTo("""
                [{"language":"de","value":"Gummibärchen"},{"value":"gummy bear"}]""");
        ICjLabel label2 = ICjLabel.fromJson(json);
        assertThat(json).isEqualTo(label2.toJsonString());
    }

}
