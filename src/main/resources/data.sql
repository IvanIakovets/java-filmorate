DELETE FROM film_likes;
DELETE FROM film_genre;
DELETE FROM friendships;
DELETE FROM user_friends;
DELETE FROM films;
DELETE FROM users;

-- =============================================
-- СБРОС АВТОИНКРЕМЕНТА
-- =============================================
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE friendships ALTER COLUMN id RESTART WITH 1;

INSERT INTO mpa_ratings (id, name) VALUES
        (1, 'G'),
        (2, 'PG'),
        (3, 'PG-13'),
        (4, 'R'),
        (5, 'NC-17');

-- Жанры (соответствуют Enum)
INSERT INTO genres (id, name) VALUES
        (1, 'Комедия'),
        (2, 'Драма'),
        (3, 'Мультфильм'),
        (4, 'Триллер'),
        (5, 'Документальный'),
        (6, 'Боевик');