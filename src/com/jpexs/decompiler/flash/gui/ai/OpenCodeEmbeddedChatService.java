/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Sends prompts through the embedded OpenCode CLI.
 */
final class OpenCodeEmbeddedChatService {

    private static final long RUN_TIMEOUT_SECONDS = 12L * 60L * 60L;
    private final String sessionId = "ses_" + java.util.UUID.randomUUID().toString().replace("-", "");
    private final Object lock = new Object();
    private OpenCodeEmbeddedRuntime.OpenCodeSession session;
    private boolean closed;

    EmbeddedResult send(String modelId, SWF swf, String userText) throws IOException {
        if (modelId == null || modelId.trim().isEmpty()) {
            throw new AIProviderException("Choose an OpenCode model before sending.");
        }
        if (userText == null || userText.trim().isEmpty()) {
            throw new AIProviderException("Enter a message before sending.");
        }
        OpenCodeEmbeddedRuntime.OpenCodeSession current;
        synchronized (lock) {
            if (closed) {
                throw new IOException("This AI session is closed.");
            }
            if (session == null) {
                session = OpenCodeEmbeddedRuntime.prepareSession(swf);
            } else {
                // Refresh the snapshot so edits made in the GUI before this message are included.
                session.refreshInput();
            }
            current = session;
            current.beginRun();
        }
        try {
            String snapshotHash = current.inputHash();
            Path cli = OpenCodeEmbeddedRuntime.cli();
            if (!Files.isRegularFile(cli)) {
                throw new AIProviderException("The embedded OpenCode CLI is missing.");
            }
            List<String> command = new ArrayList<>();
            command.add(cli.toString());
            command.add("run");
            command.add("--format");
            command.add("json");
            command.add("--model");
            command.add(modelId.trim());
            command.add("--session");
            command.add(sessionId);
            command.add(buildPrompt(userText.trim(), current));
            OpenCodeEmbeddedRuntime.CommandResult result = OpenCodeEmbeddedRuntime.run(
                    command, current.workspace, RUN_TIMEOUT_SECONDS, null);
            EmbeddedResult response = parseCliOutput(result);
            if (result.timedOut) {
                throw new AIProviderException("The OpenCode CLI timed out after 12 hours.");
            }
            if (result.exitCode != 0) {
                String detail = response.error != null ? response.error : response.text;
                if (detail == null || detail.isEmpty()) {
                    detail = result.errorSummary();
                }
                if (result.error != null && !result.error.trim().isEmpty()
                        && !detail.contains(result.error.trim())) {
                    detail = detail + "\n" + result.errorSummary();
                }
                throw new AIProviderException(detail);
            }
            if (response.error != null && !response.error.trim().isEmpty()) {
                throw new AIProviderException(response.error);
            }
            Path output = current.outputPath();
            if (snapshotHash != null
                    && !snapshotHash.equals(OpenCodeEmbeddedRuntime.sha256(current.inputPath()))) {
                throw new AIProviderException("The OpenCode CLI unexpectedly changed input.swf; "
                        + "the original file was not replaced.");
            }
            if (Files.isRegularFile(output) && Files.size(output) > 0) {
                validateSwf(output);
                ApplyResult applied = applyOutput(current, output);
                response.applyStatus = applied.status;
                response.appliedFile = applied.appliedFile;
                response.backupFile = applied.backupFile;
                if (applied.replacedOriginal) {
                    response.reloadList = current.originalList();
                }
            } else {
                response.applyStatus = ApplyStatus.NO_OUTPUT;
            }
            return response;
        } finally {
            synchronized (lock) {
                current.endRun();
                current.closeAfterRun();
            }
        }
    }

    void close() {
        synchronized (lock) {
            closed = true;
            if (session != null) {
                session.close();
            }
        }
    }

    private static String buildPrompt(String userText, OpenCodeEmbeddedRuntime.OpenCodeSession session) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are running inside an isolated FFDec AI workspace.\n\n");
        if (session.inputHash() == null) {
            prompt.append("No SWF is currently selected.\n");
        } else {
            prompt.append("The current SWF snapshot is input.swf.\n");
            prompt.append("If the user requests any modification, create output.swf. ");
            prompt.append("Do not modify input.swf.\n");
            prompt.append("FFDec CLI is available as ffdec-cli.exe on PATH.\n");
            prompt.append("Use it to export, import and replace SWF content. ");
            prompt.append("When finished, verify output.swf exists and summarize the changes.\n");
            prompt.append("The original file will only be replaced after OpenCode finishes successfully.\n");
        }
        prompt.append("\nUser message:\n").append(userText);
        return prompt.toString();
    }

    private static EmbeddedResult parseCliOutput(OpenCodeEmbeddedRuntime.CommandResult result) {
        EmbeddedResult response = new EmbeddedResult();
        StringBuilder raw = new StringBuilder();
        String[] lines = result.output.split("\\R");
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            try {
                JsonValue value = Json.parse(line);
                if (!value.isObject()) {
                    raw.append(line).append('\n');
                    continue;
                }
                JsonObject event = value.asObject();
                String type = event.getString("type", "");
                if ("text".equals(type) && event.get("part") != null
                        && event.get("part").isObject()) {
                    String text = event.get("part").asObject().getString("text", "");
                    response.text = appendSeparated(response.text, text);
                } else if ("error".equals(type) && event.get("error") != null
                        && event.get("error").isObject()) {
                    JsonObject error = event.get("error").asObject();
                    String message = error.getString("message", "OpenCode reported an error.");
                    response.text = appendSeparated(response.text, message);
                    response.error = appendSeparated(response.error, message);
                } else if ("step_finish".equals(type) && response.tokens == null
                        && event.get("part") != null && event.get("part").isObject()) {
                    JsonValue tokens = event.get("part").asObject().get("tokens");
                    if (tokens != null && tokens.isObject()) {
                        response.tokens = tokens.toString();
                    }
                }
            } catch (RuntimeException ex) {
                raw.append(line).append('\n');
            }
        }
        if (response.text == null || response.text.isEmpty()) {
            response.text = raw.toString().trim();
        }
        if (response.text == null) {
            response.text = "";
        }
        return response;
    }

    private static String appendSeparated(String base, String addition) {
        if (addition == null || addition.isEmpty()) {
            return base == null ? "" : base;
        }
        if (base == null || base.isEmpty()) {
            return addition;
        }
        if (base.endsWith("\n") || addition.startsWith("\n")) {
            return base + addition;
        }
        return base + "\n" + addition;
    }

    private static void validateSwf(Path output) throws IOException {
        if (Files.size(output) < 20) {
            throw new AIProviderException("The AI produced an incomplete output.swf; the original was not changed.");
        }
        try (InputStream input = Files.newInputStream(output)) {
            byte[] magic = new byte[3];
            int read = input.read(magic);
            String signature = read == 3 ? new String(magic, StandardCharsets.US_ASCII) : "";
            if (!"FWS".equals(signature) && !"CWS".equals(signature) && !"ZWS".equals(signature)) {
                throw new AIProviderException("The AI output is not a valid SWF file; the original was not changed.");
            }
        }
    }

    private static ApplyResult applyOutput(OpenCodeEmbeddedRuntime.OpenCodeSession session, Path output)
            throws IOException {
        ApplyResult result = new ApplyResult();
        if (!session.canReplaceOriginal()) {
            Path separate = separateOutput(session);
            Files.copy(output, separate, StandardCopyOption.REPLACE_EXISTING);
            result.appliedFile = separate.toString();
            result.status = ApplyStatus.SAVED_SEPARATE;
            result.note = "The SWF is nested or has no file path, so the result was saved as a separate file.";
            return result;
        }
        String original = session.originalPath();
        if (original == null || original.trim().isEmpty()) {
            Path separate = separateOutput(session);
            Files.copy(output, separate, StandardCopyOption.REPLACE_EXISTING);
            result.appliedFile = separate.toString();
            result.status = ApplyStatus.SAVED_SEPARATE;
            result.note = "The current SWF has no file path, so the result was saved as a separate file.";
            return result;
        }
        Path target = Paths.get(original).toAbsolutePath().normalize();
        if (!Files.isRegularFile(target)) {
            throw new IOException("The original SWF no longer exists: " + target);
        }
        Path latest = Files.createTempFile(target.getParent(), ".ffdec-current-", ".swf");
        try {
            session.saveCurrentTo(latest);
            String latestHash = OpenCodeEmbeddedRuntime.sha256(latest);
            if (!latestHash.equals(session.inputHash())) {
                Path separate = separateOutput(session);
                Files.copy(output, separate, StandardCopyOption.REPLACE_EXISTING);
                result.appliedFile = separate.toString();
                result.status = ApplyStatus.SAVED_SEPARATE;
                result.note = "The SWF was modified in the editor while the AI was running; "
                        + "the result was saved separately and the current file was not overwritten.";
                return result;
            }
        } finally {
            Files.deleteIfExists(latest);
        }
        String stamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT)
                .format(new java.util.Date());
        String fileName = target.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String extension = dot > 0 ? fileName.substring(dot) : ".swf";
        Path backup = target.resolveSibling(base + ".ai-backup-" + stamp + extension);
        Files.copy(target, backup);
        Path temp = target.resolveSibling(target.getFileName().toString() + ".ffdec-ai-tmp");
        try {
            Files.copy(output, temp, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
        result.appliedFile = target.toString();
        result.backupFile = backup.toString();
        result.note = "The original SWF was replaced; a backup was saved next to it.";
        result.status = ApplyStatus.REPLACED;
        result.replacedOriginal = true;
        return result;
    }

    private static Path separateOutput(OpenCodeEmbeddedRuntime.OpenCodeSession session) throws IOException {
        String original = session.originalPath();
        Path parent;
        String base;
        String extension = ".swf";
        if (original != null && !original.trim().isEmpty()) {
            Path target = Paths.get(original).toAbsolutePath().normalize();
            parent = target.getParent();
            String name = target.getFileName().toString();
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                base = name.substring(0, dot);
                extension = name.substring(dot);
            } else {
                base = name;
            }
        } else {
            parent = Paths.get(System.getProperty("java.io.tmpdir"));
            base = "ffdec-edited";
        }
        String stamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT)
                .format(new java.util.Date());
        Path candidate = parent.resolve(base + ".ai-edited-" + stamp + extension);
        int index = 1;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(base + ".ai-edited-" + stamp + "-" + index + extension);
            index++;
        }
        return candidate;
    }

    enum ApplyStatus {
        NO_OUTPUT,
        REPLACED,
        SAVED_SEPARATE
    }

    static final class EmbeddedResult {
        String text = "";
        String error;
        String tokens;
        String appliedFile;
        String backupFile;
        ApplyStatus applyStatus = ApplyStatus.NO_OUTPUT;
        OpenableList reloadList;
    }

    private static final class ApplyResult {
        String appliedFile;
        String backupFile;
        String note;
        ApplyStatus status = ApplyStatus.NO_OUTPUT;
        boolean replacedOriginal;
    }
}
