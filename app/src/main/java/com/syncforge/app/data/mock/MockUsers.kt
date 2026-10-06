package com.syncforge.app.data.mock

import com.syncforge.app.data.model.PresenceStatus
import com.syncforge.app.data.model.User
import kotlinx.collections.immutable.persistentListOf

object MockUsers {

    val currentUser = User(
        id = "u-00",
        name = "Aarav Mehta",
        handle = "@aarav",
        role = "Staff Platform Engineer",
        email = "aarav.mehta@syncforge.io",
        organization = "Northwind Labs",
        initials = "AM",
        avatarSeed = 0,
        presence = PresenceStatus.Online,
        timezone = "GMT+5:30",
        location = "Bengaluru, IN",
        tasksOpen = 7,
        tasksClosed = 214,
        commits = 1_842,
        reviews = 396,
        throughput = 0.94f,
        focusArea = "Realtime sync engine",
        skills = persistentListOf("Kotlin", "Rust", "gRPC", "Kubernetes", "Compose", "Postgres"),
        joinedOn = "Mar 2022",
    )

    val all = listOf(
        currentUser,
        User(
            id = "u-01", name = "Priya Raghunathan", handle = "@priya",
            role = "Engineering Manager", email = "priya.r@syncforge.io",
            organization = "Northwind Labs", initials = "PR", avatarSeed = 1,
            presence = PresenceStatus.Busy, timezone = "GMT+5:30", location = "Chennai, IN",
            tasksOpen = 4, tasksClosed = 138, commits = 612, reviews = 741,
            throughput = 0.88f, focusArea = "Platform roadmap",
            skills = persistentListOf("Roadmapping", "Kotlin", "SRE", "Hiring"),
            joinedOn = "Jan 2021",
        ),
        User(
            id = "u-02", name = "Marcus Bell", handle = "@marcus",
            role = "Senior Backend Engineer", email = "marcus.bell@syncforge.io",
            organization = "Northwind Labs", initials = "MB", avatarSeed = 2,
            presence = PresenceStatus.Online, timezone = "GMT-5", location = "Austin, US",
            tasksOpen = 9, tasksClosed = 176, commits = 1_507, reviews = 288,
            throughput = 0.91f, focusArea = "Event pipeline",
            skills = persistentListOf("Go", "Kafka", "Postgres", "Terraform"),
            joinedOn = "Aug 2021",
        ),
        User(
            id = "u-03", name = "Yuki Tanaka", handle = "@yuki",
            role = "Design Systems Lead", email = "yuki.tanaka@syncforge.io",
            organization = "Northwind Labs", initials = "YT", avatarSeed = 3,
            presence = PresenceStatus.Online, timezone = "GMT+9", location = "Tokyo, JP",
            tasksOpen = 6, tasksClosed = 122, commits = 489, reviews = 203,
            throughput = 0.86f, focusArea = "Forge design system",
            skills = persistentListOf("Figma", "Compose", "Motion", "Tokens"),
            joinedOn = "Jun 2022",
        ),
        User(
            id = "u-04", name = "Sofia Alvarez", handle = "@sofia",
            role = "Site Reliability Engineer", email = "sofia.a@syncforge.io",
            organization = "Northwind Labs", initials = "SA", avatarSeed = 4,
            presence = PresenceStatus.Away, timezone = "GMT+1", location = "Madrid, ES",
            tasksOpen = 3, tasksClosed = 159, commits = 903, reviews = 251,
            throughput = 0.79f, focusArea = "Observability & on-call",
            skills = persistentListOf("Kubernetes", "Prometheus", "Grafana", "Python"),
            joinedOn = "Nov 2021",
        ),
        User(
            id = "u-05", name = "Daniel Okafor", handle = "@daniel",
            role = "Android Engineer", email = "daniel.o@syncforge.io",
            organization = "Northwind Labs", initials = "DO", avatarSeed = 5,
            presence = PresenceStatus.Online, timezone = "GMT+1", location = "Lagos, NG",
            tasksOpen = 11, tasksClosed = 97, commits = 771, reviews = 132,
            throughput = 0.83f, focusArea = "Mobile client",
            skills = persistentListOf("Kotlin", "Compose", "Coroutines", "Baseline Profiles"),
            joinedOn = "Feb 2023",
        ),
        User(
            id = "u-06", name = "Elena Novak", handle = "@elena",
            role = "Staff Frontend Engineer", email = "elena.novak@syncforge.io",
            organization = "Northwind Labs", initials = "EN", avatarSeed = 6,
            presence = PresenceStatus.Busy, timezone = "GMT+2", location = "Prague, CZ",
            tasksOpen = 5, tasksClosed = 184, commits = 1_233, reviews = 367,
            throughput = 0.92f, focusArea = "Web console",
            skills = persistentListOf("TypeScript", "React", "WebGL", "Vite"),
            joinedOn = "Sep 2020",
        ),
        User(
            id = "u-07", name = "Kwame Asante", handle = "@kwame",
            role = "Data Engineer", email = "kwame.a@syncforge.io",
            organization = "Northwind Labs", initials = "KA", avatarSeed = 7,
            presence = PresenceStatus.Offline, timezone = "GMT", location = "Accra, GH",
            tasksOpen = 2, tasksClosed = 88, commits = 542, reviews = 91,
            throughput = 0.71f, focusArea = "Analytics warehouse",
            skills = persistentListOf("dbt", "Spark", "Snowflake", "SQL"),
            joinedOn = "Apr 2023",
        ),
        User(
            id = "u-08", name = "Hannah Lindqvist", handle = "@hannah",
            role = "Product Manager", email = "hannah.l@syncforge.io",
            organization = "Northwind Labs", initials = "HL", avatarSeed = 8,
            presence = PresenceStatus.Online, timezone = "GMT+1", location = "Stockholm, SE",
            tasksOpen = 8, tasksClosed = 113, commits = 74, reviews = 46,
            throughput = 0.77f, focusArea = "Collaboration surface",
            skills = persistentListOf("Discovery", "Analytics", "Specs", "Interviews"),
            joinedOn = "Jul 2022",
        ),
        User(
            id = "u-09", name = "Rafael Costa", handle = "@rafael",
            role = "Security Engineer", email = "rafael.c@syncforge.io",
            organization = "Northwind Labs", initials = "RC", avatarSeed = 9,
            presence = PresenceStatus.Busy, timezone = "GMT-3", location = "São Paulo, BR",
            tasksOpen = 6, tasksClosed = 104, commits = 431, reviews = 318,
            throughput = 0.81f, focusArea = "Zero-trust rollout",
            skills = persistentListOf("Threat modeling", "Rust", "OAuth", "SAST"),
            joinedOn = "Oct 2022",
        ),
    )

    private val byId = all.associateBy { it.id }

    fun user(id: String): User = byId[id] ?: currentUser

    fun users(ids: List<String>): List<User> = ids.mapNotNull { byId[it] }

    /** Teammates excluding the signed-in user, for DMs and member pickers. */
    val teammates = all.filter { it.id != currentUser.id }

    val onlineCount = all.count { it.presence == PresenceStatus.Online }
}
