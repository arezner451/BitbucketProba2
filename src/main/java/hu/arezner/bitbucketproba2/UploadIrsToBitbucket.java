/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hu.arezner.bitbucketproba2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import org.json.JSONObject;

/**
 * @created at 2020.05.06. - 17:31:33
 * @author attila rezner <attila.rezner@gmail.com>
 */
public class UploadIrsToBitbucket {
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        // Gear up Proxy handling
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("https.proxyHost", "192.168.29.1");
        systemProperties.setProperty("https.proxyPort", "8080"); 
        // args contain the IRS_ID under repo will be created in bitbucket.
        String urlString = "https://api.bitbucket.org/2.0/repositories/arezner451/" +args[1].toLowerCase();
        URL url = new URL(urlString);
        // open http connection and set headers.
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("X-Request-With", "Curl");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        // username:password -for bitbucket
        String userNamePass = args[0];
        String basicAuth = "Basic " +Base64.getEncoder().encodeToString(userNamePass.getBytes());
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("POST");
        // create request body in JSON. 
        JSONObject jsonObject = new JSONObject(
            // args contain the Bitbucket project under repo has to be created : BUS
            "{\"scm\":\"git\",\"project\": {\"key\":\"" +args[2] +"\"}}"
        );
        // write and send the POST body. 
        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        os.write(jsonObject.toString().getBytes("UTF-8"));
        osw.flush();
        os.close();
        // read the Bitbucket response.
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");            
        }
        br.close();
        // print Bitbucket response.
        System.out.println(sb.toString());    
    }
    
/*    
    curl 
        -x 192.168.29.1:8080 
        -X POST 
        -u arezner451:Attila22a 
        https://api.bitbucket.org/2.0/repositories/arezner451/irs_001394_l2a001/src 
        -F README.md=@C:/temp/README.md
*/
    
}
