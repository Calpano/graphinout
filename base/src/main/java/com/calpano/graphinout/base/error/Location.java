package com.calpano.graphinout.base.error;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {
    private int line;
    private int column;
}
