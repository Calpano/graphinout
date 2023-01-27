package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.input.InputSource;
import com.calpano.graphinout.base.output.GraphMlWriter;
import com.calpano.graphinout.base.reader.GioFileFormat;
import com.calpano.graphinout.base.reader.GioReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class TgfReader implements GioReader {
    private static final Logger log = getLogger(TgfReader.class);
    /** @Nullable TODO maybe make a true Optional */
    private  Consumer<ContentError> errorConsumer;

    @Override
    public GioFileFormat fileFormat() {
        return new GioFileFormat("tfg", "Trivial Graph Format");
    }

    public void errorHandler(Consumer<ContentError> errorConsumer) {
        this.errorConsumer=errorConsumer;
    }


    @Override
    public void read(InputSource inputSource, GioWriter writer) throws IOException {
        // FIXME ...do actual parsing

        String content = IOUtils.toString(inputSource.inputStream());

        boolean isValid = false;
        Scanner scanner = new Scanner(content);
        boolean foundHash = false;
        int lineCount = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineCount++;
            if (line.contains("#")) {
                foundHash = true;
            }
        }
        if (lineCount < 3 || !foundHash) {
            isValid = true;
        }
        scanner.close();

        if(!foundHash && errorConsumer!=null) {
            errorConsumer.accept(new ContentError(ContentError.ErrorLevel.Error,"Found no hash sign", Optional.empty()));
        }


    }
}
