package net.programmer.igoodie.lsp.init;

import net.programmer.igoodie.lsp.capability.Capabilities;
import net.programmer.igoodie.lsp.capability.TSLSCompletionCapabilities;
import net.programmer.igoodie.lsp.capability.TSLSSemanticTokenCapabilities;
import net.programmer.igoodie.registry.Registry;

public class TSLSCapabilities {

    public final static Registry<Class<?>, Capabilities<?>> REGISTRY = new Registry<>();

    public static final TSLSCompletionCapabilities COMPLETION = REGISTRY.register(new TSLSCompletionCapabilities());
    public static final TSLSSemanticTokenCapabilities SEMANTIC_TOKENS = REGISTRY.register(new TSLSSemanticTokenCapabilities());

}
