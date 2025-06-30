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

create table board_article_count (
     board_id bigint not null primary key,
     article_count bigint not null
);
