import * as path from "path";
import * as vscode from "vscode";

import { LanguageClientOptions } from "vscode-languageclient";
import { LanguageClient } from "vscode-languageclient";
import { ServerOptions } from "vscode-languageclient";

const main = "net.programmer.igoodie.lsp.TSLServerLauncher";

export function activate(context: vscode.ExtensionContext) {
  console.log("Activated");

  const { JAVA_HOME } = process.env;

  if (!JAVA_HOME) return; // TODO: What to do now..?

  const excecutable: string = path.join(JAVA_HOME, "bin", "java");
  const classPath = path.join(__dirname, "..", "libs", "ls-launcher.jar");
  const args: string[] = ["-cp", classPath];

  const serverOptions: ServerOptions = {
    command: excecutable,
    args: [...args, main],
    options: {},
  };

  const clientOptions: LanguageClientOptions = {
    // Register the server for plain text documents
    documentSelector: [{ scheme: "file", language: "tsl" }],
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
