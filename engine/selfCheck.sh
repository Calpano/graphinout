#!/bin/bash
# Run the SelfCheck class quietly
mvn compile exec:java -Dexec.mainClass=com.graphinout.engine.SelfCheck -q
