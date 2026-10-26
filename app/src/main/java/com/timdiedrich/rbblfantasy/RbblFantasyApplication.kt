package com.timdiedrich.rbblfantasy

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.timdiedrich.rbblfantasy.data.local.TokenManager
import com.timdiedrich.rbblfantasy.data.remote.ApiService
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.RbblRepositoryImpl
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepositoryImpl

class RbblFantasyApplication : Application(), ImageLoaderFactory {
    // These will be initialized when needed and survive configuration changes

    val tokenManager by lazy { TokenManager(context = this) }
    private val apiService by lazy { ApiService.create(getToken = { tokenManager.getToken() }) }
    val repository: RbblRepository by lazy { RbblRepositoryImpl(apiService) }
    val sessionRepository: SessionRepository by lazy { SessionRepositoryImpl() }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("player_image_cache"))
                    .maxSizeBytes(50 * 1024 *1024) // 50MB disk space limit
                    .build()
            }.memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
}