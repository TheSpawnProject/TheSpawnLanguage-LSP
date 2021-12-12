package net.programmer.igoodie.lsp.capability;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TSLSCompletionCapabilities extends Capabilities<CompletionOptions> {

    public enum TriggerCharacters {
        CAPTURE_PREFIX(0, "$");

        int id;
        String character;

        TriggerCharacters(int id, String character) {
            this.id = id;
            this.character = character;
        }
    }

    public List<String> getTriggerCharacters() {
        return Arrays.stream(TriggerCharacters.values())
                .map(entry -> entry.character)
                .collect(Collectors.toList());
    }

    @Override
    public CompletionOptions buildOptions() {
        CompletionOptions options = new CompletionOptions();
        options.setTriggerCharacters(getTriggerCharacters());
        return options;
    }

    @Override
    public void register(ServerCapabilities serverCapabilities, CompletionOptions options) {
        serverCapabilities.setCompletionProvider(options);
    }

}
