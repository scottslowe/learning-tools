drop table if exists product;

create table product (
	id varchar(255) not null,
	name varchar(255) not null,
	description varchar(255) not null,
	price float not null,
	primary key (id)
);

insert into product (id,name,description,price) values('PR1...210','BlackBerry 8100 Pearl','',124.60);
insert into product (id,name,description,price) values('PR1...211','Sony Ericsson W810i','',139.45);
insert into product (id,name,description,price) values('PR1...212','Samsung MM-A900M Ace','',97.80);
insert into product (id,name,description,price) values('PR1...213','Toshiba M285-E 14','',166.20);
insert into product (id,name,description,price) values('PR1...214','Nokia 2610 Phone','',145.50);
insert into product (id,name,description,price) values('PR1...215','CN Clogs Beach/Garden Clog','',190.70);
insert into product (id,name,description,price) values('PR1...216','AT&T 8525 PDA','',289.20);
insert into product (id,name,description,price) values('PR1...217','Canon Digital Rebel XT 8MP Digital SLR Camera','',13.70);
insert into product (id,name,description,price) values('PR2...310','Kindle Fire','',199.00);
insert into product (id,name,description,price) values('PR2...311','Apple TV MD199LL/A','',94.86);
insert into product (id,name,description,price) values('PR2...312','Medialink Wireless N Router','',49.99);
insert into product (id,name,description,price) values('PR2...313','Garmin nuvi 1450LMT','',147.44);
insert into product (id,name,description,price) values('PR2...314','Roku 2 XS 1080p Streaming Player','',99.00);
insert into product (id,name,description,price) values('PR2...315','Sony BDP-S590 3D Blu-ray Disk Player','',86.99);
insert into product (id,name,description,price) values('PR2...316','GoPro HD HERO2','',241.14);
insert into product (id,name,description,price) values('PR2...317','Toshiba 32C120U 32-Inch LCD HDTV','',239.99);