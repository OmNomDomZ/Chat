FROM postgres:17.0

ENV POSTGRES_DB=chat_db
ENV POSTGRES_USER=chat_user
ENV POSTGRES_PASSWORD=chat_password

COPY init_db/*.sql /docker-entrypoint-initdb.d/

EXPOSE 5432