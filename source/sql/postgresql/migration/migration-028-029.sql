-- -------
-- add modification_timestamp field to allow edition with Hibernate
-- -------

ALTER TABLE samples

ALTER TABLE experiments

ALTER TABLE materials
	ADD COLUMN modification_timestamp TIME_STAMP DEFAULT CURRENT_TIMESTAMP;


ALTER TABLE sample_properties

-- -------
-- add property data type which references materials
-- -------


ALTER TABLE property_types


ALTER TABLE sample_properties

insert into data_types
(id
 ,code
 ,description)
 values 
 (nextval('DATA_TYPE_ID_SEQ')
 ,'MATERIAL'
 ,'Reference to a material'
);

ALTER TABLE MATERIAL_PROPERTIES DROP CONSTRAINT MAPR_CK;
ALTER TABLE MATERIAL_PROPERTIES ADD CONSTRAINT MAPR_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);

ALTER TABLE EXPERIMENT_PROPERTIES DROP CONSTRAINT EXPR_CK;
ALTER TABLE EXPERIMENT_PROPERTIES ADD CONSTRAINT EXPR_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);

ALTER TABLE SAMPLE_PROPERTIES DROP CONSTRAINT SAPR_CK;
ALTER TABLE SAMPLE_PROPERTIES ADD CONSTRAINT SAPR_CK CHECK 
	((VALUE IS NOT NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NULL) OR 
	 (VALUE IS NULL AND CVTE_ID IS NOT NULL AND MATE_PROP_ID IS NULL) OR
	 (VALUE IS NULL AND CVTE_ID IS NULL AND MATE_PROP_ID IS NOT NULL)
	);

------------------------------------------------------------------------------------
--  Purpose:  Create trigger MATERIAL/SAMPLE/EXPERIMENT _PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK
--            It checks that if material property value is assigned to the entity,
--						then the material type is equal to the one described by property type.
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from material_type_property_types etpt, property_types pt 
			 where NEW.mtpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
							 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON material_properties
    FOR EACH ROW EXECUTE PROCEDURE MATERIAL_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
    
CREATE OR REPLACE FUNCTION SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from sample_type_property_types etpt, property_types pt 
			 where NEW.stpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON sample_properties
    FOR EACH ROW EXECUTE PROCEDURE SAMPLE_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
    
CREATE OR REPLACE FUNCTION EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK() RETURNS trigger AS $$
DECLARE
   v_type_id  CODE;
   v_type_id_prop  CODE;
BEGIN
   if NEW.mate_prop_id IS NOT NULL then
			-- find material type id of the property type 
			select pt.maty_prop_id into v_type_id_prop 
			  from experiment_type_property_types etpt, property_types pt 
			 where NEW.etpt_id = etpt.id AND etpt.prty_id = pt.id;
		
			if v_type_id_prop IS NOT NULL then
				-- find material type id of the material which consists the entity's property value
				select entity.maty_id into v_type_id 
				  from materials entity
				 where NEW.mate_prop_id = entity.id;
				if v_type_id != v_type_id_prop then
					RAISE EXCEPTION 'Insert/Update of property value referencing material (id: %) failed, as referenced material type is different than expected (id %, expected id: %).', 
												 NEW.mate_prop_id, v_type_id, v_type_id_prop;
				end if;
			end if;
   end if;
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK BEFORE INSERT OR UPDATE ON experiment_properties
    FOR EACH ROW EXECUTE PROCEDURE EXPERIMENT_PROPERTY_WITH_MATERIAL_DATA_TYPE_CHECK();
       
-- -------
-- remove the reminescents of the OBSERVABLE_TYPE
-- -------

ALTER TABLE data_set_types

ALTER TABLE data
