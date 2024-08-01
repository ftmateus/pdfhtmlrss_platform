FROM node:lts-alpine as build
WORKDIR /app
COPY package*.json .
RUN npm install
COPY ./babel.config.js .
COPY ./tsconfig.json .
COPY ./public ./public
COPY ./src ./src
RUN npm run build

FROM nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /etc/nginx/html
#COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80