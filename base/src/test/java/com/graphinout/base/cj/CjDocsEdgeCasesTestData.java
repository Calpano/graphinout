package com.graphinout.base.cj;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;

import java.util.stream.Stream;

public class CjDocsEdgeCasesTestData {

    /**
     * Test Case 3.3: An element with a duplicate language tag in its label to test merge-patch behavior.
     */
    public static ICjDocument duplicateLanguageInLabel() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addLabel("Old", "en");
                node.addLabel("New", "en");
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 2.2: A graph with a duplicate node ID, to test merge-patch behavior.
     */
    public static ICjDocument duplicateNodeId() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addLabelWithoutLanguage("First");
            });
            // This second node with the same ID should merge with the first
            graph.addNode(node -> {
                node.id("n1");
                node.dataMutable(data -> data.add("status", "merged"));
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 2.3: A node with a duplicate port ID, to test merge-patch behavior.
     */
    public static ICjDocument duplicatePortId() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addPort(port -> port.id("p1").addProperty("location", "left"));
                // This second port with the same ID should merge
                node.addPort(port -> port.id("p1").addProperty("location", "top"));
            });
        });
        return cjDoc;
    }

    public static Stream<CjDocsTestData.TestDoc> testDocs() {
        return Stream.of(//
                new CjDocsTestData.TestDoc("duplicateNodeId", CjDocsEdgeCasesTestData.duplicateNodeId()),//
                new CjDocsTestData.TestDoc("duplicatePortId", CjDocsEdgeCasesTestData.duplicatePortId()),//
                new CjDocsTestData.TestDoc("duplicateLanguageInLabel", duplicateLanguageInLabel())//
        );
    }

}
