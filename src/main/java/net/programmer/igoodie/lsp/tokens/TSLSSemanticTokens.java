package net.programmer.igoodie.lsp.tokens;

import net.programmer.igoodie.tsl.util.ISerializable;
import org.eclipse.lsp4j.SemanticTokens;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    public String debugString() {
        StringBuilder stringBuilder = new StringBuilder();
        List<Integer> data = serialize().getData();
        for (int i = 0; i < data.size() / 5; i++) {
            stringBuilder
                    .append(tokens.get(i)).append("\n")
                    .append(data.get(5 * i)).append(" ")
                    .append(data.get(5 * i + 1)).append(" ")
                    .append(data.get(5 * i + 2)).append(" ")
                    .append(data.get(5 * i + 3)).append(" ")
                    .append(data.get(5 * i + 4)).append("\n");
        }
        return stringBuilder.toString();
    }

}
