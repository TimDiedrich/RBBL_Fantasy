package com.timdiedrich.rbblfantasy.data.remote

import android.util.Log
import com.timdiedrich.rbblfantasy.BuildConfig
import com.timdiedrich.rbblfantasy.data.model.AuthResponse
import com.timdiedrich.rbblfantasy.data.model.RegisterRequest
import com.timdiedrich.rbblfantasy.data.model.GetStandingsResponse
import com.timdiedrich.rbblfantasy.data.model.Match
import com.timdiedrich.rbblfantasy.data.model.FantasyPlayer
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.timdiedrich.rbblfantasy.data.model.AddToRosterRequest
import com.timdiedrich.rbblfantasy.data.model.AddToRosterResponse
import com.timdiedrich.rbblfantasy.data.model.ApiErrorResponse
import com.timdiedrich.rbblfantasy.data.model.CreateLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.CreateTeamRequest
import com.timdiedrich.rbblfantasy.data.model.DraftTeamOrder
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.FantasyTeam
import com.timdiedrich.rbblfantasy.data.model.GetAllTeamsResponse
import com.timdiedrich.rbblfantasy.data.model.GetLeaguesResponse
import com.timdiedrich.rbblfantasy.data.model.GetRealMatchesResponse
import com.timdiedrich.rbblfantasy.data.model.GetRosterResponse
import com.timdiedrich.rbblfantasy.data.model.GetTeamsResponse
import com.timdiedrich.rbblfantasy.data.model.GetUndraftedPlayersResponse
import com.timdiedrich.rbblfantasy.data.model.JoinLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.JoinLeagueResponse
import com.timdiedrich.rbblfantasy.data.model.LeagueAdminSettings
import com.timdiedrich.rbblfantasy.data.model.LeagueMembership
import com.timdiedrich.rbblfantasy.data.model.LoginRequest
import com.timdiedrich.rbblfantasy.data.model.Roster
import com.timdiedrich.rbblfantasy.data.model.UpdateLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.User
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Authentication ---
    @POST("users/register")
    suspend fun registerUser( @Body request: RegisterRequest): AuthResponse

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse


    @GET("players")
    suspend fun getPlayers(@Query("fantasy_league_id") leagueId : Int): List<FantasyPlayer>

    @GET("matches")
    suspend fun getMatches(): List<Match>

    @GET("standings/league_standing")
    suspend fun getStandings(@Query("league_id") leagueId: Int): GetStandingsResponse

    @POST("fantasy_league/create")
    suspend fun createLeague(@Body request: CreateLeagueRequest): FantasyLeague


    @GET("fantasy_league/all")
    suspend fun getAllFantasyLeagues(): GetLeaguesResponse

    @GET("fantasy_league/{id}")
    suspend fun getLeague(@Path("id") id: Int): FantasyLeague

    @PUT("fantasy_league/{id}")
    suspend fun updateLeague(@Path("id") id: Int, @Body request: UpdateLeagueRequest): FantasyLeague

    @POST("fantasy_teams/create")
    suspend fun createTeam(@Body request: CreateTeamRequest): FantasyTeam

    @GET("fantasy_teams/all")
    suspend fun getUserTeamForActiveLeague(): GetTeamsResponse

    @GET("fantasy_teams/all")
    suspend fun getAllUserTeams(): GetAllTeamsResponse


    @GET("rosters/team")
    suspend fun getRoster(@Query("fantasy_team_id") teamId: Int): GetRosterResponse


    @GET("rosters/undrafted")
    suspend fun getUndrafted(@Query("fantasy_league_id") leagueId: Int): GetUndraftedPlayersResponse

    @POST("rosters/add_player")
    suspend fun addToRoster(@Body addToRosterRequest: AddToRosterRequest) : AddToRosterResponse

    @PUT("rosters/save")
    suspend fun saveRoster(@Body roster: List<AddToRosterRequest>): GetRosterResponse

    @GET("users/current")
    suspend fun getCurrentUser(): User

    @GET("real_games/all")
    suspend fun getRealMatches(): GetRealMatchesResponse

    @GET("real_games/{week}")
    suspend fun getRealMatchesByWeek(@Path("week") week: Int): GetRealMatchesResponse

    @POST("fantasy_league/join")
    suspend fun joinLeague(@Body request : JoinLeagueRequest): JoinLeagueResponse

    @GET("fantasy_league/get_memberships")
    suspend fun getMyLeagueMemberships() : List<LeagueMembership>

    @GET("fantasy_league/get_all_members")
    suspend fun getAllLeagueMembers(@Query("league_id") leagueId: Int) : List<LeagueMembership>


    @GET("league_admin_settings/{league_id}")
    suspend fun getLeagueAdminSettings(@Path("league_id") leagueId: Int): LeagueAdminSettings

    @GET("draft_order/{league_id}")
    suspend fun getDraftOrder(@Path("league_id") leagueId: String): List<DraftTeamOrder>


    companion object {
        private val BASE_URL = BuildConfig.BASE_URL

        fun create(getToken: () -> String? = { null }): ApiService {
            //Log.d("RBBLFantasy", "ApiService: BASE_URL = $BASE_URL")
            //Log.d("RBBLFantasy", "ApiService: SOCKET_URL = ${BuildConfig.SOCKET_URL}")

            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                getToken()?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }



            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            // Interceptor to parse error body on HTTP failure status codes (4xx / 5xx)
            val errorInterceptor = Interceptor { chain ->
                val response = chain.proceed(chain.request())
                if (!response.isSuccessful) {
                    val source = response.body?.source()
                    source?.request(Long.MAX_VALUE) // Buffer the entire body
                    val buffer = source?.buffer?.clone()
                    val jsonString = buffer?.readUtf8()

                    if (!jsonString.isNullOrEmpty()) {
                        val errorMessage = try {
                            moshi.adapter(ApiErrorResponse::class.java).fromJson(jsonString)?.error
                        } catch (e: Exception) {
                            null
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            // Throws IOException with backend error message for Retrofit to catch
                            throw java.io.IOException(errorMessage)
                        }
                    }
                }
                response
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(errorInterceptor)
                .addInterceptor(logger)
                .addInterceptor(authInterceptor)
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return retrofit.create(ApiService::class.java)

        }
    }
}
