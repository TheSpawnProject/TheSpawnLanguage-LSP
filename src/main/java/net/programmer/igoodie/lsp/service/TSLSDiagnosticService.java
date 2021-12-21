package net.programmer.igoodie.lsp.service;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.tsl.TheSpawnLanguage;
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
        Exception error = tslDocument.getError();

        if (error != null) {
            if (error instanceof TSLSyntaxError) {
                diagnostics.add(syntaxDiagnostic(((TSLSyntaxError) error)));
            } else {
                diagnostics.add(genericErrorDiagnostic(error));
            }
        }

        params.setDiagnostics(diagnostics);
        return params;
    }

    private Diagnostic syntaxDiagnostic(TSLSyntaxError syntaxError) {
        TSLToken associatedToken = syntaxError.getAssociatedToken();

        Position startPos = new Position(syntaxError.getLine() - 1, syntaxError.getCharacter() - 1);
        Position endPos = new Position(syntaxError.getLine() - 1,
                syntaxError.getCharacter() - 1 + (associatedToken == null ? 0 : associatedToken.getRaw().length()));

        if (startPos.equals(endPos)) {
            endPos.setCharacter(endPos.getCharacter() + 1);
        }

        Range range = new Range(startPos, endPos);

        StackTraceElement stackElem = syntaxError.getStackTrace()[0];

        return new Diagnostic(range,
                syntaxError.getMessage(),
                DiagnosticSeverity.Error,
                "tsl-" + TheSpawnLanguage.TSL_VERSION,
                stackElem.getFileName() + ":" + stackElem.getLineNumber());
    }

    private Diagnostic genericErrorDiagnostic(Exception error) {
        Position startPos = new Position(0, 0);
        Position endPos = new Position(0, 0);

        Range range = new Range(startPos, endPos);

        StackTraceElement stackElem = error.getStackTrace()[0];

        return new Diagnostic(range,
                error.getMessage(),
                DiagnosticSeverity.Error,
                "tsl-" + TheSpawnLanguage.TSL_VERSION,
                stackElem.getFileName() + ":" + stackElem.getLineNumber());
    }

}

