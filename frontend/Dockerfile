FROM node:lts as builder

WORKDIR /app

COPY . .

RUN yarn install \
    --prefer-offline \
    --non-interactive \
    --production=false

RUN yarn build

RUN rm -rf node_modules && \
    NODE_ENV=production yarn install \
    --prefer-offline \
    --pure-lockfile \
    --non-interactive \
    --production=true

FROM node:lts

WORKDIR /app

COPY --from=builder /app  .

EXPOSE 8080

CMD [ "yarn", "start" ]
