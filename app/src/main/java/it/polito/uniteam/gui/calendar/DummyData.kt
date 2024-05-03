package it.polito.uniteam.gui.calendar

import it.polito.uniteam.classes.Category
import it.polito.uniteam.classes.Member
import it.polito.uniteam.classes.Priority
import it.polito.uniteam.classes.Repetition
import it.polito.uniteam.classes.Status
import it.polito.uniteam.classes.Task
import java.time.LocalDate
import java.time.Month

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

    fun getTasksToAssign(): List<Task> {
        return listOf(
            Task().apply {
                id = 4
                name = "Task4"
                description = "Description1"
                category = Category.MEETING
                priority = Priority.HIGH
                creationDate = LocalDate.of(2024,Month.MARCH,20)
                estimatedHours = 8
                spentHours = 2
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
                creationDate = LocalDate.of(2024,Month.MARCH,21)
                estimatedHours = 10
                spentHours = 3
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
                creationDate = LocalDate.of(2024,Month.MARCH,22)
                estimatedHours = 5
                spentHours = 1
                status = Status.TODO
                repetition = Repetition.NONE
                members = listOf(member5, member6)
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
                creationDate = LocalDate.of(2024,Month.MARCH,19)
                deadline = LocalDate.of(2024, Month.MAY, 3)
                estimatedHours = 5
                spentHours = 2
                status = Status.IN_PROGRESS
                repetition = Repetition.NONE
                members = listOf(member5, member6)
                schedules = hashMapOf(LocalDate.of(2024, Month.APRIL, 30) to 5)
            },
            Task().apply {
                id = 2
                name = "Task2"
                description = "Description2"
                category = Category.PROGRAMMING
                priority = Priority.MEDIUM
                creationDate = LocalDate.of(2024,Month.MARCH,18)
                deadline = LocalDate.of(2024, Month.MAY, 5)
                estimatedHours = 4
                spentHours = 1
                status = Status.IN_PROGRESS
                repetition = Repetition.WEEKLY
                members = listOf(member1, member2)
                schedules = hashMapOf(LocalDate.of(2024, Month.MAY, 1) to 4)
            },
            Task().apply {
                id = 3
                name = "Task3"
                description = "Description3"
                category = Category.DESIGN
                priority = Priority.LOW
                creationDate = LocalDate.of(2024,Month.MARCH,17)
                deadline = LocalDate.of(2024, Month.MAY, 7)
                estimatedHours = 3
                spentHours = 2
                status = Status.IN_PROGRESS
                repetition = Repetition.DAILY
                members = listOf(member3, member4)
                schedules = hashMapOf(LocalDate.of(2024, Month.MAY, 3) to 3)
            }
        )
    }
}
