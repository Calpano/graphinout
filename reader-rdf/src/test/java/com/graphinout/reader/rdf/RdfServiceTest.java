package com.graphinout.reader.rdf;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class RdfServiceTest {

    /** Each RDF syntax is registered once; a duplicate shows up twice in format detection. */
    @Test
    void eachFormatRegisteredOnce() {
        RdfService service = new RdfService();
        assertThat(service.readers().stream().map(GioReader::fileFormat).map(f -> f.id()).toList())
                .containsNoDuplicates();
        assertThat(service.writers().stream().map(GioWriter::fileFormat).map(f -> f.id()).toList())
                .containsNoDuplicates();
    }

}
