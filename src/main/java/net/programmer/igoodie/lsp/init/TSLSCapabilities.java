package net.programmer.igoodie.lsp.init;

import net.programmer.igoodie.goodies.registry.Registry;
import net.programmer.igoodie.lsp.capability.*;

public class TSLSCapabilities {

    public final static Registry<Class<?>, Capabilities<?>> REGISTRY = new Registry<>();

    public static final TSLSHoverCapabilities HOVER = REGISTRY.register(new TSLSHoverCapabilities());
    public static final TSLSCompletionCapabilities COMPLETION = REGISTRY.register(new TSLSCompletionCapabilities());
    public static final TSLSSemanticTokenCapabilities SEMANTIC_TOKENS = REGISTRY.register(new TSLSSemanticTokenCapabilities());
    public static final TSLSOnTypeFormattingCapabilities ON_TYPE_FORMATTING = REGISTRY.register(new TSLSOnTypeFormattingCapabilities());
    public static final TSLSFoldingRangeCapabilities FOLDING_RANGE = REGISTRY.register(new TSLSFoldingRangeCapabilities());

}
