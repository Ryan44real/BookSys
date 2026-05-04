package com.tem.booksys.utils;

import com.tem.booksys.config.AppConfigProperties;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class JavaCallPython {

    private final String pythonPath;
    private final String scriptPath;

    public JavaCallPython(AppConfigProperties config) {
        this.pythonPath = config.getPython().getPythonPath();
        this.scriptPath = config.getPython().getScriptPath();
    }

    public String barcode() {
        String result = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result = line;
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
