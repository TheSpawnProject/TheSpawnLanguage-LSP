package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.lsp.util.MDUtils;
import net.programmer.igoodie.tsl.definition.TSLAction;
import net.programmer.igoodie.tsl.definition.TSLEvent;
import net.programmer.igoodie.tsl.parser.TSLParser;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLCaptureCall;
import net.programmer.igoodie.tsl.parser.token.TSLString;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import net.programmer.igoodie.tsl.util.CollectionUtils;
import net.programmer.igoodie.util.StringUtilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TSLSCompletionService {

    public List<CompletionItem> getActionKeywords(CompletionParams params, TSLDocument tslDocument) {
        CursorPlacement placement = tslDocument.getPlacement(params.getPosition());
        List<CompletionItem> completionList = new LinkedList<>();
        tslDocument.getLanguage().ACTION_REGISTRY.stream().forEach(entry -> {
            String actionName = StringUtilities.allUpper(entry.getKey());
            TSLAction action = entry.getValue();
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(actionName);
            completionItem.setDetail("Action: " + actionName);
            completionItem.setDocumentation(MDUtils.build(
                    "Usage of the action:",
                    MDUtils.codeSnippet(action.getUsage()))
            );
            completionItem.setKind(CompletionItemKind.Keyword);
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
//            completionItem.setDocumentation(MDUtils.build(
//                    "Usage of the action:",
//                    MDUtils.codeSnippet(entry.getValue().getUsage()))
//            );
            completionItem.setKind(CompletionItemKind.Keyword);
            completionList.add(completionItem);
        });
        return completionList;
    }

    public List<CompletionItem> getEventNames(CompletionParams params, TSLDocument tslDocument) {
        List<CompletionItem> completionList = new LinkedList<>();
        tslDocument.getLanguage().EVENT_REGISTRY.stream().forEach(entry -> {
            String eventName = StringUtilities.upperFirstLetters(entry.getKey());
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(eventName);
            completionItem.setDetail("Event: " + eventName);
            completionItem.setDocumentation(MDUtils.build(

            ));
            completionItem.setKind(CompletionItemKind.Event);
            completionList.add(completionItem);
        });
        return completionList;
    }

    public List<CompletionItem> getEventFields(CompletionParams params, TSLDocument tslDocument, TSLSnippetBuffer snippetBuffer) {
        List<CompletionItem> completionList = new LinkedList<>();

        List<TSLToken> tokens = snippetBuffer.getTokens();
        int onIndex = CollectionUtils.indexOfBy(tokens, token -> token.getRaw().equalsIgnoreCase("ON"));
        int withIndex = CollectionUtils.indexOfBy(tokens, token -> token.getRaw().equalsIgnoreCase("WITH"));

        List<TSLString> eventNameTokens = TSLParser.getEventNameTokens(tokens, onIndex, withIndex);
        String eventName = eventNameTokens.stream().map(TSLString::getRaw).collect(Collectors.joining(" "));

        TSLEvent tslEvent = tslDocument.getLanguage().EVENT_REGISTRY.get(eventName);

        if (tslEvent != null) {
            for (Map.Entry<String, Class<?>> entry : tslEvent.getAcceptedFields().entrySet()) {
                String eventFieldName = entry.getKey();
                Class<?> eventFieldType = entry.getValue();
                CompletionItem completionItem = new CompletionItem();
                completionItem.setLabel(eventFieldName);
                completionItem.setDetail("Event Field: " + eventFieldName);
                completionItem.setDocumentation(MDUtils.build(
                        "Name of the field:",
                        ">" + eventFieldName,
                        "",
                        "Type of the field:",
                        ">" + eventFieldType.getSimpleName(),
                        "",
                        "Associated event:",
                        ">" + StringUtilities.upperFirstLetters(eventName)
                ));
                completionItem.setKind(CompletionItemKind.Property);
                completionList.add(completionItem);
            }
        }

        return completionList;
    }

    public CompletionItem getKeyword(String keyword) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(keyword);
        completionItem.setDetail("Keyword: " + keyword);
        completionItem.setKind(CompletionItemKind.Keyword);
        return completionItem;
    }

}
