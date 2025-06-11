# Frontend Dockerfile
FROM node:20-alpine as build
WORKDIR /app

# Используем ARG для переменных времени сборки - ПЕРЕД копированием файлов!
ARG VITE_API_URL=http://localhost:8080/api
ARG VITE_TELEGRAM_BOT_NAME=alg_gor_bot

# Устанавливаем переменные окружения для production build
ENV VITE_API_URL=${VITE_API_URL}
ENV VITE_TELEGRAM_BOT_NAME=${VITE_TELEGRAM_BOT_NAME}

# Проверяем что переменные установлены правильно
RUN echo "Building with VITE_API_URL: $VITE_API_URL"

COPY package*.json ./
RUN npm install
COPY . .

RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"] 