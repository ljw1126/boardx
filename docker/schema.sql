-- article 유저 권한 부여 필요
create database article;

create table article (
     article_id bigint not null primary key,
     title varchar(100) not null,
     content varchar(3000) not null,
     board_id bigint not null,
     writer_id bigint not null,
     created_at datetime not null,
     modified_at datetime not null
);

create index idx_board_id_article_id on article(board_id asc, article_id desc);

-- board의 article 개수
create table board_article_count (
     board_id bigint not null primary key,
     article_count bigint not null
);

-- 인접리스트 방식의 댓글
-- comment 유저 권한 부여 필요
create table comment (
     comment_id bigint not null primary key,
     content varchar(3000) not null,
     article_id bigint not null,
     parent_comment_id bigint not null,
     writer_id bigint not null,
     deleted bool not null,
     created_at datetime not null
);

create index idx_article_id_parent_comment_id_comment_id on comment (
     article_id asc, parent_comment_id asc, comment_id asc
);

-- path 방식의 댓글
-- utf8mb4_bin 대소문자 구분
create table comment_v2 (
    comment_id bigint not null primary key,
    content varchar(3000) not null,
    article_id bigint not null,
    writer_id bigint not null,
    path varchar(25) character set utf8mb4 collate utf8mb4_bin not null,
    deleted bool not null,
    created_at datetime not null
);

create unique index idx_article_id_path on comment_v2 (
    article_id asc, path asc
);

-- article의 댓글 수
create table article_comment_count (
   article_id bigint not null primary key,
   comment_count bigint not null
);
