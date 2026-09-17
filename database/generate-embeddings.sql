-- Purpose:
-- Generate embeddings for all procurement policies.
--
-- Execute after:
-- 01-create-policy-table.sql
-- 02-add-vector-columns.sql
-- 03-insert-policies.sql
--
-- Confirm that the embedding model is available in SAP HANA Cloud
-- before executing this script.

UPDATE PROCUREMENT_POLICIES
SET EMBEDDING = VECTOR_EMBEDDING(
    POLICY_NAME || ': ' || POLICY_TEXT,
    'DOCUMENT',
    'SAP_GXY.20250407'
);

-- Verify vector dimensions
SELECT
    POLICY_ID,
    POLICY_NAME,
    CARDINALITY(EMBEDDING) AS VECTOR_DIMENSION
FROM PROCUREMENT_POLICIES
ORDER BY POLICY_ID;