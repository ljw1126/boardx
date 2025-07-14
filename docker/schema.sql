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

-- 게시글 좋아요
create database article_like;

create table article_like (
      article_like_id bigint not null primary key,
      article_id bigint not null,
      user_id bigint not null,
      created_at datetime not null
);

create unique index idx_article_id_user_id on article_like(
    article_id asc, user_id asc
);

create table article_like_count (
   article_id bigint not null primary key,
   like_count bigint not null
);

-- 게시글 조회수
create database article_view;

create table article_view_count (
    article_id bigint not null primary key,
    view_count bigint not null
);

-- 인기글
-- article, article_view, article_like, comment DB에 각각 테이블과 인덱스 생성해준다
create table outbox (
    outbox_id bigint not null primary key,
    shard_key bigint not null,
    event_type varchar(100) not null,
    payload varchar(5000) not null,
    created_at datetime not null
);

-- 생성 10초 이후 조건 조회를 위한 인덱스
create index idx_shard_key_created_at on outbox(shard_key asc, created_at asc);
