CREATE TABLE drone(
	drone_id bigint AUTO_INCREMENT NOT NULL,
	serial_number varchar(100) NOT NULL,
	model varchar(100) NOT NULL,
	weight_limit numeric(18, 0) NOT NULL,
	battery_capacity int NOT NULL,
	state varchar(20) NOT NULL,
    CONSTRAINT PK_drone PRIMARY KEY (drone_id) 
);


CREATE TABLE drone_actions(
	action_id bigint AUTO_INCREMENT NOT NULL,
	dispatch_id bigint NOT NULL,
	serial_number varchar(MAX) NOT NULL,
    med_name varchar(MAX) NOT NULL,
	med_code varchar(MAX) NOT NULL,
    med_weight numeric(18, 0) NOT NULL,
    med_image BLOB NULL,	
	from_location varchar(MAX)  NULL,
	to_location varchar(MAX)  NULL,
	loader_name varchar(MAX)  NULL,
	loader_contact varchar(MAX)  NULL,
	action_status varchar(20) NOT NULL,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_drone_actions PRIMARY KEY (action_id) 
);

CREATE TABLE event_log(
	eventlog_id bigint AUTO_INCREMENT NOT NULL,
	serial_number varchar(100) NOT NULL,
	previous_capacity int NOT NULL,
    battery_capacity int NOT NULL,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_event_log PRIMARY KEY (eventlog_id) 
);

