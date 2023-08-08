package com.calpano.graphinout.filemanagment;

import com.calpano.graphinout.base.output.OutputSink;
import java.io.IOException;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface SaveOutputSink {

  default String save(@Nonnull IOResource<OutputSink> outputSink, @Nonnull String sessionId)
      throws IOException {
    return save(outputSink, sessionId, Type.INPUT);
  }

  String save(@Nonnull IOResource<OutputSink> outputSink, @Nonnull String sessionId, Type type)
      throws IOException;
}
