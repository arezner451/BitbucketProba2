/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arezner.bitbucketproba2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 *
 * @author ati
 */
public class GetFromRepo {
    public static void main(String[] args) throws MalformedURLException, IOException {
        // Gear up Proxy handling
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("https.proxyHost", "192.168.29.1");
        systemProperties.setProperty("https.proxyPort", "8080");
        // base URL to query any info.
        String urlString = "https://api.bitbucket.org/2.0/repositories/arezner451";
        URL url = new URL(urlString);
        // open http connection and set headers.
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("X-Request-With", "Curl");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Accept", "application/json");
        // username:password -for bitbucket
//        String userNamePass = args[0];     
//        String basicAuth = "Basic " +Base64.getEncoder().encodeToString(userNamePass.getBytes());        
//        conn.setRequestProperty("Authorization", basicAuth);
        conn.setRequestMethod("GET");    
        // read the Bitbucket response.
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line +"\n");            
        }
        br.close();
        // print Bitbucket response.
        System.out.println(sb.toString());        
    }
}
