package net.programmer.igoodie.lsp.capability;

import org.eclipse.lsp4j.DocumentOnTypeFormattingOptions;
import org.eclipse.lsp4j.ServerCapabilities;

public class TSLSOnTypeFormattingCapabilities extends Capabilities<DocumentOnTypeFormattingOptions> {

    @Override
    public DocumentOnTypeFormattingOptions buildOptions() {
        return new DocumentOnTypeFormattingOptions("\n");
    }

    @Override
    public void register(ServerCapabilities serverCapabilities, DocumentOnTypeFormattingOptions options) {
        serverCapabilities.setDocumentOnTypeFormattingProvider(options);
    }

}
