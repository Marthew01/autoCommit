package com.zry.autocommit.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zry.autocommit.utils.CustomJschConfigSessionFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author zhang-rongyao
 * @version V1.0
 * @Package com.zry.autocommit.service
 * @date 2020/12/7/007 14:55
 */
@Component
public class GitAutoCommit {

    @Value("${jGit.local_repo_path}")
    private String LOCAL_REPO_PATH;
    @Value("${jGit.local_repogit_config}")
    private String LOCAL_REPOGIT_CONFIG;
    @Value("${jGit.remote_repo_uri}")
    private String REMOTE_REPO_URI;
    @Value("${jGit.init_local_code_dir}")
    private String INIT_LOCAL_CODE_DIR;
    @Value("${jGit.local_code_ct_sql_dir}")
    private String LOCAL_CODE_CT_SQL_DIR;
    @Value("${jGit.branch_name}")
    private String BRANCH_NAME = "v1.0";
    @Value("${jGit.git_username}")
    private String GIT_USERNAME;
    @Value("${jGit.git_password}")
    private String GIT_PASSWORD;
    @Value("${sqlbackup.path}")
    private String SQL_BACKUP_PATH;

    public enum SqlTypeEnum {
        ORECAL_BK, EMAIL, MYSQL_BK
    }

    final static Logger LOG = LoggerFactory.getLogger(GitAutoCommit.class);

    /**
     * 创建本地仓库
     * 仅需要执行一次
     * 生成.git
     */
    @PostConstruct
    private void setupRepository() {
        try {
            //设置远程服务器上的用户名和密码
            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD);
            if (StringUtils.isBlank(GIT_USERNAME) || StringUtils.isBlank(GIT_PASSWORD)) {
                Git git = Git.cloneRepository()
                        //设置远程URI
                        .setURI(REMOTE_REPO_URI)
                        //设置clone下来的分支,默认master
                        .setBranch("master")
                        //设置下载存放路径
                        .setDirectory(new File(LOCAL_REPO_PATH))
                        .call();
            } else {
                Git git = Git.cloneRepository().setURI(REMOTE_REPO_URI)
                        .setBranch("master")
                        .setDirectory(new File(LOCAL_REPO_PATH))
                        //设置权限验证
                        .setCredentialsProvider(provider)
                        .call();
            }
            //创建分支v2.0
            newBranch();
            //切换分支v2.0
            checkout();
        } catch (Exception e) {
            LOG.error("初始化异常 {}",e.getMessage());
        }
    }

    /**
     * sql脚本文件同步到git仓库
     *
     * @param sourcePath
     */
    public void commitGit(String sourcePath) {

        String tempFile = duplicateTo(SQL_BACKUP_PATH + sourcePath);
        if (!"".equals(tempFile)) {
            String comment = GIT_USERNAME + " option of auto bk mysql";
            LOG.info("====文件 {} 开始上传", tempFile);
            boolean commitAndPush = commitAndPush(tempFile, comment);
            LOG.info("====文件上传{}", commitAndPush == true ? "成功" : "失败");
        }
    }


    /**
     * 提交并推送代码至远程服务器
     *
     * @param filePath 提交文件路径(相对路径)
     * @param desc     提交描述
     * @return
     */
    private boolean commitAndPush(String filePath, String desc) {

        boolean commitAndPushFlag = true;
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD);
            //一定要用相对路径  多及目录使用 ‘/’
            git.add().addFilepattern(filePath).call();
            //提交
            git.commit().setMessage(desc).call();
            //推送到远程
            git.push().setCredentialsProvider(provider).call();
            LOG.info("Commit And Push file " + filePath + " to repository at " + git.getRepository().getDirectory());

        } catch (Exception e) {
            commitAndPushFlag = false;
            LOG.error("Commit And Push error! \n" + e.getMessage());
        }
        return commitAndPushFlag;
    }

    /**
     * 根据主干master新建分支并同步到远程仓库
     * <p>
     * BRANCH_NAME 分支名
     *
     * @throws IOException
     */
    public void newBranch() throws IOException {
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD);
            //检查新建的分支是否已经存在，如果存在则将已存在的分支强制删除并新建一个分支
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                if (ref.getName().equals(BRANCH_NAME)) {
                    LOG.info("Removing branch before");
                    git.branchDelete().setBranchNames(BRANCH_NAME).setForce(true).call();
                    break;
                }
            }
            //新建分支
            Ref ref = git.branchCreate().setName(BRANCH_NAME).call();
            //推送到远程
            git.push().add(ref)
                    //设置权限验证
                    .setCredentialsProvider(provider).call();

        } catch (Exception e) {
            LOG.error("创建分支异常{}",e.getMessage());
        }
    }

    /**
     * 切换分支
     * @param branchName
     * @return
     */
    private boolean checkout(String branchName) {

        boolean checkoutFlag = true;
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
            git.checkout().setName("refs/heads/" + branchName).setForce(true).call();
        } catch (Exception e) {
            e.printStackTrace();
            checkoutFlag = false;
        }
        return checkoutFlag;
    }

    private boolean checkout() {
        return checkout(BRANCH_NAME);
    }

    /**
     * copy sql脚本到本地仓库下 D:/workspace/project/.git
     *
     * @param sourcePath sql脚本路径
     * @return 返回相对路径
     */
    private String duplicateTo(String sourcePath) {
        String dest = LOCAL_CODE_CT_SQL_DIR + SqlTypeEnum.MYSQL_BK.name().toLowerCase();
        String targetPath = LOCAL_REPO_PATH + "/" + dest;
        File f = new File(targetPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        try {
            //清空本地上传的临时目录
            FileUtils.forceDelete(f);
            File[] list = new File(sourcePath).listFiles();
            if (list == null || list.length == 0) {
                LOG.warn("备份文件夹里没有文件，请查看备份日志");
                return "";
            } else {
                for (File tempFile : list) {
                    FileUtils.copyFileToDirectory(tempFile, new File(targetPath));
                }
            }
        } catch (IOException e) {
            LOG.error("IO异常：{}", e.getMessage());
        }
        return dest;
    }
}
