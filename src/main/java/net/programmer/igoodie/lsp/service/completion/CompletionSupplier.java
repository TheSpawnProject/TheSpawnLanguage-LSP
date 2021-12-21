package net.programmer.igoodie.lsp.service.completion;

import net.programmer.igoodie.goodies.util.StringUtilities;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.util.MDUtils;
import net.programmer.igoodie.lsp.util.extra.TSLCompletionItem;
import net.programmer.igoodie.tsl.definition.TSLAction;
import net.programmer.igoodie.tsl.definition.TSLEvent;
import net.programmer.igoodie.tsl.definition.attribute.TSLTag;
import net.programmer.igoodie.tsl.parser.TSLParser;
import net.programmer.igoodie.tsl.parser.snippet.TSLCaptureSnippet;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLCaptureCall;
import net.programmer.igoodie.tsl.parser.token.TSLString;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import net.programmer.igoodie.tsl.util.CollectionUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@FunctionalInterface
public interface CompletionSupplier {

    List<CompletionItem> supplyCompletion(CompletionParams params, TSLDocument tslDocument);

    default List<CompletionItem> getActionKeywords(TSLDocument tslDocument) {
        return tslDocument.getLanguage().ACTION_REGISTRY.stream()
                .map(entry -> {
                    String actionName = StringUtilities.allUpper(entry.getKey());
                    TSLAction action = entry.getValue();
                    TSLCompletionItem completionItem = new TSLCompletionItem();
                    completionItem.setLabel(actionName);
                    completionItem.setDetail("Action: " + actionName);
                    completionItem.setInsertTextOfDefinition(action);
                    completionItem.setDocumentation(MDUtils.build(
                            "Usage of the action:",
                            MDUtils.codeSnippet(action.getUsage()))
                    );
                    completionItem.setKind(CompletionItemKind.Keyword);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getCaptures(TSLDocument tslDocument) {
        return tslDocument.getParsedCaptureSnippets().entrySet().stream()
                .map((entry) -> {
                    String captureName = entry.getKey();
                    TSLCaptureSnippet snippet = entry.getValue();
                    TSLCaptureCall headerToken = snippet.getHeaderToken();
                    List<String> headerArgs = headerToken.getArgs();
                    TSLCompletionItem completionItem = new TSLCompletionItem();
                    StringBuilder builder = new StringBuilder("\\$" + captureName);
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
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getTagKeywords(TSLDocument tslDocument) {
        return tslDocument.getLanguage().TAG_REGISTRY.stream()
                .map(entry -> {
                    String tagName = StringUtilities.allUpper(entry.getKey());
                    TSLTag tag = entry.getValue();
                    TSLCompletionItem completionItem = new TSLCompletionItem();
                    completionItem.setLabel(tagName);
                    completionItem.setDetail("Tag: " + tagName);
                    completionItem.setInsertTextOfDefinition(tag);
                    completionItem.setKind(CompletionItemKind.Keyword);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getEventNames(TSLDocument tslDocument) {
        return tslDocument.getLanguage().EVENT_REGISTRY.stream()
                .map(entry -> {
                    String eventName = StringUtilities.upperFirstLetters(entry.getKey());
                    TSLCompletionItem completionItem = new TSLCompletionItem();
                    completionItem.setLabel(eventName);
                    completionItem.setDetail("Event: " + eventName);
                    completionItem.setDocumentation(MDUtils.build(

                    ));
                    completionItem.setKind(CompletionItemKind.Event);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getEventFields(TSLDocument tslDocument, TSLSnippetBuffer snippetBuffer) {
        List<TSLToken> tokens = snippetBuffer.getTokens();
        int onIndex = CollectionUtils.indexOfBy(tokens, token -> token.getRaw().equalsIgnoreCase("ON"));
        int withIndex = CollectionUtils.indexOfBy(tokens, token -> token.getRaw().equalsIgnoreCase("WITH"));

        List<TSLString> eventNameTokens = TSLParser.getEventNameTokens(tokens, onIndex, withIndex);
        String eventName = eventNameTokens.stream().map(TSLString::getRaw).collect(Collectors.joining(" "));

        TSLEvent tslEvent = tslDocument.getLanguage().EVENT_REGISTRY.get(eventName);

        if (tslEvent == null) {
            return Collections.emptyList();
        }

        return tslEvent.getAcceptedFields().entrySet().stream()
                .map(entry -> {
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
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getFunctions(TSLDocument tslDocument) {
        return tslDocument.getLanguage().FUNCTION_REGISTRY.stream().map(Map.Entry::getValue)
                .map(function -> {
                    TSLCompletionItem completionItem = new TSLCompletionItem();
                    completionItem.setLabel(function.getName());
                    completionItem.setDetail("Function: " + function.getName());
                    completionItem.setInsertTextOfDefinition(function);
                    completionItem.setDocumentation(MDUtils.build(
                            "DOCS: TO BE IMPLEMENTED"
                    ));
                    completionItem.setKind(CompletionItemKind.Function);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default CompletionItem getKeyword(String keyword) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(keyword);
        completionItem.setDetail("Keyword: " + keyword);
        completionItem.setKind(CompletionItemKind.Keyword);
        return completionItem;
    }

}
