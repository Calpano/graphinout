package com.calpano.graphinout.base.reader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GioFileFormat {

    /** an ID unique in GIO; Only [a-z0-9_] in the id String. */
   private String id;
    /** A human-readable name */
   private String label;

}
