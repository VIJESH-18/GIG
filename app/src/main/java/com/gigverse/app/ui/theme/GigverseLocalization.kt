package com.gigverse.app.ui.theme

object GigverseLocalization {

    fun get(key: String, language: String = "en"): String {
        val isTamil = language == "ta"
        return when (key) {
            "app_tagline" -> if (isTamil) "திறமையைக் கண்டறிந்து ஒன்றாக உருவாக்கி சிறந்ததை வழங்குங்கள்." else "Discover Talent. Build Together. Deliver Better."
            "market" -> if (isTamil) "சந்தை" else "Market"
            "bid_room" -> if (isTamil) "ஏல அறை" else "Bid Room"
            "gigs" -> if (isTamil) "கிக்ஸ்" else "Gigs"
            "my_work" -> if (isTamil) "எனது பணி" else "My Work"
            "profile" -> if (isTamil) "சுயவிவரம்" else "Profile"
            "admin" -> if (isTamil) "நிர்வாகம்" else "Admin"
            "post_project" -> if (isTamil) "திட்டம் பதிவிடுக" else "Post Project"
            "client_mode" -> if (isTamil) "வாடிக்கையாளர்" else "Client"
            "freelancer_mode" -> if (isTamil) "சுயதொழில் நிபுணர்" else "Freelancer"
            "admin_mode" -> if (isTamil) "நிர்வாகி" else "Admin"
            "sign_in" -> if (isTamil) "உள்நுழைக" else "Sign In"
            "sign_out" -> if (isTamil) "வெளியேறு" else "Sign Out"
            "register" -> if (isTamil) "பதிவுசெய்க" else "Register"
            "trust_score" -> if (isTamil) "நம்பகத்தன்மை" else "Trust Score"
            "ai_matching" -> if (isTamil) "AI சிறந்த பொருத்தம்" else "AI Smart Match"
            "tasks" -> if (isTamil) "பணிகள்" else "Tasks"
            "milestones" -> if (isTamil) "மைல்கற்கள்" else "Milestones"
            "disputes" -> if (isTamil) "சிக்கல்கள்" else "Disputes"
            "chat" -> if (isTamil) "உரையாடல்" else "Chat"
            "status_in_progress" -> if (isTamil) "செயலில் உள்ளது" else "In Progress"
            "status_completed" -> if (isTamil) "நிறைவடைந்தது" else "Completed"
            "hourly_rate" -> if (isTamil) "மணிநேர கட்டணம்" else "Hourly Rate"
            "explore_talent" -> if (isTamil) "சிறந்த திறமையாளர்களைக் கண்டறியுங்கள்" else "Explore Top Verified Talent"
            "search" -> if (isTamil) "தேடு" else "Search"
            "search_placeholder" -> if (isTamil) "கிக்ஸ் & திறமைகளைத் தேடுங்கள்..." else "Search Gigs, Projects & Talent..."
            else -> key
        }
    }
}
