CREATE TABLE chat_messages
(
    ID       SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    message  TEXT                ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);