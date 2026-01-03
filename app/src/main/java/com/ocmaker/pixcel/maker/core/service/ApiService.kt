package com.ocmaker.pixcel.maker.core.service
import com.ocmaker.pixcel.maker.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST202_OCMakerCatEmojiMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}