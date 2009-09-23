-- Create EXPERIMENT_ATTACHMENT_CONTENTS.
CREATE TABLE EXPERIMENT_ATTACHMENT_CONTENTS (ID TECH_ID NOT NULL,VALUE FILE NOT NULL);
CREATE SEQUENCE EXPERIMENT_ATTACHMENT_CONTENT_ID_SEQ;
ALTER TABLE EXPERIMENT_ATTACHMENT_CONTENTS ADD CONSTRAINT EXAC_PK PRIMARY KEY(ID);

-- Alter EXPERIMENT_ATTACHMENTS by adding the new column which has a NULL constraint.
-- The NOT NULL constraint is added later on.
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD COLUMN EXAC_ID TECH_ID;

-- Does the VALUE migration between EXPERIMENT_ATTACHMENTS to EXPERIMENT_ATTACHMENT_CONTENTS.
CREATE OR REPLACE FUNCTION move_exp_att_content() RETURNS integer AS $$
DECLARE
    rec RECORD;
    seq BIGINT;
BEGIN
  FOR rec IN SELECT id, value FROM EXPERIMENT_ATTACHMENTS LOOP
  	seq := nextval('EXPERIMENT_ATTACHMENT_CONTENT_ID_SEQ');
    INSERT INTO EXPERIMENT_ATTACHMENT_CONTENTS (id, value) VALUES (seq, rec.value);
    UPDATE EXPERIMENT_ATTACHMENTS SET exac_id = seq WHERE id = rec.id;
  END LOOP;
  RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

-- Call the function.
SELECT move_exp_att_content();
DROP FUNCTION move_exp_att_content();

-- Finish EXPERIMENT_ATTACHMENTS changes.
ALTER TABLE EXPERIMENT_ATTACHMENTS ADD CONSTRAINT EXAT_CONT_FK FOREIGN KEY (EXAC_ID) REFERENCES EXPERIMENT_ATTACHMENT_CONTENTS(ID);
ALTER TABLE EXPERIMENT_ATTACHMENTS DROP COLUMN VALUE;
ALTER TABLE EXPERIMENT_ATTACHMENTS ALTER COLUMN EXAC_ID SET NOT NULL;
CREATE INDEX EXAT_EXAC_FK_I ON EXPERIMENT_ATTACHMENTS (EXAC_ID);

-- allow longer user names
ALTER TABLE PERSONS ALTER COLUMN USER_ID TYPE VARCHAR(50);
DROP DOMAIN user_id;
CREATE DOMAIN USER_ID AS VARCHAR(50);
ALTER TABLE PERSONS ALTER COLUMN USER_ID TYPE USER_ID;