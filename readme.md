**Project: STUDENT STUDY SCHEDULE MANAGEMENT AND BOOKING APPLICATION**

## Problem statement
Create a project with the following 4 tables:
* Account(id, username, password, role)
* Student(id, studentCode, fullname, email, phone, accountId)
* Subject(id, subjectCode, subjectName, description, credits)
* StudySchedule(id, studentId, subjectId, studyDate, startTime, endTime, location, status)

And required functionalities:
* Login: Implement a login function with two roles: Admin and Student, and provide the corresponding functions for each role.
* Login Account Storage: Store login account information using SharedPreferences.
* Data Storage: Store the tables using SQLite.
* Student Management: Allow administrators to add, update, delete, search, and view student information.
* Subject Management: Allow administrators to add, update, delete, search, and view subject information.
* Study Schedule Management: Allow students to create, update, delete, search, and view their personal study schedules.
* Study Schedule Booking: Allow students to select a subject, choose a study date, set the start and end times, specify a study location, and book a study session.
* Schedule Conflict Validation: Prevent students from booking overlapping study sessions on the same date and time.
* Schedule Status Management: Allow students to view and manage their bookings with statuses such as Pending, Confirmed, and Cancelled.
* Calendar View: Display study schedules in a daily, weekly, or monthly calendar view.
* Notifications: Notify students of upcoming study sessions and schedule changes.
* Version Control and Collaboration: Work together and manage the project code using GitHub or GitLab.
* Scoring: The score is based on the time taken to complete the project. The less time a student takes, the higher the score.
