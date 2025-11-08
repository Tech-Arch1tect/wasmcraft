package uk.co.techarchitect.wasmcraft.wasm;

import com.dylibso.chicory.runtime.Store;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.Parser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;

public class WasmExecutor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    public static ExecutionHandle executeAsync(byte[] wasmBytes, WasmContext context) {
        PollingOutputStream stdout = new PollingOutputStream();
        PollingOutputStream stderr = new PollingOutputStream();
        Future<ExecutionResult> future = EXECUTOR.submit(() -> executeInternal(wasmBytes, context, stdout, stderr));
        return new ExecutionHandle(future, stdout, stderr);
    }

    public static ExecutionResult execute(byte[] wasmBytes) {
        return execute(wasmBytes, null);
    }

    public static ExecutionResult execute(byte[] wasmBytes, WasmContext context) {
        PollingOutputStream stdout = new PollingOutputStream();
        PollingOutputStream stderr = new PollingOutputStream();
        Future<ExecutionResult> future = EXECUTOR.submit(() -> executeInternal(wasmBytes, context, stdout, stderr));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("WASM execution failed", e);
            return ExecutionResult.error("Execution failed: " + e.getMessage());
        }
    }

    public static ExecutionResult executeFromResource(String resourcePath) {
        try (InputStream is = WasmExecutor.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOGGER.error("WASM resource not found: {}", resourcePath);
                return ExecutionResult.error("Resource not found: " + resourcePath);
            }
            byte[] wasmBytes = is.readAllBytes();
            return execute(wasmBytes);
        } catch (IOException e) {
            LOGGER.error("Failed to load WASM resource: {}", resourcePath, e);
            return ExecutionResult.error("Failed to load resource: " + e.getMessage());
        }
    }

    private static ExecutionResult executeInternal(byte[] wasmBytes, WasmContext context, PollingOutputStream stdout, PollingOutputStream stderr) {

        try {
            LOGGER.info("Parsing WASM module ({} bytes)", wasmBytes.length);
            var wasiOptions = WasiOptions.builder()
                .withStdout(stdout)
                .withStderr(stderr)
                .build();

            var wasi = WasiPreview1.builder()
                .withOptions(wasiOptions)
                .build();

            var store = new Store()
                .addFunction(wasi.toHostFunctions());

            if (context != null) {
                store.addFunction(context.toHostFunctions());
            }

            LOGGER.info("Instantiating WASM module with WASI support");

            store.instantiate("wasm-module", Parser.parse(wasmBytes));

            LOGGER.info("WASM execution completed successfully");

            return ExecutionResult.success("Execution completed");

        } catch (com.dylibso.chicory.wasi.WasiExitException e) {
            if (e.exitCode() == 0) {
                LOGGER.info("WASM execution completed with exit code 0");
                return ExecutionResult.success("Execution completed");
            } else {
                LOGGER.error("WASM execution exited with code: {}", e.exitCode());
                return ExecutionResult.error("Process exited with code: " + e.exitCode());
            }
        } catch (Exception e) {
            LOGGER.error("WASM execution error", e);
            return ExecutionResult.error("Execution error: " + e.getMessage());
        }
    }

    private static ExecutionResult buildResult(PollingOutputStream stdout, PollingOutputStream stderr) {
        List<String> outLines = stdout.pollLines();
        List<String> errLines = stderr.pollLines();
        String remainingOut = stdout.flushRemaining();
        String remainingErr = stderr.flushRemaining();

        StringBuilder result = new StringBuilder();

        for (String line : outLines) {
            if (result.length() > 0) result.append("\n");
            result.append(line);
        }

        if (remainingOut != null && !remainingOut.isEmpty()) {
            if (result.length() > 0) result.append("\n");
            result.append(remainingOut);
        }

        if (!errLines.isEmpty() || (remainingErr != null && !remainingErr.isEmpty())) {
            if (result.length() > 0) result.append("\n");
            result.append("STDERR: ");
            for (String line : errLines) {
                result.append(line).append("\n");
            }
            if (remainingErr != null && !remainingErr.isEmpty()) {
                result.append(remainingErr);
            }
        }

        if (result.length() == 0) {
            result.append("Execution completed (no output)");
        }

        return ExecutionResult.success(result.toString());
    }

    public static class ExecutionHandle {
        private final Future<ExecutionResult> future;
        private final PollingOutputStream stdout;
        private final PollingOutputStream stderr;

        private ExecutionHandle(Future<ExecutionResult> future, PollingOutputStream stdout, PollingOutputStream stderr) {
            this.future = future;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public PollingOutputStream getStdout() {
            return stdout;
        }

        public PollingOutputStream getStderr() {
            return stderr;
        }

        public boolean isDone() {
            return future.isDone();
        }

        public boolean cancel() {
            return future.cancel(true);
        }

        public ExecutionResult getResult() {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed to get WASM execution result", e);
                return ExecutionResult.error("Failed to get result: " + e.getMessage());
            } catch (CancellationException e) {
                return ExecutionResult.error("Execution was cancelled");
            }
        }

        public ExecutionResult getResultIfDone() {
            if (!isDone()) {
                return null;
            }
            return getResult();
        }
    }

    public static class ExecutionResult {
        private final boolean success;
        private final String output;
        private final String error;

        private ExecutionResult(boolean success, String output, String error) {
            this.success = success;
            this.output = output;
            this.error = error;
        }

        public static ExecutionResult success(String output) {
            return new ExecutionResult(true, output, null);
        }

        public static ExecutionResult error(String error) {
            return new ExecutionResult(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOutput() {
            return output;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return success ? "Success: " + output : "Error: " + error;
        }
    }
}
