package net.programmer.igoodie.lsp.data;

import net.programmer.igoodie.goodies.util.accessor.ArrayAccessor;
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

    private TSLSnippetBuffer[] lineBuffers;
    private TSLSnippetBuffer.Type[] lineTypes;

    public TSLDocument(String uri) {
        this(uri, "");
    }

    public TSLDocument(String uri, String text) {
        this.uri = uri;
        setText(text);
    }

    public String getUri() {
        return uri;
    }

    public String[] getLines() {
        return lines;
    }

    public TheSpawnLanguage getLanguage() {
        return tsl;
    }

    public TSLSyntaxError getSyntaxError() {
        return syntaxError;
    }

    public TSLSnippetBuffer[] getLineBuffers() {
        return lineBuffers;
    }

    public TSLSnippetBuffer.Type[] getLineTypes() {
        return lineTypes;
    }

    /* ------------------------------------ */

    public List<TSLSnippetBuffer> getSnippetBuffers() {
        return lexer.getSnippets();
    }

    public Map<String, TSLCaptureSnippet> getParsedCaptureSnippets() {
        return ruleset.getCaptures();
    }

    /* ------------------------------------ */

    public void setText(String text) {
        this.lines = text.split("\\r?\\n");
        try {
            this.syntaxError = null;
            this.lexer = new TSLLexer(text).lex();
            this.ruleset = new TSLParser(tsl).parse(text);

        } catch (TSLSyntaxError syntaxError) {
            this.syntaxError = syntaxError;
        }
        this.analyzeDocument();
    }

    public CursorPlacement getPlacement(Position position) {
        return new CursorPlacement(this, position);
    }

    /* ------------------------------------ */

    private void analyzeDocument() {
        lineBuffers = new TSLSnippetBuffer[lines.length];
        lineTypes = new TSLSnippetBuffer.Type[lines.length];
        ArrayAccessor<TSLSnippetBuffer> lineBuffersAccessor = ArrayAccessor.of(lineBuffers);
        ArrayAccessor<TSLSnippetBuffer.Type> lineTypesAccessor = ArrayAccessor.of(lineTypes);

        for (TSLSnippetBuffer snippetBuffer : this.getSnippetBuffers()) {
            List<TSLToken> tokens = snippetBuffer.getTokens();
            for (TSLToken token : tokens) {
                int line = token.getLine();
                lineBuffersAccessor.set(line - 1, snippetBuffer);
                lineTypesAccessor.set(line - 1, snippetBuffer.getType());
            }
        }
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
