package net.programmer.igoodie.lsp.data;

import net.programmer.igoodie.tsl.TheSpawnLanguage;
import net.programmer.igoodie.tsl.exception.TSLSyntaxError;
import net.programmer.igoodie.tsl.parser.TSLParser;
import net.programmer.igoodie.tsl.parser.snippet.TSLCaptureSnippet;
import net.programmer.igoodie.tsl.runtime.TSLRuleset;

import java.util.Map;

public class TSLDocument {

    public static final TheSpawnLanguage TSL = new TheSpawnLanguage();

    protected String uri;
    protected String text;
    protected TSLRuleset ruleset;
    protected TSLSyntaxError syntaxError;

    public TSLDocument(String uri) {
        this(uri, "");
    }

    public TSLDocument(String uri, String text) {
        this.uri = uri;
        setText(text);
    }

    public void setText(String text) {
        this.text = text;
        try {
            this.ruleset = new TSLParser(TSL).parse(text);
            this.syntaxError = null;

        } catch (TSLSyntaxError syntaxError) {
            this.syntaxError = syntaxError;
            this.ruleset = null;
        }
    }

    public String getUri() {
        return uri;
    }

    public TSLSyntaxError getSyntaxError() {
        return syntaxError;
    }

    public Map<String, TSLCaptureSnippet> getCaptureSnippets() {
        return ruleset.getCaptures();
    }

}
