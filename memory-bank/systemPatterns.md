# System Patterns

## System Architecture
- Многослойная архитектура: Controller → Service → Repository → Model (backend).
- Frontend: React SPA с роутингом, контекстом аутентификации и сервисами для API.

## Key Technical Decisions
- Spring Boot 3.x, PostgreSQL, Redis, Spring Security, OpenAPI.
- React (Vite, TypeScript, MUI, Formik, Yup, React Router).
- Docker Compose для всего стека.

## Design Patterns in Use
- Dependency Injection (Spring).
- DTO/Entity separation (минимально, можно доработать).
- Контекст аутентификации (React Context).
- Protected/Admin Route (HOC).

## Component Relationships
- Контроллеры используют сервисы, сервисы — репозитории.
- Frontend общается с backend через REST API.
- Redis кэширует пользователей.
- Docker объединяет все сервисы в одну сеть. 