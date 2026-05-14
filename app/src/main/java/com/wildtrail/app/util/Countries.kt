package com.wildtrail.app.util

/**
 * Static (ISO-derived) country list used in the sign-up dropdown. Kept as
 * a plain Kotlin object so it costs nothing to load and is trivial to test.
 *
 * Not exhaustive — covers the major destinations a hiking-app user is
 * likely to come from. The dropdown supports type-to-filter so missing
 * entries can be added cheaply.
 */
object Countries {
    val ALL: List<String> = listOf(
        "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Argentina",
        "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain",
        "Bangladesh", "Belarus", "Belgium", "Belize", "Bolivia",
        "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria",
        "Burkina Faso", "Cambodia", "Cameroon", "Canada", "Chad", "Chile",
        "China", "Colombia", "Costa Rica", "Croatia", "Cuba", "Cyprus",
        "Czechia", "Denmark", "Dominican Republic", "Ecuador", "Egypt",
        "El Salvador", "Estonia", "Ethiopia", "Fiji", "Finland", "France",
        "Georgia", "Germany", "Ghana", "Greece", "Guatemala", "Honduras",
        "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland",
        "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya",
        "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon",
        "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar", "Malaysia",
        "Maldives", "Mali", "Malta", "Mexico", "Moldova", "Monaco", "Mongolia",
        "Montenegro", "Morocco", "Mozambique", "Myanmar", "Nepal",
        "Netherlands", "New Zealand", "Nicaragua", "Nigeria", "North Macedonia",
        "Norway", "Oman", "Pakistan", "Panama", "Paraguay", "Peru",
        "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia",
        "Rwanda", "San Marino", "Saudi Arabia", "Senegal", "Serbia",
        "Singapore", "Slovakia", "Slovenia", "South Africa", "South Korea",
        "Spain", "Sri Lanka", "Sweden", "Switzerland", "Syria", "Taiwan",
        "Tajikistan", "Tanzania", "Thailand", "Togo", "Tunisia", "Turkey",
        "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom",
        "United States", "Uruguay", "Uzbekistan", "Venezuela", "Vietnam",
        "Yemen", "Zambia", "Zimbabwe", "Other",
    )
}
