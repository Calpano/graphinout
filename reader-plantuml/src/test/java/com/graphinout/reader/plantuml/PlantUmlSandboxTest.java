package com.graphinout.reader.plantuml;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

/** A PlantUML input must not make us read files, fetch URLs or read environment variables. */
class PlantUmlSandboxTest {

    @TempDir
    Path dir;

    static String read(String puml) throws IOException {
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        new PlantUmlReader().read(SingleInputSource.of("test.puml", puml), new CjStream2CjWriter(docWriter, true));
        ICjDocument doc = docWriter.resultDoc();
        return doc == null ? "" : CjDocuments.toJsonString(doc);
    }

    Path secretFile(String name, String content) throws IOException {
        Path f = dir.resolve(name);
        Files.writeString(f, content);
        return f;
    }

    @Test
    @DisplayName("!include of a local file is not read")
    void includeFile() throws IOException {
        Path inc = secretFile("inc.puml", "class Secret42\n");
        assertThat(read("@startuml\n!include " + inc + "\nclass A\n@enduml\n")).doesNotContain("Secret42");
    }

    @Test
    @DisplayName("!includesub and !import of a local file are not read")
    void includeSubAndImport() throws IOException {
        Path inc = secretFile("sub.puml", "@startuml\n!startsub S\nclass Secret42\n!endsub\n@enduml\n");
        assertThat(read("@startuml\n!includesub " + inc + "!S\nclass A\n@enduml\n")).doesNotContain("Secret42");
        assertThat(read("@startuml\n!import " + inc + "\nclass A\n@enduml\n")).doesNotContain("Secret42");
    }

    @Test
    @DisplayName("%load_json of a local file is not read")
    void loadJson() throws IOException {
        Path json = secretFile("x.json", "{\"name\":\"Secret42\"}");
        assertThat(read("@startuml\n!$j = %load_json(\"" + json + "\")\nclass $j.name\nclass A\n@enduml\n"))
                .doesNotContain("Secret42");
    }

    @Test
    @DisplayName("%getenv does not read environment variables")
    void getenv() throws IOException {
        String home = System.getenv("HOME");
        String cj = read("@startuml\nclass \"%getenv(\"HOME\")\" as H\nclass A\nA --> H\n@enduml\n");
        if (home != null && !home.isBlank()) assertThat(cj).doesNotContain(home);
    }

    @Test
    @DisplayName("%file_exists does not probe the file system")
    void fileExists() throws IOException {
        Path f = secretFile("exists.txt", "x");
        String cj = read("@startuml\n!if %file_exists(\"" + f + "\")\nclass Secret42\n!endif\nclass A\n@enduml\n");
        assertThat(cj).doesNotContain("Secret42");
    }

    @Test
    void sandboxStillParsesPlainDiagrams() throws IOException {
        assertThat(read("@startuml\nclass A\nclass B\nA --> B\n@enduml\n")).contains("\"B\"");
    }

}
