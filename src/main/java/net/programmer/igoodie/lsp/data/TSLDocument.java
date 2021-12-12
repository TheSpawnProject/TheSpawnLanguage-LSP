package net.programmer.igoodie.lsp.data;

import net.programmer.igoodie.lsp.capability.TSLSSemanticTokenCapabilities;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticToken;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticTokens;
import net.programmer.igoodie.lsp.util.CursorPlacement;
import net.programmer.igoodie.tsl.TheSpawnLanguage;
import net.programmer.igoodie.tsl.exception.TSLSyntaxError;
import net.programmer.igoodie.tsl.parser.TSLLexer;
import net.programmer.igoodie.tsl.parser.TSLParser;
import net.programmer.igoodie.tsl.parser.snippet.TSLCaptureSnippet;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippet;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLExpression;
import net.programmer.igoodie.tsl.parser.token.TSLSymbol;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import net.programmer.igoodie.tsl.runtime.TSLRuleset;
import net.programmer.igoodie.tsl.util.ExpressionUtils;
import org.eclipse.lsp4j.Position;

import java.util.List;
import java.util.Map;

public class TSLDocument {

    protected String uri;
    protected String[] lines;

    protected TheSpawnLanguage tsl = new TheSpawnLanguage();
    protected TSLRuleset ruleset = new TSLParser(tsl).parse("");
    protected TSLLexer lexer = new TSLLexer("");
    protected TSLSyntaxError syntaxError;

    public TSLDocument(String uri) {
        this(uri, "");
    }

    public TSLDocument(String uri, String text) {
        this.uri = uri;
        setText(text);
    }

    public void setText(String text) {
        this.lines = text.split("\\r?\\n");
        try {
            this.syntaxError = null;
            this.ruleset = new TSLParser(tsl).parse(text);
            this.lexer = new TSLLexer(text).lex();

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

    public CursorPlacement getPlacement(Position position) {
        return new CursorPlacement(this, position);
    }

    public TheSpawnLanguage getLanguage() {
        return tsl;
    }

    public TSLSyntaxError getSyntaxError() {
        return syntaxError;
    }

    public List<TSLSnippetBuffer> getSnippetBuffers() {
        return lexer.getSnippets();
    }

    public Map<String, TSLCaptureSnippet> getParsedCaptureSnippets() {
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
