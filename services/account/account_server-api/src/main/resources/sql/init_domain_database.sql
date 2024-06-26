-- 기존 테이블 삭제
DROP TABLE IF EXISTS oauth2_authorized_client CASCADE;
DROP TABLE IF EXISTS api_key CASCADE;
DROP TABLE IF EXISTS card CASCADE;
DROP TABLE IF EXISTS grade CASCADE;
DROP TABLE IF EXISTS member CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS payment_gateway CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- 테이블 생성
create table api_key (
                         admin_banned_at timestamp(6),
                         api_key_id bigserial not null,
                         card_deletion_banned_at timestamp(6),
                         created_at timestamp(6) not null,
                         deleted_at timestamp(6),
                         deletion_requested_at timestamp(6),
                         grade_id bigint not null,
                         member_id bigint not null,
                         modified_at timestamp(6) not null,
                         payment_failure_banned_at timestamp(6),
                         "value" varchar(255) not null unique,
                         primary key (api_key_id)
);

create table card (
                      card_id bigserial not null,
                      created_at timestamp(6) not null,
                      deleted_at timestamp(6),
                      member_id bigint not null,
                      modified_at timestamp(6) not null,
                      payment_gateway_id bigint not null,
                      acquirer_corporation varchar(255) not null,
                      bin varchar(255) not null,
                      issuer_corporation varchar(255) not null,
                      "key" varchar(255) not null unique,
                      type varchar(255) not null,
                      primary key (card_id)
);

create table grade (
                       grade_id bigserial not null,
                       grade_type varchar(255) not null unique check (grade_type in ('GRADE_FREE', 'GRADE_CLASSIC')),
                       primary key (grade_id)
);

create table member (
                        admin_banned_at timestamp(6),
                        created_at timestamp(6) not null,
                        deleted_at timestamp(6),
                        deletion_requested_at timestamp(6),
                        member_id bigserial not null,
                        modified_at timestamp(6) not null,
                        payment_failure_banned_at timestamp(6),
                        role_id bigint not null,
                        name varchar(255) not null unique,
                        password varchar(255),
                        profile_image varchar(255) not null,
                        username varchar(255) not null,
                        primary key (member_id)
);

create table payment (
                         amount numeric(38,2) not null,
                         ended_at date not null,
                         started_at date not null,
                         created_at timestamp(6) not null,
                         deleted_at timestamp(6),
                         member_id bigint not null,
                         modified_at timestamp(6) not null,
                         payment_id bigserial not null,
                         bin varchar(255),
                         issuer_corporation varchar(255),
                         "key" varchar(255) unique,
                         status varchar(255) not null check (status in ('COMPLETED_PAID', 'COMPLETED_FREE', 'FAILED')),
                         type varchar(255),
                         primary key (payment_id)
);

create table payment_gateway (
                                 payment_gateway_id bigserial not null,
                                 payment_gateway_type varchar(255) not null unique check (payment_gateway_type in ('PG_KAKAOPAY')),
                                 primary key (payment_gateway_id)
);

create table role (
                      role_id bigserial not null,
                      role_type varchar(255) not null unique check (role_type in ('ROLE_FREE', 'ROLE_NORMAL', 'ROLE_ADMIN')),
                      primary key (role_id)
);

create table oauth2_authorized_client (
                                          access_token_expires_at timestamp(6) not null,
                                          access_token_issued_at timestamp(6) not null,
                                          refresh_token_issued_at timestamp(6),
                                          access_token_type varchar(100) not null,
                                          client_registration_id varchar(100) not null,
                                          principal_name varchar(200) not null,
                                          access_token_scopes varchar(1000),
                                          access_token_value bytea not null,
                                          refresh_token_value bytea,
                                          primary key (client_registration_id, principal_name)
);

-- 외래 키 제약조건 추가
ALTER TABLE api_key
    ADD CONSTRAINT fk_api_key_grade
        FOREIGN KEY (grade_id)
            REFERENCES grade (grade_id);

ALTER TABLE api_key
    ADD CONSTRAINT fk_api_key_member
        FOREIGN KEY (member_id)
            REFERENCES member (member_id);

ALTER TABLE card
    ADD CONSTRAINT fk_card_member
        FOREIGN KEY (member_id)
            REFERENCES member (member_id);

ALTER TABLE card
    ADD CONSTRAINT fk_card_payment_gateway
        FOREIGN KEY (payment_gateway_id)
            REFERENCES payment_gateway (payment_gateway_id);

ALTER TABLE member
    ADD CONSTRAINT fk_member_role
        FOREIGN KEY (role_id)
            REFERENCES role (role_id);

ALTER TABLE payment
    ADD CONSTRAINT fk_payment_member
        FOREIGN KEY (member_id)
            REFERENCES member (member_id);

-- 초기 데이터 삽입
INSERT INTO role (role_type) VALUES ('ROLE_FREE');
INSERT INTO role (role_type) VALUES ('ROLE_NORMAL');
INSERT INTO role (role_type) VALUES ('ROLE_ADMIN');

INSERT INTO grade (grade_type) VALUES ('GRADE_FREE');
INSERT INTO grade (grade_type) VALUES ('GRADE_CLASSIC');

INSERT INTO payment_gateway (payment_gateway_type) VALUES ('PG_KAKAOPAY');
