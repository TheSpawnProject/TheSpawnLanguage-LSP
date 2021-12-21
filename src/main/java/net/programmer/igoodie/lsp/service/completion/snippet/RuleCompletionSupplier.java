package net.programmer.igoodie.lsp.service.completion.snippet;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.service.completion.CompletionSupplier;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.tsl.parser.token.TSLExpression;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RuleCompletionSupplier implements CompletionSupplier {

    @Override
    public List<CompletionItem> supplyCompletion(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());

        Optional<TSLToken> previousToken = placement.getPreviousToken();
        if (previousToken.isPresent() && previousToken.get().getRaw().equalsIgnoreCase("ON")) {
            completionList.addAll(getEventNames(tslDocument));

        } else if (previousToken.isPresent() && previousToken.get().getRaw().equalsIgnoreCase("WITH")) {
            if (placement.getSnippetBuffer().isPresent()) {
                completionList.addAll(getEventFields(tslDocument, placement.getSnippetBuffer().get()));
            }

        } else {
            completionList.addAll(getActionCompletions(tslDocument));
            if (!placement.getLandedToken().filter(token -> token instanceof TSLExpression).isPresent()) {
                completionList.addAll(getCaptures(tslDocument));
            }
            completionList.add(getKeyword("ON"));
            completionList.add(getKeyword("WITH"));
            completionList.add(getKeyword("DISPLAYING"));
        }

        return completionList;
    }

}
