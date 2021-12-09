package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.TSLServer;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.tsl.exception.TSLSyntaxError;
import net.programmer.igoodie.tsl.parser.token.TSLCaptureCall;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TSLSTextDocumentService implements TextDocumentService {

    private final Map<String, TSLDocument> OPEN_TSL_DOCUMENTS = new HashMap<>();

    private TSLServer server;

    public TSLSTextDocumentService(TSLServer server) {
        this.server = server;
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionList = new LinkedList<>();
            TSLDocument tslDocument = OPEN_TSL_DOCUMENTS.get(params.getTextDocument().getUri());

            tslDocument.getCaptureSnippets().forEach((captureName, snippet) -> {
                TSLCaptureCall headerToken = snippet.getHeaderToken();
                CompletionItem completionItem = new CompletionItem();
                if (headerToken.getArgs() != null) {
                    StringBuilder builder = new StringBuilder("$" + snippet.getName());
                    for (int i = 0; i < headerToken.getArgs().size(); i++) {
                        builder.append("${")
                                .append(i + 1)
                                .append(":")
                                .append(headerToken.getArgs().get(i))
                                .append("}");
                        if (i != headerToken.getArgs().size() - 1) {
                            builder.append(", ");
                        }
                    }
                    completionItem.setInsertText(builder.toString());
                } else {
                    completionItem.setInsertText("$" + snippet.getName());
                }
                completionItem.setLabel("$" + captureName);
                completionItem.setDetail("Detail text here");
                completionItem.setKind(CompletionItemKind.Function);
                completionList.add(completionItem);
            });

            return Either.forLeft(completionList);
        });
    }


    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        Hover hover = new Hover();

        List<Either<String, MarkedString>> list = new LinkedList<>();
        list.add(Either.forLeft("Foo"));
        list.add(Either.forLeft("Bar"));
        list.add(Either.forLeft("Baz"));

        hover.setContents(list);

        return CompletableFuture.completedFuture(hover);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem textDocument = params.getTextDocument();
        String uri = textDocument.getUri();
        String text = textDocument.getText();
        OPEN_TSL_DOCUMENTS.put(uri, new TSLDocument(uri, text));
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        VersionedTextDocumentIdentifier textDocument = params.getTextDocument();
        String uri = textDocument.getUri();
        TSLDocument tslDocument = OPEN_TSL_DOCUMENTS.computeIfAbsent(uri, TSLDocument::new);
        for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
            tslDocument.setText(change.getText());
        }

        PublishDiagnosticsParams diagnosticsParams = new PublishDiagnosticsParams();
        List<Diagnostic> diagnostics = new LinkedList<>();

        TSLSyntaxError syntaxError = tslDocument.getSyntaxError();
        if (syntaxError != null) {
            diagnosticsParams.setUri(tslDocument.getUri());

            Range range = new Range(
                    new Position(syntaxError.getLine(), syntaxError.getCharacter()),
                    new Position(syntaxError.getLine(), syntaxError.getCharacter()));

            Diagnostic diagnostic = new Diagnostic(range,
                    syntaxError.getMessage(), DiagnosticSeverity.Error, "source.tsl");
            diagnostics.add(diagnostic);
        }

        diagnosticsParams.setDiagnostics(diagnostics);
        server.getClient().publishDiagnostics(diagnosticsParams);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        OPEN_TSL_DOCUMENTS.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {}

    public static String replaceIndices(String text, int beginIndex, int endIndex, String replacement) {
        String beginning = text.substring(0, beginIndex);
        String ending = text.substring(endIndex + 1);
        return beginning + replacement + ending;
    }

}
