create table if not exists big_event.feedback
(
    id            int auto_increment
        primary key,
    courseitemid  int                                                                              not null,
    feedback      varchar(1000)                                                                    null,
    feedback_type enum ('negative', 'somewhatnegative', 'neutral', 'somewhatpositive', 'positive') not null
)
    engine = InnoDB;


create table if not exists big_event.courseitem
(
    id               int unsigned auto_increment comment 'ID'
        primary key,
    title            varchar(30)    not null comment 'courseItem',
    prompt           varchar(10000) null comment 'courseItemDesc',
    chart            varchar(30)    null comment 'courseItemChart',
    course_id        int unsigned   not null comment 'course_id',
    positive         int            null,
    neutral          int            null,
    somewhatpositive int            null,
    somewhatnegative int            null,
    negative         int            null,
    good             int            null,
    bad              int            null,
    normal           int            null,
    airesult         text           null
)
    engine = InnoDB;


create table if not exists big_event.user
(
    id          int unsigned auto_increment comment 'ID'
        primary key,
    username    varchar(20)             not null comment 'username',
    password    varchar(32)             null comment 'password',
    nickname    varchar(10)  default '' null comment 'nickname',
    email       varchar(128) default '' null comment 'email',
    user_pic    varchar(128) default '' null comment 'avatar',
    create_time datetime                not null comment 'create time',
    update_time datetime                not null comment 'update time',
    role        tinyint                 null,
    constraint username
        unique (username)
)
    comment '用户表' engine = InnoDB;