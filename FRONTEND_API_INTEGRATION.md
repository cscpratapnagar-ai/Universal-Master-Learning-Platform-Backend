# Universal Master Learning Platform - Frontend API Integration

## Base URL
http://localhost:8080/api/v1

## Student flow
1. Login and retain authenticated user id/token.
2. GET /student/courses/users/{userId}
3. GET /student/learning/enrollments/{enrollmentId}
4. POST /student/learning/enrollments/{enrollmentId}/lessons/{lessonId}/complete
5. POST /student/assessments/{assessmentId}/submit
6. POST /certificates/courses/{courseId}/users/{userId}/issue
7. GET /certificates/verify/{certificateNumber}

## Course authoring
POST /courses
POST /learning/courses/{courseId}/modules
POST /learning/modules/{moduleId}/lessons
POST /courses/{id}/publish

## Assessment authoring
POST /assessments/courses/{courseId}
POST /assessments/{assessmentId}/questions

All secured endpoints must send Authorization: Bearer <accessToken>.
