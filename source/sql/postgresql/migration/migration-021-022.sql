-- Drop the UKs SAMP_BK_DBIN_UK and SAMP_BK_GROU_UK of the SAMPLES table

ALTER TABLE SAMPLES DROP CONSTRAINT SAMP_BK_DBIN_UK;
ALTER TABLE SAMPLES DROP CONSTRAINT SAMP_BK_GROU_UK;

-- Recreate the UKs SAMP_BK_DBIN_UK and SAMP_BK_GROU_UK of the SAMPLES table, but without the SATY_ID column.

ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_BK_DBIN_UK UNIQUE(CODE,DBIN_ID);
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_BK_GROU_UK UNIQUE(CODE,GROU_ID);
