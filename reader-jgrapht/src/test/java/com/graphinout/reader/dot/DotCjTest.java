package com.graphinout.reader.dot;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.text.TextReader;
import com.graphinout.foundation.text.TextWriterOnStringBuilder;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

class DotCjTest {

    private static final Logger log = getLogger(DotCjTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.reader.dot.DotTests#dotResources")
    @Description("Test DOT->CjDoc->DOT (all)")
    void test_Dot_Cj_Dot(String displayName, Resource resource) throws IOException {
        String dotText1 = resource.getContentAsString();

        // DOT -> CJ
        ICjDocument cjDoc1;
        {
            DotLines2CjDocument dotLines2CjDocument = new DotLines2CjDocument();
            TextReader.read(dotText1, dotLines2CjDocument);
            cjDoc1 = dotLines2CjDocument.resultDocument();
        }

        TextWriterOnStringBuilder textWriterOnStringBuilder = new TextWriterOnStringBuilder();
        CjDocument2Dot.toDotSyntax(cjDoc1, textWriterOnStringBuilder);
        String dotText2 = textWriterOnStringBuilder.toString();

        DotAssert.xAssertThatIsSameDot(dotText2, dotText1, null);

        log.info("CJ: " + CjDocuments.toJsonString(cjDoc1));

    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.reader.dot.DotTests#dotResources")
    @Description("Test DOT->CjDoc->DOT (all)")
    void test_Dot_Cj_Dot_Cj(String displayName, Resource resource) throws IOException {
        String dotText1 = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", dotText1);

        // DOT -> CJ
        ICjDocument cjDoc1;
        {
            DotLines2CjDocument dotLines2CjDocument = new DotLines2CjDocument();
            TextReader.read(dotText1, dotLines2CjDocument);
            cjDoc1 = dotLines2CjDocument.resultDocument();
        }

        TextWriterOnStringBuilder textWriterOnStringBuilder = new TextWriterOnStringBuilder();
        CjDocument2Dot.toDotSyntax(cjDoc1, textWriterOnStringBuilder);
        String dotText2 = textWriterOnStringBuilder.toString();
        ICjDocument cjDoc2;
        {
            DotLines2CjDocument dotLines2CjDocument = new DotLines2CjDocument();
            TextReader.read(dotText2, dotLines2CjDocument);
            cjDoc2 = dotLines2CjDocument.resultDocument();
        }

        CjAssert.xAssertThatIsSameCj(cjDoc2, cjDoc1, null);
    }


}
