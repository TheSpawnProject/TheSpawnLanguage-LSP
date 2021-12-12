package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.TSLServer;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.data.TSLSOpenDocuments;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticTokens;
import net.programmer.igoodie.tsl.parser.token.TSLCaptureCall;
import net.programmer.igoodie.util.StringUtilities;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TSLSTextDocumentService implements TextDocumentService {

    private final TSLServer server;
    private final TSLSDiagnosticService diagnosticService;

    private final TSLSOpenDocuments openDocuments;

    public TSLSTextDocumentService(TSLServer server) {
        this.server = server;
        this.diagnosticService = new TSLSDiagnosticService();
        this.openDocuments = new TSLSOpenDocuments();
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionList = new LinkedList<>();

            TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());

//            {
//                CompletionItem completionItem = new CompletionItem();
//                completionItem.setLabel("tsl:word_debug");
//                completionItem.setDetail(tslDocument.getWord(param.get));
//                completionItem.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN,
//                        "```tsl\n" + "\n```"));
//                completionItem.setKind(CompletionItemKind.Constant);
//                completionList.add(completionItem);
//            }

            tslDocument.getLanguage().ACTION_REGISTRY.stream().forEach(entry -> {
                String actionName = StringUtilities.allUpper(entry.getKey());
                CompletionItem completionItem = new CompletionItem();
                completionItem.setLabel(actionName);
                completionItem.setDetail("Action: " + actionName);
                completionItem.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN,
                        "```tsl\n" + "\n```"));
                completionItem.setKind(CompletionItemKind.Constant);
                completionList.add(completionItem);
            });

            tslDocument.getCaptureSnippets().forEach((captureName, snippet) -> {
                TSLCaptureCall headerToken = snippet.getHeaderToken();
                List<String> headerArgs = headerToken.getArgs();
                CompletionItem completionItem = new CompletionItem();
                StringBuilder builder = new StringBuilder(snippet.getName().replaceAll("\\$", "\\$"));
                if (headerArgs != null && headerArgs.size() != 0) {
                    builder.append('(');
                    for (int i = 0; i < headerArgs.size(); i++) {
                        builder.append("${")
                                .append(i + 1)
                                .append(":")
                                .append(headerArgs.get(i))
                                .append("}");
                        if (i != headerArgs.size() - 1) {
                            builder.append(", ");
                        }
                    }
                    builder.append(")");
                }
                completionItem.setInsertText(builder.toString());
                completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
                completionItem.setLabel("$" + captureName);
                completionItem.setDetail("Detail text here");
                completionItem.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN,
                        snippet.getCapturedTokens().stream()
                                .map(token -> token.getRaw().replaceAll("`", "\\`"))
                                .collect(Collectors.joining(" ", "```tsl\n = ", "\n```"))));
                completionItem.setKind(CompletionItemKind.Function);
                completionList.add(completionItem);
            });

            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel("tsl:semantic_debug");
            TSLSSemanticTokens semanticTokens = tslDocument.generateSemanticTokens();
            completionItem.setInsertText(semanticTokens.debugString());
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
        TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
        SemanticTokens serialized = tslDocument.generateSemanticTokens().serialize();
        return CompletableFuture.completedFuture(serialized);
    }

    /* -------------------------------------- */

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
        PublishDiagnosticsParams diagnosticsParams = diagnosticService.diagnose(tslDocument);
        server.getClient().publishDiagnostics(diagnosticsParams);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
        for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
            tslDocument.setText(change.getText());
        }
        PublishDiagnosticsParams diagnosticsParams = diagnosticService.diagnose(tslDocument);
        server.getClient().publishDiagnostics(diagnosticsParams);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String documentUri = params.getTextDocument().getUri();
        openDocuments.remove(documentUri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {}

}
