package com.graphinout.reader.cj;

import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.base.json.JsonReaderImpl;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

public class CjStreamTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON->CJ->JSON (all)")
    void test_Json_Cj(String displayName, Resource resource) throws IOException {
        String json_in = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json_in);

        // JSON -> CJ doc
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(inputSource, jsonWriter_in);
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();
        if (cjDoc == null) {
            cjDoc = new CjDocumentElement();
        }

        // CJ doc -> CJ
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        ICjStream cjStream = new CjStream2CjWriter(cj2JsonWriter);
        CjDocument2CjStream.toCjStream(cjDoc, cjStream);
        String json_out = json2StringWriter.jsonString();

        CjAssert.xAssertThatIsSameCj(json_out, json_in, null);
    }

}
