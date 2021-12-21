package net.programmer.igoodie.lsp.service.completion.token;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.service.completion.CompletionSupplier;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.LinkedList;
import java.util.List;

public class ExpressionCompletionSupplier implements CompletionSupplier {

    @Override
    public List<CompletionItem> supplyCompletion(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        completionList.addAll(getFunctionCompletions(tslDocument));
        return completionList;
    }

}
