package workertest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {
    @FunctionalInterface
    interface OnHttpResponse {
        void onResponse(String r);
    }
    
    @FunctionalInterface
    public interface OnTempratureConverted {
        void temp(Optional<Double> d);
    }
    
    private void post(final String url, String body, OnHttpResponse responceCb) throws IOException {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        
        // Header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Length", body.length() + "");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();

        /*// Handle response code if needed
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        */

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //System.out.println(response.toString());
        responceCb.onResponse(response.toString());
    }
    
    private void convertTemprature(final String url, final String body, OnTempratureConverted tempCb) {
        try {
            post(url, body, (r) -> {
                // Sample response as of 31/10/2018:
                // <?xml version="1.0" encoding="utf-8"?><string xmlns="https://www.w3schools.com/xml/">33.8</string>
                String pattern = ">-*[0-9]+.*[0-9]*<";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(r.toString());
                if (m.find( )) {
                    String val = m.group(0);
                    val = val.substring(1,  val.length());
                    val = val.substring(0, val.length() - 1);
                    Double d = new Double(val);
                    tempCb.temp(Optional.of(d));
                } else {
                   System.out.println("NO MATCH");
                   tempCb.temp(Optional.empty());
                }
            });
        } catch (IOException | NumberFormatException e) {
            tempCb.temp(Optional.empty());
        }
    }
    
    public void celsiusToFahrenheit(double c, OnTempratureConverted tempCb) {
        String url = "https://www.w3schools.com/xml/tempconvert.asmx/CelsiusToFahrenheit";
        String body = "Celsius=" + c;
        convertTemprature(url, body, tempCb);
    }
    
    public void fahrenheitToCelsius(double f, OnTempratureConverted tempCb) {
        String url = "https://www.w3schools.com/xml/tempconvert.asmx/FahrenheitToCelsius";
        String body = "Fahrenheit=" + f;
        convertTemprature(url, body, tempCb);
    }
}