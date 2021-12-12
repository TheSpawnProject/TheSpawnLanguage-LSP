package net.programmer.igoodie.lsp.data;

import net.programmer.igoodie.lsp.capability.TSLSSemanticTokenCapabilities;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticToken;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticTokens;
import net.programmer.igoodie.tsl.TheSpawnLanguage;
import net.programmer.igoodie.tsl.exception.TSLSyntaxError;
import net.programmer.igoodie.tsl.parser.TSLParser;
import net.programmer.igoodie.tsl.parser.snippet.TSLCaptureSnippet;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippet;
import net.programmer.igoodie.tsl.parser.token.TSLExpression;
import net.programmer.igoodie.tsl.parser.token.TSLSymbol;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import net.programmer.igoodie.tsl.runtime.TSLRuleset;
import net.programmer.igoodie.tsl.util.ExpressionUtils;
import net.programmer.igoodie.util.ArrayAccessor;
import org.eclipse.lsp4j.Position;

import java.util.List;
import java.util.Map;

public class TSLDocument {

    protected String uri;
    protected String[] lines;

    protected TheSpawnLanguage tsl = new TheSpawnLanguage();
    protected TSLRuleset ruleset = new TSLParser(tsl).parse("");
    protected TSLSyntaxError syntaxError;

    public TSLDocument(String uri) {
        this(uri, "");
    }

    public TSLDocument(String uri, String text) {
        this.uri = uri;
        setText(text);
    }

    public void setText(String text) {
        this.lines = text.split("\r?\n");
        try {
            this.syntaxError = null;
            this.ruleset = new TSLParser(tsl).parse(text);

        } catch (TSLSyntaxError syntaxError) {
            this.syntaxError = syntaxError;
        }
    }

    public String getUri() {
        return uri;
    }

    public String[] getLines() {
        return lines;
    }

    public String getWord(Position position) {
        String line = ArrayAccessor.of(lines).getOrDefault(position.getLine(), "");
        int start = 0;
        int end = 0;
        for (int i = 0; i < line.length(); i++) {
            char prev = i == 0 ? '\0' : line.charAt(i - 1);
            char current = line.charAt(i);

            if (current != ' ' && Character.isWhitespace(prev)) {
                start = i;
            }

            end = i;

            if (position.getCharacter() == i) {
                break;
            }
        }
        return start == end ? "" : line.substring(start, end);
    }

    public TheSpawnLanguage getLanguage() {
        return tsl;
    }

    public TSLSyntaxError getSyntaxError() {
        return syntaxError;
    }

    public List<TSLSnippet> getAllSnippets() {
        return ruleset.getSnippets();
    }

    public Map<String, TSLCaptureSnippet> getCaptureSnippets() {
        return ruleset.getCaptures();
    }

    public TSLSSemanticTokens generateSemanticTokens() {
        TSLSSemanticTokens semanticTokens = new TSLSSemanticTokens();
        if (ruleset == null) return semanticTokens;
        for (TSLSnippet snippet : ruleset.getSnippets()) {
            for (TSLToken token : snippet.getAllTokens()) {
                if (token instanceof TSLSymbol) {
                    semanticTokens.addToken(new TSLSSemanticToken()
                            .setDebug(token.getRaw())
                            .setLine(token.getLine())
                            .setCharacter(token.getCharacter())
                            .setLength(token.getRaw().length())
                            .setType(TSLSSemanticTokenCapabilities.TokenTypes.FUNCTION)
                            .setModifier(0));

                } else if (token instanceof TSLExpression) {
                    TSLExpression expression = (TSLExpression) token;
                    ExpressionUtils.traverseMatches(expression.getExpression(), ExpressionUtils.CAPTURE_PARAMETER_PATTERN, (start, end) -> {
                        int length = end - start;
                        semanticTokens.addToken(new TSLSSemanticToken()
                                .setDebug(token.getRaw().substring(start, end - 1))
                                .setLine(token.getLine())
                                .setCharacter(token.getCharacter() + 2 + start)
                                .setLength(length)
                                .setType(TSLSSemanticTokenCapabilities.TokenTypes.VARIABLE)
                                .setModifier(0));
                    });
                }
            }
        }
        return semanticTokens;
    }

}
