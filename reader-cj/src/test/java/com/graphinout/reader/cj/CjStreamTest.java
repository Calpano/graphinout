package com.graphinout.reader.cj;

import com.graphinout.base.CjDocument2CjStream;
import com.graphinout.base.CjStream2GioWriter;
import com.graphinout.base.Gio2CjStream;
import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.api.CjStream2CjWriter;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.cj.stream.impl.CjStream2CjDocumentWriter;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

public class CjStreamTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON->CJ->Gio->CJ->JSON (all)")
    void test_Json_Cj_Gio(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json);

        // JSON -> CJ doc
        CjStream2CjDocumentWriter cj2ElementsWriter = new CjStream2CjDocumentWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(inputSource, jsonWriter_in);
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();
        if (cjDoc == null) {
            cjDoc = new CjDocumentElement();
        }

        // CJ doc -> GIO
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        ICjStream cjStream = new CjStream2CjWriter(cj2JsonWriter);
        GioWriter gioWriter = new Gio2CjStream(cjStream);
        CjStream2GioWriter cjStream2GioWriter = new CjStream2GioWriter(gioWriter);
        CjDocument2CjStream.toCjStream(cjDoc, cjStream2GioWriter);

        CjAssert.xAssertThatIsSameCj(json2StringWriter.jsonString(), json, null);
    }

}
