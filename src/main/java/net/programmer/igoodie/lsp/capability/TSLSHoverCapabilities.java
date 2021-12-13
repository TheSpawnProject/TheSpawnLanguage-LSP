package net.programmer.igoodie.lsp.capability;

import org.eclipse.lsp4j.HoverOptions;
import org.eclipse.lsp4j.ServerCapabilities;

public class TSLSHoverCapabilities extends Capabilities<HoverOptions> {

    @Override
    public HoverOptions buildOptions() {
        return new HoverOptions();
    }

    @Override
    public void register(ServerCapabilities serverCapabilities, HoverOptions options) {
        serverCapabilities.setHoverProvider(options);
    }

}
