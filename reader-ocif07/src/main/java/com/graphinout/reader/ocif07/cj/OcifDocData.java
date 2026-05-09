package com.graphinout.reader.ocif07.cj;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif07.OCIF;
import com.graphinout.reader.ocif07.document.IOcifDocumentMutable;
import com.graphinout.reader.ocif07.document.IOcifResource;
import com.graphinout.reader.ocif07.document.IOcifSchema;
import com.graphinout.reader.ocif07.document.impl.OcifDocument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;
import static com.graphinout.foundation.pure.functional.Nullables.nonNull;
import static com.graphinout.reader.ocif07.Ocifs.factory;

/**
 * Decorate an {@link IJsonObjectMutable} for OCIF document level data stored as CJ document data
 */
public class OcifDocData {

    private final IJsonObjectMutable o;

    public OcifDocData(IJsonObjectMutable o) {this.o = o;}

    public static OcifDocData ofOcifDoc(OcifDocument ocifDocument) {
        OcifDocData ocifDocLevelData = new OcifDocData(factory().createObjectMutable());
        ocifDocLevelData.schemaUri(ocifDocument.ocifSchemaURI());
        ocifDocLevelData.resources(ocifDocument.resources());
        ocifDocLevelData.schemas(ocifDocument.schemas());
        ifPresentAccept(ocifDocument.rootNode(), n -> ocifDocLevelData.rootNode(n.id()));
        return ocifDocLevelData;
    }

    public static void toOcifDocument(@Nullable IJsonValue jsonValue, IOcifDocumentMutable ocifDocument) {
        if (jsonValue == null)
            return;
        OcifDocData d = new OcifDocData(jsonValue.asObject().mutableCopy());
        ifPresentAccept(d.schemaUri(), ocifDocument::ocifSchemaURI);
        ifPresentAccept(d.rootNode(), ocifDocument::rootNodeId);

        List<ContentError> contentErrors = new ArrayList<>();

        List<IOcifResource> resources = d.resources(contentErrors::add);
        if (!contentErrors.isEmpty()) {
            throw new IllegalArgumentException("Invalid OCIF document: " + contentErrors);
        }
        resources.forEach(ocifDocument::addResource);

        List<IOcifSchema> schemas = d.schemas(contentErrors::add);
        if (!contentErrors.isEmpty()) {
            throw new IllegalArgumentException("Invalid OCIF document: " + contentErrors);
        }
        schemas.forEach(ocifDocument::addSchema);
    }


    public boolean isEmpty() {
        return o.isEmpty();
    }

    public IJsonObject jsonObject() {
        return o;
    }

    public void resources(List<IOcifResource> resources) {
        IJsonArrayMutable a = factory().createArrayMutable();
        resources.forEach(r -> a.add(IOcifResource.resourceToJson(r)));
        if (!a.isEmpty()) {
            o.setArray(OCIF.Root.RESOURCES, a);
        }
    }


    public List<IOcifResource> resources(Consumer<ContentError> errorHandler) {
        IJsonArray a = nonNull(o.get(OCIF.Root.RESOURCES), IJsonValue::asArray, factory().createArray());
        return a.stream().map(IJsonValue::asObject).map(
                        json -> IOcifResource.jsonToOcifResource(json, errorHandler))
                .map(x -> (IOcifResource) x)
                .toList();
    }

    public void rootNode(String rootNodeId) {
        o.add(OCIF.Root.ROOT_NODE, rootNodeId);
    }

    public String rootNode() {
        return o.getString(OCIF.Root.ROOT_NODE);
    }

    public void schemaUri(@NonNull String schemaUri) {
        o.add(OCIF._OCIF_, schemaUri);
    }

    public String schemaUri() {
        return o.getString(OCIF._OCIF_);
    }

    public List<IOcifSchema> schemas(Consumer<ContentError> errorHandler) {
        IJsonArray a = nonNull(o.get(OCIF.Root.SCHEMAS), IJsonValue::asArray, factory().createArray());
        return a.stream().map(IJsonValue::asObject).map(
                        json -> IOcifSchema.toOcifSchema(json, errorHandler))
                .toList();
    }

    public void schemas(List<IOcifSchema> schemas) {
        IJsonArrayMutable a = factory().createArrayMutable();
        schemas.forEach(s -> a.add(IOcifSchema.schemaToJson(s)));
        if (!a.isEmpty()) {
            o.setArray(OCIF.Root.SCHEMAS, a);
        }
    }

}
