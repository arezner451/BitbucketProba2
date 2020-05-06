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
 *
 * @author ati
 */
public class SendToRepo {
    public static void main(String[] args) throws MalformedURLException, IOException {
        // Gear up Proxy handling
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("https.proxyHost", "192.168.29.1");
        systemProperties.setProperty("https.proxyPort", "8080"); 
        
        String urlString = "https://api.bitbucket.org/2.0/repositories/arezner451/proba3";
        String userNamePass = args[0];// username:password -for bitbucket
        String basicAuth = "Basic " +Base64.getEncoder().encodeToString(userNamePass.getBytes());
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        
        conn.setRequestProperty("X-Request-With", "Curl");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("POST");
                
        JSONObject jsonObject = new JSONObject(
            "{\"scm\":\"git\",\"project\": {\"key\":\"SER\"}}"
        );
        
        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        os.write(jsonObject.toString().getBytes("UTf-8"));
        osw.flush();
        os.close();
        
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line +"\n");            
        }
        br.close();
        System.out.println(sb.toString());
    }
}
