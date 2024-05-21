package it.polito.uniteam.classes

import android.net.Uri
import androidx.compose.runtime.toMutableStateList
import java.time.DayOfWeek
import java.time.LocalDate
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

    val member1 = Member().apply {
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
            role = categoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = emptyList()
    }

    val member2 = Member().apply {
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
            role = categoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = emptyList()
    }

    val member3 = Member().apply {
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
            role = categoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = emptyList()
    }

    val member4 = Member().apply {
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
            role = categoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = emptyList()
    }

    val member5 = Member().apply {
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
            role = categoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = emptyList()
    }

    val member6 = Member().apply {
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
            role = categoryRole.NONE
            weeklyAvailabilityTimes = 0
            weeklyAvailabilityHours = Pair(0, 0)
        })
        chats = emptyList()
    }

    fun getMembers(): List<Member> {
        return listOf(member1, member2, member3, member4, member5, member6)
    }

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
                spentTime = Pair(2, 0)
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member4, member5, member6)
                schedules = hashMapOf(getDate(DayOfWeek.TUESDAY) to Pair(5, 0))
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
                spentTime = Pair(1, 0)
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(getDate(DayOfWeek.WEDNESDAY) to Pair(4, 0))
            },
            Task().apply {
                id =++taskId
                name = "Task3"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(5)
                estimatedTime = Pair(3, 0)
                spentTime = Pair(2, 0)
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(getDate(DayOfWeek.FRIDAY) to Pair(3, 0))
            }
        )
    }

    private fun getComments(member1: Member, member2: Member): List<Comment> {
        return listOf(
            Comment(
                id =++commentId,
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
                    id =  ++historyId,
                    comment = "${member5.username} joined the team",
                    date = LocalDate.now().toString(),
                    user = member
                )
            )
        } else {
            return listOf(
                History(
                    id =  ++historyId,
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
                id =++fileId,
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
                chat = null
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
                spentTime = Pair(2,0)
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member4, member5, member6)
                schedules = hashMapOf(getDate(DayOfWeek.TUESDAY) to Pair(5,0))
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
                spentTime = Pair(1,0)
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(getDate(DayOfWeek.WEDNESDAY) to Pair(4,0))
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
                spentTime = Pair(2,0)
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(getDate(DayOfWeek.FRIDAY) to Pair(3,0))
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
                spentTime = Pair(2,0)
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
                spentTime = Pair(3,0)
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
                spentTime = Pair(1,0)
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
                spentTime = Pair(2,0)
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member4, member5, member6)
                schedules = hashMapOf(getDate(DayOfWeek.TUESDAY) to Pair(5,0))
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
                spentTime = Pair(1,0)
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(getDate(DayOfWeek.WEDNESDAY) to Pair(4,0))
            },
            Task().apply {
                id =3
                name = "Task3"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.now().minusDays(3)
                deadline = LocalDate.now().plusDays(5)
                estimatedTime = Pair(3,0)
                spentTime = Pair(2,0)
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(getDate(DayOfWeek.FRIDAY) to Pair(3,0))
            }
        )
    }
}
