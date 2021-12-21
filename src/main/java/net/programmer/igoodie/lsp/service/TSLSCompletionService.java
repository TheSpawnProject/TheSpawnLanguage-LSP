package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.goodies.util.builder.InlineMapBuilder;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.service.completion.CompletionSupplier;
import net.programmer.igoodie.lsp.service.completion.snippet.CaptureCompletionSupplier;
import net.programmer.igoodie.lsp.service.completion.snippet.RuleCompletionSupplier;
import net.programmer.igoodie.lsp.service.completion.snippet.TagCompletionSupplier;
import net.programmer.igoodie.lsp.service.completion.token.ExpressionCompletionSupplier;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLExpression;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TSLSCompletionService {

    public static final Map<TSLSnippetBuffer.Type, CompletionSupplier> SNIPPET_COMPLETION_SUPPLIERS = InlineMapBuilder.<TSLSnippetBuffer.Type, CompletionSupplier>of(HashMap::new)
            .entry(TSLSnippetBuffer.Type.TAG, new TagCompletionSupplier())
            .entry(TSLSnippetBuffer.Type.RULE, new RuleCompletionSupplier())
            .entry(TSLSnippetBuffer.Type.CAPTURE, new CaptureCompletionSupplier())
            .build();

    public static final Map<Class<?>, CompletionSupplier> TOKEN_COMPLETION_SUPPLIERS = InlineMapBuilder.<Class<?>, CompletionSupplier>of(HashMap::new)
            .entry(TSLExpression.class, new ExpressionCompletionSupplier())
            .build();

    public List<CompletionItem> generateCompletions(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());

        placement.getSnippetType().map(SNIPPET_COMPLETION_SUPPLIERS::get)
                .ifPresent(supplier -> completionList.addAll(supplier.supplyCompletion(params, tslDocument)));

        placement.getLandedToken().map(Object::getClass).map(TOKEN_COMPLETION_SUPPLIERS::get)
                .ifPresent(supplier -> completionList.addAll(supplier.supplyCompletion(params, tslDocument)));

        return completionList;
    }

}
