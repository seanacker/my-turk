FROM node:14
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY package.json /usr/src/app/
RUN npm install --force -g yarn
RUN yarn install
COPY . /usr/src/app
EXPOSE 8080
CMD ["yarn", "serve"]
