package net.programmer.igoodie.lsp.capability;

import org.eclipse.lsp4j.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TSLSSemanticTokenCapabilities extends Capabilities<SemanticTokensWithRegistrationOptions> {

    public enum TokenTypes {
        COMMENT(0, SemanticTokenTypes.Comment),
        FUNCTION(1, SemanticTokenTypes.Function),
        VARIABLE(2, SemanticTokenTypes.Variable),
        KEYWORD(3, SemanticTokenTypes.Keyword),
        ;

        private final int id;
        private final String value;

        TokenTypes(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public enum TokenTypeModifiers {
//        ABSTRACT(1, SemanticTokenModifiers.Abstract),
//        READONLY(1 << 1, SemanticTokenModifiers.Readonly),
//        DOCUMENTATION(1 << 2, SemanticTokenModifiers.Documentation)
        ;

        private final int id;
        private final String value;

        TokenTypeModifiers(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public static List<String> getTokenTypes() {
        return Arrays.stream(TokenTypes.values())
                .map(TokenTypes::getValue)
                .collect(Collectors.toList());
    }

    public static List<String> getTokenTypeModifiers() {
        return Arrays.stream(TokenTypeModifiers.values())
                .map(TokenTypeModifiers::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public SemanticTokensWithRegistrationOptions buildOptions() {
        SemanticTokensLegend legend = new SemanticTokensLegend(getTokenTypes(), getTokenTypeModifiers());
        SemanticTokensWithRegistrationOptions options = new SemanticTokensWithRegistrationOptions(legend);
        options.setFull(new SemanticTokensServerFull());
        return options;
    }

    @Override
    public void register(ServerCapabilities serverCapabilities, SemanticTokensWithRegistrationOptions options) {
        serverCapabilities.setSemanticTokensProvider(options);
    }

}
