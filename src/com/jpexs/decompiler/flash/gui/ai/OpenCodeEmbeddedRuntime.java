/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Integration with the OpenCode CLI that is bundled with FFDec.
 *
 * <p>The CLI is executed as a separate process with an isolated working
 * directory. The SWF is copied to that directory as {@code input.swf}; when
 * the user asks the assistant to modify it, the modified file must be written
 * as {@code output.swf}. The original file is replaced only after the output
 * has been validated and a backup has been created.</p>
 *
 * <p>No credentials are passed to the CLI. The CLI uses the OpenCode account
 * and configuration already selected by the user on this machine.</p>
 */
final class OpenCodeEmbeddedRuntime {

    static final String VERSION = "2.2.5";
    static final String EXPECTED_SHA256 = "0C346FDAF519D2F23A31D3B6CB9D65E369A72D77CAC6F027B5E0A06376EC3A43";
    private static final long MODELS_TIMEOUT_SECONDS = 45;
    private static final long RUN_TIMEOUT_SECONDS = 12 * 60 * 60;
    private static final int MAX_OUTPUT_BYTES = 4 * 1024 * 1024;

    private OpenCodeEmbeddedRuntime() {
    }

    static Path installDir() {
        try {
            Path location = Paths.get(OpenCodeEmbeddedRuntime.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            return Files.isDirectory(location) ? location : location.getParent();
        } catch (Exception ex) {
            return null;
        }
    }

    static Path bundledCli() {
        try {
            Path jarLocation = Paths.get(OpenCodeEmbeddedRuntime.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            Path parent = Files.isDirectory(jarLocation) ? jarLocation : jarLocation.getParent();
            if (parent != null) {
                return parent.resolve(Paths.get("opencode", "opencode.exe"));
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    static Path cli() throws IOException {
        String override = System.getProperty("ffdec.opencode.path");
        if (override == null || override.trim().isEmpty()) {
            override = System.getenv("FFDEC_OPENCODE_PATH");
        }
        if (override != null && !override.trim().isEmpty()) {
            Path path = Paths.get(override.trim()).toAbsolutePath().normalize();
            requireWindowsExecutable(path);
            return path;
        }
        Path bundled = bundledCli();
        if (bundled != null && Files.isRegularFile(bundled)) {
            verifyBundledCli(bundled);
            return bundled;
        }
        throw new AIProviderException("The embedded OpenCode CLI is missing or corrupted. "
                + "Reinstall FFDec or set the FFDEC_OPENCODE_PATH environment variable to a compatible opencode.exe.");
    }

    static List<String> listModels() throws IOException {
        Path cli = cli();
        List<String> command = new ArrayList<>();
        command.add(cli.toString());
        command.add("models");
        CommandResult result = run(command, null,
                MODELS_TIMEOUT_SECONDS, null);
        if (result.timedOut) {
            throw new AIProviderException("Timed out while listing OpenCode models.");
        }
        if (result.exitCode != 0) {
            throw new AIProviderException("OpenCode could not list its models. " + result.errorSummary());
        }
        List<String> models = new ArrayList<>();
        for (String line : result.output.split("\\R")) {
            String model = line.trim();
            if (model.isEmpty()) {
                continue;
            }
            // Defensive check: reject diagnostics that do not look like a model id.
            if (model.indexOf(' ') >= 0 || model.indexOf('\t') >= 0) {
                continue;
            }
            models.add(model);
        }
        if (models.isEmpty()) {
            throw new AIProviderException("OpenCode returned an empty model list. "
                    + "Sign in to OpenCode and try again.");
        }
        return models;
    }

    static OpenCodeSession prepareSession(SWF swf) throws IOException {
        Path workspace = Files.createTempDirectory("ffdec-ai-opencode-");
        try {
            Files.write(workspace.resolve("opencode.json"), openCodeConfig().getBytes(StandardCharsets.UTF_8));
            String agents = agentInstructions();
            Files.write(workspace.resolve("AGENTS.md"), agents.getBytes(StandardCharsets.UTF_8));
            Path install = installDir();
            if (install == null || !Files.isRegularFile(install.resolve("ffdec-cli.exe"))) {
                throw new AIProviderException("ffdec-cli.exe is missing from the FFDec installation. "
                        + "Rebuild or reinstall FFDec.");
            }
            OpenCodeSession session = new OpenCodeSession(workspace, swf);
            session.refreshInput();
            return session;
        } catch (IOException | RuntimeException ex) {
            deleteRecursively(workspace);
            throw ex;
        }
    }

    static CommandResult run(List<String> command, Path cwd, long timeoutSeconds,
            StringBuilder outputSink) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (cwd != null) {
            builder.directory(cwd.toFile());
        }
        Path install = installDir();
        if (install != null) {
            String existing = null;
            String pathKey = "PATH";
            for (String key : builder.environment().keySet()) {
                if ("PATH".equalsIgnoreCase(key)) {
                    existing = builder.environment().get(key);
                    pathKey = key;
                    break;
                }
            }
            builder.environment().put(pathKey, install.toString()
                    + java.io.File.pathSeparator + (existing == null ? "" : existing));
        }
        builder.environment().put("NO_COLOR", "1");
        builder.environment().put("TERM", "dumb");
        Process process = builder.start();
        StreamReader stdout = new StreamReader(process.getInputStream(), outputSink);
        StreamReader stderr = new StreamReader(process.getErrorStream(), null);
        Thread outThread = new Thread(stdout, "ffdec-opencode-stdout");
        Thread errThread = new Thread(stderr, "ffdec-opencode-stderr");
        outThread.setDaemon(true);
        errThread.setDaemon(true);
        outThread.start();
        errThread.start();
        boolean timedOut = false;
        try {
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                timedOut = true;
                process.destroyForcibly();
                process.waitFor(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("Interrupted while waiting for the OpenCode CLI.", ex);
        } finally {
            try {
                outThread.join(5000);
                errThread.join(5000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return new CommandResult(timedOut, timedOut ? -1 : process.exitValue(),
                stdout.content.toString(), stderr.content.toString());
    }

    private static void requireWindowsExecutable(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("OpenCode executable not found: " + path);
        }
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".exe")) {
            throw new IOException("FFDEC_OPENCODE_PATH must point to a Windows opencode.exe file.");
        }
    }

    private static void verifyBundledCli(Path path) throws IOException {
        long size = Files.size(path);
        if (size <= 0) {
            throw new AIProviderException("The embedded OpenCode CLI is empty. Reinstall FFDec.");
        }
        String hash = sha256(path);
        if (!EXPECTED_SHA256.equalsIgnoreCase(hash)) {
            throw new AIProviderException("The embedded OpenCode CLI failed its integrity check. "
                    + "Reinstall FFDec.");
        }
    }

    static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[1024 * 1024];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, count);
                }
            }
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException("SHA-256 is not available.", ex);
        }
    }

    private static String openCodeConfig() {
        return "{\n"
                + "  \"$schema\": \"https://opencode.ai/config.json\",\n"
                + "  \"model\": \"opencode/pulse-alpha-free\",\n"
                + "  \"permissions\": [\n"
                + "    { \"action\": \"*\", \"resource\": \"*\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"execute\", \"resource\": \"*\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"read\", \"resource\": \"*\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"read\", \"resource\": \"*.env\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"read\", \"resource\": \"*.env.*\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"read\", \"resource\": \"*.key\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"read\", \"resource\": \"*.pem\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"glob\", \"resource\": \"*\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"grep\", \"resource\": \"*\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"edit\", \"resource\": \"*\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"shell\", \"resource\": \"ffdec-cli.exe *\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"shell\", \"resource\": \"ffdec-cli.exe*\", \"effect\": \"allow\" },\n"
                + "    { \"action\": \"external_directory\", \"resource\": \"*\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"webfetch\", \"resource\": \"*\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"websearch\", \"resource\": \"*\", \"effect\": \"deny\" },\n"
                + "    { \"action\": \"question\", \"resource\": \"*\", \"effect\": \"deny\" }\n"
                + "  ]\n"
                + "}\n";
    }

    private static String agentInstructions() {
        return "# FFDec AI workspace\n\n"
                + "You are editing a Flash SWF for the user in an isolated workspace.\n\n"
                + "Rules:\n"
                + "- Work only inside the current directory. Do not read or write outside it.\n"
                + "- The source file is `input.swf`; never pass it as the desired output.\n"
                + "- When the user asks for an edit, create `output.swf` with the changes.\n"
                + "- Use the FFDec CLI for binary operations; do not invent SWF data or shell commands.\n"
                + "- FFDec CLI is available as `ffdec-cli.exe` and is already on PATH.\n"
                + "- Useful FFDec CLI commands: `ffdec-cli.exe -export <types> <outdir> input.swf`, "
                + "`-importScript`, `-importImages`, `-importSounds`, `-importShapes`, "
                + "`-importSprites`, `-importText`, `-replace`, `-remove`, `-header -set`, "
                + "and `-compress`. Run `ffdec-cli.exe -help <command>` to see exact syntax.\n"
                + "- If replacing ActionScript, export it first, edit only the relevant source files, "
                + "then import/replace it to produce `output.swf`.\n"
                + "- If the request is informational, answer without creating `output.swf`.\n"
                + "- Do not use the network or download dependencies.\n"
                + "- Do not run destructive commands outside the workspace.\n"
                + "- When finished, summarize what changed and whether `output.swf` was created.\n";
    }

    private static String escapeMarkdown(String value) {
        return value.replace("`", "\\`");
    }

    static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // Temporary files may be locked; leave them for the OS to clean.
                }
            });
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }

    static final class CommandResult {
        final boolean timedOut;
        final int exitCode;
        final String output;
        final String error;

        CommandResult(boolean timedOut, int exitCode, String output, String error) {
            this.timedOut = timedOut;
            this.exitCode = exitCode;
            this.output = output == null ? "" : output;
            this.error = error == null ? "" : error;
        }

        String errorSummary() {
            String text = error.trim();
            if (text.isEmpty()) {
                text = output.trim();
            }
            if (text.length() > 500) {
                text = text.substring(0, 500) + "...";
            }
            return text.isEmpty() ? "exit code " + exitCode : text;
        }
    }

    static final class OpenCodeSession {
        final Path workspace;
        private SWF swf;
        private String inputHash;
        private int activeRuns;
        private boolean closed;

        OpenCodeSession(Path workspace, SWF swf) {
            this.workspace = workspace;
            this.swf = swf;
        }

        synchronized void refreshInput() throws IOException {
            if (closed) {
                throw new IOException("This AI workspace is closed.");
            }
            Path input = workspace.resolve("input.swf");
            Path output = workspace.resolve("output.swf");
            Files.deleteIfExists(output);
            if (swf == null) {
                Files.deleteIfExists(input);
                inputHash = null;
                return;
            }
            if (swf.getOpenableList() == null) {
                // A nested SWF cannot be written by the CLI without changing its parent.
                // Write a standalone copy so the assistant can still inspect or transform it.
                Main.saveFile(swf, input.toString());
            } else {
                Main.saveFile(swf, input.toString());
            }
            inputHash = sha256(input);
        }

        synchronized void beginRun() throws IOException {
            if (closed) {
                throw new IOException("This AI workspace is closed.");
            }
            activeRuns++;
        }

        synchronized void endRun() {
            activeRuns = Math.max(0, activeRuns - 1);
        }

        synchronized boolean isClosed() {
            return closed;
        }

        synchronized void close() {
            closed = true;
            if (activeRuns == 0) {
                deleteRecursively(workspace);
            }
        }

        synchronized void closeAfterRun() {
            if (closed && activeRuns == 0) {
                deleteRecursively(workspace);
            }
        }

        synchronized void saveCurrentTo(Path target) throws IOException {
            if (swf == null) {
                throw new IOException("No SWF is currently selected.");
            }
            Main.saveFile(swf, target.toString());
        }

        synchronized boolean canReplaceOriginal() {
            return swf != null && swf.getOpenableList() != null;
        }

        synchronized String inputHash() {
            return inputHash;
        }

        synchronized Path inputPath() {
            return workspace.resolve("input.swf");
        }

        synchronized Path outputPath() {
            return workspace.resolve("output.swf");
        }

        synchronized String originalPath() {
            return swf == null ? null : swf.getFile();
        }

        synchronized OpenableList originalList() {
            return swf == null ? null : swf.getOpenableList();
        }
    }

    private static final class StreamReader implements Runnable {
        private final InputStream input;
        private final StringBuilder sink;
        private final StringBuilder limited = new StringBuilder();
        final StringBuilder content = new StringBuilder();

        StreamReader(InputStream input, StringBuilder sink) {
            this.input = input;
            this.sink = sink;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (content.length() < MAX_OUTPUT_BYTES) {
                        int remaining = MAX_OUTPUT_BYTES - content.length();
                        content.append(line, 0, Math.min(line.length(), remaining));
                        if (content.length() < MAX_OUTPUT_BYTES) {
                            content.append('\n');
                        }
                    }
                    if (sink != null) {
                        sink.append(line).append('\n');
                    }
                }
            } catch (IOException ex) {
                content.append("[CLI output read failed: ").append(ex.getMessage()).append("]\n");
            }
        }
    }
}
