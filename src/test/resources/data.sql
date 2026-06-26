INSERT INTO roles (role_name) VALUES ('MANGAKA');
INSERT INTO roles (role_name) VALUES ('TANTOU');
INSERT INTO roles (role_name) VALUES ('ADMIN');
INSERT INTO roles (role_name) VALUES ('ASSISTANT');

INSERT INTO user_status (user_status_name) VALUES ('active');
INSERT INTO user_status (user_status_name) VALUES ('inactive');

INSERT INTO series_status (series_status_name) VALUES ('approved');
INSERT INTO series_status (series_status_name) VALUES ('hiatus');
INSERT INTO series_status (series_status_name) VALUES ('completed');
INSERT INTO series_status (series_status_name) VALUES ('cancelled');

INSERT INTO decision_type (decision_type_name) VALUES ('approved');
INSERT INTO decision_type (decision_type_name) VALUES ('rejected');
INSERT INTO decision_type (decision_type_name) VALUES ('revision');

INSERT INTO publication_types (publication_type_name) VALUES ('weekly');
INSERT INTO publication_types (publication_type_name) VALUES ('monthly');

INSERT INTO genres (genre_name) VALUES ('Action');
INSERT INTO genres (genre_name) VALUES ('Romance');
INSERT INTO genres (genre_name) VALUES ('Fantasy');

INSERT INTO region_type (region_type_name) VALUES ('speech_bubble');
INSERT INTO region_type (region_type_name) VALUES ('sound_effect');

INSERT INTO task_type (task_type_name) VALUES ('inking');
INSERT INTO task_type (task_type_name) VALUES ('toning');
INSERT INTO task_type (task_type_name) VALUES ('background');

INSERT INTO chapter_status (chapter_status_name) VALUES ('draft');
INSERT INTO chapter_status (chapter_status_name) VALUES ('in_progress');
INSERT INTO chapter_status (chapter_status_name) VALUES ('completed');

INSERT INTO task_status (task_status_name) VALUES ('todo');
INSERT INTO task_status (task_status_name) VALUES ('in_progress');
INSERT INTO task_status (task_status_name) VALUES ('done');

INSERT INTO task_submission_status (task_submission_status_name) VALUES ('pending');
INSERT INTO task_submission_status (task_submission_status_name) VALUES ('approved');
INSERT INTO task_submission_status (task_submission_status_name) VALUES ('rejected');

INSERT INTO page_status (page_status_name) VALUES ('pending');
INSERT INTO page_status (page_status_name) VALUES ('approved');

INSERT INTO annotation_status (annotation_status_name) VALUES ('open');
INSERT INTO annotation_status (annotation_status_name) VALUES ('resolved');

INSERT INTO annotation_type (annotation_type_name) VALUES ('comment');
INSERT INTO annotation_type (annotation_type_name) VALUES ('correction');

INSERT INTO manuscript_type (type_name) VALUES ('sketch');
INSERT INTO manuscript_type (type_name) VALUES ('final');