package cn.xxx.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.IOException;

@Slf4j
public class EexcuteUtil {

    public static class CollectingLogOutputStream extends LogOutputStream {

        @Override
        protected void processLine(String line, int level) {
            log.info("日志级别{}：{}",level,line);
        }
    }

    public static int execCmd(String command, String[] params) throws IOException {
        PumpStreamHandler streamHandler = new PumpStreamHandler(new CollectingLogOutputStream());
        CommandLine commandline = new CommandLine(command);

        if (params != null && params.length > 0) {
            commandline.addArguments(params, false);
        }
        // execCmd
        DefaultExecutor exec = new DefaultExecutor();
        exec.setExitValues(null);
        exec.setStreamHandler(streamHandler);
        log.info("exec: {}", commandline.toString());

        return exec.execute(commandline);// exit code: 0=success, 1=error
    }
}
