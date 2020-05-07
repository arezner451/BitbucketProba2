/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.arezner.bitbucketproba2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * run: arezner451:password BUS
 * 
 * @created at 2020.05.07. - 16:52:46
 * @author attila rezner <attila.rezner@gmail.com>
 */
public class IRSupload {
    // Location of the Excel data
    private static final String DATA_DIR_PATH = "c:/TEMP/BitbucketUpload";
    private static final String FILE_EXT = ".txt";

    public static void main(String[] args) throws Exception {
        // Gear up Proxy handling
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("https.proxyHost", "192.168.29.1");
        systemProperties.setProperty("https.proxyPort", "8080");
        // Get the work books
        Workbook[] workbooks = getWorkbooks(DATA_DIR_PATH);
        // roll over all workbooks
        for (Workbook workbook : workbooks) {
            Sheet mainSheet = workbook.getSheetAt(0);
            // args[0] username:password -for bitbucket
            // args[1] project name
            String fileName = getCellValue(mainSheet.getRow(1).getCell(1));
            // 2nd row is the first real data row...
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < mainSheet.getLastRowNum(); i++) {
                Row row = mainSheet.getRow(i);        
                
                String data = getCellValue(row.getCell(0));
                String value = getCellValue(row.getCell(1));
                if (!data.isEmpty() && !value.isEmpty()) {
                    sb.append(data).append(" : ");
                    sb.append(value).append("\n");
                }
            }
            createFile(fileName, sb.toString());
            uploadMetaDataInToRepo(args[0], fileName, sb.toString());
            removeFile(fileName);                                    
        }
    }

    private static Workbook[] getWorkbooks(String directory) throws Exception {
        List<Workbook> workbooks = new LinkedList<>();
        File[] files = new File(directory).listFiles();
        if (files != null) {
            for (File file : files) {
                InputStream ins = null;
                try {
                    ins = new BufferedInputStream(new FileInputStream(file));
                    String extension = FilenameUtils.getExtension(file.getName());
                    if ("xlsx".equals(extension.toLowerCase())) {
                        workbooks.add(new XSSFWorkbook(ins));
                    }
                } finally {
                    if (ins != null) {
                        try {
                            ins.close();
                        } catch (IOException e) {
                            // We ignore exceptions here.
                        }
                    }
                }
            }
        }
        return workbooks.toArray(new Workbook[workbooks.size()]);
    }

    private static String getCellValue(Cell cell) {
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf((int)cell.getNumericCellValue());
        } else {
            return "";
        }
    }
    
    private static void createFile(String fileName, String fileContent) 
        throws IOException {
        // 
        if (!fileContent.isEmpty()) { 
            File file = new File(DATA_DIR_PATH +File.separator +fileName +FILE_EXT);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContent);
            writer.close();
        }
    }
    
    private static void uploadMetaDataInToRepo(String userNamePass, 
        String fileName, String fileContent) throws IOException {
        // args contain the IRS_ID under repo will be created in bitbucket.
        String urlString = "https://api.bitbucket.org/2.0/repositories/arezner451/irss/src";
        URL url = new URL(urlString);
        // open http connection and set headers.
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("X-Request-With", "Curl");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // userNamePass username:password -for bitbucket
        conn.setRequestProperty("Authorization", "Basic " +Base64.getEncoder().encodeToString(userNamePass.getBytes()));
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        byte[] postData = (fileName +FILE_EXT +"=" +fileContent).getBytes("UTF-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");   
        // write and send the POST body. 
        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        os.write(postData);
        osw.flush();
        os.close();       
        // read the Bitbucket response.   
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null && line.length() > 0) {
                sb.append(line +"\n");            
            }
            br.close();
            // print Bitbucket response.
            if (sb.length() > 0) {
                System.out.println(sb.toString());
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            // if repo already exists
            if (ioe.getMessage().contains("HTTP response code: 400")) {
                // ok            
            } else {
                throw ioe;
            }
        } finally {
            conn.disconnect();            
        }
    }
    
    private static void removeFile(String fileName) throws IOException {
        // 
        File file = new File(DATA_DIR_PATH +File.separator +fileName +FILE_EXT);
        file.delete();
    }
 
}
