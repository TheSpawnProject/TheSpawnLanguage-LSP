package net.programmer.igoodie.lsp.data;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

import java.util.HashMap;
import java.util.Map;

public class TSLSOpenDocuments {

    private final Map<String, TSLDocument> openDocuments;

    public TSLSOpenDocuments() {
        this.openDocuments = new HashMap<>();
    }

    public TSLDocument putDocument(String uri, TSLDocument tslDocument) {
        return openDocuments.put(uri, tslDocument);
    }

    public TSLDocument getDocument(TextDocumentItem textDocument) {
        String documentUri = textDocument.getUri();
        return openDocuments.computeIfAbsent(documentUri, uri -> new TSLDocument(uri, textDocument.getText()));
    }

    public TSLDocument getDocument(TextDocumentIdentifier textDocument) {
        String documentUri = textDocument.getUri();
        return openDocuments.computeIfAbsent(documentUri, TSLDocument::new);
    }

    public TSLDocument remove(String uri) {
        return openDocuments.remove(uri);
    }

}
