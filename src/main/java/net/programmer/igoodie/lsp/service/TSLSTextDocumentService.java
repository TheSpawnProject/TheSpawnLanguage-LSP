package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.TSLServer;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.data.TSLSOpenDocuments;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TSLSTextDocumentService implements TextDocumentService {

    private final TSLServer server;
    private final TSLSDiagnosticService diagnosticService;
    private final TSLSCompletionService completionService;

    private final TSLSOpenDocuments openDocuments;

    public TSLSTextDocumentService(TSLServer server) {
        this.server = server;
        this.diagnosticService = new TSLSDiagnosticService();
        this.completionService = new TSLSCompletionService();
        this.openDocuments = new TSLSOpenDocuments();
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionList = new LinkedList<>();

            TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
            CursorPlacement placement = tslDocument.getPlacement(params.getPosition());

            if (placement.getSnippetType().orElse(null) == TSLSnippetBuffer.Type.TAG) {
//                completionList.addAll(completionService.getTagKeywords(params, tslDocument));

            } else if (placement.getSnippetType().orElse(null) == TSLSnippetBuffer.Type.RULE) {
//                completionList.addAll(completionService.getActionKeywords(params, tslDocument));
//                completionList.addAll(completionService.getCaptures(params, tslDocument));

            } else if(placement.getSnippetType().orElse(null) == TSLSnippetBuffer.Type.CAPTURE) {
                completionList.addAll(completionService.getActionKeywords(params, tslDocument));
                completionList.addAll(completionService.getCaptures(params, tslDocument));

            } else {

            }

//            CompletionItem completionItem = new CompletionItem();
//            completionItem.setLabel("tsl:semantic_debug");
//            TSLSSemanticTokens semanticTokens = tslDocument.generateSemanticTokens();
//            completionItem.setInsertText(semanticTokens.debugString());
//            completionItem.setKind(CompletionItemKind.Module);
//            completionList.add(completionItem);

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
