package com.tem.booksys.utils;

import java.io.*;

public class JavaCallPython {
    public static String barcode() {
        String result = null;

        try {
            String need = null;
            // 设置Python解释器和Python脚本路径
            String pythonInterpreter = "C:/Users/Rain/AppData/Local/Microsoft/WindowsApps/python.exe"; // 根据实际情况修改解释器路径
            String pythonScript = "C:/Users/Rain/Desktop/BYSJ/barcode.py"; // 根据实际情况修改脚本路径

            // 构建ProcessBuilder对象
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScript);
            Process process = processBuilder.start();

            // 获取Python进程的输入输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            // 向Python进程输入数据
            need = null;
            // 读取Python进程的输出结果
            String line;
            while ((line = reader.readLine()) != null) {
                // 处理输出结果
                System.out.println(line);
                need = line;
            }

            // 等待Python进程执行完毕并获取退出值
            int exitCode = process.waitFor();

            System.out.println("Python process exited with code " + exitCode);
            System.out.println("卧槽" + need);
            result = need;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}
