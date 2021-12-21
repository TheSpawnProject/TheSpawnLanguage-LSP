package net.programmer.igoodie.lsp.capability;

import org.eclipse.lsp4j.FoldingRangeProviderOptions;
import org.eclipse.lsp4j.ServerCapabilities;

public class TSLSFoldingRangeCapabilities extends Capabilities<FoldingRangeProviderOptions> {

    @Override
    public FoldingRangeProviderOptions buildOptions() {
        return new FoldingRangeProviderOptions();
    }

    @Override
    public void register(ServerCapabilities serverCapabilities, FoldingRangeProviderOptions options) {
        serverCapabilities.setFoldingRangeProvider(options);
    }

}
