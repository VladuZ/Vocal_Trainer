package com.example.testing;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("auth/register")
    Call<User> registerUser(@Body User user);

    @POST("auth/login")
    Call<LoginResponse> loginUser(@Body User user);

    @PATCH("user/change/username")
    Call<ChangeResponse> changeUsername(@Query("newUsername") String newUsername);

    @PATCH("user/change/email")
    Call<ChangeResponse> changeEmail(@Query("newEmail") String newEmail);

    @PATCH("user/change/password")
    Call<ChangeResponse> changePassword(@Query("newPassword") String newPassword);

    @POST("user/delete")
    Call<Void> deleteAccount();

    @Multipart
    @POST("exercise/create")
    Call<Void> createExercise(
            @Part("exerciseName") RequestBody exerciseName,
            @Part MultipartBody.Part data,
            @Part("exerciseBpm") int exerciseBpm
    );

    @GET("exercise/find")
    Call<List<ExerciseDto>> findExercises();

    @GET("exercise/get/{id}")
    Call<ExerciseDto> getExercise(@Path("id") Long id);

    @DELETE("exercise/delete/{id}")
    Call<Void> deleteExercise(@Path("id") Long id);

    @POST("sharing/create")
    Call<Void> createSharing(@Query("id") Long id, @Query("targetName")String targetName);

    @GET("sharing/find")
    Call<List<SharingDto>> findSharings();

    @POST("sharing/accept/{id}")
    Call<Void> acceptSharing(@Path("id") Long id);

    @DELETE("sharing/decline/{id}")
    Call<Void> declineSharing(@Path("id") Long id);
}

