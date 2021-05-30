# The instructions at https://docs.docker.com/get-started/08_using_compose/
# seem to contain an error. To run correctly, do:

git clone https://github.com/docker/getting-started.git
cd getting-started
# compose-file from tutorial:
cat << EOF > docker-compose.yml
version: "3.7"

services:
  app:
    image: node:12-alpine
    command: sh -c "yarn install && yarn run dev"
    ports:
      - 3000:3000
    working_dir: /app
    volumes:
      - ./:/app
    environment:
      MYSQL_HOST: mysql
      MYSQL_USER: root
      MYSQL_PASSWORD: secret
      MYSQL_DB: todos

  mysql:
    image: mysql:5.7
    volumes:
      - todo-mysql-data:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: todos

volumes:
  todo-mysql-data:
EOF
sed -i 's/\(yarn install\)/cd app \&\& \1/g' docker-compose.yml
docker-compose up -d

# Wait a while, then go to localhost:3000
