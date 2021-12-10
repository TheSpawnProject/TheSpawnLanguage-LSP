package net.programmer.igoodie.lsp;

import net.programmer.igoodie.lsp.service.TSLSTextDocumentService;
import net.programmer.igoodie.lsp.service.TSLSWorkspaceService;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticTokens;
import net.programmer.igoodie.tsl.TheSpawnLanguage;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.concurrent.CompletableFuture;

public class TSLServer implements LanguageServer, LanguageClientAware {

    private int errorCode = 1;

    private LanguageClient client;

    private final TSLSTextDocumentService textDocumentService;
    private final TSLSWorkspaceService workspaceService;

    public TSLServer() {
        this.textDocumentService = new TSLSTextDocumentService(this);
        this.workspaceService = new TSLSWorkspaceService();
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        CompletionOptions completionOptions = new CompletionOptions();
        capabilities.setCompletionProvider(completionOptions);

        HoverOptions hoverOptions = new HoverOptions();
        capabilities.setHoverProvider(hoverOptions);

        capabilities.setSemanticTokensProvider(TSLSSemanticTokens.getSemanticTokensWithRegistrationOptions());

        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setName("TSL");
        serverInfo.setVersion(TheSpawnLanguage.TSL_VERSION);

        InitializeResult result = new InitializeResult(capabilities, serverInfo);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        errorCode = 0; // Exit requested by the client
        return null;
    }

    @Override
    public void exit() {
        System.exit(errorCode);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    public LanguageClient getClient() {
        return client;
    }

}
