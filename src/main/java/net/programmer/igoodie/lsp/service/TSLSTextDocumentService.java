package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.TSLServer;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.data.TSLSOpenDocuments;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLExpression;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
                completionList.addAll(completionService.getTagKeywords(params, tslDocument));

            } else if (placement.getSnippetType().orElse(null) == TSLSnippetBuffer.Type.RULE) {
                Optional<TSLToken> previousToken = placement.getPreviousToken();
                if (previousToken.isPresent() && previousToken.get().getRaw().equalsIgnoreCase("ON")) {
                    completionList.addAll(completionService.getEventNames(params, tslDocument));

                } else if (previousToken.isPresent() && previousToken.get().getRaw().equalsIgnoreCase("WITH")) {
                    if (placement.getSnippetBuffer().isPresent()) {
                        completionList.addAll(completionService.getEventFields(params, tslDocument, placement.getSnippetBuffer().get()));
                    }

                } else {
                    completionList.addAll(completionService.getActionKeywords(params, tslDocument));
                    if (!placement.getLandedToken().filter(token -> token instanceof TSLExpression).isPresent()) {
                        completionList.addAll(completionService.getCaptures(params, tslDocument));
                    }
                    completionList.add(completionService.getKeyword("ON"));
                    completionList.add(completionService.getKeyword("WITH"));
                    completionList.add(completionService.getKeyword("DISPLAYING"));
                }

            } else if (placement.getSnippetType().orElse(null) == TSLSnippetBuffer.Type.CAPTURE) {
                completionList.addAll(completionService.getActionKeywords(params, tslDocument));
                if (!placement.getLandedToken().filter(token -> token instanceof TSLExpression).isPresent()) {
                    completionList.addAll(completionService.getCaptures(params, tslDocument));
                }
            }

            return Either.forLeft(completionList);
        });
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        Hover hover = new Hover();

        TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());

        hover.setContents(placement.debugContent());

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
