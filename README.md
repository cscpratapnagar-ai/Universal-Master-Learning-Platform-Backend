# Universal Master Learning Platform - Backend

Enterprise backend for the Universal Master Learning Platform.

## Architecture
Modular monolith with layered architecture:
Controller → Service → ServiceImpl → Repository → Entity

Feature modules own their DTOs, mappers, exceptions and events where needed.
