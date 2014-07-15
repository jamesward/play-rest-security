# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table todo (
  id                        bigint not null,
  value                     varchar(1024) not null,
  user_id                   bigint,
  constraint pk_todo primary key (id))
;

create table user (
  id                        bigint not null,
  auth_token                varchar(255),
  email_address             varchar(256) not null,
  sha_password              varbinary(64) not null,
  full_name                 varchar(256) not null,
  creation_date             timestamp not null,
  constraint uq_user_email_address unique (email_address),
  constraint pk_user primary key (id))
;

create sequence todo_seq;

create sequence user_seq;

alter table todo add constraint fk_todo_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_todo_user_1 on todo (user_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists todo;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists todo_seq;

drop sequence if exists user_seq;

