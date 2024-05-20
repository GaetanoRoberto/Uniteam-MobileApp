package it.polito.uniteam.gui.calendar

import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Task
import it.polito.uniteam.classes.Team
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object DummyDataProvider {
    val member1 = Member().apply {
        id = 1
        fullName = "John Doe"
        username = "johndoe"
        email = "john@example.com"
        location = "New York"
        description = "Software Engineer"
        kpi = "Excellent"
    }

    val member2 = Member().apply {
        id = 2
        fullName = "Jane Smith"
        username = "janesmith"
        email = "jane@example.com"
        location = "San Francisco"
        description = "UX Designer"
        kpi = "Good"
    }

    val member3 = Member().apply {
        id = 3
        fullName = "Alice Johnson"
        username = "alicejohnson"
        email = "alice@example.com"
        location = "London"
        description = "Product Manager"
        kpi = "Average"
    }

    val member4 = Member().apply {
        id = 4
        fullName = "Michael Johnson"
        username = "michaelj"
        email = "michael@example.com"
        location = "Los Angeles"
        description = "Data Scientist"
        kpi = "Excellent"
    }

    val member5 = Member().apply {
        id = 5
        fullName = "Emily Brown"
        username = "emilyb"
        email = "emily@example.com"
        location = "Chicago"
        description = "Graphic Designer"
        kpi = "Good"
    }

    val member6 = Member().apply {
        id = 6
        fullName = "Alex Smith"
        username = "alexsmith"
        email = "alex@example.com"
        location = "Seattle"
        description = "Software Developer"
        kpi = "Average"
    }

    fun getDummyProfile() : Member {
        return member1
    }

    fun getMembers(): List<Member> {
        return listOf(
            member2,
            member3,
            member4,
            member5,
            member6
        )
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

    private fun getDate(day: DayOfWeek): LocalDate {
        if(LocalDate.now().dayOfWeek < day) {
            return LocalDate.now().with(TemporalAdjusters.nextOrSame(day))
        } else {
            return LocalDate.now().with(TemporalAdjusters.previousOrSame(day))
        }
    }

    fun getTeams(): List<Team>{
        return listOf(
            Team().apply {
                id=1
                name= "Team1"
                description= "description1"
                members= mutableListOf(member1, member2)
                tasks= mutableListOf(Task().apply {
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
                    members = listOf(member1, member5, member6)
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
                    })

            },
            Team().apply {
                id=2
                name= "Team2"
                description= "description1"
                members= mutableListOf(member1, member2)
                tasks= mutableListOf(Task().apply {
                    id = 3
                    name = "Task3"
                    description = "Description1"
                    category = Category.MEETING
                    priority = Priority.HIGH
                    creationDate = LocalDate.now().minusDays(3)
                    deadline = LocalDate.now().plusDays(3)
                    estimatedTime = Pair(5,0)
                    spentTime = Pair(2,0)
                    status = Status.IN_PROGRESS
                    repetition = Repetition.NONE
                    members = listOf(member1, member5, member6)
                    schedules = hashMapOf(getDate(DayOfWeek.TUESDAY) to Pair(5,0))
                },
                    Task().apply {
                        id = 4
                        name = "Task4"
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
                    })

            }
        )
    }

    fun getScheduledTasks(): List<Task> {
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
            }
        )
    }
}
