package com.s0l.moneyexchanger.api.interfaces

import com.s0l.moneyexchanger.BuildConfig
import com.s0l.moneyexchanger.model.ExchangeInfoModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

//https://api.exchangeratesapi.io/latest
interface ExchangeRatesApiService {
    @GET("latest")
    fun getCurrentExchangeInfo(): Observable<ExchangeInfoModel>

    companion object Factory {
        private const val apiURL = "https://api.exchangeratesapi.io/"
        private const val networkDelay: Long = 5

        fun createRetrofit(): ExchangeRatesApiService {
            val moshiBuilder = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .client(getHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshiBuilder))
                .baseUrl(apiURL)
                .build()
            return retrofit.create(ExchangeRatesApiService::class.java)
        }

        private fun getHttpClient(): OkHttpClient {
            val okHttpBuilder = OkHttpClient.Builder()
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
            okHttpBuilder.connectTimeout(networkDelay, TimeUnit.SECONDS)
            okHttpBuilder.readTimeout(networkDelay, TimeUnit.SECONDS)
            okHttpBuilder.writeTimeout(networkDelay, TimeUnit.SECONDS)

            okHttpBuilder.addInterceptor(interceptor)

            return okHttpBuilder.build()
        }
    }
}