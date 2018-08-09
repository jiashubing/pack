/******************************************************************************
 * FILE       : APPHOME com.sinosoft.wiki.server.FileControl
 * CREATETIME : 2007-1-5 上午11:24:51
 * MODIFYLIST ：Name       Date            Reason/Contents
 *          --------------------------------------------------------------------
 *
 ******************************************************************************/
package cn.jiashubing.pack;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileControl {

    /**
     * 删除文件或文件夹
     *
     * @param path     要删除哪个目录下的文件
     * @param fileName 要删除的文件或文件夹名
     */
    public void delFileORFolder(String path, String fileName) {

        File file = new File(path, fileName);
        if (file.isFile())
            delFile(file);
        if (file.isDirectory())
            delFolder(file);

    }

    /**
     * 复制文件或文件夹
     *
     * @param path     被控制的文件所在路径
     * @param srcName  源文件名
     * @param copyName 要复制成的文件名
     */
    public void copyFile(String path, String srcName, String copyName) {
        File srcFile = new File(path, srcName);
        File copyFile = new File(path, copyName);
        if (srcFile.isFile()) {
            copyOneFile(srcFile, copyFile);
        } else if (srcFile.isDirectory()) {
            copyFolder(srcFile, copyFile);
        } else {
            System.out.println("复制文件类型==无法判断");
        }

    }

    /**
     * 重命名文件或文件夹
     *
     * @param path    被控制的文件所在路径
     * @param oldName 原文件名
     * @param newName 新文件名
     */
    public boolean renameFile(String path, String oldName, String newName) {
        File oldFile = new File(path, oldName);
        File newFile = new File(path, newName);
        boolean success = oldFile.renameTo(newFile);
        return success;
    }

    /**
     * 删除文件
     *
     * @param filePathAndName 文本文件完整绝对路径及文件名
     * @return Boolean 成功删除返回true遭遇异常返回false
     */
    public void delFile(File filePathAndName) {
        try {
            File myDelFile = filePathAndName;
            if (myDelFile.exists())
                myDelFile.delete();
        } catch (Exception e) {
            // e.printStackTrace();
        }

    }

    /**
     * 删除文件夹
     *
     * @param folderPath 文件夹完整绝对路径
     * @return
     */
    public void delFolder(File folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容

            folderPath.delete(); // 删除空文件夹
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     * @return
     */
    public boolean delAllFile(File path) {
        boolean bea = false;

        if (!path.exists()) {
            return bea;
        }
        if (!path.isDirectory()) {// 不是目录
            return bea;
        }
        String[] tempList = path.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {

            temp = new File(path, tempList[i]);

            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(temp);// 先删除文件夹里面的文件
                delFolder(temp);// 再删除空文件夹
                bea = true;
            }
        }
        return bea;
    }

    /**
     * 复制单个文件
     *
     * @param oldPathFile 准备复制的文件源
     * @param newPathFile 拷贝到新文件
     * @return
     */
    public void copyOneFile(File oldPathFile, File newPathFile) {
        // int bytesum = 0;
        int byteread = 0;
        // File oldfile = new File(oldPathFile);
        if (oldPathFile.exists() && !newPathFile.exists()) { // 原文件存在,新文件不存在时

            // 读入原文件
            try (InputStream inStream = new FileInputStream(oldPathFile);
                 FileOutputStream output = new FileOutputStream(newPathFile)) {
                byte[] buffer = new byte[512000];// 固定缓冲区的大小, 避免缓冲区占用太多内存

                while ((byteread = inStream.read(buffer)) != -1) {
                    // bytesum += byteread; //字节数 文件大小
                    output.write(buffer, 0, byteread);
                }
                // System.out.println("复制单个文件,size="+bytesum);
                output.flush();
            } catch (Exception e) {
                System.out.println("复制单个文件操作出错");
            }
        }
    }

    /**
     * 复制整个文件夹的内容
     *
     * @param oldPath 准备拷贝的目录
     * @param newPath 指定绝对路径的新目录
     * @return
     */
    public void copyFolder(File oldPath, File newPath) {
        try {
            newPath.mkdirs(); // 如果文件夹不存在 则建立新文件夹
            String[] file = oldPath.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                temp = new File(oldPath, file[i]);

                if (temp.isFile()) {
                    File copyToPathFile = new File(newPath, temp.getName());
                    copyOneFile(temp, copyToPathFile);
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(new File(oldPath, file[i]), new File(newPath,
                            file[i]));
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 压缩文件
     *
     * @param srcfile File[] 需要压缩的文件列表
     * @param zipfile File 压缩后的文件
     */
    public static void ZipFiles(File[] srcfile, File zipfile) {
        byte[] buf = new byte[1024];
        try {
            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                    zipfile));
            // Compress the files
            for (int i = 0; i < srcfile.length; i++) {
                FileInputStream in = new FileInputStream(srcfile[i]);
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // Complete the entry
                out.closeEntry();
                in.close();
            }
            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压缩
     *
     * @param zipfile File 需要解压缩的文件
     * @param descDir String 解压后的目标目录(一般是解压到zipfile的当前路径
     */
    public static void UnZipFiles(File zipFile, String descDir) {
        if (descDir == null || descDir.equals(""))
            descDir = zipFile.getParent() + ConstantUtil.XIE_GANG;

        try {

            int i = 0;
            String dirname = descDir;
            File newdir = new File(dirname);
            newdir.mkdir();

            byte[] c = new byte[1024];
            int slen;

            // 建立与目标文件的输入连接
            ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry file = null;
            try {
                file = in.getNextEntry();
            } catch (IllegalArgumentException e) {// 读取中文文件时出现的异常
                file = in.getNextEntry();
            }
            while (file != null) {

                i = file.getName().replace('/', '\\').lastIndexOf('\\');
                if (i != -1) {
                    File dirs = new File(dirname + File.separator
                            + file.getName().substring(0, i));
                    dirs.mkdirs();
                    dirs = null;
                }
                // System.out.print("Extract "+(file.getName()).replace('/','\\')+" ........ ");

                if (file.isDirectory()) {
                    File dirs = new File(file.getName());
                    dirs.mkdir();
                    dirs = null;
                } else {
                    FileOutputStream out = new FileOutputStream(dirname
                            + File.separator + file.getName());
                    while ((slen = in.read(c, 0, c.length)) != -1)
                        out.write(c, 0, slen);
                    out.close();
                }
                // System.out.print("O.K.\n");
                try {
                    file = in.getNextEntry();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    file = in.getNextEntry();
                }
            }
            in.close();
        } catch (ZipException zipe) {
            System.out.println("不是一个ZIP文件！,文件格式错误");
            zipe.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("读取" + zipFile + "时错误！,文件读取错误");
            ioe.printStackTrace();

        }
        System.out.println("UnZipFiles O.K.");

    }

    public static void main(String[] args) {

        FileControl
                .UnZipFiles(new File("E:/temp/BBS/blueOS20070531.zip"), null);
    }

}
