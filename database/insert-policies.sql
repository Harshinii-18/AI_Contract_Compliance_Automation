-- Purpose:
-- Insert procurement compliance policies.
-- Execute after 01-create-policy-table.sql.
--
-- Execute this script only once to avoid duplicate policies.

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P001',
        'PAYMENT TERMS',
        'Payment terms must not exceed 45 days.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P002',
        'DELIVERY LEAD TIME',
        'The delivery date must be no more than 30 calendar days after the purchase order date.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P003',
        'CURRENCY',
        'Purchase orders must use USD.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P004',
        'SHIPPING TERMS',
        'Allowed shipping terms: FOB Destination, CIF, DAP.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P005',
        'PURCHASE ORDER APPROVAL',
        'Purchase orders with a gross amount greater than USD 10,000 require human review.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P006',
        'LINE ITEM QUANTITY',
        'Quantity must be greater than zero.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P007',
        'LINE ITEM AMOUNT',
        'For each line item: quantity multiplied by unit price must equal line-item net amount, with a tolerance of USD 0.01.'
    );

INSERT INTO PROCUREMENT_POLICIES
    (
        POLICY_ID,
        POLICY_NAME,
        POLICY_TEXT
    )
VALUES
    (
        'P008',
        'PURCHASE ORDER TOTAL',
        'The sum of all line-item net amounts must equal the PO net amount, with a tolerance of USD 0.01.'
    );

-- Verify inserted policies
SELECT
    ID,
    POLICY_ID,
    POLICY_NAME,
    POLICY_TEXT
FROM PROCUREMENT_POLICIES
ORDER BY POLICY_ID;

-- Expected result: 8 policies
SELECT COUNT(*) AS POLICY_COUNT
FROM PROCUREMENT_POLICIES;