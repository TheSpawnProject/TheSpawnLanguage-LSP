package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.lsp.util.MDUtils;
import net.programmer.igoodie.tsl.parser.token.TSLCaptureCall;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import net.programmer.igoodie.util.StringUtilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.LinkedList;
import java.util.List;

public class TSLSCompletionService {

    public List<CompletionItem> getActionKeywords(CompletionParams params, TSLDocument tslDocument) {
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());
        List<CompletionItem> completionList = new LinkedList<>();
        tslDocument.getLanguage().ACTION_REGISTRY.stream().forEach(entry -> {
            String actionName = StringUtilities.allUpper(entry.getKey());
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(actionName);
            completionItem.setDetail("Action: " + actionName);
            completionItem.setKind(CompletionItemKind.Keyword);
            completionItem.setDocumentation(
                    placement.getPreviousToken().orElse(null) + "" +
                            placement.getLandedToken().orElse(null) + "" +
                            placement.getNextToken().orElse(null) + ""
            );
            completionList.add(completionItem);
        });
        return completionList;
    }

    public List<CompletionItem> getCaptures(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        tslDocument.getParsedCaptureSnippets().forEach((captureName, snippet) -> {
            CompletionItem completionItem = new CompletionItem();
            TSLCaptureCall headerToken = snippet.getHeaderToken();
            List<String> headerArgs = headerToken.getArgs();
            StringBuilder builder = new StringBuilder(snippet.getName().replaceAll("\\$", "\\$"));
            if (headerArgs != null && headerArgs.size() != 0) {
                builder.append('(');
                for (int i = 0; i < headerArgs.size(); i++) {
                    builder.append("${").append(i + 1).append(":").append(headerArgs.get(i)).append("}");
                    if (i != headerArgs.size() - 1) {
                        builder.append(", ");
                    }
                }
                builder.append(")");
            }
            completionItem.setInsertText(builder.toString());
            completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
            completionItem.setLabel("$" + captureName);
            completionItem.setDetail("Capture $" + captureName);
            completionItem.setDocumentation(MDUtils.build(
                    "Source of the capture:",
                    MDUtils.codeSnippet(snippet.getCapturedTokens(), TSLToken::getRaw)
            ));
            completionItem.setKind(CompletionItemKind.Function);
            completionList.add(completionItem);
        });
        return completionList;
    }

    public List<CompletionItem> getTagKeywords(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        tslDocument.getLanguage().TAG_REGISTRY.stream().forEach(entry -> {
            String actionName = StringUtilities.allUpper(entry.getKey());
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(actionName);
            completionItem.setDetail("Tag: " + actionName);
            completionItem.setKind(CompletionItemKind.Keyword);
            completionList.add(completionItem);
        });
        return completionList;
    }

}
