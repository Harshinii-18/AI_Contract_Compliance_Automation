-- Purpose:
-- Test semantic similarity search against procurement policies.
--
-- Execute after embeddings have been generated successfully.

------------------------------------------------------------
-- TEST 1: Payment-term policy retrieval
------------------------------------------------------------

SELECT
    POLICY_ID,
    POLICY_NAME,
    POLICY_TEXT,

    COSINE_SIMILARITY(
        VECTOR_EMBEDDING(
            'The supplier is requesting payment after 60 days.',
            'QUERY',
            'SAP_GXY.20250407'
        ),
        EMBEDDING
    ) AS SIMILARITY

FROM PROCUREMENT_POLICIES

ORDER BY SIMILARITY DESC
LIMIT 1;


------------------------------------------------------------
-- TEST 2: Purchase-order total policy retrieval
------------------------------------------------------------

SELECT
    POLICY_ID,
    POLICY_NAME,
    POLICY_TEXT,

    COSINE_SIMILARITY(
        VECTOR_EMBEDDING(
            'The total of all line items does not match the purchase order net amount.',
            'QUERY',
            'SAP_GXY.20250407'
        ),
        EMBEDDING
    ) AS SIMILARITY

FROM PROCUREMENT_POLICIES

ORDER BY SIMILARITY DESC
LIMIT 3;