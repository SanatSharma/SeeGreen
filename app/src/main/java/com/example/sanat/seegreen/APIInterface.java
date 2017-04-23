package com.example.sanat.seegreen;

/**
 * Created by David on 4/22/2017.
 */

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {
    @GET("analyze")
    Call<AnalyzeResponse> getAnalytics(@Query("query") String query);
}
