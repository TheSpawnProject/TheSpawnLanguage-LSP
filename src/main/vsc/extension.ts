import * as path from "path";
import * as vscode from "vscode";

import { LanguageClientOptions } from "vscode-languageclient/node";
import { LanguageClient } from "vscode-languageclient/node";
import { ServerOptions } from "vscode-languageclient/node";

const main = "net.programmer.igoodie.lsp.TSLServerLauncher";

export function activate(context: vscode.ExtensionContext) {
  console.log("Activated");

  const { JAVA_HOME } = process.env;

  if (!JAVA_HOME) {
    vscode.window.showWarningMessage(
      "Java is not installed in this device. TSLS won't be able to assist you while coding your rulesets.",
      "Visit Download Page",
      "Ignore",
      "Don't Show This Again"
    );
    return; // TODO: What to do now..?
  }

  const excecutable: string = path.join(JAVA_HOME, "bin", "java");
  const classPath = path.join(__dirname, "..", "libs", "ls-launcher.jar");
  const args: string[] = ["-cp", classPath];

  const serverOptions: ServerOptions = {
    command: excecutable,
    args: [...args, main],
    options: {},
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: [
      { scheme: "file", language: "tsl" },
      { scheme: "untitled", language: "tsl" },
    ],
  };

  const disposable = new LanguageClient(
    "TSL",
    "The Spawn Language Server",
    serverOptions,
    clientOptions
  ).start();

  context.subscriptions.push(disposable);
}

export function deactivate() {
  console.log("Deactivated");
}
