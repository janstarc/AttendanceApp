Attendance App 
==============
Attendance App is an Android app, combined with a simple server application in PHP and mySQL database to check student attendance at lectures.  
For the php server, check the **INSERT LINK** repository.

Problem
-------------
The app solves a problem of checking attendance of the people at some event – 
at lectures at the university, lab practices, conferences...
Its use could be extended to all scenarios, where checking attendance is needed.

In most cases, especially at university lectures, very outdated systems to 
check the attendance are being used. Usually a slate of paper is as high-tech as it gets...

Solution
------------
**AttendanceApp** is an Android app, used for scanning QR code at the lecture and thus confirming student's attendance at the lecture.  

It also has a few safety switches built-in in order to prevent "fake" attendances - QR code needs to be scanned near to location
that lecturer entered as a location of the lecture. The location is checked by the cellular network (since GPS signal is usually weak
inside a lecture hall).

Architecture
------------
* Android app for students
* Web application (PHP) to access/edit data for administrators - teachers
* mySQL database

Application overview
=====================
Screenshots of the basic functionalities of the app.

Home Page
---------
<img src="ReadMeScreenshots/1_HomeScreen.jpg" alt="Home Screen"  height="550">


Lesson Check-In
---------------
<img src="ReadMeScreenshots/2_LessonCheckIn.jpg" alt="Lesson CheckIn"  height="550">


Attendance Check
----------------
<img src="ReadMeScreenshots/3_AttendanceCheck1.jpg" alt="Attendance Check"  height="550">
<br><br>
<img src="ReadMeScreenshots/3_AttendanceCheck2.jpg" alt="Attendance Check 2"  height="550">


Register To Course
------------------
<img src="ReadMeScreenshots/4_RegisterToCourse.jpg" alt="Register to Course"  height="550">

Login
-----
<img src="ReadMeScreenshots/5_Login.jpg" alt="Login"  height="550">


Registration
------------
<img src="ReadMeScreenshots/7_Registration.jpg" alt="Registration"  height="550">


