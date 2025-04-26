package com.appdev.softec.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.appdev.softec.domain.model.UserEntity
import com.appdev.softec.domain.repository.SignUpRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject


class SignUpRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SignUpRepository {

    private val profileBucketId = "profileimages"
    private val profileFolderPath = "public/1ruh469_1"


    override suspend fun signUp(
        userEntity: UserEntity,
        result: (message: String, success: Boolean) -> Unit
    ) {
        try {
            // Skip the image upload step
//            val profileImageUrl = ""

            // Create user with Firebase Auth
            firebaseAuth.createUserWithEmailAndPassword(userEntity.email, userEntity.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                        val userProfile = hashMapOf(
                            "name" to userEntity.name,
                            "email" to userEntity.email,
                            "userId" to userId
                        )

                        firestore.collection("profiles")
                            .document(userId)
                            .set(userProfile)
                            .addOnSuccessListener {
                                result("Account created successfully", true)
                            }
                            .addOnFailureListener { e ->
                                result("Account created, but data upload failed", false)
                            }
                    } else {
                        result(task.exception!!.message.toString(), false)
                    }
                }
        } catch (e: Exception) {
            result("Failed to create account: ${e.message}", false)
        }
    }

//    private suspend fun uploadProfileImageToSupabase(imageUri: Uri, imageBytes: ByteArray): String {
//        return withContext(Dispatchers.IO) {
//            try {
//                val extension = when {
//                    // Try to get from Uri
//                    imageUri.lastPathSegment?.contains(".") == true ->
//                        imageUri.lastPathSegment?.substringAfterLast('.')?.lowercase()
//                    // Fallback to detecting from bytes
//                    else -> when {
//                        imageBytes.size >= 2 && imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte() -> "jpg"
//                        imageBytes.size >= 8 && String(
//                            imageBytes.take(8).toByteArray()
//                        ) == "PNG\r\n\u001a\n" -> "png"
//
//                        else -> "jpg" // Default fallback
//                    }
//                } ?: "jpg"
//
//                // Validate extension
//                val safeExtension = when (extension.lowercase()) {
//                    "jpg", "jpeg", "png", "gif" -> extension
//                    else -> "jpg"
//                }
//
//                val fileName = "${UUID.randomUUID()}.$safeExtension"
//                val fullPath = "$profileFolderPath/$fileName"
//
//                val bucket = supabaseClient.storage.from(profileBucketId)
//                bucket.upload(
//                    path = fullPath,
//                    data = imageBytes
//                ) { upsert = false }
//
//                bucket.publicUrl(fullPath)
//            } catch (e: Exception) {
//                Log.d("CHKAZX","${e.localizedMessage}")
//                throw Exception("${e.message}")
//            }
//        }
//    }
}

