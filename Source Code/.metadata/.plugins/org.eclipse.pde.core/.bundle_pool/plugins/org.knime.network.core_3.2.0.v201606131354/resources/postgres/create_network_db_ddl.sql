-- 
--Execute this statement as SUPERUSER (e.g. postgres) to create the network 
--users, schema and objects
--
--Create all necessary users
--
CREATE USER knimenet WITH CREATEDB PASSWORD 'knimenet';
CREATE USER knimenet_usr PASSWORD 'knimenet';

--
--Create the knimenet schema
--
CREATE SCHEMA knimenet;

ALTER SCHEMA knimenet OWNER TO knimenet;

SET search_path = knimenet, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;


--*************************************************************************************************
-- Name: PUBLISHED; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "published" (
    PNETID bigint NOT NULL,
    NAME character varying NOT NULL,
    DESCRIPTION character varying NOT NULL,
	PARTNER  character varying NOT NULL
);
ALTER TABLE knimenet."published" OWNER TO knimenet;

COMMENT ON TABLE "published" IS 'Contains the published networks';

ALTER TABLE ONLY "published"
    ADD CONSTRAINT published_pk PRIMARY KEY (PNETID);
ALTER TABLE ONLY "published"
    ADD CONSTRAINT published_name_uk UNIQUE (NAME);	
	
	
--*************************************************************************************************
-- Name: PUBLISHED_NETWORKS; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "published_networks" (
    PNETID bigint NOT NULL references published(PNETID) ON DELETE CASCADE,
    NETID bigint NOT NULL,
	ORDERID bigint NOT NULL
	
);
ALTER TABLE knimenet."published_networks" OWNER TO knimenet;

COMMENT ON TABLE "published_networks" IS 'Contains the published networks to networks relations';
COMMENT ON COLUMN "published_networks".ORDERID IS 'This id defines the order the sub networks have to be merged from most actual(1) to least actual (last nr) per name';

ALTER TABLE ONLY "published_networks"
    ADD CONSTRAINT pnetid_netid_uk UNIQUE (PNETID, NETID);

	
--*************************************************************************************************
-- Name: NETWORK; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "network" (
    NETID bigint NOT NULL,
    TID bigint NOT NULL,
    ALIAS character varying NOT NULL,
    TYPE character varying NOT NULL
);
ALTER TABLE knimenet."network" OWNER TO knimenet;

ALTER TABLE ONLY "network"
    ADD CONSTRAINT network_netid_uk UNIQUE (NETID, TID);
	
	
--*************************************************************************************************
-- Name: TABLE; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "table" (
    TID bigint NOT NULL,
    OID bigint NOT NULL
);
ALTER TABLE knimenet."table" OWNER TO knimenet;

COMMENT ON TABLE "table" IS 'Partition table';

ALTER TABLE ONLY "table"
    ADD CONSTRAINT table_tid_uk UNIQUE (TID, OID);
	
CREATE INDEX partition_oid_idx ON "table" USING btree (OID);


--*************************************************************************************************
-- Name: OBJECT; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "object" (
    OID bigint NOT NULL
);
ALTER TABLE knimenet."object" OWNER TO knimenet;

ALTER TABLE ONLY "object"
    ADD CONSTRAINT object_pk PRIMARY KEY (OID);

	
--*************************************************************************************************
-- Name: LINK; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "link" (
    LID bigint NOT NULL,
    SOURCE bigint NOT NULL,
    TARGET bigint NOT NULL
);
ALTER TABLE knimenet."link" OWNER TO knimenet;

COMMENT ON COLUMN "link".LID IS 'Link identifier';
COMMENT ON COLUMN "link".SOURCE IS 'Source Object identifier of the link';
COMMENT ON COLUMN "link".TARGET IS 'Target Object identifier of the link';

ALTER TABLE ONLY "link"
    ADD CONSTRAINT link_source_uk UNIQUE (SOURCE, TARGET);
ALTER TABLE ONLY "link"
    ADD CONSTRAINT link_pk PRIMARY KEY (LID);

CREATE INDEX link_target_idx ON "link" USING btree (TARGET);


--*************************************************************************************************
-- Name: feature_definition; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "feature_definition" (
    NETID bigint NOT NULL,
    ATTRIBUTE character varying NOT NULL,
    TYPE character varying NOT NULL
);
ALTER TABLE knimenet."feature_definition" OWNER TO knimenet;

ALTER TABLE ONLY "feature_definition"
    ADD CONSTRAINT feature_definition_netid_uk UNIQUE (NETID, ATTRIBUTE);
	
	
--*************************************************************************************************
-- Name: feature; Type: TABLE; Schema: knimenet; Owner: knimenet; Tablespace: 
--*************************************************************************************************
CREATE TABLE "feature" (
    NETID bigint NOT NULL,
    OID bigint NOT NULL,
    ATTRIBUTE character varying NOT NULL,
    VALUE character varying NOT NULL
);
ALTER TABLE knimenet."feature" OWNER TO knimenet;

ALTER TABLE ONLY "feature"
    ADD CONSTRAINT feature_netid_uk UNIQUE (NETID, OID, ATTRIBUTE);
	
CREATE INDEX feature_attr_idx ON "feature" USING btree (ATTRIBUTE);
CREATE INDEX feature_oid_idx ON "feature" USING btree (OID);
CREATE INDEX feature_value_idx ON "feature" USING btree (VALUE);


--*************************************************************************************************
-- Name: OID_SEQ; Type: SEQUENCE; Schema: knimenet; Owner: knimenet
--*************************************************************************************************
CREATE SEQUENCE "oid_seq"
    START WITH 1
    INCREMENT BY 100
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
ALTER TABLE knimenet."oid_seq" OWNER TO knimenet;
--the increment and cache must be the same as in the portgres.properties file
--which is located in the org/knime/network/core/core/impl/database/postgres/
--folder

--
-- Grant knimenet_USR all necessary rights
--
GRANT USAGE ON SCHEMA knimenet to knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "published" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "published_networks" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "network" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "table" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "object" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "link" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "feature_definition" TO knimenet_usr;
GRANT SELECT, INSERT, UPDATE, DELETE ON "feature" TO knimenet_usr;
GRANT USAGE ON "oid_seq" TO knimenet_usr;

--
-- To delete all objects execute the following statements:
--
--drop owned by knimenet;
--drop user knimenet;
--drop user knimenet_usr;