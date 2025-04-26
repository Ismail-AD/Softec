package com.appdev.softec.domain.repository

import com.appdev.softec.data.repository.LoginRepositoryImpl
import com.appdev.softec.data.repository.SignUpRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): LoginRepository {
        return LoginRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideSignUpRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        supabaseClient: SupabaseClient
    ): SignUpRepository {
        return SignUpRepositoryImpl(firebaseAuth, firestore, supabaseClient)
    }
}