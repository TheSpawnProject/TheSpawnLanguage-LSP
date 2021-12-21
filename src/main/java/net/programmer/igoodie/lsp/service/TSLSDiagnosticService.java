package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.tsl.exception.TSLSyntaxError;
import net.programmer.igoodie.tsl.parser.token.TSLToken;
import org.eclipse.lsp4j.*;

import java.util.LinkedList;
import java.util.List;

public class TSLSDiagnosticService {

    public PublishDiagnosticsParams diagnose(TSLDocument tslDocument) {
        PublishDiagnosticsParams params = new PublishDiagnosticsParams();
        params.setUri(tslDocument.getUri());

        List<Diagnostic> diagnostics = new LinkedList<>();
        TSLSyntaxError syntaxError = tslDocument.getSyntaxError();

        if (syntaxError != null) {
            TSLToken associatedToken = syntaxError.getAssociatedToken();

            Position startPos = new Position(syntaxError.getLine() - 1, syntaxError.getCharacter() - 1);
            Position endPos = new Position(syntaxError.getLine() - 1,
                    syntaxError.getCharacter() - 1 + (associatedToken == null ? 0 : associatedToken.getRaw().length()));

            if (startPos.equals(endPos)) {
                endPos.setCharacter(endPos.getCharacter() + 1);
            }

            Range range = new Range(startPos, endPos);

            StackTraceElement stackElem = syntaxError.getStackTrace()[0];

            Diagnostic diagnostic = new Diagnostic(range,
                    syntaxError.getMessage(),
                    DiagnosticSeverity.Error,
                    String.format("(%s:%s)", stackElem.getFileName(), stackElem.getLineNumber()));

            diagnostics.add(diagnostic);
        }

        params.setDiagnostics(diagnostics);
        return params;
    }

}
