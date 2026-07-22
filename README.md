# java-filmorate
Template repository for Filmorate project.


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
