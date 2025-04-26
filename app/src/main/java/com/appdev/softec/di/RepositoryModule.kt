package com.appdev.softec.di

import com.appdev.softec.data.repository.LoginRepositoryImpl
import com.appdev.softec.data.repository.MoodRepositoryImpl
import com.appdev.softec.data.repository.SignUpRepositoryImpl
import com.appdev.softec.data.repository.TaskRepositoryImpl
import com.appdev.softec.domain.repository.LoginRepository
import com.appdev.softec.domain.repository.MoodRepository
import com.appdev.softec.domain.repository.SignUpRepository
import com.appdev.softec.domain.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideTaskRepository(firebaseAuth: FirebaseAuth,firestore: FirebaseFirestore): TaskRepository {
        return TaskRepositoryImpl(firestore,firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideMoodRepository(firebaseAuth: FirebaseAuth,firestore: FirebaseFirestore): MoodRepository {
        return MoodRepositoryImpl(firestore,firebaseAuth)
    }


    @Provides
    @Singleton
    fun provideSignUpRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): SignUpRepository {
        return SignUpRepositoryImpl(firebaseAuth, firestore)
    }
}