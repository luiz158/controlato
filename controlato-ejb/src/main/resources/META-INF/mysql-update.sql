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

########################################################################
# Reorganizationof users' data.
# 02/12/2012
# Hildeberto Mendonca
# Version 0.12

create table authentication (
    username            varchar(100) not null,
    password            varchar(100) not null,
    user_account        char(32)     not null
) engine innodb;

alter table authentication add constraint pk_authentication primary key (username);
alter table authentication add constraint fk_user_authentication foreign key (user_account) references user_account(id) on delete cascade;

insert into authentication (username, password, user_account) select username, password, id from user_account;

alter table user_account drop column username;
alter table user_account drop column password;
alter table user_account drop column first_name;
alter table user_account drop column last_name;
alter table user_account drop column date_format;
alter table user_account drop column number_format;

alter table user_account add unverified_email varchar(100) null;
alter table user_account add currency char(3) null;
create index idx_user_account_currency on user_account (currency);

delete from message_template;

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

alter table place add user_account char(32) null;
create index idx_place_user_account on place (user_account);

alter table account add currency char(3) null;
alter table account add user_account char(32) null;
create index idx_account_currency on account (currency);
create index idx_account_user_account on account (user_account);

alter table transaction add currency char(3) null;
alter table transaction add user_account char(32) null;
create index idx_transaction_currency on transaction (currency);
create index idx_transaction_user_account on transaction (user_account);

alter table category_operation add user_account char(32) null;
create index idx_category_user_account on category_operation (user_account);

alter table expenditure add currency char(3) null;
alter table expenditure add user_account char(32) null;
create index idx_expenditure_currency on expenditure (currency);
create index idx_expenditure_user_account on expenditure (user_account);

alter table revenue add currency char(3) null;
alter table revenue add user_account char(32) null;
create index idx_revenue_currency on revenue (currency);
create index idx_revenue_user_account on revenue (user_account);

alter table transfer add currency char(3) null;
alter table transfer add user_account char(32) null;
create index idx_transfer_currency on transfer (currency);
create index idx_transfer_user_account on transfer (user_account);

drop table purchase;
drop table listed_item;
drop table product;
drop table item;
drop table unit;

########################################################################
# Adding the column checked to help on the comparison with account transactions.
# 21/12/2012
# Hildeberto Mendonca
# Version 0.11

alter table expenditure add checked tinyint(1) null;
alter table revenue add checked tinyint(1) null;
alter table transfer add checked tinyint(1) null;

########################################################################
# Adding a table to register transfers.
# 03/12/2012
# Hildeberto Mendonca
# Version 0.10

create table transfer (
    id                 char(32)      not null,
    description        varchar(255)  not null,
    amount             decimal(12,2) not null,
    date_transfer      date          not null,
    source_account     char(32)      not null,
    source_transaction char(32)      not null,
    target_account     char(32)      not null,
    target_transaction char(32)      not null
) engine innodb;

alter table transfer add constraint pk_transfer primary key (id);
create index idx_source_account on transfer (source_account);
create index idx_target_account on transfer (target_account);
create index idx_source_transaction on transfer (source_transaction);
create index idx_target_transaction on transfer (target_transaction);
alter table transfer add constraint fk_source_account foreign key (source_account) references account (id) on delete cascade;
alter table transfer add constraint fk_target_account foreign key (target_account) references account (id) on delete cascade;
alter table transfer add constraint fk_source_transaction foreign key (source_transaction) references transaction (id) on delete cascade;
alter table transfer add constraint fk_target_transaction foreign key (target_transaction) references transaction (id) on delete cascade;

########################################################################
# Adding the international identification of accounts.
# 22/11/2012
# Hildeberto Mendonca
# Version 0.9

alter table account add iban varchar(36) null;
alter table account add swift varchar(36) null;

########################################################################
# Remotion of the field "ExpirationDate" from the expenditure because
# it simply has no point at the moment. We keep only the date of payment.
# 18/11/2012
# Hildeberto Mendonca
# Version 0.8

alter table expenditure drop column expiration_date;

########################################################################
# Removing the store of timezones
# 19/09/2012
# Hildeberto Mendonca
# Version 0.7

alter table account drop column timezone;
alter table place add website varchar(255) null;
alter table place add email varchar(255) null;

########################################################################
# Allowing the user to comment and rate products.
# 09/09/2012
# Hildeberto Mendonca
# Version 0.6

alter table account add reference_number varchar(20) null;
alter table place add location varchar(100) null;
alter table place add city varchar(100) null;
alter table place add postal_code varchar(10) null;
alter table place add phone_number varchar(20) null;
alter table product add comment text null;
alter table product add rating tinyint(1) null;

########################################################################
# Creates the tables for the family financial module.
# 09/07/2012
# Hildeberto Mendonca
# Version 0.5

create table account (
    id           char(32)      not null,
    name         varchar(50)   not null,
    account_type varchar(10)   not null, % credit, debit, investment
    balance      decimal(12,2) not null
) engine innodb;

alter table account add constraint pk_account primary key (id);

create table transaction (
    id               char(32)      not null,
    account          char(32)      not null,
    amount           decimal(12,2) not null,
    date_registered  datetime      not null,
    description      varchar(255)  not null,
    transaction_type varchar(10)   not null
) engine innodb;

alter table transaction add constraint pk_transaction primary key (id);
create index idx_transaction_account on transaction (account);
alter table transaction add constraint fk_transaction_account foreign key (account) references account (id) on delete cascade;

create table category_operation (
    id               char(32)      not null,
    name             varchar(50)   not null,
    transaction_type varchar(10)   not null,
    parent_category  char(32)          null,
    budget_allocated decimal(12,2)     null,
    budget_limit     decimal(12,2)     null
) engine innodb;

alter table category_operation add constraint pk_category_operation primary key (id);
create index idx_parent_category on category_operation (parent_category);
alter table category_operation add constraint fk_parent_category foreign key (parent_category) references category_operation (id) on delete set null;

create table expenditure (
    id              char(32)      not null,
    receiver        char(32)      not null,
    category        char(32)      not null,
    account         char(32)      not null,
    transaction     char(32)      not null,
    amount          decimal(12,2) not null,
    expiration_date date              null,
    debit_date      date              null,
    description     varchar(255)      null
) engine innodb;

alter table expenditure add constraint pk_expenditure primary key (id);
create index idx_receiver_expenditure on expenditure (receiver);
create index idx_category_expenditure on expenditure (category);
create index idx_account_expenditure on expenditure (account);
create index idx_transaction_expenditure on expenditure (transaction);
alter table expenditure add constraint fk_receiver_expenditure foreign key (receiver) references place (id) on delete cascade;
alter table expenditure add constraint fk_category_expenditure foreign key (category) references category_operation (id) on delete cascade;
alter table expenditure add constraint fk_account_expenditure foreign key (account) references account (id) on delete cascade;
alter table expenditure add constraint fk_transaction_expenditure foreign key (transaction) references transaction (id) on delete cascade;

create table revenue (
    id          char(32)      not null,
    payer       char(32)      not null,
    category    char(32)      not null,
    account     char(32)      not null,
    transaction char(32)      not null,
    amount      decimal(12,2) not null,
    credit_date date              null,
    description varchar(255)      null
) engine innodb;

alter table revenue add constraint pk_revenue primary key (id);
create index idx_payer_revenue on revenue (payer);
create index idx_category_revenue on revenue (category);
create index idx_account_revenue on revenue (account);
create index idx_transaction_revenue on revenue (transaction);
alter table revenue add constraint fk_payer_revenue foreign key (payer) references place (id) on delete cascade;
alter table revenue add constraint fk_category_revenue foreign key (category) references category_operation (id) on delete cascade;
alter table revenue add constraint fk_account_revenue foreign key (account) references account (id) on delete cascade;
alter table revenue add constraint fk_transaction_revenue foreign key (transaction) references transaction (id) on delete cascade;

alter table item drop index idx_item_user_account;
alter table item drop column user_account;
alter table product drop index idx_product_user_account;
alter table product drop column user_account;
alter table listed_item drop index idx_listed_item_user_account;
alter table listed_item drop column user_account;
alter table place drop index idx_place_user_account;
alter table place drop column user_account;
alter table purchase drop index idx_purchase_user_account;
alter table purchase drop column user_account;
alter table purchase add constraint fk_purchased_place foreign key (place) references place (id) on delete set null;

########################################################################
# Database structure update
# 01/06/2011
# Hildeberto Mendonca
# Version 0.4
create table language (
    acronym varchar(5)  not null,
    name    varchar(30) not null
) engine innodb;

alter table language add constraint pk_language primary key (acronym);

insert into language values ('en', 'English');
insert into language values ('pt', 'Portugues');

alter table user_account add language varchar(5) null;
create index idx_language_user on user_account (language);
alter table user_account add constraint fk_language_user foreign key (language) references language(acronym) on delete cascade;

alter table user_account add timezone varchar(50) null;

drop table message_template;
create table message_template (
    id       char(32)     not null,
    language varchar(5)   not null,
    title    varchar(255) not null,
    body     text         not null
) engine innodb;
alter table message_template add constraint pk_message_template primary key (id, language);
alter table message_template add constraint fk_message_template_language foreign key (language) references language (acronym) on delete cascade;

insert into message_template (id, language, title, body) values
    ('03BD6F3ACE4C48BD8660411FC8673DB4', 'en', '[Controlato] Registration Deactivated', ' <p>Dear <b>#{userAccount.firstName}</b>,</p><p>We are very sorry to inform that we cannot keep you as a Controlato user.</p><p>Reason: <i>#{userAccount.deactivationReason}</i></p><p>We kindly apologize for the inconvenience and we count on your understanding.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382D91454F8155A720F3326F1B', 'en', '[Controlato] A New User Joined Controlato', ' <p>Dear Controlato Manager, </p><p>the user <b>#{userAccount.fullName}</b> joined Controlato at #{userAccount.registrationDate}.</p><p>Regards,</p><p><b>Controlato Management</b></p>'),
    ('47DEE5C2E0E14F8BA4605F3126FBFAF4', 'en', '[Controlato] Welcome to Controlato!', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>thank you for joining Controlato! You are confirmed as a user of Controlato. Welcome to <b><a href=''http://www.controlato.com''>Controlato</a></b>!</p><p>Thank you!</p><p><b>Controlato Team</b></p>'),
    ('67BE6BEBE45945D29109A8D6CD878344', 'en', '[Controlato] Request for Password Change', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>you requested to change your Controlato password. The authorization code to perform this operation is:</p><p>#{userAccount.confirmationCode}</p><p>Inform this code in the form that you just saw right after requesting the new password or just follow the link below to fill out the form automatically:</p><p><a href=''http://#{serverAddress}/change_password.xhtml?cc=#{userAccount.confirmationCode}''>http://#{serverAddress}/change_password.xhtml?cc=#{userAccount.confirmationCode}</a></p><p>Thank you for visiting Controlato!<br/>'),
    ('E3F122DCC87D42248872878412B34CEE', 'en', '[Controlato] Email Confirmation', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>we received a request to register you in Controlato. We would like to confirm your email address to be able to contact you when necessary. You just have to click on the link below to confirm your email:</p><p><a href=''http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}''>http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}</a></p><p>If the address above does not look like a link, please select, copy and paste it into your browser''s address window. If you did not registered at Controlato and believe that this message was sent by mistake, please ignore it and accept our apologies.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('IKWMAJSNDOE3F122DCC87D4224887287', 'en', '[Controlato] User Deactivated', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>we''ve just heard that you want to leave us :( Thank you for using Controlato. We hope to see you again soon!</p><p>All the best,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382IKEJSUIWOK5A720F3326F1B', 'en', '[Controlato] A Member Was Deactivated', '<p>Dear Controlato Manager, </p><p> the user <b>#{userAccount.fullName}</b> was deactivated from Controlato due to the following reason:</p><p><i>#{userAccount.deactivationReason}</i></p><p>Regards,</p><p><b>Controlato Management</b></p>');

insert into message_template (id, language, title, body) values
    ('03BD6F3ACE4C48BD8660411FC8673DB4', 'pt', '[Controlato] Registration Deactivated', ' <p>Dear <b>#{userAccount.firstName}</b>,</p><p>We are very sorry to inform that we cannot keep you as a Controlato user.</p><p>Reason: <i>#{userAccount.deactivationReason}</i></p><p>We kindly apologize for the inconvenience and we count on your understanding.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382D91454F8155A720F3326F1B', 'pt', '[Controlato] A New User Joined Controlato', ' <p>Dear Controlato Manager, </p><p>the user <b>#{userAccount.fullName}</b> joined Controlato at #{userAccount.registrationDate}.</p><p>Regards,</p><p><b>Controlato Management</b></p>'),
    ('47DEE5C2E0E14F8BA4605F3126FBFAF4', 'pt', '[Controlato] Welcome to Controlato!', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>thank you for joining Controlato! You are confirmed as a user of Controlato. Welcome to <b><a href=''http://www.controlato.com''>Controlato</a></b>!</p><p>Thank you!</p><p><b>Controlato Team</b></p>'),
    ('67BE6BEBE45945D29109A8D6CD878344', 'pt', '[Controlato] Request for Password Change', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>you requested to change your Controlato password. The authorization code to perform this operation is:</p><p>#{userAccount.confirmationCode}</p><p>Inform this code in the form that you just saw right after requesting the new password or just follow the link below to fill out the form automatically:</p><p><a href=''http://#{serverAddress}/change_password.xhtml?cc=#{userAccount.confirmationCode}''>http://#{serverAddress}/change_password.xhtml?cc=#{userAccount.confirmationCode}</a></p><p>Thank you for visiting Controlato!<br/>'),
    ('E3F122DCC87D42248872878412B34CEE', 'pt', '[Controlato] Email Confirmation', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>we received a request to register you in Controlato. We would like to confirm your email address to be able to contact you when necessary. You just have to click on the link below to confirm your email:</p><p><a href=''http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}''>http://#{serverAddress}/EmailConfirmation?code=#{userAccount.confirmationCode}</a></p><p>If the address above does not look like a link, please select, copy and paste it into your browser''s address window. If you did not registered at Controlato and believe that this message was sent by mistake, please ignore it and accept our apologies.</p><p>Best Regards,</p><p><b>Controlato Team</b></p>'),
    ('IKWMAJSNDOE3F122DCC87D4224887287', 'pt', '[Controlato] User Deactivated', '<p>Hi <b>#{userAccount.firstName}</b>,</p><p>we''ve just heard that you want to leave us :( Thank you for using Controlato. We hope to see you again soon!</p><p>All the best,</p><p><b>Controlato Team</b></p>'),
    ('0D6F96382IKEJSUIWOK5A720F3326F1B', 'pt', '[Controlato] A Member Was Deactivated', '<p>Dear Controlato Manager, </p><p> the user <b>#{userAccount.fullName}</b> was deactivated from Controlato due to the following reason:</p><p><i>#{userAccount.deactivationReason}</i></p><p>Regards,</p><p><b>Controlato Management</b></p>');

########################################################################
# Database structure update
# 25/06/2011
# Hildeberto Mendonca
# Version 0.3
alter table listed_item modify quantity decimal(10,3) null;
alter table user_account drop gender;
alter table user_account drop birth_date;
alter table user_account drop photo;

########################################################################
# Database structure update
# 14/06/2011
# Hildeberto Mendonca
# Version 0.2
drop table unit_transformation;

create index idx_item_user_account on item (user_account);
create index idx_product_user_account on product (user_account);
create index idx_listed_item_user_account on listed_item (user_account);
create index idx_purchase_user_account on purchase (user_account);

create table place (
    id           char(32)     not null,
    name         varchar(100) not null,
    user_account char(32)     not null
) engine innodb;

alter table place add constraint pk_place primary key (id);
create index idx_place_user_account on place (user_account);

alter table purchase add place char(32) null;
create index idx_purchase_place on purchase (place);
alter table purchase add constraint fk_purchase_place foreign key (place) references place(id) on delete set null;

########################################################################
# Adds a column in the table listed_item to store a link to a webpage where is
# possible to find more information about the item to buy.
# 12/06/2011
# Hildeberto Mendonca
# Version 0.1
alter table listed_item add info_link varchar(1024) null;