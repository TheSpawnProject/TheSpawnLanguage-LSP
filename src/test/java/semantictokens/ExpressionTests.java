package semantictokens;

import net.programmer.igoodie.lsp.data.TSLDocument;
import net.programmer.igoodie.lsp.tokens.TSLSSemanticTokens;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionTests {

    @Test
    public void testExpressionCaptureVars() {
        TSLDocument tslDocument = new TSLDocument("", "$capture = ${{{x}} + {{x}} / 2 * Math.random() % {{x}}}");
        TSLSSemanticTokens semanticTokens = tslDocument.generateSemanticTokens();
        Assertions.assertEquals(4, semanticTokens.getTokens().size());
    }

    @Test
    public void testExpressionCaptureVars2() {
        TSLDocument tslDocument = new TSLDocument("", "# IMPORT myLibrary %path/to/my/file.js% # Future Idea\n" +
                "#! COOLDOWN 1000\n" +
                "#! COOLDOWN 3000\n" +
                "#! COOLDOWN 2000 # <-- Dis will override the ones above\n" +
                "\n" +
                "#### Captures ####\n" +
                "\n" +
                "$name\n" +
                "= diamond # Inline comment!\n" +
                "\n" +
                "$someAction\n" +
                "= PRINT ${(_mc.player.direction.mult(5)).length}\n" +
                "# Inline comment!\n" +
                "\n" +
                "# BUG: Lexer 228 | Using '}' inside Expression fails\n" +
                "# $runScript(file) = 5\n" +
                "# = ${function myFunc(a, b, c) {}; runScript(param(\"file\"))}\n" +
                "# # Inline comment!\n" +
                "\n" +
                "$parameterizedCapture1(x, y)\n" +
                "= PRINT ${param(\"x\") + param(\"y\") + hex(\"#FFFFFF\")}\n" +
                "\n" +
                "$parameterizedCapture2(x)\n" +
                "= PRINT %apple{{x}}%\n" +
                "\n" +
                "$parameterizedCapture3(x, y, z)\n" +
                "= PRINT {{x}} {{y}} {{z}}\n" +
                "\n" +
                "$parameterizedCapture4(x,y,z)\n" +
                "= EITHER {{X}} OR {{Y}} OR {{X}}\n" +
                " OR PRINT %apple%\n" +
                "\n" +
                "# BUG: Lexer 207 | Nesting Capture Calls fail\n" +
                "# $embeddedAction\n" +
                "# = PRINT $parameterizedAction($name, %Foo bar baz \"is\"%, $runScript(%foo%)) # Inline comment!\n" +
                "\n" +
                "#### Rules ####\n" +
                "\n" +
                "DROP diamond\n" +
                " ON Dummy Event\n" +
                "\n" +
                "PRINT %Hey dude! This message is evaluated @ ${_currentUnix()}%\n" +
                " ON Alert Event\n" +
                " WITH time = 12345\n" +
                "\n" +
                "@suppressNotifications\n" +
                "@suppressNotifications\n" +
                "PRINT foo\n" +
                " ON Alert Event\n" +
                "\n" +
                "@suppressNotifications\n" +
                "@suppressNotifications\n" +
                "PRINT foo\n" +
                "ON Alert Event\n" +
                "\n" +
                "$someAction # Inline comment!\n" +
                " ON Alert Event\n" +
                "\n" +
                "@suppressNotifications\n" +
                "PRINT ${_maximumOf(10,20) + Math.random()}\n" +
                " ON Alert Event\n" +
                "\n" +
                "DROP apple\n" +
                " ON Donation\n" +
                " WITH amount = 12345\n" +
                " WITH unit = TRY\n" +
                "\n" +
                "# Eyyy, a comment here!\n" +
                "\n" +
                "#*\n" +
                " Eyyyy, yet another comment here!\n" +
                "*#\n" +
                "\n" +
                "# @notificationVolume(2f)\n" +
                "# @notificationPitch(2f)\n" +
                "EITHER\n" +
                " DROP $name\n" +
                " OR\n" +
                " (EITHER DROP apple OR DROP string)\n" +
                " OR\n" +
                " (EITHER\n" +
                "    DROP apple\n" +
                "    OR\n" +
                "    (EITHER\n" +
                "        DROP string\n" +
                "        OR\n" +
                "        DROP diamond))\n" +
                " ON Alert Event\n" +
                "\n" +
                "PRINT\n" +
                "%${_mult(10,20)} Time to ditch indentations, ${\"aye\"}!%\n" +
                "ON Alert Event");
        TSLSSemanticTokens semanticTokens = tslDocument.generateSemanticTokens();
        System.out.println(semanticTokens.serialize());
    }
    
}
