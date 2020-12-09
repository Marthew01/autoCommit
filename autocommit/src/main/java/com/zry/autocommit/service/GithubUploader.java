package com.zry.autocommit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * @author zhang-rongyao
 * @version V1.0
 * @Package com.zry.autocommit.service
 * @date 2020/12/4/004 16:23
 */
@Service
public class GithubUploader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubUploader.class);
    private static final String URI_SEPARATOR = "/";
    private static final Set<String> ALLOW_FILE_SUFFIX = new HashSet<>(Arrays.asList("txt", "sql", "dat"));

    @Value("${github.bucket.url}")
    private String url;

    @Value("${sqlbackup.path}")
    private String sqlBackupPath;

    @Value("${github.bucket.api}")
    private String api;

    @Value("${github.bucket.access-token}")
    private String accessToken;

    @Autowired
    RestTemplate restTemplate;


    /**
     * 使用accessToken进行上传，token有时间限制，不建议使用该方法
     * 遍历文件夹里所有sql文件
     *
     * @param dataPath
     * @throws IOException
     */
    public void preUploadFile(String dataPath) throws IOException {
//      "D:\\project\\daychange\\40\\temp\\2020\\12\\4\\";
        String basePath = sqlBackupPath + dataPath;
        LOGGER.info("文件 {}", basePath);
        File[] list = new File(basePath).listFiles();
        if (list == null || list.length == 0) {
            LOGGER.warn("文件夹里没有文件，请查看备份日志");
        } else {
            for (File file : list) {
                LOGGER.info("====文件 {} 开始上传", file.getName());
                upload(file);
                LOGGER.info("====文件 {} 上传完成", file.getName());
            }
        }
    }

    /**
     * @param file
     * @throws IOException
     */

    public void upload(File file) throws IOException {

        byte[] bytes = file2byte(file);
        String fileName = file.getName();
        String suffix = this.getSuffix(fileName).toLowerCase();
        if (!ALLOW_FILE_SUFFIX.contains(suffix)) {
            throw new IllegalArgumentException("不支持的文件后缀：" + suffix);
        }
        exec(bytes, fileName);
    }

    /**
     * @param multipartFile
     * @throws IOException
     */
    public void upload(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        String suffix = this.getSuffix(fileName).toLowerCase();
        if (!ALLOW_FILE_SUFFIX.contains(suffix)) {
            throw new IllegalArgumentException("不支持的文件后缀：" + suffix);
        }
        exec(multipartFile.getBytes(), fileName);
    }

    /**
     * 上传
     * @param bytes    文件二进制
     * @param fileName 文件名
     */
    public void exec(byte[] bytes, String fileName) {

        String[] folders = this.getDateFolder();
        // 最终的文件路径
        String filePath = new StringBuilder(
                String.join(URI_SEPARATOR, folders))
                .append(URI_SEPARATOR)
                .append(fileName).toString();
        LOGGER.info("上传文件到Github：{}", filePath);
        JsonObject payload = new JsonObject();
        payload.add("message", new JsonPrimitive("file upload"));
        payload.add("content", new JsonPrimitive(Base64.getEncoder().encodeToString(bytes)));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "token " + this.accessToken);
        LOGGER.info("上传文件：{}", this.api + filePath);
        ResponseEntity<String> responseEntity = this.restTemplate.exchange(this.api + filePath, HttpMethod.PUT,
                new HttpEntity<String>(payload.toString(), httpHeaders), String.class);
        if (responseEntity.getStatusCode().isError()) {
            LOGGER.error("上传失败");
        }
        JsonObject response = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
        LOGGER.info("上传完毕");
    }


    /**
     * 将文件转换成byte数组
     *
     * @param file
     * @return
     */
    public byte[] file2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    /**
     * 获取文件的后缀
     *
     * @param fileName
     * @return
     */
    protected String getSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            String suffix = fileName.substring(index + 1);
            if (!suffix.isEmpty()) {
                return suffix;
            }
        }
        throw new IllegalArgumentException("非法的文件名称：" + fileName);
    }

    /**
     * 按照年月日获取打散的打散目录
     * yyyy/mm/dd
     *
     * @return
     */
    protected String[] getDateFolder() {
        String[] retVal = new String[3];

        LocalDate localDate = LocalDate.now();
        retVal[0] = localDate.getYear() + "";

        int month = localDate.getMonthValue();
        retVal[1] = month < 10 ? "0" + month : month + "";

        int day = localDate.getDayOfMonth();
        retVal[2] = day < 10 ? "0" + day : day + "";

        return retVal;
    }
}