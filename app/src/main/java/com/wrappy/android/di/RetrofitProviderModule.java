package com.wrappy.android.di;

import javax.inject.Named;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;
import com.wrappy.android.BuildConfig;
import com.wrappy.android.server.AuthRepository;
import com.wrappy.android.server.AuthorizationService;
import com.wrappy.android.server.account.AccountService;
import com.wrappy.android.server.util.AuthBasicInterceptor;
import com.wrappy.android.server.util.AuthBearerInterceptor;
import com.wrappy.android.server.util.LiveDataCallAdapterFactory;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RetrofitProviderModule {

    @Provides
    @AppScope
    AuthBasicInterceptor provideAuthBasicInterceptor() {
        return new AuthBasicInterceptor();
    }

    @Provides
    @AppScope
    @Named("no_token")
    OkHttpClient provideUnsafeOkHttpClient(AuthBasicInterceptor basicInterceptor) {
        // TODO: replace with normal OkHttpClient once server-side fixes https
        try {
            // Create a trust manager that does not validate certificate chains
//            final TrustManager[] trustAllCerts = new TrustManager[]{
//                    new X509TrustManager() {
//                        @Override
//                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
//                        }
//
//                        @Override
//                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
//                        }
//
//                        @Override
//                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                            return new java.security.cert.X509Certificate[]{};
//                        }
//                    }
//            };
//            // Install the all-trusting trust manager
//            final SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//            // Create an ssl socket factory with our all-trusting manager
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            builder.sslSocketFactory(sslSocketFactory);
//            builder.hostnameVerifier((hostname, session) -> true);

            builder.addInterceptor(basicInterceptor);
            //builder.addInterceptor(new HttpLoggingInterceptor().setLevel(Level.BODY));
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @AppScope
    @Named("no_token")
    Retrofit provideRetrofitWithoutAuth(@Named("no_token") OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL_CORE_SERVICE)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @AppScope
    AuthorizationService provideAuthService(@Named("no_token") Retrofit retrofit) {
        return retrofit.create(AuthorizationService.class);
    }

    @Provides
    @AppScope
    AuthBearerInterceptor provideAuthBearerInterceptor(AuthRepository authRepository) {
        return new AuthBearerInterceptor(authRepository);
    }

    @Provides
    @AppScope
    OkHttpClient provideOkHttpClient(AuthBearerInterceptor authBearerInterceptor) {
        try {
            // Create a trust manager that does not validate certificate chains
//            final TrustManager[] trustAllCerts = new TrustManager[]{
//                    new X509TrustManager() {
//                        @Override
//                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
//                        }
//
//                        @Override
//                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
//                        }
//
//                        @Override
//                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                            return new java.security.cert.X509Certificate[]{};
//                        }
//                    }
//            };
//            // Install the all-trusting trust manager
//            final SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//            // Create an ssl socket factory with our all-trusting manager
//            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
//            builder.sslSocketFactory(sslSocketFactory);
//            builder.hostnameVerifier((hostname, session) -> true);

            builder.addInterceptor(authBearerInterceptor);
            //builder.addInterceptor(new HttpLoggingInterceptor().setLevel(Level.BODY));
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        return new OkHttpClient.Builder()
//                .addInterceptor(authBearerInterceptor)
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(Level.BODY))
//                .build();
    }

    @Provides
    @AppScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL_CORE_SERVICE)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @AppScope
    AccountService provideAccountService(Retrofit retrofit) {
        return retrofit.create(AccountService.class);
    }
}
