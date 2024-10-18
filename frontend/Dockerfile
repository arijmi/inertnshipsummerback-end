# Use the official Node.js image to build the frontend
FROM node:20.15.1 AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build --prod

# Use the official Nginx image to serve the frontend
FROM nginx:alpine
COPY --from=build /app/www /usr/share/nginx/html
EXPOSE 80