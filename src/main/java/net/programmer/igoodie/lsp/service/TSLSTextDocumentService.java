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
            TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
            return Either.forLeft(completionService.generateCompletions(params, tslDocument));
        });
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());

        Hover hover = new Hover();
        hover.setContents(placement.debugContent());

        return CompletableFuture.completedFuture(hover);
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
        SemanticTokens serialized = tslDocument.generateSemanticTokens().serialize();
        return CompletableFuture.completedFuture(serialized);
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        String typedText = params.getCh();
        List<TextEdit> edits = new LinkedList<>();

        MessageParams messageParams = new MessageParams();
        messageParams.setMessage(typedText);
        messageParams.setType(MessageType.Info);
        server.getClient().showMessage(messageParams);

        if (typedText.startsWith("\n")) {
            Position position = params.getPosition();
            TSLDocument tslDocument = openDocuments.getDocument(params.getTextDocument());
            CursorPlacement placement = tslDocument.getPlacement(position);

            if (placement.getSnippetType().filter(type -> type == TSLSnippetBuffer.Type.COMMENT).isPresent()) {
                TextEdit textEdit = new TextEdit();
                textEdit.setRange(new Range(position, position));
                textEdit.setNewText("<FORMATTINGHERE>");
                edits.add(textEdit);
            }
        }

        return CompletableFuture.completedFuture(edits);
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return TextDocumentService.super.formatting(params); // TODO
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
