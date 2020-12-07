package com.zry.autocommit.utils;

import com.zry.autocommit.service.GithubUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author zhang-rongyao
 * @version V1.0
 * @Package com.zry.schdualmysql.utils
 * @date 2020/12/2/002 17:52
 */
@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/");

    @Autowired
    private GithubUploader githubUploader;

    /**
     * 表示每个星期日早5点
     * 0 0 5 ? * SUN
     *
     * 测试用  每30秒执行一次
     * 30 * * * * ?
     */
    @Scheduled(cron = "30 * * * * ?")
    public void reportCurrentTime() {

        String dataPath = LocalDate.now().format(formatter);
        log.info("The time is now {}", dataPath);
        try {
            githubUploader.preUploadFile(dataPath);
        } catch (IOException e) {
            log.info("IO异常：{}",e.getMessage());
        }
    }
}
