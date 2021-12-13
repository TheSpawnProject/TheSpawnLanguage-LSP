package net.programmer.igoodie.lsp.util;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MDUtils {

    public static MarkupContent build(String... lines) {
        return new MarkupContent(MarkupKind.MARKDOWN, String.join("\n", lines));
    }

    public static <T> String codeSnippet(Collection<T> collection, Function<T, String> stringifier) {
        return collection.stream()
                .map(element -> stringifier.apply(element).replaceAll("`", "\\`"))
                .collect(Collectors.joining(" ", "```tsl\n = ", "\n```"));
    }

    public static String codeSnippet(String tslScript) {
        return "```tsl\n" + tslScript + "\n```";
    }

}
