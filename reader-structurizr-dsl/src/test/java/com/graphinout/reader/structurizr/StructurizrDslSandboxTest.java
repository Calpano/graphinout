package com.graphinout.reader.structurizr;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** A DSL input must not make us read files, fetch URLs, read environment variables or run code. */
class StructurizrDslSandboxTest {

    @TempDir
    Path dir;

    record Result(String cj, List<ContentError> errors) {}

    static Result read(String dsl) throws IOException {
        StructurizrDslReader reader = new StructurizrDslReader();
        List<ContentError> errors = new ArrayList<>();
        reader.setContentErrorHandler(errors::add);
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.dsl", dsl), new CjStream2CjWriter(docWriter, true));
        ICjDocument doc = docWriter.resultDoc();
        return new Result(doc == null ? "" : CjDocuments.toJsonString(doc), errors);
    }

    static String workspace(String model) {
        return "workspace {\n model {\n" + model + "\n }\n}\n";
    }

    void assertRefused(Result r, String featureHint) {
        assertThat(r.cj()).doesNotContain("Secret42");
        assertThat(r.errors()).isNotEmpty();
        assertThat(r.errors().getFirst().message).contains(featureHint);
    }

    @Test
    @DisplayName("!include of a local file is refused")
    void includeFile() throws IOException {
        Path inc = dir.resolve("inc.dsl");
        Files.writeString(inc, "softwareSystem s \"Secret42\"\n");
        assertRefused(read(workspace("!include " + inc)), "not permitted");
    }

    @Test
    @DisplayName("!include of a URL is refused")
    void includeUrl() throws IOException {
        assertRefused(read(workspace("!include https://example.org/x.dsl")), "not permitted");
    }

    @Test
    @DisplayName("workspace extends a local file is refused")
    void extendsFile() throws IOException {
        Path base = dir.resolve("base.dsl");
        Files.writeString(base, workspace("softwareSystem s \"Secret42\""));
        assertRefused(read("workspace extends " + base + " {\n model {\n }\n}\n"), "not permitted");
    }

    @Test
    @DisplayName("!script and !docs are refused")
    void scriptsAndDocs() throws IOException {
        assertRefused(read(workspace("!script groovy {\n workspace.model.addSoftwareSystem('Secret42')\n }")), "not permitted");
        assertThat(read("workspace {\n !docs " + dir + "\n model {\n }\n}\n").errors()).isNotEmpty();
    }

    @Test
    @DisplayName("${NAME} does not read environment variables")
    void environment() throws IOException {
        String home = System.getenv("HOME");
        Result r = read(workspace("softwareSystem s \"${HOME}\""));
        if (home != null && !home.isBlank()) assertThat(r.cj()).doesNotContain(home);
    }

    @Test
    void sandboxStillParsesPlainModels() throws IOException {
        Result r = read(workspace("a = softwareSystem \"A\"\nb = softwareSystem \"B\"\na -> b \"uses\""));
        assertThat(r.errors()).isEmpty();
        assertThat(r.cj()).contains("\"A\"");
    }

}
