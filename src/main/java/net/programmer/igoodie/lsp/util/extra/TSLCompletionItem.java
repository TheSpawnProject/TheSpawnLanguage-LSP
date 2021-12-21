package net.programmer.igoodie.lsp.util.extra;

import net.programmer.igoodie.tsl.definition.TSLDefinition;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

public class TSLCompletionItem extends CompletionItem {

    public void setInsertTextOfDefinition(TSLDefinition definition) {
        if (definition.getCompletion() != null) {
            setInsertText(definition.getCompletion());
            setInsertTextFormat(InsertTextFormat.Snippet);
        }
    }

}
