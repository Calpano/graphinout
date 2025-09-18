package com.calpano.graphinout.base.cj;

import org.junit.jupiter.params.provider.Arguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class CjFileProvider {

    public static Stream<Arguments> cjFileProvider() throws Exception {
        Path testResourcesPath = Paths.get("src/test/resources/cj");
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath).filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".cj.json") || p.toString().endsWith(".cj")).map(p -> Arguments.of(p.toString().substring(baseLen).replace('\\', '/'), p));
    }

}
