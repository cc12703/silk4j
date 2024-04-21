package io.github.mzdluo123.silk4j;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;




public class NativeLibLoader {

    public static void load(File libDir) throws IOException {
        try {
            // 尝试直接加载
            System.loadLibrary("lame");
            System.loadLibrary("silk");
        } catch (UnsatisfiedLinkError ignore) {

            try {
                System.loadLibrary("liblame");
                System.loadLibrary("libsilk");
            } catch (UnsatisfiedLinkError i) {
                // 失败后
                loadFromResources(libDir);
            }
        }

    }

    private static void loadFromResources(File libDir) throws IOException {
        File dstDir = new File(libDir, "silk4j_lib");
        if (dstDir.exists()) {
            loadFromLocalDir(dstDir);
            return;
        }


        releaseFromResources(dstDir);
        loadFromLocalDir(dstDir);
    }


    private static void releaseFromResources(File dstDir) throws IOException {
        InputStream fileListInputStream = NativeLibLoader.class.getResourceAsStream("/silk4j_libs/filelist.txt");
        if (fileListInputStream == null) {
            throw new FileNotFoundException("未找到文件列表，请尝试从GitHub下载最新的AllInOneJar");
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fileListInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> fileList = bufferedReader.lines().collect(Collectors.toList());
        int successCount = 0;
        String dirName = getOSName() + "-" + getOSArch();
        for (String libFile : fileList) {
            if (!libFile.contains(dirName))
                continue;


            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }
            
            InputStream inStream = NativeLibLoader.class.getResourceAsStream(libFile.substring(1));
            File dstFile = new File(dstDir, libFile.substring(libFile.lastIndexOf('/') + 1));
            AudioUtils.streamToTempFile(inStream, dstFile);
            successCount += 1;
        }

        if (successCount < 2) {
            throw new UnsatisfiedLinkError("未找到适用于当前操作系统的动态链接库，请联系作者");
        }
    }


    private static void loadFromLocalDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        

        for (File file : files) {
            if (file.getName().endsWith(".so")
                || file.getName().endsWith(".dll")
                || file.getName().endsWith(".dylib")) {
                System.load(file.getAbsolutePath());
            }
        }
    }




    private static String getOSName() {
        String val = System.getProperty("os.name").toLowerCase();
        if (val.startsWith("windows")) {
            return "windows-shared";
        } else if (val.startsWith("mac")) {
            return "macos";
        } else if (val.startsWith("linux")) {
            return "linux";
        } else {
            return "unknown";
        }
    }

    private static String getOSArch() {
        String val = System.getProperty("os.arch").toLowerCase();
        if (val.contains("x86")) {
            return "x86";
        } else if (val.contains("x86_64")) {
            return "x86_64";
        } else if (val.contains("amd64")) {
            return "x86_64";
        } else if (val.contains("arm")) {
            return "arm";
        } else if (val.contains("arm64")) {
            return "arm64";
        } else if (val.contains("aarch64")) {
            return "arm64";
        } else {
            return "unknown";
        }
    }
}
