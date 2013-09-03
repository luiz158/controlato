# Controlato is a web-based service conceived and designed to assist families
# on the control of their daily lives.
# Copyright (C) 2012 Controlato.org
#
# Controlato is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Controlato is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.

# You should have received a copy of the GNU Affero General Public License
# along with Controlato. Look for the file LICENSE.txt at the root level.
# If you do not, see http://www.gnu.org/licenses/.


drop table if exists transfer;
drop table if exists revenue;
drop table if exists expenditure;
drop table if exists category_operation;
drop table if exists transaction;
drop table if exists account;
drop table if exists place;
drop table if exists history_message;
drop table if exists message_template;
drop table if exists user_group;
drop table if exists access_group;
drop table if exists authentication;
drop table if exists user_account;
drop table if exists currency;
drop table if exists language;
drop table if exists application_property;

create table application_property (
    property_key   varchar(100) not null,
    property_value text             null
) engine myisam;

alter table application_property add constraint pk_application_property primary key (property_key);

create table language (
    acronym varchar(5)  not null,
    name    varchar(30) not null
) engine myisam;

alter table language add constraint pk_language primary key (acronym);

insert into language values ('en', 'English');
insert into language values ('pt', 'Portugues');

create table currency (
    acronym       char(3)      not null,
    name          varchar(30)  not null,
    symbol        varchar(5)       null
) engine myisam;

alter table currency add constraint pk_currency primary key (acronym);

insert into currency values ('EUR','Euro','€');
insert into currency values ('USD','US Dolar','$');
insert into currency values ('BRL','Real','R$');
insert into currency values ('GBP','Pounds','£');

create table user_account (
    id                  char(32)          not null,
    email               varchar(100)      not null,
    unverified_email    varchar(100)          null,
    language            varchar(5)            null,
    currency            char(3)               null,
    date_format         varchar(20)           null,
    number_format       varchar(10)           null,
    confirmation_code   varchar(32)           null,
    registration_date   timestamp             null,
    last_update         timestamp             null,
    deactivated         tinyint(1)            null,
    deactivation_date   timestamp             null,
    deactivation_reason varchar(255)          null,
    deactivation_type   char(15)              null
) engine innodb;

alter table user_account add constraint pk_user_account primary key (id);
create unique index idx_unique_user_email on user_account (email);
create index idx_user_account_currency on user_account (currency);

create table authentication (
    username            varchar(100) not null,
    password            varchar(100) not null,
    user_account        char(32)     not null
) engine = innodb;

alter table authentication add constraint pk_authentication primary key (username);
alter table authentication add constraint fk_user_authentication foreign key (user_account) references user_account(id) on delete cascade;

create table access_group (
    id           char(32)     not null,
    name         varchar(100) not null,
    description  text             null,
    user_default tinyint(1)       null
) engine innodb;

alter table access_group add constraint pk_access_group primary key (id);
create unique index idx_unique_group_name on access_group (name);

create table user_group (
    access_group char(32)     not null,
    user_account char(32)     not null,
    username     varchar(100) not null,
    group_name   varchar(100) not null
) engine innodb;

alter table user_group add constraint pk_user_group primary key (access_group, user_account);
create index idx_group_user on user_group (access_group);
create index idx_user_group on user_group (user_account);
alter table user_group add constraint fk_group_user foreign key (access_group) references access_group(id) on delete cascade;
alter table user_group add constraint fk_user_group foreign key (user_account) references user_account(id) on delete cascade;

create table message_template (
    id       char(32)     not null,
    language varchar(5)   not null,
    title    varchar(255) not null,
    body     text         not null
) engine innodb;

alter table message_template add constraint pk_message_template primary key (id, language);

insert into message_template (id, language, title, body) values
    ('03BD6F3ACE4C48BD8660411FC8673DB4', 'en', '[Controlato] Registration Deactivated', ' <p>Hello,</p><p>We are very sorry to inform that we cannot keep you as a Controlato user.</p><p>The reason is because #{userAccount.deactivationReason}</p><p>We kindly apologize for the inconvenience and we count on your understanding.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382D91454F8155A720F3326F1B', 'en', '[Controlato] A New User Joined Controlato', ' <p>Dear Controlato Manager, </p><p>A new user joined Controlato at #{userAccount.registrationDate}.</p><p>Regards,</p><p><b>Controlato Management</b></p>'),
    ('47DEE5C2E0E14F8BA4605F3126FBFAF4', 'en', '[Controlato] Welcome to Controlato!', '<p>Hello!,</p><p>Thank you for joining <b><a href=''http://www.controlato.com''>Controlato</a></b>! You are now confirmed as a user.</p><p>Start using Controlato now at: http://www.controlato.com!</p><p><b>Controlato Team</b></p>'),
    ('67BE6BEBE45945D29109A8D6CD878344', 'en', '[Controlato] Request for Password Change', '<p>Hello,</p><p>you requested to change your Controlato password. The authorization code to perform this operation is:</p><p>#{userAccount.confirmationCode}</p><p>Inform this code in the form that you just saw right after requesting the new password</p><p>Thank you for visiting Controlato!<br/>'),
    ('E3F122DCC87D42248872878412B34CEE', 'en', '[Controlato] Email Confirmation', '<p>Hello,</p><p>we received a request to register you in Controlato. We would like to confirm your email address to be able to contact you when necessary. You just have to click on the link below to confirm your email:</p><p><a href=''http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}''>http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}</a></p><p>If the address above does not look like a link, please select, copy and paste it into your browser''s address window. If you did not registered at Controlato and believe that this message was sent by mistake, please ignore it and accept our apologies.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('IKWMAJSNDOE3F122DCC87D4224887287', 'en', '[Controlato] User Deactivated', '<p>Hello,</p><p>we''ve just heard that you want to leave us :( Thank you for using Controlato. We hope to see you again soon!</p><p>All the best,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382IKEJSUIWOK5A720F3326F1B', 'en', '[Controlato] A Member Was Deactivated', '<p>Dear Controlato Manager, </p><p> the user <b>#{userAccount.email}</b> was deactivated from Controlato due to the following reason:</p><p><i>#{userAccount.deactivationReason}</i></p><p>Regards,</p><p><b>Controlato Management</b></p>');

insert into message_template (id, language, title, body) values
    ('03BD6F3ACE4C48BD8660411FC8673DB4', 'pt', '[Controlato] Registration Deactivated', ' <p>Hello,</p><p>We are very sorry to inform that we cannot keep you as a Controlato user.</p><p>The reason is because #{userAccount.deactivationReason}</p><p>We kindly apologize for the inconvenience and we count on your understanding.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382D91454F8155A720F3326F1B', 'pt', '[Controlato] A New User Joined Controlato', ' <p>Dear Controlato Manager, </p><p>A new user joined Controlato at #{userAccount.registrationDate}.</p><p>Regards,</p><p><b>Controlato Management</b></p>'),
    ('47DEE5C2E0E14F8BA4605F3126FBFAF4', 'pt', '[Controlato] Welcome to Controlato!', '<p>Hello!,</p><p>Thank you for joining <b><a href=''http://www.controlato.com''>Controlato</a></b>! You are now confirmed as a user.</p><p>Start using Controlato now at: http://www.controlato.com!</p><p><b>Controlato Team</b></p>'),
    ('67BE6BEBE45945D29109A8D6CD878344', 'pt', '[Controlato] Request for Password Change', '<p>Hello,</p><p>you requested to change your Controlato password. The authorization code to perform this operation is:</p><p>#{userAccount.confirmationCode}</p><p>Inform this code in the form that you just saw right after requesting the new password</p><p>Thank you for visiting Controlato!<br/>'),
    ('E3F122DCC87D42248872878412B34CEE', 'pt', '[Controlato] Email Confirmation', '<p>Hello,</p><p>we received a request to register you in Controlato. We would like to confirm your email address to be able to contact you when necessary. You just have to click on the link below to confirm your email:</p><p><a href=''http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}''>http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}</a></p><p>If the address above does not look like a link, please select, copy and paste it into your browser''s address window. If you did not registered at Controlato and believe that this message was sent by mistake, please ignore it and accept our apologies.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('IKWMAJSNDOE3F122DCC87D4224887287', 'pt', '[Controlato] User Deactivated', '<p>Hello,</p><p>we''ve just heard that you want to leave us :( Thank you for using Controlato. We hope to see you again soon!</p><p>All the best,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382IKEJSUIWOK5A720F3326F1B', 'pt', '[Controlato] A Member Was Deactivated', '<p>Dear Controlato Manager, </p><p> the user <b>#{userAccount.email}</b> was deactivated from Controlato due to the following reason:</p><p><i>#{userAccount.deactivationReason}</i></p><p>Regards,</p><p><b>Controlato Management</b></p>');

create table message_history (
    id           char(32)     not null,
    subject      varchar(255) not null,
    body         text         not null,
    recipient    char(32)     not null,
    message_sent tinyint(1)       null,
    date_sent    datetime         null
) engine innodb;

alter table message_history add constraint pk_message_history primary key (id);
create index idx_message_history_recipient on message_history (recipient);
alter table message_history add constraint fk_message_history_recipient foreign key (recipient) references user_account(id) on delete cascade;

create table place (
    id           char(32)     not null,
    name         varchar(100) not null,
    location     varchar(100)     null,
    city         varchar(100)     null,
    postal_code  varchar(10)      null,
    phone_number varchar(20)      null,
    website      varchar(255)     null,
    email        varchar(255)     null,
    user_account char(32)         null
) engine innodb;

alter table place add constraint pk_place primary key (id);
create index idx_place_user_account on place (user_account);

create table account (
    id               char(32)      not null,
    name             varchar(50)   not null,
    account_type     varchar(10)   not null,
    balance          decimal(12,2) not null,
    reference_number varchar(20)       null,
    iban             varchar(36)       null,
    swift            varchar(36)       null,
    currency         char(3)           null,
    user_account     char(32)          null
) engine innodb;

alter table account add constraint pk_account primary key (id);
create index idx_account_currency on account (currency);
create index idx_account_user_account on account (user_account);

create table transaction (
    id               char(32)      not null,
    account          char(32)      not null,
    amount           decimal(12,2) not null,
    date_registered  datetime      not null,
    description      varchar(255)  not null,
    transaction_type varchar(10)   not null,
    currency         char(3)           null,
    user_account     char(32)          null
) engine innodb;

alter table transaction add constraint pk_transaction primary key (id);
create index idx_transaction_account on transaction (account);
create index idx_transaction_currency on transaction (currency);
create index idx_transaction_user_account on transaction (user_account);
alter table transaction add constraint fk_transaction_account foreign key (account) references account (id) on delete cascade;

create table category_operation (
    id               char(32)      not null,
    name             varchar(50)   not null,
    transaction_type varchar(10)   not null,
    parent_category  char(32)          null,
    budget_allocated decimal(12,2)     null,
    budget_limit     decimal(12,2)     null,
    user_account     char(32)          null
) engine innodb;

alter table category_operation add constraint pk_category_operation primary key (id);
create index idx_parent_category on category_operation (parent_category);
create index idx_category_user_account on category_operation (user_account);
alter table category_operation add constraint fk_parent_category foreign key (parent_category) references category_operation (id) on delete set null;

create table expenditure (
    id              char(32)      not null,
    receiver        char(32)      not null,
    category        char(32)      not null,
    account         char(32)      not null,
    transaction     char(32)      not null,
    amount          decimal(12,2) not null,
    debit_date      date              null,
    description     varchar(255)      null,
    checked         tinyint(1)        null,
    currency        char(3)           null,
    user_account    char(32)          null
) engine innodb;

alter table expenditure add constraint pk_expenditure primary key (id);
create index idx_receiver_expenditure on expenditure (receiver);
create index idx_category_expenditure on expenditure (category);
create index idx_account_expenditure on expenditure (account);
create index idx_transaction_expenditure on expenditure (transaction);
create index idx_expenditure_currency on expenditure (currency);
create index idx_expenditure_user_account on expenditure (user_account);
alter table expenditure add constraint fk_receiver_expenditure foreign key (receiver) references place (id) on delete cascade;
alter table expenditure add constraint fk_category_expenditure foreign key (category) references category_operation (id) on delete cascade;
alter table expenditure add constraint fk_account_expenditure foreign key (account) references account (id) on delete cascade;
alter table expenditure add constraint fk_transaction_expenditure foreign key (transaction) references transaction (id) on delete cascade;

create table revenue (
    id           char(32)      not null,
    payer        char(32)      not null,
    category     char(32)      not null,
    account      char(32)      not null,
    transaction  char(32)      not null,
    amount       decimal(12,2) not null,
    credit_date  date              null,
    description  varchar(255)      null,
    checked     tinyint(1)         null,
    currency     char(3)           null,
    user_account char(32)          null
) engine innodb;

alter table revenue add constraint pk_revenue primary key (id);
create index idx_payer_revenue on revenue (payer);
create index idx_category_revenue on revenue (category);
create index idx_account_revenue on revenue (account);
create index idx_transaction_revenue on revenue (transaction);
create index idx_revenue_currency on revenue (currency);
create index idx_revenue_user_account on revenue (user_account);
alter table revenue add constraint fk_payer_revenue foreign key (payer) references place (id) on delete cascade;
alter table revenue add constraint fk_category_revenue foreign key (category) references category_operation (id) on delete cascade;
alter table revenue add constraint fk_account_revenue foreign key (account) references account (id) on delete cascade;
alter table revenue add constraint fk_transaction_revenue foreign key (transaction) references transaction (id) on delete cascade;

create table transfer (
    id                 char(32)      not null,
    description        varchar(255)  not null,
    amount             decimal(12,2) not null,
    date_transfer      date          not null,
    source_account     char(32)      not null,
    source_transaction char(32)      not null,
    target_account     char(32)      not null,
    target_transaction char(32)      not null,
    checked            tinyint(1)        null,
    currency           char(3)           null,
    user_account       char(32)          null
) engine innodb;

alter table transfer add constraint pk_transfer primary key (id);
create index idx_source_account on transfer (source_account);
create index idx_target_account on transfer (target_account);
create index idx_source_transaction on transfer (source_transaction);
create index idx_target_transaction on transfer (target_transaction);
create index idx_transfer_currency on transfer (currency);
create index idx_transfer_user_account on transfer (user_account);
alter table transfer add constraint fk_source_account foreign key (source_account) references account (id) on delete cascade;
alter table transfer add constraint fk_target_account foreign key (target_account) references account (id) on delete cascade;
alter table transfer add constraint fk_source_transaction foreign key (source_transaction) references transaction (id) on delete cascade;
alter table transfer add constraint fk_target_transaction foreign key (target_transaction) references transaction (id) on delete cascade;