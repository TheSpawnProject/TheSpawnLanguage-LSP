package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.TSLServer;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticTokens;
import net.programmer.igoodie.tsl.parser.token.TSLCaptureCall;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TSLSTextDocumentService implements TextDocumentService {

    private final Map<String, TSLDocument> OPEN_TSL_DOCUMENTS = new HashMap<>();

    private final TSLServer server;
    private final TSLSDiagnosticService diagnosticService;

    public TSLSTextDocumentService(TSLServer server) {
        this.server = server;
        this.diagnosticService = new TSLSDiagnosticService();
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionList = new LinkedList<>();

//            CompletionItem completionItem2 = new CompletionItem();
//            completionItem2.setLabel("tsldebug:openfiles");
//            StringBuilder stringBuilder2 = new StringBuilder();
//            OPEN_TSL_DOCUMENTS.forEach((key, document) -> {
//                stringBuilder2.append(key).append("\n");
//            });
//            completionItem2.setInsertText(stringBuilder2.toString());
//            completionItem2.setKind(CompletionItemKind.Module);
//            completionList.add(completionItem2);

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
                completionItem.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN,
                        snippet.getCapturedTokens().stream()
                                .map(token -> token.getRaw().replaceAll("`", "\\`"))
                                .collect(Collectors.joining(" ", "```", "```"))));
                completionItem.setKind(CompletionItemKind.Function);
                completionList.add(completionItem);
            });

            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel("tsl:debugg");
            StringBuilder stringBuilder = new StringBuilder();
            TSLSSemanticTokens semanticTokens = tslDocument.generateSemanticTokens();
            List<Integer> data = semanticTokens.serialize().getData();
            for (int i = 0; i < data.size() / 5; i++) {
                stringBuilder
                        .append(semanticTokens.getTokens().get(i)).append("\n")
                        .append(data.get(5 * i)).append(" ")
                        .append(data.get(5 * i + 1)).append(" ")
                        .append(data.get(5 * i + 2)).append(" ")
                        .append(data.get(5 * i + 3)).append(" ")
                        .append(data.get(5 * i + 4)).append("\n");
            }
            completionItem.setInsertText(stringBuilder.toString());
            completionItem.setKind(CompletionItemKind.Module);
            completionList.add(completionItem);

            return Either.forLeft(completionList);
        });
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        Hover hover = new Hover();

        List<Either<String, MarkedString>> list = new LinkedList<>();
//        list.add(Either.forLeft("Foo"));
//        list.add(Either.forLeft("Bar"));
//        list.add(Either.forLeft("Baz"));

        hover.setContents(list);

        return CompletableFuture.completedFuture(hover);
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        TSLDocument tslDocument = OPEN_TSL_DOCUMENTS.get(params.getTextDocument().getUri());
        SemanticTokens serialized = tslDocument.generateSemanticTokens().serialize();
        return CompletableFuture.completedFuture(serialized);
    }

    /* -------------------------------------- */

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem textDocument = params.getTextDocument();
        String uri = textDocument.getUri();
        String text = textDocument.getText();

        TSLDocument tslDocument = new TSLDocument(uri, text);

        OPEN_TSL_DOCUMENTS.put(uri, tslDocument);

        PublishDiagnosticsParams diagnosticsParams = diagnosticService.diagnose(tslDocument);
        server.getClient().publishDiagnostics(diagnosticsParams);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        VersionedTextDocumentIdentifier textDocument = params.getTextDocument();
        String uri = textDocument.getUri();
        TSLDocument tslDocument = OPEN_TSL_DOCUMENTS.computeIfAbsent(uri, TSLDocument::new);
        for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
            tslDocument.setText(change.getText());
        }

        PublishDiagnosticsParams diagnosticsParams = diagnosticService.diagnose(tslDocument);
        server.getClient().publishDiagnostics(diagnosticsParams);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // For some reason, when switching tabs, VSCode sends didClose but not didOpen...
        OPEN_TSL_DOCUMENTS.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {}

}
