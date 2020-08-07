CREATE TABLE tl_laqa11_content (
       uid BIGINT(20) NOT NULL AUTO_INCREMENT
     , qa_content_id BIGINT(20) NOT NULL
     , title TEXT
     , instructions MEDIUMTEXT
     , creation_date DATETIME
     , update_date DATETIME
     , submission_deadline DATETIME
     , reflect TINYINT(1) NOT NULL DEFAULT 0
     , questions_sequenced TINYINT(1) NOT NULL DEFAULT 0
     , username_visible TINYINT(1) NOT NULL DEFAULT 0
     , allow_rate_answers TINYINT(1) NOT NULL DEFAULT 0
     , created_by BIGINT(20) NOT NULL DEFAULT 0
     , define_later TINYINT(1) NOT NULL DEFAULT 0
     , reflectionSubject MEDIUMTEXT
     , lockWhenFinished TINYINT(1) NOT NULL DEFAULT 1
     , showOtherAnswers TINYINT(1) NOT NULL DEFAULT 1
     , allow_rich_editor TINYINT(1) NOT NULL DEFAULT 0
     , use_select_leader_tool_ouput tinyint(1) NOT NULL DEFAULT 0
     , notify_response_submit tinyint(1) NOT NULL DEFAULT 0
     , minimum_rates int(11) DEFAULT 0
     , maximum_rates int(11) DEFAULT 0
     , no_reedit_allowed tinyint(1) NOT NULL DEFAULT 0
     , show_other_answers_after_deadline tinyint(1) NOT NULL DEFAULT 0
     ,  PRIMARY KEY (uid)
     ,  KEY tl_laqa11_content_qa_content_id (qa_content_id)
);

CREATE TABLE tl_laqa11_session (
       uid BIGINT(20) NOT NULL AUTO_INCREMENT
     , qa_session_id BIGINT(20) NOT NULL
     , session_start_date DATETIME
     , session_end_date DATETIME
     , session_name VARCHAR(100)
     , session_status VARCHAR(100)
     , qa_content_id BIGINT(20) NOT NULL
     , qa_group_leader_uid bigint(20)
     , PRIMARY KEY (uid)
     , UNIQUE KEY qa_session_id (qa_session_id)
     , KEY qa_content_id (qa_content_id)
     , CONSTRAINT FK_tl_laqa11_session_1 FOREIGN KEY (qa_content_id)
     		REFERENCES tl_laqa11_content (uid) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tl_laqa11_que_usr (
       uid BIGINT(20) NOT NULL AUTO_INCREMENT
     , que_usr_id BIGINT(20) NOT NULL
     , username VARCHAR(255)
     , responseFinalized TINYINT(1) NOT NULL DEFAULT 0
     , qa_session_id BIGINT(20) NOT NULL
     , fullname VARCHAR(255)
     , learnerFinished TINYINT(1) NOT NULL
     , PRIMARY KEY (uid)
     , UNIQUE KEY que_usr_id (que_usr_id,qa_session_id)
     , INDEX (qa_session_id)
     , CONSTRAINT FK_tl_laqa11_que_usr_1 FOREIGN KEY (qa_session_id)
                  REFERENCES tl_laqa11_session (uid) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tl_laqa11_que_content (
       uid BIGINT(20) NOT NULL AUTO_INCREMENT
     , question MEDIUMTEXT
     , feedback MEDIUMTEXT
     , display_order INT(5) DEFAULT 1
     , qa_content_id BIGINT(20)
     , answer_required TINYINT(1) NOT NULL DEFAULT 0
     , min_words_limit int(11) DEFAULT 0
     , PRIMARY KEY (uid)
     , INDEX (qa_content_id)
     , CONSTRAINT FK_tl_laqa11_que_content_1 FOREIGN KEY (qa_content_id)
                  REFERENCES tl_laqa11_content (uid) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tl_laqa11_usr_resp (
       response_id BIGINT(20) NOT NULL AUTO_INCREMENT
     , answer MEDIUMTEXT
     , time_zone VARCHAR(255)
     , attempt_time DATETIME
     , que_usr_id BIGINT(20) NOT NULL
     , qa_que_content_id BIGINT(20)
     , visible TINYINT(1) NOT NULL DEFAULT 1
     , answer_autosaved MEDIUMTEXT
     , PRIMARY KEY (response_id)
     , INDEX (que_usr_id)
     , CONSTRAINT FK_tl_laqa11_usr_resp_3 FOREIGN KEY (que_usr_id)
                  REFERENCES tl_laqa11_que_usr (uid) ON DELETE CASCADE ON UPDATE CASCADE
     , INDEX (qa_que_content_id)
     , CONSTRAINT FK_tl_laqa11_usr_resp_2 FOREIGN KEY (qa_que_content_id)
                  REFERENCES tl_laqa11_que_content (uid) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tl_laqa11_conditions (
       condition_id BIGINT(20) NOT NULL
	 , content_uid BIGINT(20)
     , PRIMARY KEY (condition_id)
	 , CONSTRAINT QaConditionInheritance FOREIGN KEY (condition_id)
                  REFERENCES lams_branch_condition(condition_id) ON DELETE CASCADE ON UPDATE CASCADE
	 , CONSTRAINT QaConditionToQaContent FOREIGN KEY (content_uid)
                  REFERENCES tl_laqa11_content(uid) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tl_laqa11_condition_questions (
 	   condition_id BIGINT(20)
 	 , question_uid BIGINT(20)
 	 , PRIMARY KEY (condition_id,question_uid)
	 , CONSTRAINT QaConditionQuestionToQaCondition FOREIGN KEY (condition_id)
                  REFERENCES tl_laqa11_conditions(condition_id) ON DELETE CASCADE ON UPDATE CASCADE
	 , CONSTRAINT QaConditionQuestionToQaQuestion FOREIGN KEY (question_uid)
                  REFERENCES tl_laqa11_que_content(uid) ON DELETE CASCADE ON UPDATE CASCADE	
);


ALTER TABLE tl_laqa11_session ADD CONSTRAINT FK_laqa11_session1 FOREIGN KEY (qa_group_leader_uid)
		REFERENCES tl_laqa11_que_usr (uid) ON DELETE SET NULL ON UPDATE CASCADE;


-- data for content table
INSERT INTO tl_laqa11_content (qa_content_id, title, instructions, creation_date, lockWhenFinished)  VALUES (${default_content_id}, 'Q&A', 'Instructions', NOW() , 0);

-- data for content questions table
INSERT INTO tl_laqa11_que_content (question, display_order, qa_content_id) VALUES ('Sample Question 1?',1,1);

SET FOREIGN_KEY_CHECKS=1;