package com.zry.autocommit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

class AutocommitApplicationTests {
    final static Logger LOG = LoggerFactory.getLogger(AutocommitApplicationTests.class);

    @Test
    void contextLoads() {
        Boolean commitAndPush = false;

        LOG.info("====文件上传{}", commitAndPush == true ? "成功" : "失败");
    }

}
