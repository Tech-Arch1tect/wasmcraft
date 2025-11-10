package uk.co.techarchitect.wasmcraft.computer.command.builtin;

import uk.co.techarchitect.wasmcraft.computer.command.Command;
import uk.co.techarchitect.wasmcraft.computer.command.CommandContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadCommand implements Command {
    @Override
    public String getName() {
        return "download";
    }

    @Override
    public String getUsage() {
        return "download <url>";
    }

    @Override
    public String getDescription() {
        return "Download WASM from URL";
    }

    @Override
    public void execute(CommandContext context, String[] args) {
        if (args.length < 1) {
            context.addOutput("Usage: download <url>");
            context.addOutput("Example: download https://example.com/program.wasm");
            return;
        }

        String url = String.join(" ", args);

        String filename;
        try {
            String path = url.substring(url.lastIndexOf('/') + 1);
            if (path.isEmpty() || !path.contains(".")) {
                context.addOutput("ERROR: Invalid URL - cannot determine filename");
                return;
            }
            filename = path;
            if (!filename.endsWith(".wasm")) {
                filename = filename + ".wasm";
            }
        } catch (Exception e) {
            context.addOutput("ERROR: Invalid URL format");
            return;
        }

        context.addOutput("Downloading from " + url + "...");

        try {
            byte[] data = downloadFile(url);
            context.getFileSystem().put(filename, data);
            context.addOutput("Downloaded " + filename + " (" + data.length + " bytes)");
            context.markChanged();
        } catch (Exception e) {
            context.addOutput("ERROR: " + e.getMessage());
        }
    }

    private byte[] downloadFile(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "Wasmcraft/1.0");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
        }

        int contentLength = connection.getContentLength();
        if (contentLength > 10 * 1024 * 1024) {
            throw new IOException("File too large (max 10MB)");
        }

        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }
}
