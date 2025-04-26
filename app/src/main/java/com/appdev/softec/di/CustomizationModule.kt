package com.appdev.softec.di

import android.content.Context
import com.appdev.softec.utils.CustomizationPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CustomizationModule {

    @Provides
    @Singleton
    fun provideCustomizationPreferences(
        @ApplicationContext context: Context
    ): CustomizationPreferences {
        return CustomizationPreferences(context)
    }
}