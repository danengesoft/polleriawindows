FROM node:24-alpine@sha256:06d7adf3e0ef61a13f1d28682726366f97a15aa1888c934d3ecec86c33918b53 AS build
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts
COPY angular.json tsconfig*.json ./
COPY src src
COPY public public
RUN npm run build

FROM nginx:1.28-alpine@sha256:a8b39bd9cf0f83869a2162827a0caf6137ddf759d50a171451b335cecc87d236
COPY --from=build /build/dist/polleria-frontend/browser /usr/share/nginx/html
# La configuración final se monta por Compose; sin código Node en runtime.
EXPOSE 80 443
