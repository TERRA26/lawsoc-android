package com.lawsoc.app

object TestUsers {
    private const val TEST_PASSWORD = "lawsoc123-"

    private val allowedUsers = setOf(
        // C Suites
        "alvinchen@lawsoc.org.sg",
        "shawn@lawsoc.org.sg",

        // Users
        "atiqa@lawsoc.org.sg",
        "johanna@lawsoc.org.sg",
        "rachellam@lawsoc.org.sg",
        "sabrinaansari@lawsoc.org.sg",
        "sharmila@lawsoc.org.sg",
        "sivaranjani@lawsoc.org.sg",

        // IT Team
        "josephtay@lawsoc.org.sg",
        "yongchingng@lawsoc.org.sg",
        "nurulamni@lawsoc.org.sg",
        "josephlo@lawsoc.org.sg",

        // Clixer Team
        "michelle.l@clixer.com",
        "ng.miow@clixer.com",
        "yongi.n@clixer.com",
        "kna.t@clixer.com",
        "andrew.k@clixer.com",
        "jeremy.b@clixer.com",
        "jiahao.n@clixer.com",

        // Space&Miller Team
        "jeremyspace@spacemillerco.com",
        "patryk@spacemillerco.com",
        "christophe@spacemillerco.com"
    )

    fun isAllowedUser(email: String): Boolean {
        return allowedUsers.contains(email.toLowerCase())
    }

    fun isValidTestCredentials(email: String, password: String): Boolean {
        return isAllowedUser(email) && password == TEST_PASSWORD
    }
}