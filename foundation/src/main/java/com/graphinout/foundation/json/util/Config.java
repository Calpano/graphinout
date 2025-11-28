package com.graphinout.foundation.json.util;

import java.util.Set;

record Config(int maxLineLength, Set<String> forceMultiLineKeys) {}
