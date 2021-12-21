package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.tsl.parser.snippet.TSLRuleSnippet;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippet;
import org.eclipse.lsp4j.FoldingRange;

import java.util.List;
import java.util.stream.Collectors;

public class TSLSFoldingRangeService {

    public List<FoldingRange> getFoldingRanges(TSLDocument tslDocument) {
        return tslDocument.getParsedSnippets().stream()
                .map(this::snippetFoldingRange)
                .collect(Collectors.toList());
    }

    public boolean isFoldable(TSLSnippet snippet) {
        return snippet.getBeginningLine() != snippet.getEndingLine();
    }

    public FoldingRange snippetFoldingRange(TSLSnippet snippet) {
        if (!isFoldable(snippet)) return null;

        if (snippet instanceof TSLRuleSnippet) {
            return snippetFoldingRange(((TSLRuleSnippet) snippet).getActionSnippet());

        } else {
            return new FoldingRange(
                    snippet.getBeginningLine() - 1,
                    snippet.getEndingLine() - 1
            );
        }
    }

}
