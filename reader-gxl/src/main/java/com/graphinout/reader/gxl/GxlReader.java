package com.graphinout.reader.gxl;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrors;
import com.graphinout.foundation.pure.input.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Reads GXL 1.0 (Graph eXchange Language, {@code gxl.dtd}). See {@link Gxl2Cj} for the mapping to CJ.
 */
public class GxlReader implements GioReader {

    public static final String FORMAT_ID = "gxl";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "GXL Graph eXchange Language", ".gxl", ".gxl.xml");
    private static final Logger log = LoggerFactory.getLogger(GxlReader.class);

    private static final int XML_DECL_LOOKAHEAD = 1024;

    private Consumer<ContentError> errorHandler = ContentErrors.NOOP;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (!(inputSource instanceof SingleInputSource sis)) {
            throw new IllegalArgumentException("GXL reader needs a single input source");
        }
        GxlXml.Element root;
        try (InputStream raw = sis.inputStream()) {
            InputStream in = skipWhitespaceBeforeXmlDeclaration(raw, sis.name());
            org.xml.sax.InputSource saxInput = new org.xml.sax.InputSource(in);
            sis.encoding().ifPresent(cs -> saxInput.setEncoding(cs.name()));
            saxInput.setSystemId(sis.name());
            root = GxlXml.parse(saxInput);
        } catch (SAXParseException e) {
            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Error,
                    "Failed to parse GXL '" + sis.name() + "': " + e.getMessage(),
                    new Location(e.getLineNumber(), e.getColumnNumber())));
            return;
        } catch (SAXException | ParserConfigurationException e) {
            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Error,
                    "Failed to parse GXL '" + sis.name() + "': " + e.getMessage()));
            return;
        }
        ICjDocument cjDoc = new Gxl2Cj(errorHandler).toCjDocument(root);
        log.debug("Read GXL '{}'", sis.name());
        cjDoc.fire(new CjWriter2CjStream(cjStream), false);
    }

    /**
     * Some GXL files in the wild have blank lines before {@code <?xml ...?>}, which makes them not well-formed. Skip that
     * whitespace (with a warning) so they can be read; byte-level, so the declared encoding is still honoured.
     */
    private InputStream skipWhitespaceBeforeXmlDeclaration(InputStream raw, String name) throws IOException {
        BufferedInputStream in = new BufferedInputStream(raw);
        in.mark(XML_DECL_LOOKAHEAD);
        int skipped = 0;
        int b = in.read();
        while (skipped < XML_DECL_LOOKAHEAD - 6 && (b == ' ' || b == '\t' || b == '\r' || b == '\n')) {
            skipped++;
            b = in.read();
        }
        byte[] next = new byte[5];
        next[0] = (byte) b;
        int n = 1 + in.readNBytes(next, 1, 4);
        in.reset();
        if (skipped > 0 && n == 5 && new String(next, StandardCharsets.US_ASCII).equals("<?xml")) {
            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn,
                    "Skipped " + skipped + " whitespace characters before the XML declaration in '" + name + "'"));
            in.skipNBytes(skipped);
        }
        return in;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

}
