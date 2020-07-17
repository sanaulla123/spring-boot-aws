CREATE TABLE IF NOT EXISTS app_user(
    username VARCHAR(20) NOT NULL PRIMARY KEY,
    password VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS task(
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(512) NOT NULL,
    description VARCHAR(1024),
    status VARCHAR(10) NOT NULL DEFAULT 'Todo',
    created_by VARCHAR(20) NOT NULL,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status_by VARCHAR(20),
    status_on DATETIME,
    updated_by VARCHAR(20),
    updated_on DATETIME
);