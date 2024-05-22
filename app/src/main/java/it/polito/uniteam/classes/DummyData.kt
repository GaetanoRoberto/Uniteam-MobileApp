package it.polito.uniteam.classes

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

object DummyDataProvider {
    private var memberId: Int = 0
    private var taskId: Int = 0
    private var teamId: Int = 0
    private var chatId: Int = 0
    private var historyId: Int = 0
    private var messageId: Int = 0
    private var fileId: Int = 0
    private var commentId: Int = 0



    var member1 = Member().apply {
        id = ++memberId
        fullName = "John Doe"
        username = "johndoe"
        email = "john@example.com"
        location = "New York"
        description = "Software Engineer"
        kpi = "Excellent"
        profileImage = Uri.EMPTY
        permissionrole = permissionRole.USER
        teamsInfo = hashMapOf(1 to MemberTeamInfo().apply {
            role = CategoryRole.PROGRAMMER
            weeklyAvailabilityTimes = 5
            weeklyAvailabilityHours = Pair(3, 0)
        })
        chats = mutableListOf()
    }

    var member2 = Member().apply {
        id = ++memberId
        fullName = "Jane Smith Nome Lungo per vedere se ci entra"
        username = "janesmith"
        email = "jane@example.com"
        location = "San Francisco"
        description = "UX Designer"
        kpi = "Good"
        profileImage = Uri.EMPTY
        permissionrole = permissionRole.USER
        teamsInfo = hashMapOf(1 to MemberTeamInfo().apply {
            role = CategoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = mutableListOf()
    }

    var member3 = Member().apply {
        id = ++memberId
        fullName = "Alice Johnson"
        username = "alicejohnson"
        email = "alice@example.com"
        location = "London"
        description = "Product Manager"
        kpi = "Average"
        profileImage = Uri.EMPTY
        permissionrole = permissionRole.USER
        teamsInfo = hashMapOf(1 to MemberTeamInfo().apply {
            role = CategoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = mutableListOf()
    }

    var member4 = Member().apply {
        id = ++memberId
        fullName = "Michael Johnson"
        username = "michaelj"
        email = "michael@example.com"
        location = "Los Angeles"
        description = "Data Scientist"
        kpi = "Excellent"
        profileImage = Uri.EMPTY
        permissionrole = permissionRole.USER
        teamsInfo = hashMapOf(1 to MemberTeamInfo().apply {
            role = CategoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = mutableListOf()
    }

    var member5 = Member().apply {
        id = ++memberId
        fullName = "Emily Brown"
        username = "emilyb"
        email = "emily@example.com"
        location = "Chicago"
        description = "Graphic Designer"
        kpi = "Good"
        profileImage = Uri.EMPTY
        permissionrole = permissionRole.USER
        teamsInfo = hashMapOf(1 to MemberTeamInfo().apply {
            role = CategoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = mutableListOf()
    }

    var member6 = Member().apply {
        id = ++memberId
        fullName = "Alex Smith"
        username = "alexsmith"
        email = "alex@example.com"
        location = "Seattle"
        description = "Software Developer"
        kpi = "Average"
        profileImage = Uri.EMPTY
        permissionrole = permissionRole.USER
        teamsInfo = hashMapOf(1 to MemberTeamInfo().apply {
            role = CategoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = mutableListOf()
    }
    // Messages for direct chats
    var directMessages1 = mutableStateListOf(
        Message(id = 1, senderId = 1, message = "Hello!", creationDate = LocalDateTime.now().minusDays(1), status = messageStatus.UNREAD),
        Message(id = 2, senderId = 2, message = "Hi!", creationDate = LocalDateTime.now().minusDays(1)),
        Message(id = 3, senderId = 1, message = "How are you?", creationDate = LocalDateTime.now().minusHours(5)),
        Message(id = 4, senderId = 2, message = "I'm good, thanks!", creationDate = LocalDateTime.now().minusMinutes(1)),
        Message(id = 5, senderId = 1, message = "Great to hear!", creationDate = LocalDateTime.now()),
        Message(id = 6, senderId = 2, message = "What about you?", creationDate = LocalDateTime.now().minusHours(2)),
        Message(id = 31, senderId = 2, message = "What about you?", creationDate = LocalDateTime.now().minusHours(2)),
        Message(id = 32, senderId = 2, message = "What about you?", creationDate = LocalDateTime.now().minusHours(2)),
        Message(id = 33, senderId = 2, message = "What about you?", creationDate = LocalDateTime.now().minusHours(2)),
        Message(id = 34, senderId = 2, message = "What about you?", creationDate = LocalDateTime.now().minusHours(2)),
        Message(id = 35, senderId = 1, message = "What about you?", creationDate = LocalDateTime.now()),
    )

    var directMessages2 = mutableStateListOf(
        Message(id = 7, senderId = 3, message = "Hey there!", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 8, senderId = 4, message = "Hello!", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 9, senderId = 3, message = "What's up?", creationDate = LocalDateTime.now().minusDays(1)),
        Message(id = 10, senderId = 4, message = "Not much, you?", creationDate = LocalDateTime.now().minusHours(6)),
        Message(id = 11, senderId = 3, message = "Same here.", creationDate = LocalDateTime.now().minusHours(5)),
        Message(id = 12, senderId = 4, message = "Cool.", creationDate = LocalDateTime.now().minusHours(4))
    )

    var directMessages3 = mutableStateListOf(
        Message(id = 13, senderId = 5, message = "Hi!", creationDate = LocalDateTime.now().minusDays(3)),
        Message(id = 14, senderId = 1, message = "Hey!", creationDate = LocalDateTime.now().minusDays(3)),
        Message(id = 15, senderId = 5, message = "Long time no see.", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 16, senderId = 1, message = "Yeah, it's been a while.", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 17, senderId = 5, message = "We should catch up.", creationDate = LocalDateTime.now().minusDays(1)),
        Message(id = 18, senderId = 1, message = "Absolutely.", creationDate = LocalDateTime.now().minusHours(8))
    )

    // Messages for group chats
    var groupMessages1 = mutableStateListOf(
        Message(id = 19, senderId = 1, message = "Welcome to the team!", creationDate = LocalDateTime.now().minusDays(4), membersUnread = listOf(2,3,4,5)),
        Message(id = 20, senderId = 2, message = "Thank you!", creationDate = LocalDateTime.now().minusDays(4), membersUnread = listOf(2,3,4,5,1)),
        Message(id = 21, senderId = 3, message = "Glad to be here.", creationDate = LocalDateTime.now().minusDays(3), membersUnread = listOf(2,3,4,1,5)),
        Message(id = 22, senderId = 4, message = "Hello everyone!", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 23, senderId = 5, message = "Hi all!", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 24, senderId = 1, message = "Let's get started.", creationDate = LocalDateTime.now().minusDays(1))
    )

    var groupMessages2 = mutableStateListOf(
        Message(id = 25, senderId = 3, message = "Team meeting at 10AM.", creationDate = LocalDateTime.now().minusDays(5), membersUnread = listOf(2,3,4,1,5)),
        Message(id = 26, senderId = 2, message = "Got it.", creationDate = LocalDateTime.now().minusDays(5)),
        Message(id = 27, senderId = 1, message = "See you there.", creationDate = LocalDateTime.now().minusDays(4)),
        Message(id = 28, senderId = 4, message = "I'll be there.", creationDate = LocalDateTime.now().minusDays(3)),
        Message(id = 29, senderId = 5, message = "Looking forward to it.", creationDate = LocalDateTime.now().minusDays(2)),
        Message(id = 30, senderId = 3, message = "Don't forget to prepare your reports.", creationDate = LocalDateTime.now().minusDays(1))
    )

    // Chats
    var directChat1 = Chat(id = 1, sender = member1, receiver = member2, messages = directMessages1)
    var directChat2 = Chat(id = 2, sender = member3, receiver = member4, messages = directMessages2)
    var directChat3 = Chat(id = 3, sender = member5, receiver = member1, messages = directMessages3)

    var groupChat1 = Chat(id = 4, sender = member1, teamId = 1, messages = groupMessages1)
    var groupChat2 = Chat(id = 5, sender = member3, teamId = 2, messages = groupMessages2)
    val allChats = listOf(directChat1, directChat2, directChat3, groupChat1, groupChat2 )


    fun getMembers(): List<Member> {
        member1.chats.add(1)
        member3.chats.add(2)
        member5.chats.add(3)

        return listOf(member1, member2, member3, member4, member5, member6)
    }
/*
    private fun getChat(member1: Member, member2: Member): Chat {
        val chat = Chat()
        chat.id = ++chatId
        chat.sender = member1
        chat.receiver = member2
        val m = Message()
        m.id = ++messageId
        m.message = member1.hashCode().toString() + member2.hashCode().toString()
        chat.messages = listOf(m)
        return chat
    }
*/

    private fun getTasks(member1: Member, member2: Member, member3: Member): List<Task> {
        return listOf(
            Task().apply {
                id = ++taskId
                name = "Task1"
                description = "Description1"
                category = Category.MEETING
                priority = Priority.HIGH
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(3)
                estimatedTime = Pair(5, 0)
                spentTime = hashMapOf(member4 to Pair(2, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member4, member5, member6)
                schedules = hashMapOf(Pair(member4,getDate(DayOfWeek.TUESDAY)) to Pair(5, 0))
            },
            Task().apply {
                id = ++taskId
                name = "Task2"
                description = "Description2"
                category = Category.PROGRAMMING
                priority = Priority.MEDIUM
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(4)
                estimatedTime = Pair(4, 0)
                spentTime = hashMapOf(member1 to Pair(1, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(Pair(member1,getDate(DayOfWeek.WEDNESDAY)) to Pair(4, 0))
            },
            Task().apply {
                id = ++taskId
                name = "Task3"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(5)
                estimatedTime = Pair(3, 0)
                spentTime = hashMapOf(member3 to Pair(2, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(Pair(member3,getDate(DayOfWeek.FRIDAY)) to Pair(3, 0))
            }
        )
    }

    private fun getComments(member1: Member, member2: Member): List<Comment> {
        return listOf(
            Comment(
                id = ++commentId,
                user = member1,
                commentValue = "Great job!",
                date = LocalDate.now().toString(),
                hour = "10:00 AM"
            ),
            Comment(
                id = ++commentId,
                user = member2,
                commentValue = "Thanks!",
                date = LocalDate.now().toString(),
                hour = "11:00 AM"
            )
        )
    }

    private fun getHistory(member: Member, isTeamHistory: Boolean = false): List<History> {
        if (isTeamHistory) {
            return listOf(
                History(
                    id = ++historyId,
                    comment = "Task created successfully",
                    date = LocalDate.now().toString(),
                    user = member
                ),
                History(
                    id = ++historyId,
                    comment = "${member5.username} joined the team",
                    date = LocalDate.now().toString(),
                    user = member
                )
            )
        } else {
            return listOf(
                History(
                    id = ++historyId,
                    comment = "Task completed successfully",
                    date = LocalDate.now().toString(),
                    user = member
                ),
                History(
                    id = ++historyId,
                    comment = "Task assigned to team member",
                    date = LocalDate.now().toString(),
                    user = member
                )
            )
        }
    }

    private fun getFiles(member: Member): List<File> {
        return listOf(
            File(
                id = ++fileId,
                user = member,
                filename = "Report.pdf",
                date = LocalDate.now().toString(),
                uri = Uri.EMPTY
            ),
            File(
                id = ++fileId,
                user = member,
                filename = "Presentation.pptx",
                date = LocalDate.now().toString(),
                uri = Uri.EMPTY
            )
        )
    }

    fun getTeams(): List<Team> {
        val teams = mutableListOf<Team>()
        val members = getMembers()
        val tasks = getTasks(members[0], members[1], members[2])

        for (i in 0 until members.size) {
            tasks.forEach { task ->
                task.taskComments = getComments(members[i], members[(i + 1) % members.size])
                task.taskHistory = getHistory(members[i])
                task.taskFiles = getFiles(members[i])
            }
            val team = Team(
                id = ++teamId,
                name = "Team $i",
                description = "Description for Team $i",
                image = Uri.EMPTY,
                creationDate = LocalDate.now(),
                members = members.shuffled().take(5).toMutableList(),
                tasks = tasks.toMutableList(),
                teamHistory = getHistory(members[i],isTeamHistory = true),
                chat = if (i == 0) groupChat1 else if (i==1) groupChat2 else null
            )
            teams.add(team)
        }
        return teams
    }
    private fun getDate(day: DayOfWeek): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(day))
    }

    fun getTasksToAssign(): List<Task> {
        return listOf(
            Task().apply {
                id = 1
                name = "Task1"
                description = "Description1"
                category = Category.MEETING
                priority = Priority.HIGH
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(3)
                estimatedTime = Pair(5,0)
                spentTime = hashMapOf(member4 to Pair(2, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member4, member5, member6)
                schedules = hashMapOf(Pair(member4,getDate(DayOfWeek.TUESDAY)) to Pair(5,0))
            },
            Task().apply {
                id = 2
                name = "Task2"
                description = "Description2"
                category = Category.PROGRAMMING
                priority = Priority.MEDIUM
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(4)
                estimatedTime = Pair(4,0)
                spentTime = hashMapOf(member1 to Pair(1, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(Pair(member1,getDate(DayOfWeek.WEDNESDAY)) to Pair(4,0))
            },
            Task().apply {
                id = 3
                name = "Task3"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(5)
                estimatedTime = Pair(3,0)
                spentTime = hashMapOf(member3 to Pair(2, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(Pair(member3,getDate(DayOfWeek.FRIDAY)) to Pair(3,0))
            },
            Task().apply {
                id = 4
                name = "Task4"
                description = "Description1"
                category = Category.MEETING
                priority = Priority.HIGH
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(6)
                estimatedTime = Pair(8,0)
                spentTime = hashMapOf(member1 to Pair(2, 0))
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
            },
            Task().apply {
                id = 5
                name = "Task5"
                description = "Description2"
                category = Category.PROGRAMMING
                priority = Priority.MEDIUM
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(7)
                estimatedTime = Pair(10,0)
                spentTime = hashMapOf(member3 to Pair(3, 0))
                status = Status.TODO
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
            },
            Task().apply {
                id = 6
                name = "Task6"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(8)
                estimatedTime = Pair(5,0)
                spentTime = hashMapOf(member5 to Pair(2,0))
                status = Status.TODO
                repetition = Repetition.NONE
                members = listOf(member5, member6)
            }
        )
    }

    fun getScheduledTasks(): List<Task> {
        return listOf(
            Task().apply {
                id =1
                name = "Task1"
                description = "Description1"
                category = Category.MEETING
                priority = Priority.HIGH
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(3)
                estimatedTime = Pair(5,0)
                spentTime = hashMapOf(member4 to Pair(2,0))
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member4, member5, member6)
                schedules = hashMapOf(Pair(member4,getDate(DayOfWeek.TUESDAY)) to Pair(5,0))
            },
            Task().apply {
                id = 2
                name = "Task2"
                description = "Description2"
                category = Category.PROGRAMMING
                priority = Priority.MEDIUM
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(4)
                estimatedTime = Pair(4,0)
                spentTime = hashMapOf(member2 to Pair(1,0))
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(Pair(member2,getDate(DayOfWeek.WEDNESDAY)) to Pair(4,0))
            },
            Task().apply {
                id = 3
                name = "Task3"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(5)
                estimatedTime = Pair(3,0)
                spentTime = hashMapOf(member3 to Pair(2,0))
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(Pair(member3,getDate(DayOfWeek.FRIDAY)) to Pair(3,0))
            }
        )
    }
}
