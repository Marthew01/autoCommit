/*
package com.zry.autocommit.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

*/
/**
 * @author zhang-rongyao
 * @version V1.0
 * @Package com.zry.autocommit.service
 * @date 2020/12/7/007 14:55
 *//*

public class JGitUtil {

    @Value("${jGit.local_repo_path}")
    private static String LOCAL_REPO_PATH = "D:/workspace/project";
    @Value("${jGit.local_repogit_config}")
    private static String LOCAL_REPOGIT_CONFIG = "D:/workspace/project/.git";
    @Value("${jGit.remote_repo_uri}")
    private static String REMOTE_REPO_URI = "git@github.com:Marthew01/atguigu.git";
    @Value("${jGit.init_local_code_dir}")
    private static String INIT_LOCAL_CODE_DIR = "D:/workspace";
    @Value("${jGit.local_code_ct_sql_dir}")
    private static String LOCAL_CODE_CT_SQL_DIR = "backupDB/";
    @Value("${jGit.branch_name}")
    private static String BRANCH_NAME = "v1.0";
    @Value("${jGit.git_username}")
    private static String GIT_USERNAME = "Marthew01";
    @Value("${jGit.git_password}")
    private static String GIT_PASSWORD = "13734171265rong.";

    public enum SqlTypeEnum {
        SQL_CALC, EMAIL, MYSQL_TO_HIVE, HIVE_TO_MYSQL
    }

    final static Logger LOG = LoggerFactory.getLogger(JGitUtil.class);

    public static void main(String[] args) throws IOException{
//        setupRepository();
//        createNewRepository(LOCAL_REPO_PATH);
        writeFileToGit(SqlTypeEnum.SQL_CALC, "zhangry001", "-- 测试hehe \n select * from dual daf;", "test_测试_201806071322.sql", "test");
    }

    */
/**
     * sql脚本文件同步到git仓库
     *
     * @param qte       SQl类型
     * @param loginName 系统登录名
     * @param fileName  文件名
     * @param sqlConent 文件内容
     * @param comment   提交说明
     * @return
     *//*

    public static boolean writeFileToGit(SqlTypeEnum qte, String loginName, String sqlConent, String fileName, String comment) {

//        JGitUtil.pull();
        String dest = LOCAL_CODE_CT_SQL_DIR + qte.name().toLowerCase();
        String path = LOCAL_REPO_PATH + "/" + dest;
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        dest = dest + "/" + fileName;
        path = path + "/" + fileName;
        comment = loginName + " option of " + comment;
//
        return true == JGitUtil.createFile(sqlConent, path) == JGitUtil.commitAndPush(dest, comment);
    }

    */
/**
     * 根据主干master新建分支并同步到远程仓库
     *
     * @param branchName 分支名
     * @throws IOException
     * @throws GitAPIException
     *//*

    public static String newBranch(String branchName, Git git) throws IOException {
        String newBranchIndex = "refs/heads/"+branchName;
        String gitPathURI = "";
        try {
            //检查新建的分支是否已经存在，如果存在则将已存在的分支强制删除并新建一个分支
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                if (ref.getName().equals(newBranchIndex)) {
                    System.out.println("Removing branch before");
                    git.branchDelete().setBranchNames(branchName).setForce(true).call();
                    break;
                }
            }
            //新建分支
            Ref ref = git.branchCreate().setName(branchName).call();
            //推送到远程
            git.push().add(ref).call();
            gitPathURI = REMOTE_REPO_URI + " " + "feature/" + branchName;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return gitPathURI;
    }

    */
/**
     * 提交并推送代码至远程服务器
     *
     * @param filePath 提交文件路径(相对路径)
     * @param desc     提交描述
     * @return
     *//*

    public static boolean commitAndPush(String filePath, String desc) {

        boolean commitAndPushFlag = true;
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD);

            git.add().addFilepattern(filePath).call();
            //提交
            git.commit().setMessage(desc).call();
            //推送到远程
            if (StringUtils.isBlank(GIT_USERNAME) || StringUtils.isBlank(GIT_PASSWORD)) {
                git.push().setCredentialsProvider(provider).call();
            } else {
                git.push().call();
            }
            LOG.info("Commit And Push file " + filePath + " to repository at " + git.getRepository().getDirectory());

        } catch (Exception e) {
            e.printStackTrace();
            commitAndPushFlag = false;
            LOG.error("Commit And Push error! \n" + e.getMessage());
        }
        return commitAndPushFlag;

    }


    public static boolean pull() {
        return pull(BRANCH_NAME);
    }

    */
/**
     * 拉取远程代码
     *
     * @param remoteBranchName
     * @return 远程分支名
     *//*

    public static boolean pull(String remoteBranchName) {

        boolean pullFlag = true;
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
//        	 UsernamePasswordCredentialsProvider provider =new UsernamePasswordCredentialsProvider(GIT_USERNAME,GIT_PASSWORD);
            git.pull()
                    .setRemoteBranchName(remoteBranchName)
//        	 .setCredentialsProvider(provider)
                    .call();
        } catch (Exception e) {
            e.printStackTrace();
            pullFlag = false;
        }
        return pullFlag;
    }

    public static boolean checkout(String branchName) {

        boolean checkoutFlag = true;
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
            git.checkout().setName("refs/heads/" + branchName).setForce(true).call();
        } catch (Exception e) {
            e.printStackTrace();
            checkoutFlag = false;
        }
        return checkoutFlag;
    }

    public static boolean checkout() {

        return checkout(BRANCH_NAME);

    }

    */
/**
     * 从远程获取最新版本到本地   不会自动合并 merge
     *
     * @return
     *//*

    public static boolean fetch() {

        boolean fetchFlag = true;
        try (Git git = Git.open(new File(LOCAL_REPOGIT_CONFIG));) {
            git.fetch().setCheckFetchedObjects(true).call();
        } catch (Exception e) {
            e.printStackTrace();
            fetchFlag = false;
        }
        return fetchFlag;
    }

    */
/**
     * 拉取新创建的分支到本地
     *
     * @param cloneURL
     * @return
     *//*

    @SuppressWarnings("static-access")
    public static boolean pullNewBranchToLocal(String cloneURL) {
        boolean resultFlag = false;
        String[] splitURL = cloneURL.split(" ");
        String branchName = splitURL[1];
        String fileDir = INIT_LOCAL_CODE_DIR + "/" + branchName;
        //检查目标文件夹是否存在
        File file = new File(fileDir);
        if (file.exists()) {
            deleteFolder(file);
        }
        Git git;
        try {
            git = Git.open(new File(LOCAL_REPOGIT_CONFIG));
            git.cloneRepository().setURI(cloneURL).setDirectory(file).call();
            resultFlag = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return resultFlag;
    }


    private static void deleteFolder(File file) {
        if (file.isFile() || file.list().length == 0) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFolder(files[i]);
                files[i].delete();
            }
        }
    }

    */
/**
     * 生成文件写内容
     *
     * @param content  文件内容
     * @param filePath 文件名称
     *//*

    @SuppressWarnings("unused")
    private static boolean createFile(String content, String filePath) {

        //删除前一天临时目录
//	  File af = new File(filePath+File.separator+DateUtil.getAgoBackDate(-1));
//	  if (af.exists()) {
//		  deleteFolder(af);
//	  }
//	  //创建临时存储目录
//	  File f = new File(filePath+File.separator+DateUtil.getAgoBackDate(0));
//	  if (!f.exists()) {
//		f.mkdirs();
//	  }
//	  if (!fileName.endsWith(".sql")) {
//		  fileName+=".sql";
//	  }
        boolean createFileFlag = true;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                createFileFlag = false;
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8));) {
            bw.write(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            createFileFlag = false;
        } catch (IOException e) {
            e.printStackTrace();
            createFileFlag = false;
        }
        return createFileFlag;
    }

    */
/**
     * 创建本地新仓库
     *
     * @param repoPath 仓库地址 D:/workspace/TestGitRepository
     * @return
     * @throws IOException
     *//*

    public static Repository createNewRepository(String repoPath) throws IOException {
        File localPath = new File(repoPath);
        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();
        return repository;
    }

    */
/**
     * 创建仓库，仅需要执行一次
     *//*

    public static boolean setupRepository() {
        boolean setupRepositoryFlag = true;
        try {
            //设置远程服务器上的用户名和密码
            UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD);
            if (StringUtils.isBlank(GIT_USERNAME) || StringUtils.isBlank("")) {
                Git git = Git.cloneRepository().setURI(REMOTE_REPO_URI) //设置远程URI
                        .setBranch("master")   //设置clone下来的分支,默认master
                        .setDirectory(new File(LOCAL_REPO_PATH))  //设置下载存放路径
                        .call();
            } else {
                Git git = Git.cloneRepository().setURI(REMOTE_REPO_URI) //设置远程URI
                        .setBranch("master")   //设置clone下来的分支,默认master
                        .setDirectory(new File(LOCAL_REPO_PATH))  //设置下载存放路径
                        .setCredentialsProvider(provider) //设置权限验证
                        .call();
            }
        } catch (Exception e) {
            e.printStackTrace();
            setupRepositoryFlag = false;
        }
        return setupRepositoryFlag;
    }

}
*/
