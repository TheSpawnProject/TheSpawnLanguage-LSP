package net.programmer.igoodie.lsp.service.completion;

import net.programmer.igoodie.goodies.util.Couple;
import net.programmer.igoodie.goodies.util.StringUtilities;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.util.MDUtils;
import net.programmer.igoodie.tsl.definition.TSLAction;
import net.programmer.igoodie.tsl.definition.TSLDefinition;
import net.programmer.igoodie.tsl.definition.TSLEvent;
import net.programmer.igoodie.tsl.definition.TSLFunction;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface CompletionSupplier {

    List<CompletionItem> supplyCompletion(CompletionParams params, TSLDocument tslDocument);

    /* ---------------------------- */

    default CompletionItem completionOfAction(TSLAction action) {
        String actionName = StringUtilities.allUpper(action.getName());
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(actionName);
        completionItem.setDetail("Action: " + actionName);
        completionItem.setDocumentation(MDUtils.build(
                "Usage of the action:",
                MDUtils.codeSnippet(action.getUsage()))
        );
        completionItem.setKind(CompletionItemKind.Keyword);
        return completionItem;
    }

    default CompletionItem completionOfFunction(TSLFunction function) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(function.getName());
        completionItem.setDetail("Function: " + function.getName());
        completionItem.setDocumentation(MDUtils.build(
                "DOCS: TO BE IMPLEMENTED"
        ));
        completionItem.setKind(CompletionItemKind.Keyword);
        return completionItem;
    }

    default List<CompletionItem> completionOfSnippets(TSLDefinition definition, Supplier<CompletionItem> supplier) {
        return completionOfSnippets(definition, supplier, CompletionItemKind.Snippet);
    }

    default List<CompletionItem> completionOfSnippets(TSLDefinition definition, Supplier<CompletionItem> supplier, CompletionItemKind kind) {
        List<Couple<String, String>> completionSnippets = definition.getCompletionSnippets();
        if (completionSnippets == null) return Collections.emptyList();
        return completionSnippets.stream()
                .map(couple -> {
                    CompletionItem completionItem = supplier.get();
                    completionItem.setLabel(couple.getFirst());
                    completionItem.setInsertText(couple.getSecond());
                    completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
                    completionItem.setKind(kind);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> completionOfCaptureParams(TSLCaptureCall captureCall) {
        return captureCall.getArgs().stream()
                .map(arg -> {
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(arg);
                    completionItem.setDetail("Argument: {{" + arg + "}}");
                    completionItem.setInsertText("{{" + arg + "}}");
                    completionItem.setKind(CompletionItemKind.Variable);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    /* ---------------------------- */

    default List<CompletionItem> getActionCompletions(TSLDocument tslDocument) {
        return tslDocument.getLanguage().ACTION_REGISTRY.stream()
                .map(Map.Entry::getValue)
                .flatMap(action -> Stream.concat(
                        completionOfSnippets(action, () -> completionOfAction(action)).stream(),
                        Stream.of(completionOfAction(action))
                ))
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getFunctionCompletions(TSLDocument tslDocument) {
        return tslDocument.getLanguage().FUNCTION_REGISTRY.stream()
                .map(Map.Entry::getValue)
                .flatMap(function -> Stream.concat(
                        completionOfSnippets(function, () -> completionOfFunction(function), CompletionItemKind.Function).stream(),
                        Stream.of(completionOfFunction(function))
                ))
                .collect(Collectors.toList());
    }

    /* ---------------------------- */

    default List<CompletionItem> getCaptures(TSLDocument tslDocument) {
        return tslDocument.getParsedCaptureSnippets().entrySet().stream()
                .map((entry) -> {
                    String captureName = entry.getKey();
                    TSLCaptureSnippet snippet = entry.getValue();
                    TSLCaptureCall headerToken = snippet.getHeaderToken();
                    List<String> headerArgs = headerToken.getArgs();
                    CompletionItem completionItem = new CompletionItem();
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
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(tagName);
                    completionItem.setDetail("Tag: " + tagName);
                    completionItem.setKind(CompletionItemKind.Keyword);
                    return completionItem;
                })
                .collect(Collectors.toList());
    }

    default List<CompletionItem> getEventNames(TSLDocument tslDocument) {
        return tslDocument.getLanguage().EVENT_REGISTRY.stream()
                .map(entry -> {
                    String eventName = StringUtilities.upperFirstLetters(entry.getKey());
                    CompletionItem completionItem = new CompletionItem();
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

    default CompletionItem getKeyword(String keyword) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(keyword);
        completionItem.setDetail("Keyword: " + keyword);
        completionItem.setKind(CompletionItemKind.Keyword);
        return completionItem;
    }

}
