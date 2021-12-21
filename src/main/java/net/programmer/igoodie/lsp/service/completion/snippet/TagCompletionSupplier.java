package net.programmer.igoodie.lsp.service.completion.snippet;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.service.completion.CompletionSupplier;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.List;

public class TagCompletionSupplier implements CompletionSupplier {

    @Override
    public List<CompletionItem> supplyCompletion(CompletionParams params, TSLDocument tslDocument) {
        return getTagKeywords(tslDocument);
    }

}
