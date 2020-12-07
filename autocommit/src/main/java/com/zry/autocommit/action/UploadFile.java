package com.zry.autocommit.action;

import com.zry.autocommit.service.GithubUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zhang-rongyao
 * @version V1.0
 * @Package com.zry.autocommit.action
 * @date 2020/12/4/004 16:23
 */
@Controller
public class UploadFile {

    @Autowired
    private GithubUploader githubUploader;

    @PostMapping("/upload")
    public void UploadSQLFile(MultipartFile multipartFile) {
        try {
            githubUploader.upload(multipartFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
