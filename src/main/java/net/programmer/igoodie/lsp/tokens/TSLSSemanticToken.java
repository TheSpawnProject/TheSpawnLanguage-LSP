package net.programmer.igoodie.lsp.tokens;

import net.programmer.igoodie.lsp.capability.TSLSSemanticTokenCapabilities;
import org.jetbrains.annotations.NotNull;

public class TSLSSemanticToken implements Comparable<TSLSSemanticToken> {

    protected String debug;
    protected int line, character;
    protected int length;
    protected int type, modifier;

    public int getLine() {
        return line;
    }

    public int getCharacter() {
        return character;
    }

    public int getLength() {
        return length;
    }

    public int getType() {
        return type;
    }

    public int getModifier() {
        return modifier;
    }

    public TSLSSemanticToken setDebug(String debug) {
        this.debug = debug;
        return this;
    }

    public TSLSSemanticToken setLine(int line) {
        this.line = line;
        return this;
    }

    public TSLSSemanticToken setCharacter(int character) {
        this.character = character;
        return this;
    }

    public TSLSSemanticToken setLength(int length) {
        this.length = length;
        return this;
    }

    public TSLSSemanticToken setType(int type) {
        this.type = type;
        return this;
    }

    public TSLSSemanticToken setType(TSLSSemanticTokenCapabilities.TokenTypes tokenType) {
        return setType(tokenType.getId());
    }

    public TSLSSemanticToken setModifier(int modifier) {
        this.modifier = modifier;
        return this;
    }

    public int deltaLine(TSLSSemanticToken prev) {
        return this.line - prev.line;
    }

    public int deltaStartChar(TSLSSemanticToken prev) {
        if (prev.line != this.line) return this.character - 1;
        return this.character - prev.character;
    }

    @Override
    public int compareTo(@NotNull TSLSSemanticToken that) {
        if (this.line != that.line)
            return Integer.compare(this.line, that.line);
        return Integer.compare(this.character, that.character);
    }

    @Override
    public String toString() {
        return "{" +
                "debug='" + debug + '\'' +
                ", line=" + line +
                ", character=" + character +
                ", length=" + length +
                ", type=" + type +
                ", modifier=" + modifier +
                '}';
    }

}
