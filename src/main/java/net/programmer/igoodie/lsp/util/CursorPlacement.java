package net.programmer.igoodie.lsp.util;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import org.eclipse.lsp4j.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CursorPlacement {

    private @NotNull final TSLDocument tslDocument;
    private @NotNull final Position position;

    private @Nullable TSLSnippetBuffer.Type snippetType;
    private @Nullable TSLToken previousToken;
    private @Nullable TSLToken landedToken;
    private @Nullable TSLToken nextToken;

    public CursorPlacement(@NotNull TSLDocument tslDocument, @NotNull Position position) {
        this.tslDocument = tslDocument;
        this.position = position;
        calculateLandings();
    }

    private void calculateLandings() {
        int line = position.getLine() + 1;
        int character = position.getCharacter() + 1;

        TSLToken previousToken = null;

        for (TSLSnippetBuffer snippet : tslDocument.getSnippetBuffers()) {
            if (landedToken != null) break;
            for (TSLToken token : snippet.getTokens()) {
                if (landedToken != null) {
                    nextToken = token;
                    break;
                }
                if (token.getLine() == line) {
                    this.snippetType = snippet.getType();
                    int length = token.getRaw().length();
                    // TODO: Consider cursor inbetween tokens...
                    if (inRange(character, token.getCharacter(), token.getCharacter() + length)) {
                        this.previousToken = previousToken;
                        this.landedToken = token;
                        continue;
                    }
                }
                previousToken = token;
            }
            previousToken = null;
        }
    }

    private boolean inRange(int number, int min, int max) {
        return min <= number && number <= max;
    }

    public @NotNull Position getPosition() {
        return position;
    }

    public @NotNull TSLDocument getTslDocument() {
        return tslDocument;
    }

    public Optional<TSLToken> getPreviousToken() {
        return Optional.ofNullable(previousToken);
    }

    public Optional<TSLToken> getLandedToken() {
        return Optional.ofNullable(landedToken);
    }

    public Optional<TSLToken> getNextToken() {
        return Optional.ofNullable(nextToken);
    }

    public Optional<TSLSnippetBuffer.Type> getSnippetType() {
        return Optional.ofNullable(snippetType);
    }

}
