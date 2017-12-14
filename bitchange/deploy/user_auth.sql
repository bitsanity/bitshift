--------------------------------------------------------
--  File created - Monday-September-14-2015   
--------------------------------------------------------
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "USER_AUTHORIZATION"';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "USER_AUTHORIZATION_HISTORY"'; 
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

-- GRANT CHANGE NOTIFICATION TO DATA_SERVICE;

--------------------------------------------------------
--  DDL for Sequence USER_AUTHORIZATION_SEQ
--------------------------------------------------------
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE "USER_AUTHORIZATION_SEQ"';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2289 THEN
      RAISE;
    END IF;
END;
/
CREATE SEQUENCE  "USER_AUTHORIZATION_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE
/

--------------------------------------------------------
--  DDL for Table USER_AUTHORIZATION
--------------------------------------------------------

CREATE TABLE "USER_AUTHORIZATION" 
   (	"ID" NUMBER --DEFAULT USER_AUTHORIZATION_SEQ.NEXTVAL
   ,
	"CLIENT_ID" VARCHAR2(50 BYTE), 
	"KEY_TYPE" CHAR(1 BYTE), 
	"CREATED_ON" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP, 
	"ASYM_KEY" BLOB, 
	"EXPIRATION" TIMESTAMP (6), 
	"DESCRIPTION" VARCHAR2(255 BYTE),
	"KEY_ALGORITHM" VARCHAR(10) DEFAULT 'RSA'
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
 LOB ("ASYM_KEY") STORE AS BASICFILE (
  ENABLE STORAGE IN ROW CHUNK 8192 RETENTION 
  NOCACHE LOGGING 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) 
/  

--------------------------------------------------------
--  DDL for Index USER_AUTHORIZATION_PK
--------------------------------------------------------
CREATE UNIQUE INDEX "USER_AUTHORIZATION_PK" ON "USER_AUTHORIZATION" ("ID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
/

--------------------------------------------------------
--  Constraints for Table USER_AUTHORIZATION
--------------------------------------------------------
ALTER TABLE "USER_AUTHORIZATION" MODIFY ("DESCRIPTION" NOT NULL ENABLE)
/
ALTER TABLE "USER_AUTHORIZATION" MODIFY ("EXPIRATION" NOT NULL ENABLE)
/
ALTER TABLE "USER_AUTHORIZATION" ADD CONSTRAINT "USER_AUTHORIZATION_PK" PRIMARY KEY ("ID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  ENABLE
/
ALTER TABLE "USER_AUTHORIZATION" MODIFY ("ASYM_KEY" NOT NULL ENABLE)
/
ALTER TABLE "USER_AUTHORIZATION" MODIFY ("CREATED_ON" NOT NULL ENABLE)
/
ALTER TABLE "USER_AUTHORIZATION" MODIFY ("KEY_TYPE" NOT NULL ENABLE)
/
ALTER TABLE "USER_AUTHORIZATION" MODIFY ("CLIENT_ID" NOT NULL ENABLE)
/

--------------------------------------------------------
--  DDL for Trigger CREATE_PK
--------------------------------------------------------
CREATE OR REPLACE TRIGGER "CREATE_PK" 
   before insert on "USER_AUTHORIZATION" 
   for each row 
begin  
   if inserting then 
      select USER_AUTHORIZATION_SEQ.nextval into :NEW."ID" from dual;  
   end if; 
end;
/
ALTER TRIGGER "CREATE_PK" ENABLE
/

--------------------------------------------------------
--  DDL for history table
--------------------------------------------------------

CREATE TABLE USER_AUTHORIZATION_HISTORY 
(
  ID NUMBER NOT NULL 
, CLIENT_ID VARCHAR2(50 BYTE) NOT NULL 
, KEY_TYPE CHAR(1 BYTE) NOT NULL 
, CREATED_ON TIMESTAMP(6) NOT NULL 
, ASYM_KEY BLOB NOT NULL 
, EXPIRATION TIMESTAMP(6) NOT NULL 
, DESCRIPTION VARCHAR2(255 BYTE) NOT NULL
, KEY_ALGORITHM VARCHAR(10)
, DEPRECATED_ON TIMESTAMP NOT NULL 
) 
LOGGING 
PCTFREE 10 
PCTUSED 40 
INITRANS 1 
STORAGE 
( 
  INITIAL 65536 
  NEXT 1048576 
  MINEXTENTS 1 
  MAXEXTENTS UNLIMITED 
  FREELISTS 1 
  FREELIST GROUPS 1 
  BUFFER_POOL DEFAULT 
) 
NOPARALLEL 
LOB (ASYM_KEY) STORE AS SYS_LOB0000020284C00005$$ 
( 
  ENABLE STORAGE IN ROW 
  CHUNK 8192 
  RETENTION 
  NOCACHE 
  LOGGING 
  STORAGE 
  ( 
    INITIAL 65536 
    NEXT 1048576 
    MINEXTENTS 1 
    MAXEXTENTS UNLIMITED 
    FREELISTS 1 
    FREELIST GROUPS 1 
    BUFFER_POOL DEFAULT 
  )  
)
/

CREATE UNIQUE INDEX USER_AUTHORIZATION_HISTORY_PK ON USER_AUTHORIZATION_HISTORY (CLIENT_ID ASC, DEPRECATED_ON ) 
LOGGING 
PCTFREE 10 
INITRANS 2 
STORAGE 
( 
  INITIAL 65536 
  NEXT 1048576 
  MINEXTENTS 1 
  MAXEXTENTS UNLIMITED 
  FREELISTS 1 
  FREELIST GROUPS 1 
  BUFFER_POOL DEFAULT 
) 
NOPARALLEL
/

ALTER TABLE USER_AUTHORIZATION_HISTORY
ADD CONSTRAINT USER_AUTHORIZATION_HISTORY_PK PRIMARY KEY 
(
  CLIENT_ID, DEPRECATED_ON 
)
USING INDEX USER_AUTHORIZATION_HISTORY_PK
ENABLE
/

-----------------------------
-- HISTORY creation trigger
-----------------------------

CREATE OR REPLACE TRIGGER USER_AUTH_HIST_TRG 
BEFORE UPDATE ON USER_AUTHORIZATION 
REFERENCING OLD AS OLD NEW AS NEW FOR EACH ROW
DECLARE
    v_rec USER_AUTHORIZATION_HISTORY%ROWTYPE;
BEGIN
    v_rec.ASYM_KEY := :OLD.ASYM_KEY;
    v_rec.CLIENT_ID := :OLD.CLIENT_ID;
    v_rec.CREATED_ON := :OLD.CREATED_ON;
    v_rec.DESCRIPTION := :OLD.DESCRIPTION;
    v_rec.EXPIRATION := :OLD.EXPIRATION;
    v_rec.ID := :OLD.ID;
    v_rec.KEY_TYPE := :OLD.KEY_TYPE;
    v_rec.KEY_ALGORITHM := :OLD.KEY_ALGORITHM;
    v_rec.DEPRECATED_ON := CURRENT_TIMESTAMP;

    INSERT INTO USER_AUTHORIZATION_HISTORY VALUES v_rec;

    :NEW.CREATED_ON := CURRENT_TIMESTAMP;

END;
/