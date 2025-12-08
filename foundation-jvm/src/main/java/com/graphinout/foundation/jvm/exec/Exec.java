package com.graphinout.foundation.jvm.exec;

import com.graphinout.foundation.pure.collections.bi.IPair;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;


/**
 * TODO kill processes on JVM exit
 *
 * @author xamde
 */
public class Exec {

    public static class Call {

        /** deeper in Java the command parts are just space-concatenated */
        final String[] command;
        @Nullable final File workDir;
        /**
         * format [ "key=value", "key=value",..]; @Nullable
         */
        private String[] envPairs;

        /**
         * @param workDir null = inherit workDir of parent process
         * @param command to run. Deeper in Java the command parts are just space-concatenated.
         */
        public Call(final @Nullable File workDir, final String[] command) {
            super();
            this.workDir = workDir;
            this.command = command;
        }

        public String[] getEnvPairs() {
            return this.envPairs;
        }

        @Override
        public String toString() {
            return "Executing = " + String.join(" ", this.command) + "\n" + // .
                    "WorkDir   = " + (this.workDir == null ? "--" : this.workDir.getAbsolutePath()) + "\n" // .
                    + "Env       = " + Arrays.toString(this.envPairs);
        }

        void setEnv(final Map<String, String> env) {
            if (env != null) {
                envPairs = new String[env.size()];
                int i = 0;
                for (final Entry<String, String> e : env.entrySet()) {
                    envPairs[i] = e.getKey() + "=" + e.getValue();
                    i++;
                }
            }
        }

        @SafeVarargs
        final void setEnv(final IPair<String, String>... env) {
            if (env != null) {
                envPairs = new String[env.length];
                for (int i = 0; i < envPairs.length; i++) {
                    final IPair<String, String> pair = env[i];
                    envPairs[i] = pair.getFirst() + "=" + pair.getSecond();
                }
            }
        }

    }

    public static class CallConf {

        boolean captureErrStream;

        boolean captureOutStream;
        /** maximal time before timeout. use -1 for infinity. */
        long maxMillis;
        boolean showErrStream;
        /** when setting to false, some processes don't run well */
        boolean showOutStream;

        public CallConf(final long maxMillis, final boolean captureOutStream, final boolean captureErrStream, final boolean showOutStream, final boolean showErrStream) {
            super();
            this.maxMillis = maxMillis;
            this.captureOutStream = captureOutStream;
            this.captureErrStream = captureErrStream;
            this.showOutStream = showOutStream;
            this.showErrStream = showErrStream;
        }

    }

    public static class Command {

        final Call call;

        final CallConf callConf;

        public Command(final Call call, final CallConf callConf) {
            super();
            this.call = call;
            this.callConf = callConf;
        }

    }

    public static class Result {

        public static final Result DO_NOTHING = create(true, 0, 0, false);
        public StringBuilder errStream;
        public StringBuilder outStream;
        private boolean started;
        private int statusCode;
        private long timeInMillis;
        private boolean timeOut;

        private Result() {}

        private static Result create(final boolean started, final int statusCode, final int timeInMillis, final boolean timeOut) {
            final Result r = new Result();
            r.started = started;
            r.statusCode = statusCode;
            r.timeInMillis = timeInMillis;
            r.timeOut = timeOut;
            return r;
        }

        public int getStatusCode() {
            return this.statusCode;
        }

        public boolean isOK() {
            return this.started && getStatusCode() == 0;
        }

        public boolean isTimeout() {
            return this.timeOut;
        }

        public void markAsFailed() {
            this.started = false;
        }

        @Override
        public String toString() {
            return "Result [ok=" + this.started + ", timeInMillis=" + this.timeInMillis + ", statusCode=" + this.statusCode + ", timeOut=" + this.timeOut + "]";
        }

    }

    private static final Logger log = LoggerFactory.getLogger(Exec.class);

    /**
     * @param cmd to execute
     * @return Result object
     */
    public static Result execute(final Command cmd) {
        log.info(cmd.toString());

        // prepare result
        final Runtime runtime = Runtime.getRuntime();
        InputStream processIn = null;
        InputStream processErr = null;
        OutputStream processOut = null;
        Thread killThread = null;
        try {
            log.info("Workdir: " + cmd.call.workDir.getAbsolutePath());
            log.info("Call:    " + String.join(" ", cmd.call.command));
            log.info("--- Command Output --- 8< --------------- Command running ...");
            final Process process = runtime.exec(cmd.call.command, cmd.call.getEnvPairs(), cmd.call.workDir);
            processIn = process.getInputStream();
            processErr = process.getErrorStream();
            processOut = process.getOutputStream();
            // spawn a thread in which the blocking call happens
            final Thread runThread = new Thread(() -> {
                try {
                    process.waitFor();
                } catch (final InterruptedException e) {
                    log.info("Command interrupted");
                    Thread.currentThread().interrupt();
                }
            });
            killThread = new Thread(process::destroyForcibly);

            Runtime.getRuntime().addShutdownHook(killThread);
            runThread.start();

            final long startMs = System.currentTimeMillis();
            boolean timeLeft = true;
            boolean isDone = false;
            final StringBuilder outStream = new StringBuilder();
            final StringBuilder errStream = new StringBuilder();

            while (timeLeft && !isDone) {
                timeLeft = System.currentTimeMillis() < startMs + cmd.callConf.maxMillis;
                try {
                    process.exitValue();
                    isDone = true;
                } catch (final IllegalThreadStateException e) {
                    isDone = false;
                }

                /* pump streams */
                while (processIn.available() > 0) {
                    final char c = (char) processIn.read();
                    if (cmd.callConf.captureOutStream) {
                        outStream.append(c);
                    }
                    if (cmd.callConf.showOutStream) {
                        System.out.print(c);
                    }
                }
                if (cmd.callConf.showOutStream) {
                    System.out.flush();
                }

                while (processErr.available() > 0) {
                    final char c = (char) processErr.read();
                    if (cmd.callConf.captureErrStream) {
                        errStream.append(c);
                    }
                    if (cmd.callConf.showErrStream) {
                        System.err.print(c);
                    }
                }
                if (cmd.callConf.showErrStream) {
                    System.err.flush();
                }

                // always pump user input back to console
                while (System.in.available() > 0) {
                    processOut.write(new byte[]{(byte) System.in.read()});
                }

                if (!isDone) {
                    /* progress message */
                    final long secondsLeft = (startMs + cmd.callConf.maxMillis - System.currentTimeMillis()) / 1000;
                    if (secondsLeft % 10 == 0) {
                        log.info(secondsLeft + " seconds left for '" + cmd.call.command + "'");
                    }

                    /* output thread sleeps, process thread runs */
                    Thread.sleep(1000);
                }
            }
            // we waited long enough
            final Result result = new Result();
            result.timeInMillis = System.currentTimeMillis() - startMs;
            if (isDone) {
                result.started = true;
                result.timeOut = false;
                result.statusCode = process.exitValue();
            } else {
                result.started = false;
                result.timeOut = true;
                result.statusCode = -1;
                // kill process
                runThread.interrupt();
                process.destroy();
            }
            result.outStream = outStream;
            result.errStream = errStream;
            log.info("---------------------------------- >8 --- ... Command Done.  ");
            log.info("Result = " + result);
            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (killThread != null) {
                Runtime.getRuntime().removeShutdownHook(killThread);
            }
            try {
                if (processErr != null) {
                    processErr.close();
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if (processOut != null) {
                    processOut.close();
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if (processIn != null) {
                    processIn.close();
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param workDir          as context to command
     * @param command          to run
     * @param maxMillis        maximal time before timeout. use -1 for infinity.
     * @param captureOutStream to get it in Result object
     * @param captureErrStream to get it in Result object
     * @param showOutStream    when setting to false, some processes don't run well
     * @param showErrStream
     * @param env              array of Pairs, each element of which has environment variable settings in the format
     *                         name=value, or null if the subprocess should inherit the environment of the current
     *                         process.
     * @return Result will be {@link Result#isOK()} if all went well (exit code == 0)
     */
    @SafeVarargs
    public static Result execute(final File workDir, final String[] command, final long maxMillis, final boolean captureOutStream, final boolean captureErrStream, final boolean showOutStream, final boolean showErrStream, final IPair<String, String>... env) {

        Call call = new Call(workDir, command);
        call.setEnv(env);
        CallConf callConf = new CallConf(maxMillis, captureOutStream, captureErrStream, showOutStream, showErrStream);
        final Command cmd = new Command(call, callConf);
        return execute(cmd);
    }

    /**
     * @param workDir for command
     * @param command to run
     * @param env     array of Pairs, each element of which has environment variable settings in the format name=value,
     *                or null if the subprocess should inherit the environment of the current process.
     * @return true if all went well (exit code == 0)
     */
    @SafeVarargs
    public static Result execute(final File workDir, final String[] command, final int maxRuntimeMillis, final IPair<String, String>... env) {
        return execute(workDir, command, maxRuntimeMillis, true, true, true, true, env);
    }

}
