package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.ReaderTests;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.reader.ContentError;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlSAXSchemaValidationTest {

    private static final Logger log = getLogger(GraphmlReaderTest2.class);

    private static Stream<String> getAllGraphmlFiles() {
        return ReaderTests.getAllTestResourceFilePaths().filter(path -> path.endsWith(".graphml"));
    }

    @BeforeEach
    void setUp() {
    }

    protected Map<String, Long> expectedErrors(@NotNull String resourceName) {
        Map<String, Long> errorLongMap = new HashMap<>();
        switch (resourceName) {
            case "graphin/graphml/aws/AWS - Analytics.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Application Services.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Compute.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Database.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Developer Tools.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Enterprise Applications.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - General.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Internet of Things.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Management Tools.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Mobile Services.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Networking.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - On-Demand Workforce.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - SDK.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Security & Identity.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/aws/AWS - Storage & Content Delivery.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Palette ToolTip' of attribute 'attr.name' on element 'ns0:key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Palette ToolTip' is not a valid value for 'NMTOKEN'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'ns0:key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'ns2:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 21L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 21L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 21L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 21L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'ns2:Geometry'.", null).toString(), 21L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:SVGNode'. No child element is expected at this point.", null).toString(), 21L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'ns2:Resources'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/images/anforderungscluster.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 2L);
                return errorLongMap;
            case "graphin/graphml/images/bausteinsicht_ebene1.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 32L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PreferredPlacementDescriptor'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:UMLClassNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 12L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:UMLNoteNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 20L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 12L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 10L);
                return errorLongMap;
            case "graphin/graphml/images/bausteinsicht_ebene2.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 113L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PreferredPlacementDescriptor'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 60L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:UMLClassNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 54L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:UMLNoteNode'. No child element is expected at this point.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 27L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 27L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 59L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 54L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 13L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 35L);
                return errorLongMap;
            case "graphin/graphml/images/ci-cd-process.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 10L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 10L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 5L);
                return errorLongMap;
            case "graphin/graphml/images/flask-pulsar.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 15L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PreferredPlacementDescriptor'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:UMLClassNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:UMLNoteNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 7L);
                return errorLongMap;
            case "graphin/graphml/images/kubernetes-architecture.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 43L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 32L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 25L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 18L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 11L);
                return errorLongMap;
            case "graphin/graphml/images/kubernetes-ci-cd-process.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 46L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 26L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 20L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 10L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 10L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 26L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 20L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 13L);
                return errorLongMap;
            case "graphin/graphml/images/kubernetes-clean-architecture-reduced.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 8L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 4L);
                return errorLongMap;
            case "graphin/graphml/images/kubernetes-clean-architecture.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 17L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 58L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'raised' is not allowed to appear in element 'y:BorderStyle'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 56L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 22L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 17L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 36L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 22L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 14L);
                return errorLongMap;
            case "graphin/graphml/images/kubernetes-opensource-deployment.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.a: Invalid content was found starting with element '{\"http://www.yworks.com/xml/graphml\":PreferredPlacementDescriptor}'. One of '{\"http://www.yworks.com/xml/graphml\":LabelModel, \"http://www.yworks.com/xml/graphml\":ModelParameter}' is expected.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 24L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:Resource'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:EdgeLabel'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 10L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 14L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'data'.", null).toString(), 13L);
                return errorLongMap;
            case "graphin/graphml/images/projektstrukturplan.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'xml:space' is not allowed to appear in element 'y:NodeLabel'.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 29L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 28L);
                return errorLongMap;
            case "graphin/graphml/nested-graph-structures/nested_graph_graphical_representation.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/nested-graph-structures/sample.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/auto_oscillator.graphml":
                return errorLongMap;
            case "graphin/graphml/samples/classements.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 17L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 8L);
                return errorLongMap;
            case "graphin/graphml/samples/four_neuron_cpg.graphml":
                return errorLongMap;
            case "graphin/graphml/samples/gml.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SplineEdge'. No child element is expected at this point.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:GenericEdge' is not complete. One of '{\"http://www.yworks.com/xml/graphml\":EdgeLabel, \"http://www.yworks.com/xml/graphml\":SourcePort, \"http://www.yworks.com/xml/graphml\":TargetPort, \"http://www.yworks.com/xml/graphml\":UserData}' is expected.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-identity-constraint.4.3: Key 'edge_source_ref' with value 'n6' not found for identity constraint of element 'graph'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericEdge'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 9L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-identity-constraint.4.3: Key 'edge_target_ref' with value 'n7' not found for identity constraint of element 'graph'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/got-graph.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/graph1.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 20L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 30L);
                return errorLongMap;
            case "graphin/graphml/samples/graph1_test.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-identity-constraint.4.3: Key 'data_key_ref' with value 'd7' not found for identity constraint of element 'graphml'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-identity-constraint.4.3: Key 'edge_target_ref' with value 'n4' not found for identity constraint of element 'graph'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/graph2.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 34L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 33L);
                return errorLongMap;
            case "graphin/graphml/samples/graph3.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 16L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 22L);
                return errorLongMap;
            case "graphin/graphml/samples/graph4.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 23L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 63L);
                return errorLongMap;
            case "graphin/graphml/samples/graph5.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 31L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 36L);
                return errorLongMap;
            case "graphin/graphml/samples/graph6.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 24L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 27L);
                return errorLongMap;
            case "graphin/graphml/samples/greek2.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'y coordinate' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 3' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 0' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'color.r' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'color.g' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'importance' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'x coordinate' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'color.b' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 3' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 2' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 0' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.3: Element 'graphml' cannot have character [children], because the type's content type is element-only.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'station name' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'title' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 1' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 2' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 1' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'x coordinate' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'y coordinate' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'station name' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/guava-17.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 226L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 226L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 1529L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 452L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 5923L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 452L);
                return errorLongMap;
            case "graphin/graphml/samples/haitimap2.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'y coordinate' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 3' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 0' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'color.r' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'color.g' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'x coordinate' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'color.b' is not allowed to appear in element 'key'.", null).toString(), 4L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 3' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 2' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 0' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.3: Element 'graphml' cannot have character [children], because the type's content type is element-only.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'station name' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'line 1' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 2' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'line 1' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'x coordinate' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'y coordinate' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'station name' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/les-miserables.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Modularity Class' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-attribute.3: The value 'Edge Label' of attribute 'attr.name' on element 'key' is not valid with respect to its type, 'key.name.type'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Edge Label' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-datatype-valid.1.2.1: 'Modularity Class' is not a valid value for 'NMTOKEN'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/samples/nested-sample-graph.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ProxyAutoBoundsNode'. No child element is expected at this point.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 44L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedHeight' is not allowed to appear in element 'y:State'.", null).toString(), 22L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 50L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'closedWidth' is not allowed to appear in element 'y:State'.", null).toString(), 22L);
                return errorLongMap;
            case "graphin/graphml/svg/DelayedMessageDispatching.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:TableNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 11L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 10L);
                return errorLongMap;
            case "graphin/graphml/svg/EngineEventDispatching.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:TableNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 2L);
                return errorLongMap;
            case "graphin/graphml/svg/FlexsimObjectEventDispatching.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:TableNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 5L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 4L);
                return errorLongMap;
            case "graphin/graphml/svg/FlexsimObjectHierarchy.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/svg/FlexsimObjectOnDrawDispatching.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:TableNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 7L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 6L);
                return errorLongMap;
            case "graphin/graphml/svg/MessageDispatching.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.foldertype' is not allowed to appear in element 'node'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:TableNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 5L);
                return errorLongMap;
            case "graphin/graphml/svg/OnClickEngine.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'width' is not allowed to appear in element 'y:Geometry'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.b: The content of element 'y:Geometry' is not complete. One of '{WC[##any]}' is expected.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'height' is not allowed to appear in element 'y:Geometry'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'x' is not allowed to appear in element 'y:Geometry'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:SVGNode'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'y' is not allowed to appear in element 'y:Geometry'.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 2L);
                return errorLongMap;
            case "graphin/graphml/svg/byteblock.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/svg/objectdatatypeSimple.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/svg/predrawfunction.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 3L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 2L);
                return errorLongMap;
            case "graphin/graphml/svg/resetChain.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.3.2.2: Attribute 'yfiles.type' is not allowed to appear in element 'key'.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:ShapeNode'. No child element is expected at this point.", null).toString(), 2L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:Resources'. No child element is expected at this point.", null).toString(), 1L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:GenericNode'. No child element is expected at this point.", null).toString(), 6L);
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-complex-type.2.4.d: Invalid content was found starting with element 'y:PolyLineEdge'. No child element is expected at this point.", null).toString(), 6L);
                return errorLongMap;
            case "graphin/graphml/synthetic/graph-a.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/synthetic/graph-b.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/synthetic/graph-nested.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/synthetic/graph-with-nested-ports.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'graphml'.", null).toString(), 1L);
                return errorLongMap;
            case "graphin/graphml/synthetic/invalid-root.graphml":
                errorLongMap.put(new ContentError(ContentError.ErrorLevel.Error, "cvc-elt.1.a: Cannot find the declaration of element 'myroot'.", null).toString(), 1L);
                return errorLongMap;


        }
        return errorLongMap;
    }

    @ParameterizedTest
    @MethodSource("getAllGraphmlFiles")
    void readAllGraphmlFiles(String filePath) throws Exception {

        URL resourceUrl = ClassLoader.getSystemResource(filePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(filePath, content)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            boolean isValid = graphmlReader.isValid(singleInputSource);
            Map<String, Long> actualErrors = contentErrors.stream().collect(Collectors.groupingBy(ContentError::toString, Collectors.counting()));
            Map<String, Long> expectedErrors = expectedErrors(filePath);
            for (Map.Entry<String, Long> entry : actualErrors.entrySet()) {
                assertFalse(isValid);
                assertTrue(expectedErrors.containsKey(entry.getKey()));
                assertEquals(expectedErrors.get(entry.getKey()), entry.getValue());
            }
            // check that each expected error actually happened
            for (Map.Entry<String, Long> entry : expectedErrors.entrySet()) {
                assertTrue(actualErrors.containsKey(entry.getKey()),"Expected error "+entry.getKey());
                assertEquals(actualErrors.get(entry.getKey()), entry.getValue());
            }
        }
    }

    @Test
    void read() throws Exception {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "aws", "AWS - Analytics.graphml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        try (SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content)) {
            GraphmlReader graphmlReader = new GraphmlReader();
            List<ContentError> contentErrors = new ArrayList<>();
            graphmlReader.errorHandler(contentErrors::add);
            boolean isValid = graphmlReader.isValid(singleInputSource);
            Map<String, Long> expectedErrors = expectedErrors("graphin/graphml/aws/AWS - Analytics.graphml");

            Map<String, Long> map = contentErrors.stream().collect(Collectors.groupingBy(ContentError::toString, Collectors.counting()));
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                assertFalse(isValid);
                assertTrue(expectedErrors.containsKey(entry.getKey()));
                assertEquals(expectedErrors.get(entry.getKey()), entry.getValue());
//
            }
        }
    }
}
