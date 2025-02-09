#stage 1
FROM node:latest as node
WORKDIR /app
COPY elevator/ .
RUN npm install
RUN npm run build --prod

#stage 2
FROM nginx:alpine
COPY --from=node /app/dist/elevator/browser /usr/share/nginx/html

# Fix 404 on reload due to angular routing.
COPY nginx.conf /etc/nginx/nginx.conf
