package org.mcgill.ca.it.security;

import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import static okhttp3.OkHttpClient.Builder;

/**
 * SSLContextManager provides an OkHttpClient
 */
public class SSLContextManager {
    private TrustManager[] trustManagers;
    private SSLContext sslContext;

    private SSLContextManager() {

        this.trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        try {
            this.sslContext = SSLContext.getInstance("SSL");
            this.sslContext.init(null, this.trustManagers, new SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }
    }

    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    public TrustManager getFirstTrustManager() {
        return getTrustManagers()[0];
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public SSLSocketFactory getSslFactory() {
        return getSslContext().getSocketFactory();
    }

    public static OkHttpClient getHttpClient(int timeout) {
        SSLContextManager sslContextManager = new SSLContextManager();
        Builder builder = new Builder();
        builder.sslSocketFactory(sslContextManager.getSslFactory(), (X509TrustManager) sslContextManager.getFirstTrustManager());
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
        builder.hostnameVerifier((hostname, session) -> true);
        return builder.build();
    }
}
