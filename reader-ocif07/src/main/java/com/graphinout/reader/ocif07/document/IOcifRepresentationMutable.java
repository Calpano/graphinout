package com.graphinout.reader.ocif07.document;

import com.graphinout.reader.ocif07.document.extension.representation.IOcifRepresentationExtension;
import org.jspecify.annotations.NonNull;

public interface IOcifRepresentationMutable extends IOcifRepresentation, IOcifEntityMutable {

    @NonNull IOcifRepresentation addExtension(@NonNull IOcifRepresentationExtension extension);

}
