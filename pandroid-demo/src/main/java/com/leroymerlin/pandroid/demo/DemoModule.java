package com.leroymerlin.pandroid.demo;

import android.content.Context;
import android.text.TextUtils;

import com.leroymerlin.pandroid.analytics.AnalyticsManager;
import com.leroymerlin.pandroid.analytics.impl.GoogleAnalyticsManager;
import com.leroymerlin.pandroid.app.PandroidConfig;
import com.leroymerlin.pandroid.demo.globals.review.ReviewManager;
import com.leroymerlin.pandroid.demo.globals.review.ReviewManagerImpl;
import com.leroymerlin.pandroid.demo.globals.review.ReviewService;
import com.leroymerlin.pandroid.log.LogWrapper;
import com.leroymerlin.pandroid.net.PandroidCallAdapterFactory;
import com.leroymerlin.pandroid.ui.picture.PictureManager;
import com.leroymerlin.pandroid.ui.picture.impl.GlidePictureManagerImpl;
import com.leroymerlin.pandroid.ui.toast.ToastManager;
import com.leroymerlin.pandroid.ui.toast.impl.SuperToastManagerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by florian on 04/01/16.
 */
@Module
public class DemoModule {


    public DemoModule() {
    }


    //tag::Retrofit[]
    @Provides
    @Singleton
    ReviewService provideDemoService(OkHttpClient.Builder clientBuilder, Context context, LogWrapper logWrapper) {
        //PandroidCall handle Action Delegate on main thread and mock annotation
        PandroidCallAdapterFactory factory = PandroidCallAdapterFactory.create(context, logWrapper);
        factory.setMockEnable(PandroidConfig.DEBUG);
        Retrofit.Builder builder = new Retrofit.Builder()
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(factory)
                .baseUrl(BuildConfig.BASE_URL_PRODUCT);
        return builder.build().create(ReviewService.class);
    }
    //end::Retrofit[]

    @Provides
    ReviewManager provideReviewManager(ReviewManagerImpl reviewManager) {
        return reviewManager;
    }

    //tag::provideAnalyticsManager[]
    @Provides
    @Singleton
    AnalyticsManager provideAnalyticsManager(GoogleAnalyticsManager googleAnalyticsManager) {

        // set your GA ID Analytics
        String AnalyticsGaId = "YOUR_ANALYTICS_GA_ID";

        if (!TextUtils.isEmpty(AnalyticsGaId)) {
            googleAnalyticsManager.addTracker(
                    googleAnalyticsManager.newTrackerBuilder().enableAutoTracking(true).setId(AnalyticsGaId).setName("MainTracker").build()
            );
        }
        return googleAnalyticsManager;
    }
    //end::provideAnalyticsManager[]

    //tag::provideToastManager[]
    @Provides
    ToastManager provideToastManager(SuperToastManagerImpl superToastManager) {
        return superToastManager;
    }
    //end::provideToastManager[]

    //tag::Glide[]
    @Provides
    @Singleton
    PictureManager providePictureManager(Context context) {
        GlidePictureManagerImpl glidePictureManager = new GlidePictureManagerImpl(context);
        glidePictureManager.setPlaceHolder(R.drawable.pandroid_img_nophoto);
        return glidePictureManager;
    }
    //end::Glide[]

}
