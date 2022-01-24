package org.sec.data;

import org.sec.model.ResultInfo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ResultOutput {
    public static void write(String path, List<ResultInfo> results) {
        try {
            if (path == null || path.equals("")) {
                path = "result.txt";
            }
            Path finalPath = Paths.get(path);
            StringBuilder sb = new StringBuilder();
            for (ResultInfo resultInfo : results) {
                sb.append(resultInfo);
            }
            Files.write(finalPath, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
