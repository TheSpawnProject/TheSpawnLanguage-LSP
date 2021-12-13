package net.programmer.igoodie.lsp.init;

import net.programmer.igoodie.lsp.capability.*;
import net.programmer.igoodie.registry.Registry;

public class TSLSCapabilities {

    public final static Registry<Class<?>, Capabilities<?>> REGISTRY = new Registry<>();

    public static final TSLSHoverCapabilities HOVER = REGISTRY.register(new TSLSHoverCapabilities());
    public static final TSLSCompletionCapabilities COMPLETION = REGISTRY.register(new TSLSCompletionCapabilities());
    public static final TSLSSemanticTokenCapabilities SEMANTIC_TOKENS = REGISTRY.register(new TSLSSemanticTokenCapabilities());
    public static final TSLSOnTypeFormattingCapabilities ON_TYPE_FORMATTING = REGISTRY.register(new TSLSOnTypeFormattingCapabilities());

}
