package com.zry.autocommit.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @author zhang-rongyao
 * @version V1.0
 * @Package com.zry.autocommit.utils
 * @date 2020/12/8/008 16:34
 */
@Component
public class CustomJschConfigSessionFactory extends JschConfigSessionFactory {
    @Override
    protected void configure(OpenSshConfig.Host host, Session session) {
    }
    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {

        JSch defaultJSch = new JSch();
        //私钥文件的路径,可放在项目目录中
        defaultJSch.addIdentity(".ssh/id_rsa");
        //git 仓库域名对应的known_hosts文件,可放在项目目录中
        defaultJSch.setKnownHosts(".ssh/known_hosts");
        return defaultJSch;
    }
}
