package cn.jiashubing.pack;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 提交要更新的文件
 * 使用方法：
 * 1.配置local.properties文件
 * 2.将要提取的文件路径放到 filelist.txt，支持的3种形式（******代表任意字符）：
 * ******DHSystem/prpins/src/com/sinosoft/prpall/blsvr/cb/BLCPolicy.java
 * ******eclipseWorkSpace/DH-prpins/src/com/sinosoft/prpall/blsvr/cb/BLCPolicy.java
 * DH-prpins/src/com/sinosoft/prpall/blsvr/cb/BLCPolicy.java
 * 再处理每一行时，如果有DHSystem或eclipseWorkSpace就截取这两种字符后面的那些字符处理，
 * 如果没有DHSystem或eclipseWorkSpace，就取整行进行处理
 * 3.运行本类
 * 4.在eclipseWorkSpace/archive/目录下得到提取的文件，
 * 5.将eclipseWorkSpace/archive/ 目录下的所有文件打包
 *
 * @author ★LiuPing
 * @modified:r ☆LiuPing(2008-7-25 下午04:23:25): <br>
 */
public class AppPage {
    private static String eclipseWorkSpace = null;//斜杠结尾
    //    private static String svnSpace=null;
    private static String fileName = "filelist.txt";
    private static File patchListFile = null;
    private static Map<String, String> projectMap = new HashMap<String, String>();

    static {
        loadPorperties();
    }

    private static void interFiles() throws UnsupportedEncodingException {
        InputStream fr = AppPage.class.getResourceAsStream(fileName);
        BufferedReader in = new BufferedReader(new InputStreamReader(fr));
        FileControl fc = new FileControl();
        //清空原来的archive文件夹
        eclipseWorkSpace = new String(eclipseWorkSpace.getBytes("8859_1"), "GB2312");
        File folderPath = new File(eclipseWorkSpace, "archive");
        fc.delFolder(folderPath);
        folderPath.mkdirs();//创建文件夹
        patchListFile = new File(eclipseWorkSpace + "archive/patchList.txt");

        String oneLine = new String();
        try {
            if (!patchListFile.exists()) patchListFile.createNewFile();//创建文件列表

            while ((oneLine = in.readLine()) != null) {
                if (oneLine == null || oneLine.length() < 2) continue;
                if (oneLine.startsWith("//")) continue;
                if (oneLine.indexOf(ConstantUtil.XIE_GANG) < 0) continue;
                if (oneLine.indexOf(ConstantUtil.XIE_GANG) < 0) continue;
//                if(oneLine.indexOf(svnSpace)>=0){
//                    oneLine=oneLine.substring(svnSpace.length());
//                }
                //TODO 这里的目的是复制源文件，但是我不需要它复制，所以就先注释了
               /* if(oneLine.indexOf("/src/")>=0){
                    copySRCFile(oneLine,"");//先复制源文件
                }*/

                if (oneLine.indexOf("src/") >= 0) {//转换为class文件的路径
                    oneLine = oneLine.replaceAll("src/", "web/WEB-INF/classes/");
                    oneLine = oneLine.replaceAll(".java", ".class");
                }
                copyFile(oneLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 将一行文件替换成本地标准的文件
     *
     * @param oneLine
     */
    @SuppressWarnings("unused")
    private static String replaceToLocalPath(String oneLine) {
        String filePath = null;

        oneLine = oneLine.trim();
        //去掉eclipseWorkSpace和DHSystem前面的那一段，得到prpins/src/com/sinosoft/prpall/pubfun/Bill.java
        if (oneLine.indexOf(eclipseWorkSpace) >= 0) {
            oneLine = oneLine.substring(oneLine.indexOf(eclipseWorkSpace) + eclipseWorkSpace.length());
        }
//        if(oneLine.indexOf(svnSpace)>=0){
//            oneLine=oneLine.substring(svnSpace.length()+1);
//        }

        if (oneLine.indexOf(ConstantUtil.XIE_GANG) < 0) return oneLine;
        //将 prpins 替换成 DH-prpins
        String lineProjectName = oneLine.substring(0, oneLine.indexOf(ConstantUtil.XIE_GANG));//prpins
        String localProjectName = lineProjectName;
        String fileName = oneLine.substring(oneLine.indexOf(ConstantUtil.XIE_GANG));
        Iterator<String> projectMapIt = projectMap.keySet().iterator();
        while (projectMapIt.hasNext()) {
            String eclipseProjectName = projectMapIt.next();//
            String svnProjectName = projectMap.get(eclipseProjectName);//prpins
            if (svnProjectName != null && svnProjectName.equals(lineProjectName)) {
                localProjectName = eclipseProjectName;
                break;
            }
        }
        filePath = localProjectName + fileName;
        return filePath;
    }

    private static void copyFile(String filefullName) {
        File srcFile = new File(eclipseWorkSpace + filefullName);
        if (!srcFile.isFile()) {
            System.err.println("#文件不存在：【" + eclipseWorkSpace + filefullName + "】");
            return;
        }
        //System.out.println(filefullName);
        String filepath = eclipseWorkSpace + "archive/web/" + filefullName.substring(filefullName.indexOf(ConstantUtil.XIE_GANG), filefullName.lastIndexOf(ConstantUtil.XIE_GANG));
        File saveFloder = new File(filepath);
        if (!saveFloder.exists()) {
            saveFloder.mkdirs();//创建文件夹
        }

        File copyFile = new File(eclipseWorkSpace + "archive/web/" + filefullName.substring(filefullName.indexOf(ConstantUtil.XIE_GANG)));
        FileControl fc = new FileControl();
        fc.copyOneFile(srcFile, copyFile);
        //增加内部类的拷贝
        if (srcFile.isFile() && srcFile.getName().toLowerCase().endsWith(".class")) {
            //查看有没有存在内部类
            File[] filist = srcFile.getParentFile().listFiles();
            int len = filist.length;
            for (int i = len - 1; i > -1; i--) {
                if (filist[i].getName().startsWith(srcFile.getName().substring(0, srcFile.getName().indexOf(".")) + "$") && filist[i].getName().toLowerCase().endsWith(".class")) {
                    fc.copyOneFile(filist[i], new File(copyFile.getAbsolutePath().replace(srcFile.getName(), filist[i].getName())));

                }
            }
        }
        writeToPatchList(filefullName);
        System.out.println("复制文件到==" + copyFile.getPath());
    }


    private static void loadPorperties() {
        InputStream in = new AppPage().getClass().getResourceAsStream("local.properties");
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator propsIt = props.keySet().iterator();
        while (propsIt.hasNext()) {
            String realProject = (String) propsIt.next();
            if (realProject.equals("eclipseWorkSpace")) {
                eclipseWorkSpace = props.getProperty(realProject);
                continue;
            }
//            if(realProject.equals("svnSpace")){
//                svnSpace=props.getProperty(realProject);
//                continue;
//            }
            String localProjiec = props.getProperty(realProject);
            projectMap.put(localProjiec, realProject);
        }
    }

    private static void writeToPatchList(String filefullName) {
        String fileName = filefullName.substring(filefullName.indexOf(ConstantUtil.XIE_GANG) + 1);

        try {
            FileWriter fw = new FileWriter(patchListFile.getPath(), true);
            fw.write(fileName + "\n");
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * @param args
     * @throws UnsupportedEncodingException
     * @modified: ☆LiuPing(2008-7-25 下午04:23:27): <br>
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        AppPage.interFiles();
    }

}