# java-filmorate
Приложение для оценки фильмов. Пользователи могут добавлять фильмы, ставить лайки, добавлять друзей и просматривать рейтинги.


# Схема базы данных Filmorate
ссылка на диаграму: https://dbdiagram.io/d/6a60e2d3067336e1ded1e92c

## Таблицы

### users (Пользователи)

| Колонка | Тип | Ограничения | Описание |
|---------|-----|-------------|----------|
| `id` | `INT` | PRIMARY KEY, AUTO_INCREMENT | Уникальный идентификатор пользователя |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | Электронная почта |
| `login` | `VARCHAR(255)` | NOT NULL, UNIQUE | Логин пользователя |
| `name` | `VARCHAR(100)` | NOT NULL | Имя пользователя |
| `birthday` | `DATE` | NOT NULL | Дата рождения |

### films (Фильмы)

| Колонка | Тип | Ограничения | Описание |
|---------|-----|-------------|----------|
| `id` | `INT` | PRIMARY KEY, AUTO_INCREMENT | Уникальный идентификатор фильма |
| `name` | `VARCHAR(255)` | NOT NULL | Название фильма |
| `description` | `VARCHAR(200)` | NULL | Описание фильма (макс. 200 символов) |
| `release_date` | `DATE` | NOT NULL | Дата релиза |
| `duration` | `INT` | NOT NULL | Продолжительность в минутах |
| `genre_id` | `INT` | NOT NULL, FOREIGN KEY → `genres(id)` | Идентификатор жанра |
| `mpa_rating_id` | `INT` | NOT NULL, FOREIGN KEY → `mpa_ratings(id)` | Идентификатор рейтинга MPA |

### genres (Жанры)

| Колонка | Тип | Ограничения | Описание |
|---------|-----|-------------|----------|
| `id` | `INT` | PRIMARY KEY | Уникальный идентификатор жанра |
| `name` | `VARCHAR(50)` | NOT NULL, UNIQUE | Название жанра |

### mpa_ratings (Рейтинги MPA)

| Колонка | Тип | Ограничения | Описание |
|---------|-----|-------------|----------|
| `id` | `INT` | PRIMARY KEY | Уникальный идентификатор рейтинга |
| `name` | `VARCHAR(10)` | NOT NULL, UNIQUE | Название рейтинга |

### film_likes (Лайки фильмов)

| Колонка | Тип | Ограничения | Описание |
|---------|-----|-------------|----------|
| `film_id` | `INT` | PRIMARY KEY, FOREIGN KEY → `films(id)` | Идентификатор фильма |
| `user_id` | `INT` | PRIMARY KEY, FOREIGN KEY → `users(id)` | Идентификатор пользователя |

### friendship (Дружба)

| Колонка | Тип | Ограничения | Описание |
|---------|-----|-------------|----------|
| `user_id` | `INT` | PRIMARY KEY, FOREIGN KEY → `users(id)` | Идентификатор пользователя |
| `friend_id` | `INT` | PRIMARY KEY, FOREIGN KEY → `users(id)` | Идентификатор друга |

## Примеры запросов

### Пользователи

**Создание пользователя**
```sql
INSERT INTO users (email, login, name, birthday) 
VALUES ('user@example.com', 'user1', 'Иван Петров', '1990-05-15');
```
**Создание пользователя**
```sql
SELECT * FROM users;
```

**Получение пользователя по ID**
```sql
SELECT * FROM users WHERE id = 1;
```
**Обновление пользователя**
```sql
UPDATE users
SET email = 'newemail@example.com', name = 'Иван Сидоров'
WHERE id = 1;
```

### Фильмы
**Создание фильма**
```sql
INSERT INTO films (name, description, release_date, duration, genre_id, mpa_rating_id)
VALUES ('Начало', 'Научно-фантастический боевик о вторжении в сны', '2010-07-16', 148, 6, 3);
```

**Получение всех фильмов с жанром и рейтингом**
```sql
SELECT f.*, g.name AS genre_name, m.name AS mpa_name
FROM films AS f
         JOIN genres g ON f.genre_id = g.id
         JOIN mpa_ratings m ON f.mpa_rating_id = m.id;
```

**Получение фильма по ID**
```sql
SELECT f.*, g.name AS genre_name, m.name AS mpa_name
FROM films AS f
         JOIN genres g ON f.genre_id = g.id
         JOIN mpa_ratings m ON f.mpa_rating_id = m.id
WHERE f.id = 1;
```

### Лайки
**Получить все лайки фильма**
```sql
SELECT u.id, u.login, u.name
FROM film_likes AS fl
         JOIN users u ON fl.user_id = u.id
WHERE fl.film_id = 1;
```

**Получить топ N самых популярных фильмов (по количеству лайков)**
```sql
SELECT f.id, f.name, COUNT(fl.user_id) AS likes_count
FROM films AS f
         LEFT JOIN film_likes fl ON f.id = fl.film_id
GROUP BY f.id, f.name
ORDER BY likes_count DESC
    LIMIT 10;
```

### Технологии
**Java 21,**
**Spring Boot 3,**
**PostgreSQL,**
**Maven,**
**Lombok**
