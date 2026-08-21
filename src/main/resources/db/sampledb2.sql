CREATE TABLE comtn_role_permission (
   role_id varchar(20) NOT NULL,
   approval_yn bpchar(1) DEFAULT 'N'::bpchar NOT NULL,
   doc_yn bpchar(1) DEFAULT 'N'::bpchar NOT NULL,
   send_yn bpchar(1) DEFAULT 'N'::bpchar NOT NULL,
   select_yn bpchar(1) DEFAULT 'N'::bpchar NOT NULL,
   CONSTRAINT pk_role_permission PRIMARY KEY (role_id)
);

CREATE TABLE comtn_user_role (
   role_id varchar(20) NOT NULL,
   user_id varchar(5) NOT NULL,
   CONSTRAINT pk_comtn_user_role PRIMARY KEY (user_id, role_id)
);

INSERT INTO comtn_user_role (role_id,user_id) VALUES
    ('A','11111'),
    ('B','11111');

INSERT INTO comtn_role_permission (role_id,approval_yn,doc_yn,send_yn,select_yn) VALUES
    ('A','Y','Y','Y','Y'),
    ('B','N','Y','Y','N');