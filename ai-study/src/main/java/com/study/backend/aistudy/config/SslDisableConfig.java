package com.study.backend.aistudy.config;

import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

// 학습/테스트 환경용 SSL 검증 우회 — 프로덕션에서는 절대 사용 금지
@Configuration
public class SslDisableConfig {

  static {
    try {
      TrustManager[] trustAll = new TrustManager[]{
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
          }
      };
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAll, new java.security.SecureRandom());
      SSLContext.setDefault(sc);
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
