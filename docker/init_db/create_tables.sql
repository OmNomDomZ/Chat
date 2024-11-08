CREATE TABLE chat_messages
(
    ID       SERIAL PRIMARY KEY,
    event    VARCHAR(30) NOT NULL,
    username VARCHAR(50) NOT NULL,
    message  TEXT                ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);