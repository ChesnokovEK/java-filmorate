--mpa
INSERT INTO mpa(name) VALUES('G');
INSERT INTO mpa(name) VALUES('PG');
INSERT INTO mpa(name) VALUES('PG-13');
INSERT INTO mpa(name) VALUES('R');
INSERT INTO mpa(name) VALUES('NC-17');

--genres
INSERT INTO genres(name) VALUES('Комедия');
INSERT INTO genres(name) VALUES('Драма');
INSERT INTO genres(name) VALUES('Мультфильм');
INSERT INTO genres(name) VALUES('Триллер');
INSERT INTO genres(name) VALUES('Документальный');
INSERT INTO genres(name) VALUES('Боевик');

--films
INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES('film1', 'film1_desc', '1999-05-03', 100, 1);
INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES('film2', 'film2_desc', '2001-11-05', 111, 2);
INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES('film3', 'film3_desc', '2012-07-02', 121, 3);
INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES('film4', 'film4_desc', '2011-08-01', 90, 4);
INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES('film5', 'film5_desc', '2008-09-11', 130, 5);

--film_genres
INSERT INTO film_genres(film_id, genre_id) VALUES(1, 1), (1, 2), (1, 3);
INSERT INTO film_genres(film_id, genre_id) VALUES(2, 6), (2, 2);
INSERT INTO film_genres(film_id, genre_id) VALUES(3, 1), (3, 5);
INSERT INTO film_genres(film_id, genre_id) VALUES(4, 4);
INSERT INTO film_genres(film_id, genre_id) VALUES(5, 1);

--users
INSERT INTO users(email, login, name, birthday) VALUES('username1@gmail.com', 'user_login1', 'user1', '1894-09-11');
INSERT INTO users(email, login, name, birthday) VALUES('username2@gmail.com', 'user_login2', 'user2', '1991-05-01');
INSERT INTO users(email, login, name, birthday) VALUES('username3@gmail.com', 'user_login3', 'user3', '1999-06-02');
INSERT INTO users(email, login, name, birthday) VALUES('username4@gmail.com', 'user_login4', 'user4', '1985-08-13');
INSERT INTO users(email, login, name, birthday) VALUES('username5@gmail.com', 'user_login5', 'user5', '1922-09-22');

--friends
INSERT INTO friends(user_id, friend_id) VALUES(1, 3);
INSERT INTO friends(user_id, friend_id) VALUES(2, 3), (2, 4);
INSERT INTO friends(user_id, friend_id) VALUES(3, 2), (3, 1);
INSERT INTO friends(user_id, friend_id) VALUES(4, 2);
INSERT INTO friends(user_id, friend_id) VALUES(5, 1);

--likes
INSERT INTO likes(film_id, user_id) VALUES(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);
INSERT INTO likes(film_id, user_id) VALUES(2, 1), (2, 4);
INSERT INTO likes(film_id, user_id) VALUES(3, 1), (3, 3), (3, 4);
INSERT INTO likes(film_id, user_id) VALUES(4, 1);
INSERT INTO likes(film_id, user_id) VALUES(5, 5);