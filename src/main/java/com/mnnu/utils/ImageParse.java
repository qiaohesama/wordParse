package com.mnnu.utils;

import org.apache.poi.util.IOUtils;

import java.io.*;

/**
 * @author qiaoh
 */
public class ImageParse {
    int number = 0;
    private String targetDir;
    private String baseUrl;

    public ImageParse(String targetDir, String baseUrl) {
        super();
        this.targetDir = targetDir;
        this.baseUrl = baseUrl;
    }


    public String parse(byte[] data, String extName) {
        return parse(new ByteArrayInputStream(data), extName);
    }


    public String parse(InputStream in, String extName) {
        if (extName.lastIndexOf(".") > -1) {
            extName = extName.substring(extName.lastIndexOf(".") + 1);
        }
        String filename = "image_" + (number++) + "." + extName;
        File target = new File(targetDir);
        if (!target.exists()) {
            target.mkdirs();
        }
        try {
            IOUtils.copy(in, new FileOutputStream(new File(target, filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baseUrl + filename;
    }
}
