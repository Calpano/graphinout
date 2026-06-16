package com.graphinout.engine;

import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.base.output.OutputSink;
import com.graphinout.reader.dot.DotReader;
import com.graphinout.reader.tgf.TgfReader;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class KnownIssuesTest {

    @Test
    void test_tgf_to_dot() throws Exception {
        String tgf = """
                a
                b
                c
                d
                #
                a b
                b c
                b d
                """;
        GioEngineCore core = new GioEngineCore();
        GioReader tgfReader = core.getReader(TgfReader.FORMAT_ID);
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("tgf-input", tgf);

        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        GioWriter dotWriter = core.getWriter(DotReader.FORMAT_ID);
        ICjStream cjStream = dotWriter.createCjStream(outputSink);

        tgfReader.read(inputSource, cjStream);
        String result = outputSink.getBufferAsUtf8String();

        // TGF is treated as directed (source -> target), so the DOT writer renders a `digraph` with `->`.
        assertThat(result.trim()).isEqualTo("""
                digraph {
                  a;
                  b;
                  c;
                  d;
                  a -> b;
                  b -> c;
                  b -> d;
                }""");
    }

}
