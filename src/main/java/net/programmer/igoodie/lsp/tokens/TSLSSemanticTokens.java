package net.programmer.igoodie.lsp.tokens;

import net.programmer.igoodie.tsl.util.ISerializable;
import org.eclipse.lsp4j.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TSLSSemanticTokens implements ISerializable<SemanticTokens> {

    List<TSLSSemanticToken> tokens = new LinkedList<>();

    public void addToken(TSLSSemanticToken token) {
        this.tokens.add(token);
    }

    public List<TSLSSemanticToken> getTokens() {
        return tokens;
    }

    @Override
    public SemanticTokens serialize() {
        List<Integer> data = new LinkedList<>();
        TSLSSemanticToken prevToken = null;
        Collections.sort(tokens); // Sort beforehand for delta calculations
        for (TSLSSemanticToken token : tokens) {
            // 0 : Line
            data.add(prevToken == null ? token.getLine() - 1 : token.deltaLine(prevToken));
            // 1 : Char
            data.add(prevToken == null ? token.getCharacter() - 1 : token.deltaStartChar(prevToken));
            // 2 : Length
            data.add(token.getLength());
            // 3 : Type
            data.add(token.getType());
            // 4 : Modifiers
            data.add(token.getModifier());
            prevToken = token;
        }
        return new SemanticTokens(data);
    }

    @Override
    public void deserialize(SemanticTokens serialized) {
        int line = 0;
        int character = 0;
        this.tokens = new LinkedList<>();
        List<Integer> data = serialized.getData();
        for (int i = 0; i < data.size() / 5; i++) {
            TSLSSemanticToken token = new TSLSSemanticToken()
                    .setLine(line += data.get(i)) // TODO: consider delta stuff
                    .setCharacter(character += data.get(i + 1)) // TODO: consider delta stuff
                    .setLength(data.get(i + 2))
                    .setType(data.get(i + 4))
                    .setModifier(data.get(i + 5));
            tokens.add(token);
        }
    }

    public enum TokenTypes {
        COMMENT(0, SemanticTokenTypes.Comment),
        FUNCTION(1, SemanticTokenTypes.Function),
        VARIABLE(2, SemanticTokenTypes.Variable),
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
//        DECLARATION(1, SemanticTokenModifiers.Declaration),
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

    public static SemanticTokensWithRegistrationOptions getSemanticTokensWithRegistrationOptions() {
        SemanticTokensLegend legend = new SemanticTokensLegend(getTokenTypes(), getTokenTypeModifiers());
        SemanticTokensWithRegistrationOptions options = new SemanticTokensWithRegistrationOptions(legend);
        options.setFull(new SemanticTokensServerFull());
        return options;
    }

}
