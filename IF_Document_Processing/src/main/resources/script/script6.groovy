import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.time.LocalDate
import java.time.temporal.ChronoUnit

def Message processData(Message message) {

    def reader = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(reader)

    List<String> errors = []

    def header = json?.header ?: [:]
    def lineItems = json?.lineItems ?: []

    // 1. Required Header Fields & Validity (Checking for null or empty/blank string)
    if (!header.documentNumber || header.documentNumber.toString().trim().isEmpty()) {

    // =========================================================
    // 1. REQUIRED HEADER FIELDS
    // =========================================================

    if (isBlank(header.documentNumber)) {
        errors.add("Missing Required Field: PO Number")
    }
    if (!header.senderName || header.senderName.toString().trim().isEmpty()) {

    if (isBlank(header.senderName)) {
        errors.add("Missing Required Field: Supplier")
    }
    if (!header.currency || header.currency.toString().trim().isEmpty()) {

    if (isBlank(header.currency)) {
        errors.add("Missing Required Field: Currency")
    }

    // 2. Line Items Validation & Mathematical Consistency
    if (!lineItems) {
        errors.add("Missing Line Items: At least one line item is required")
    if (isBlank(header.documentDate)) {
        errors.add("Missing Required Field: PO Date")
    }

    if (isBlank(header.deliveryDate)) {
        errors.add("Missing Required Field: Delivery Date")
    }

    if (isBlank(header.paymentTerms)) {
        errors.add("Missing Required Field: Payment Terms")
    }

    if (isBlank(header.shippingTerms)) {
        errors.add("Missing Required Field: Shipping Terms")
    }


    // =========================================================
    // 2. CURRENCY VALIDATION - P003
    // 2. LINE ITEMS VALIDATION
    // =========================================================

    if (!lineItems || lineItems.isEmpty()) {

        errors.add(
            "Missing Line Items: At least one line item is required"
        )

    }


    // =========================================================
    // 3. CURRENCY VALIDATION - P003
    // =========================================================

    if (!isBlank(header.currency)) {

        if (header.currency.toString().trim().toUpperCase() != "USD") {

            errors.add(
                "P003 Currency Violation: Currency must be USD (Found: ${header.currency})"
            )
        }
    }


    // =========================================================
    // 3. DATE VALIDATION - P002
    // 4. DATE VALIDATION - P002
    // =========================================================

    LocalDate poDate = null
    LocalDate deliveryDate = null

    if (!isBlank(header.documentDate)) {

        try {
            poDate = LocalDate.parse(header.documentDate.toString().trim())

            poDate = LocalDate.parse(
                header.documentDate.toString().trim()
            )

        } catch (Exception e) {

            errors.add(
                "Invalid PO Date: ${header.documentDate}. Expected format: YYYY-MM-DD"
            )
        }
    }


    if (!isBlank(header.deliveryDate)) {

        try {
            deliveryDate = LocalDate.parse(header.deliveryDate.toString().trim())

            deliveryDate = LocalDate.parse(
                header.deliveryDate.toString().trim()
            )

        } catch (Exception e) {

            errors.add(
                "Invalid Delivery Date: ${header.deliveryDate}. Expected format: YYYY-MM-DD"
            )
        }
    }


    if (poDate != null && deliveryDate != null) {

        if (deliveryDate.isBefore(poDate)) {

            errors.add(
                "P002 Delivery Date Violation: Delivery date ${deliveryDate} is before PO date ${poDate}"
            )

    } else {

            long leadTime = ChronoUnit.DAYS.between(poDate, deliveryDate)
            long leadTime =
                ChronoUnit.DAYS.between(poDate, deliveryDate)

            if (leadTime > 30) {

                errors.add(
                    "P002 Delivery Lead Time Violation: Delivery is ${leadTime} days after PO date. Maximum allowed is 30 days"
                )
            }
        }
    }


    // =========================================================
    // 4. PAYMENT TERMS VALIDATION - P001
    // 5. PAYMENT TERMS VALIDATION - P001
    // =========================================================

    if (!isBlank(header.paymentTerms)) {

        String paymentTerms = header.paymentTerms.toString().trim()
        String paymentTerms =
            header.paymentTerms.toString().trim()

        def matcher = paymentTerms =~ /(?i)(\d+)\s*(?:days?|d)\b/
        def matcher =
            paymentTerms =~ /(?i)(\d+)\s*(?:days?|d)\b/

        if (matcher.find()) {

            int paymentDays = matcher.group(1).toInteger()
            int paymentDays =
                matcher.group(1).toInteger()

            if (paymentDays > 45) {

                errors.add(
                    "P001 Payment Terms Violation: Payment terms are Net ${paymentDays} days. Maximum allowed is 45 days"
                )
            }

        } else {

            errors.add(
                "Invalid Payment Terms: Unable to determine payment days from '${paymentTerms}'"
            )
        }
    }


    // =========================================================
    // 5. SHIPPING TERMS VALIDATION - P004
    // 6. SHIPPING TERMS VALIDATION - P004
    // =========================================================

    if (!isBlank(header.shippingTerms)) {

        String shippingTerms =
            header.shippingTerms.toString().trim().toUpperCase()

        List<String> allowedShippingTerms = [
            "FOB DESTINATION",
            "CIF",
            "DAP"
        ]

        if (!allowedShippingTerms.contains(shippingTerms)) {

            errors.add(
                "P004 Shipping Terms Violation: '${header.shippingTerms}' is not allowed. Allowed values: FOB Destination, CIF, DAP"
            )
        }
    }


    // =========================================================
    // 6. LINE ITEM VALIDATION
    // 7. LINE ITEM VALIDATION - P006 / P007
    // =========================================================

    if (!lineItems || lineItems.isEmpty()) {

        errors.add(
            "Missing Line Items: At least one line item is required"
        )

    } else {
    if (lineItems && !lineItems.isEmpty()) {

        lineItems.eachWithIndex { item, index ->

            int itemNo = index + 1
            
            def qty = parseDouble(item.quantity)
            def price = parseDouble(item.unitPrice)
            def total = parseDouble(item.netAmount)
            def lineTotal = parseDouble(item.netAmount)

            // Data Validity Checks (> 0)
            if (qty <= 0) {
                errors.add("Line Item ${itemNo}: Quantity must be greater than 0 (Found: ${item.quantity})")

            // -------------------------------------------------
            // P006 - Quantity > 0
            // Quantity validation
            // -------------------------------------------------

            if (item.quantity == null || !isValidNumber(item.quantity)) {
            if (item.quantity == null ||
                !isValidNumber(item.quantity)) {

                errors.add(
                    "Line Item ${itemNo}: Invalid Quantity (Found: ${item.quantity})"
                )

            } else if (qty <= 0) {

                errors.add(
                    "P006 Quantity Violation - Line Item ${itemNo}: Quantity must be greater than 0 (Found: ${item.quantity})"
                )
            }
            if (price <= 0) {
                errors.add("Line Item ${itemNo}: Unit Price must be greater than 0 (Found: ${item.unitPrice})")


            // -------------------------------------------------
            // Unit Price > 0
            // Unit Price validation
            // -------------------------------------------------

            if (item.unitPrice == null || !isValidNumber(item.unitPrice)) {
            if (item.unitPrice == null ||
                !isValidNumber(item.unitPrice)) {

                errors.add(
                    "Line Item ${itemNo}: Invalid Unit Price (Found: ${item.unitPrice})"
                )

            } else if (price <= 0) {

                errors.add(
                    "Line Item ${itemNo}: Unit Price must be greater than 0 (Found: ${item.unitPrice})"
                )
            }
            if (total <= 0) {
                errors.add("Line Item ${itemNo}: Total Amount must be greater than 0 (Found: ${item.netAmount})")


            // -------------------------------------------------
            // Line Amount > 0
            // Net Amount validation
            // -------------------------------------------------

            if (item.netAmount == null || !isValidNumber(item.netAmount)) {
            if (item.netAmount == null ||
                !isValidNumber(item.netAmount)) {

                errors.add(
                    "Line Item ${itemNo}: Invalid Net Amount (Found: ${item.netAmount})"
                )

            } else if (lineTotal <= 0) {

                errors.add(
                    "Line Item ${itemNo}: Net Amount must be greater than 0 (Found: ${item.netAmount})"
                )
            }

            // Mathematical Consistency Check (Qty * Price == Total)
            if (qty > 0 && price > 0 && total > 0) {
                def calculatedTotal = qty * price
                if (Math.abs(calculatedTotal - total) > 0.01) {
                    errors.add("Line Item ${itemNo}: Math mismatch! (${qty} x ${price} = ${calculatedTotal}, but Total is ${total})")

            // -------------------------------------------------
            // P007 - Qty × Unit Price = Net Amount
            // -------------------------------------------------

            if (qty > 0 && price > 0 && lineTotal > 0) {
            if (qty > 0 &&
                price > 0 &&
                lineTotal > 0) {

                double calculatedTotal = qty * price
                double calculatedTotal =
                    qty * price

                if (Math.abs(calculatedTotal - lineTotal) > 0.01) {

                    errors.add(
                        "P007 Line Amount Violation - Line Item ${itemNo}: ${qty} x ${price} = ${calculatedTotal}, but Net Amount is ${lineTotal}"
                    )
                }
            }
        }
    }

    // 3. Set Validation Status explicitly as String ("true" or "false")

    // =========================================================
    // 7. PO NET AMOUNT VALIDATION - P008
    // 8. PO NET AMOUNT VALIDATION - P008
    // =========================================================

    if (header.netAmount == null || !isValidNumber(header.netAmount)) {
    if (header.netAmount == null ||
        !isValidNumber(header.netAmount)) {

        errors.add(
            "Missing or Invalid PO Net Amount"
        )

    } else if (lineItems && !lineItems.isEmpty()) {

        double poNetAmount = parseDouble(header.netAmount)
        double poNetAmount =
            parseDouble(header.netAmount)

        double lineItemsTotal = 0.0

        lineItems.each { item ->
            lineItemsTotal += parseDouble(item.netAmount)

            lineItemsTotal +=
                parseDouble(item.netAmount)
        }

        if (Math.abs(lineItemsTotal - poNetAmount) > 0.01) {

            errors.add(
                "P008 PO Total Violation: Sum of line items is ${lineItemsTotal}, but PO Net Amount is ${poNetAmount}"
            )
        }
    }


    // =========================================================
    // 8. FINAL VALIDATION RESULT
    // 9. FINAL VALIDATION RESULT
    // =========================================================

    boolean validBool = errors.isEmpty()
    boolean validBool =
        errors.isEmpty()
    
    message.setProperty("isValid", validBool.toString())
    message.setProperty("validationErrors", errors.join(" | "))
    message.setProperty(
        "isValid",
        validBool.toString()
    )

    // Append validation results to JSON
    message.setProperty(
        "validationErrors",
        errors.join(" | ")
    )


    // =========================================================
    // 9. APPEND VALIDATION RESULT TO JSON
    // 10. APPEND VALIDATION RESULT TO JSON
    // =========================================================

    json.validation = [
        isValid: validBool,
        errors : errors
    ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
    message.setHeader("Content-Type", "application/json")

    // =========================================================
    // 11. SET MESSAGE BODY
    // =========================================================

    message.setBody(
        JsonOutput.prettyPrint(
            JsonOutput.toJson(json)
        )
    )

    message.setHeader(
        "Content-Type",
        "application/json"
    )

    return message
}


// =============================================================
// HELPER METHODS
// =============================================================

def boolean isBlank(value) {

    return value == null ||
           value.toString().trim().isEmpty()
}


def boolean isValidNumber(value) {

    if (value == null) {
        return false
    }

    try {
        Double.parseDouble(value.toString().trim())

        Double.parseDouble(
            value.toString().trim()
        )

        return true

    } catch (Exception e) {

        return false
    }
}


def double parseDouble(value) {
    if (value == null) return 0.0

    if (value == null) {
        return 0.0
    }

    try {

        if (value instanceof Number) {
            return value.toDouble()
        }
        return value.toString().replaceAll("[^0-9.]", "").toDouble()

        return Double.parseDouble(
            value.toString().trim()
        )

    } catch (Exception e) {

        return 0.0
    }
}
