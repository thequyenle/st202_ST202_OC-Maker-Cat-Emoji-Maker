package com.oc.maker.cat.emoji.core.service
import com.oc.maker.cat.emoji.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST202_OCMakerCatEmojiMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}