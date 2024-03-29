package net.programmer.igoodie.lsp.util;

import net.programmer.igoodie.goodies.util.accessor.ArrayAccessor;
import net.programmer.igoodie.goodies.util.accessor.ListAccessor;
import net.programmer.igoodie.goodies.util.accessor.StringCharAccessor;
import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.tsl.parser.snippet.TSLSnippetBuffer;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CursorPlacement {

    private @NotNull final TSLDocument tslDocument;
    private @NotNull final Position position;

    private @Nullable TSLSnippetBuffer.Type snippetType;
    private @Nullable TSLSnippetBuffer snippetBuffer;
    private @Nullable TSLToken previousToken;
    private @Nullable TSLToken landedToken;
    private @Nullable TSLToken nextToken;
    private String landedCharacter;

    public CursorPlacement(@NotNull TSLDocument tslDocument, @NotNull Position position) {
        this.tslDocument = tslDocument;
        this.position = position;
        calculateLandings();
    }

    private void calculateLandings() {
        int line = position.getLine() + 1;
        int character = position.getCharacter() + 1;

        String lineText = ArrayAccessor.of(tslDocument.getLines()).get(line - 1);
        StringCharAccessor lineCharAccessor = StringCharAccessor.of(lineText);
        this.landedCharacter = lineCharAccessor.getOrDefault(character - 2, '\0') + ""
                + lineCharAccessor.getOrDefault(character - 1, '\0') + ""
                + lineCharAccessor.getOrDefault(character, '\0');

        for (TSLSnippetBuffer snippetBuffer : tslDocument.getSnippetBuffers()) {
            List<TSLToken> snippetTokens = snippetBuffer.getTokens();
            if (snippetTokens.size() <= 0) continue;

            ListAccessor<TSLToken> tokenAccessor = ListAccessor.of(snippetTokens);
            for (int i = 0; i < snippetTokens.size(); i++) {
                TSLToken previousToken = tokenAccessor.get(i - 1);
                TSLToken token = tokenAccessor.get(i);
                TSLToken nextToken = tokenAccessor.get(i + 1);

                int snippetLine = token.getLine();
                if (line != snippetLine) continue; // Oopsie, not this line

                // Landed right in a token
                if (inRange(character, token.getCharacter(), token.getCharacter() + token.getRaw().length())) {
                    this.previousToken = previousToken;
                    this.landedToken = token;
                    this.nextToken = nextToken;
                    this.snippetBuffer = snippetBuffer;
                    this.snippetType = snippetBuffer.getType();
                    break;
                }

                // Landed right between prev and token
                if (previousToken == null && inRange(character, 0, token.getCharacter())) {
                    this.nextToken = token;
                    this.snippetBuffer = snippetBuffer;
                    this.snippetType = snippetBuffer.getType();
                    break;

                } else if (previousToken != null && inRange(character, previousToken.getCharacter() + previousToken.getRaw().length(), token.getCharacter())) {
                    this.previousToken = previousToken;
                    this.nextToken = token;
                    this.snippetBuffer = snippetBuffer;
                    this.snippetType = snippetBuffer.getType();
                    break;
                }

                // Landed right between next and token
                if (nextToken == null && character > token.getCharacter() + token.getRaw().length()) {
                    this.previousToken = token;
                    this.snippetBuffer = snippetBuffer;
                    this.snippetType = snippetBuffer.getType();
                    break;

                } else if (nextToken != null && inRange(character, token.getCharacter() + token.getRaw().length(), nextToken.getCharacter())) {
                    this.previousToken = token;
                    this.nextToken = nextToken;
                    this.snippetBuffer = snippetBuffer;
                    this.snippetType = snippetBuffer.getType();
                    break;
                }
            }
        }

        if (snippetBuffer == null) { // Still couldn't match, is it a trailing or leading cursor?
            for (TSLSnippetBuffer snippetBuffer : tslDocument.getSnippetBuffers()) {
                for (TSLToken snippetToken : snippetBuffer.getTokens()) {
                    int snippetLine = snippetToken.getLine();
                    if (snippetLine == line) {
                        if (character < snippetToken.getCharacter()) {
                            this.snippetBuffer = snippetBuffer;
                            this.snippetType = this.snippetBuffer.getType();
                            this.nextToken = snippetToken;
                        } else {
                            this.snippetBuffer = snippetBuffer;
                            this.snippetType = this.snippetBuffer.getType();
                            this.previousToken = snippetToken;
                        }
                    }
                }
            }
        }

        // After iterating thru all the snippets, none of them matched
        if (snippetType == null) {
            ArrayAccessor<TSLSnippetBuffer.Type> lineTypeAccessor = ArrayAccessor.of(tslDocument.getLineTypes());
            TSLSnippetBuffer.Type previousLineType = lineTypeAccessor.get(line - 2);
            TSLSnippetBuffer.Type nextLineType = lineTypeAccessor.get(line);

            // Absolute empty lines count as Rule beginnings
            this.snippetType = previousLineType == null
                    ? TSLSnippetBuffer.Type.RULE : previousLineType;
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

    public String getLandedCharacter() {
        return landedCharacter;
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

    public Optional<TSLSnippetBuffer> getSnippetBuffer() {
        return Optional.ofNullable(snippetBuffer);
    }

    public Optional<TSLSnippetBuffer.Type> getSnippetType() {
        return Optional.ofNullable(snippetType);
    }

    public Optional<TSLSnippetBuffer> getSnippetAbove() {
        int line = position.getLine();
        return Optional.ofNullable(ArrayAccessor.of(tslDocument.getLineBuffers()).get(line - 1));
    }

    public Optional<TSLSnippetBuffer> getSnippetBelow() {
        int line = position.getLine();
        return Optional.ofNullable(ArrayAccessor.of(tslDocument.getLineBuffers()).get(line + 1));
    }

    public MarkupContent debugContent() {
        return MDUtils.build(
                "**Snippet Type:** " + this.getSnippetType(),
                "",
                "**Landed String:** [" + this.getLandedCharacter() + "]",
                "",
                "**Prev:** " + this.getPreviousToken(),
                "",
                "**Land:** " + this.getLandedToken(),
                "",
                "**Next:** " + this.getNextToken(),
                "",
                "---",
                "",
                "**Tokens:** " + getSnippetBuffer().map(TSLSnippetBuffer::getTokens),
                "",
                "**Begin Line:** " + getSnippetBuffer().map(TSLSnippetBuffer::getBeginningLine),
                "",
                "**End Line:** " + getSnippetBuffer().map(TSLSnippetBuffer::getEndingLine)
        );
    }

}
