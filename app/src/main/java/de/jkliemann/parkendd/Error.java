package de.jkliemann.parkendd;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by jkliemann on 14.12.14.
 */
public class Error {

    public static void showLongErrorToast(Context context, String errorMessage){
        Toast errorToast = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
        errorToast.show();
        return;
    }

    public static void showShortErrorToast(Context context, String errorMessage){
        Toast errorToast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
        errorToast.show();
        return;
    }

    public static HttpURLConnection getUnsecureConnection(URL url) throws IOException{
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
}
