-- Purpose:
-- Add the vector column required for semantic search.
-- Execute after 01-create-policy-table.sql.

ALTER TABLE PROCUREMENT_POLICIES
ADD (
    EMBEDDING REAL_VECTOR
);

-- Verify the vector column
SELECT
    POLICY_ID,
    POLICY_NAME,
    EMBEDDING
FROM PROCUREMENT_POLICIES;