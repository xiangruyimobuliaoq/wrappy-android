package com.wrappy.android.common.glide;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wrappy.android.common.utils.Base64ImageFile;
import com.wrappy.android.server.response.CoreServiceResponse;


public class WrappyUserFileGlideLoader implements ModelLoader<GlideUrl, InputStream> {
    private ModelLoader<GlideUrl, InputStream> httpUrlLoader;
    private Gson gson;

    public WrappyUserFileGlideLoader(ModelLoader<GlideUrl, InputStream> httpUrlLoader, Gson gson) {
        this.httpUrlLoader = httpUrlLoader;
        this.gson = gson;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl glideUrl, int width, int height, @NonNull Options options) {
        LoadData<InputStream> httpLoadData = httpUrlLoader.buildLoadData(glideUrl, width, height, options);
        return new LoadData<>(glideUrl, new Fetcher(httpLoadData.fetcher, gson));
    }

    @Override
    public boolean handles(@NonNull GlideUrl glideUrl) {
        String stringUrl = glideUrl.toStringUrl();
        return stringUrl.contains("/api/accounts/userFile/") |
                stringUrl.contains("/api/accounts/getGroupFile/");
    }

    private static class Fetcher implements DataFetcher<InputStream> {
        private DataFetcher<InputStream> httpDataFetcher;
        private Gson gson;

        public Fetcher(DataFetcher<InputStream> httpDataFetcher, Gson gson) {
            this.httpDataFetcher = httpDataFetcher;
            this.gson = gson;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
            httpDataFetcher.loadData(priority, new DataCallback<InputStream>() {
                @Override
                public void onDataReady(@Nullable InputStream data) {
                    try (InputStreamReader reader = new InputStreamReader(data)) {
                        CoreServiceResponse<Base64ImageFile> jsonResponse = gson.fromJson(
                                reader,
                                new TypeToken<CoreServiceResponse<Base64ImageFile>>() {
                                }.getType());
                        if (jsonResponse.isSuccess()) {
                            if (jsonResponse.getData() != null) {
                                String base64Data = jsonResponse.getData().getStringValue();
                                callback.onDataReady(new ByteArrayInputStream(Base64.decode(base64Data, Base64.DEFAULT)));
                            } else {
                                callback.onDataReady(null);
                            }
                        } else {
                            callback.onLoadFailed(new Exception("Access token expired"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onLoadFailed(e);
                    }
                }

                @Override
                public void onLoadFailed(@NonNull Exception e) {
                    callback.onLoadFailed(e);
                }
            });
        }

        @Override
        public void cleanup() {
            httpDataFetcher.cleanup();
        }

        @Override
        public void cancel() {
            httpDataFetcher.cancel();
        }

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.REMOTE;
        }
    }

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        Gson gson;

        public Factory(Gson gson) {
            this.gson = gson;
        }

        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new WrappyUserFileGlideLoader(multiFactory.build(GlideUrl.class, InputStream.class), gson);
        }

        @Override
        public void teardown() {
        }
    }
}
