package de.jkliemann.parkendd;

import android.location.Location;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Formatter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by jkliemann on 21.02.15.
 */
public class Util {

    public static HttpURLConnection getUnsecureConnection(URL url) throws IOException {
        TrustManager trustAll[] = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};
        SSLContext sslc = null;
        try {
            sslc = SSLContext.getInstance("SSL");
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        try {
            sslc.init(null, trustAll, new SecureRandom());
        }catch (KeyManagementException e){
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
        HostnameVerifier allValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allValid);

        return (HttpURLConnection) url.openConnection();
    }

    public static double getDistance(Location src, Location dst){
        return src.distanceTo(dst);
    }

    public static String getViewDistance(double dist){
        String vdist = "";
        if(dist < 1000){
            vdist = "ca. " + Integer.toString((int)Math.round(dist / 10) * 10) + "m";
        }else if( dist < 10000){
            Formatter fmt = new Formatter();
            fmt.format("%.1f", dist/1000);
            vdist = "ca. " + fmt.toString() + "km";
        }else{
            vdist = "ca. " + Integer.toString((int)Math.round(dist) / 1000) + "km";
        }
        return vdist;
    }

}
