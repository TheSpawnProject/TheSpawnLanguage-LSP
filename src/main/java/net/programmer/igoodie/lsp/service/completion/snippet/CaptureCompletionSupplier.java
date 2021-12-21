package net.programmer.igoodie.lsp.service.completion.snippet;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.service.completion.CompletionSupplier;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.tsl.parser.token.TSLExpression;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.LinkedList;
import java.util.List;

public class CaptureCompletionSupplier implements CompletionSupplier {

    @Override
    public List<CompletionItem> supplyCompletion(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());

        completionList.addAll(getActionKeywords(tslDocument));

        if (!placement.getLandedToken().filter(token -> token instanceof TSLExpression).isPresent()) {
            completionList.addAll(getCaptures(tslDocument));
        }

        return completionList;
    }

}
