package uk.co.techarchitect.wasmcraft.wasm;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PollingOutputStream extends OutputStream {
    private final ConcurrentLinkedQueue<String> lines = new ConcurrentLinkedQueue<>();
    private final StringBuilder currentLine = new StringBuilder();

    @Override
    public synchronized void write(int b) {
        if (b == '\n') {
            String line = currentLine.toString();
            lines.offer(line);
            currentLine.setLength(0);
        } else if (b != '\r') {
            currentLine.append((char) b);
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        String text = new String(b, off, len, StandardCharsets.UTF_8);
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                String line = currentLine.toString();
                lines.offer(line);
                currentLine.setLength(0);
            } else if (c != '\r') {
                currentLine.append(c);
            }
        }
    }

    public List<String> pollLines() {
        List<String> result = new ArrayList<>();
        String line;
        while ((line = lines.poll()) != null) {
            result.add(line);
        }
        return result;
    }

    public synchronized String flushRemaining() {
        if (currentLine.length() > 0) {
            String remaining = currentLine.toString();
            currentLine.setLength(0);
            return remaining;
        }
        return null;
    }
}
