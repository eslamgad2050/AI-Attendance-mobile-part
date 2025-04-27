
# AI-Attendance

## Project Overview

![Project](https://github.com/AI-Attendance/Project-Overview/assets/51399509/67640ad0-5abe-4e44-99b8-ca3c96e57303)

The project aims to develop a smart attendance system that uses AI technology to recognize faces, and integrates a database, a mobile app, and a web app.

(note: this repo is only for the mobile app part)

## AI Camera

* A camera is used to collect frames for employees entering the company at the start of their work day.
* Each frame is processed through detection then recognition to identify the employees.
* Each employee is then registered into the database with their time of attendance.
* Lastly, the attendance log can be verified from the web application at either the employee profile or the admin profile.

https://github.com/AI-Attendance/Project-Overview/assets/51399509/7536ffb6-8632-4940-9c87-c84001587ac8

## Mobile App

* Some employees are allowed to register their attendance through a mobile application provided that they register themselves at specific locations determined by the admin.
* The employee can register his attendance or leave at the allowed times and within a certain distance of the location registered for them.
* When he takes a selfie, the photo is sent to the AI server to verify that the id used in the app is the exact one that is linked to the employee in the image. The location and times are validated as well.
* Finally, the attendance can be viewed from the employee's page or the admin's logs in the web application.

https://github.com/AI-Attendance/Project-Overview/assets/51399509/03bde6ae-ac12-4b93-b3e9-8710fc0520ec

## Be at your Desk

* Each employee has a desk to work on with his computer powered on through their work hours.
* A webcam is attached to his computer and a service is run through the work hours to collect the hourly absence times of employees.
* The webcam takes photos at random timepoints to verify the existence of the employee.
* These photos are sent to an AI server that uses AI modules to verify the employee's existence in the image.
* When the employee leaves his desk, the webcam registers him as absent after some allowance time.
* When he returns back, the webcam registers that too.
* Lastly, all of this log is available through the web application to the admin.

https://github.com/AI-Attendance/Project-Overview/assets/51399509/88df649b-0fe9-40a7-b2f1-b6a52d541405


## Web App

* The admin can register a new user of the application by entering their information such as national ID, name, job, email, phone number, picture of the user and whether the user is an admin.
* The user can then log in to their new account and see their attendance status for today and previous days.
* The web app also has some configuration options such as the exact time for attendance and leaving and some other settings to adjust the program as needed.
* In attendance logging, you can search for a specific day, time, employer or employee.
* It also provides the closest candidate in case the program makes a wrong identification and it shows the taken picture that the identification is based on.
* You can also edit user information in case some details about a specific user need to be updated such as job position, email, phone number or picture.
* Location can be added or modified in case of remote employees that can be used in the mobile app.
* Also, you can see in the Not at desk feature when each employee has left or returned to their desk.

https://github.com/AI-Attendance/Project-Overview/assets/102368041/d5b27633-7f86-4f86-b46f-12e9bb753b9b
