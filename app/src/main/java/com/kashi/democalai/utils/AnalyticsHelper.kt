package com.kashi.democalai.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor() {
    
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    
    fun initialize(context: Context) {
        firebaseAnalytics = Firebase.analytics
    }
    
    // User Authentication Events
    fun logUserSignIn(method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
    
    fun logUserSignOut() {
        firebaseAnalytics.logEvent("user_sign_out", Bundle())
    }
    
    fun logUserRegistration(method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
    
    // Content Creation Events
    fun logPostCreated(postLength: Int, hasMedia: Boolean = false) {
        firebaseAnalytics.logEvent("post_created") {
            param("post_length", postLength.toLong())
            param("has_media", if (hasMedia) "yes" else "no")
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_post")
        }
    }
    
    fun logPostCreationStarted() {
        firebaseAnalytics.logEvent("post_creation_started", Bundle())
    }
    
    fun logPostCreationCancelled() {
        firebaseAnalytics.logEvent("post_creation_cancelled", Bundle())
    }
    
    // Content Consumption Events
    fun logPostViewed(postId: String, authorId: String, isOwnPost: Boolean) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.ITEM_ID, postId)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "post")
            param("author_id", authorId)
            param("is_own_post", if (isOwnPost) "yes" else "no")
        }
    }
    
    fun logFeedViewed(feedType: String, postCount: Int) {
        firebaseAnalytics.logEvent("feed_viewed") {
            param("feed_type", feedType) // "all_posts" or "my_posts"
            param("post_count", postCount.toLong())
        }
    }
    
    fun logFeedRefreshed(feedType: String) {
        firebaseAnalytics.logEvent("feed_refreshed") {
            param("feed_type", feedType)
        }
    }
    
    fun logLoadMorePosts(feedType: String, currentPostCount: Int) {
        firebaseAnalytics.logEvent("load_more_posts") {
            param("feed_type", feedType)
            param("current_post_count", currentPostCount.toLong())
        }
    }
    
    // Navigation Events
    fun logScreenView(screenName: String, screenClass: String? = null) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
    }
    
    // Filter and Search Events
    fun logFilterToggled(newFilterState: String) {
        firebaseAnalytics.logEvent("filter_toggled") {
            param("filter_type", newFilterState) // "all_posts" or "my_posts"
        }
    }
    
    // Error Events
    fun logError(errorType: String, errorMessage: String, screenName: String) {
        firebaseAnalytics.logEvent("app_error") {
            param("error_type", errorType)
            param("error_message", errorMessage)
            param("screen_name", screenName)
        }
    }
    
    // User Engagement Events
    fun logUserEngagement(action: String, duration: Long? = null) {
        firebaseAnalytics.logEvent("user_engagement") {
            param("action", action)
            duration?.let { param("duration_ms", it) }
        }
    }
    
    // App Performance Events
    fun logAppLaunch() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())
    }
    
    fun logAppBackground() {
        firebaseAnalytics.logEvent("app_background", Bundle())
    }
    
    // Business Metrics
    fun logDailyActiveUser() {
        firebaseAnalytics.logEvent("daily_active_user", Bundle())
    }
    
    // Note: Sessions are automatically tracked by Firebase Analytics
    // We don't need to manually log session start events
    
    // User Properties
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
    
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
    
    // Custom events for business insights
    fun logUserRetention(daysSinceFirstUse: Int) {
        firebaseAnalytics.logEvent("user_retention") {
            param("days_since_first_use", daysSinceFirstUse.toLong())
        }
    }
    
    fun logFeatureUsage(featureName: String, usageCount: Int) {
        firebaseAnalytics.logEvent("feature_usage") {
            param("feature_name", featureName)
            param("usage_count", usageCount.toLong())
        }
    }
}

// Extension function for easier Bundle creation
private inline fun FirebaseAnalytics.logEvent(name: String, block: Bundle.() -> Unit) {
    val bundle = Bundle().apply(block)
    logEvent(name, bundle)
}

private fun Bundle.param(key: String, value: String) {
    putString(key, value)
}

private fun Bundle.param(key: String, value: Long) {
    putLong(key, value)
} 