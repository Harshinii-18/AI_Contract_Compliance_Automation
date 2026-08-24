import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
    def reader = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(reader)

    List<String> errors = []

    def header = json?.header ?: [:]
    def lineItems = json?.lineItems ?: []

    // 1. Required Header Fields & Validity (Checking for null or empty/blank string)
    if (!header.documentNumber || header.documentNumber.toString().trim().isEmpty()) {
        errors.add("Missing Required Field: PO Number")
    }
    if (!header.senderName || header.senderName.toString().trim().isEmpty()) {
        errors.add("Missing Required Field: Supplier")
    }
    if (!header.currency || header.currency.toString().trim().isEmpty()) {
        errors.add("Missing Required Field: Currency")
    }

    // 2. Line Items Validation & Mathematical Consistency
    if (!lineItems) {
        errors.add("Missing Line Items: At least one line item is required")
    } else {
        lineItems.eachWithIndex { item, index ->
            int itemNo = index + 1
            
            def qty = parseDouble(item.quantity)
            def price = parseDouble(item.unitPrice)
            def total = parseDouble(item.netAmount)

            // Data Validity Checks (> 0)
            if (qty <= 0) {
                errors.add("Line Item ${itemNo}: Quantity must be greater than 0 (Found: ${item.quantity})")
            }
            if (price <= 0) {
                errors.add("Line Item ${itemNo}: Unit Price must be greater than 0 (Found: ${item.unitPrice})")
            }
            if (total <= 0) {
                errors.add("Line Item ${itemNo}: Total Amount must be greater than 0 (Found: ${item.netAmount})")
            }

            // Mathematical Consistency Check (Qty * Price == Total)
            if (qty > 0 && price > 0 && total > 0) {
                def calculatedTotal = qty * price
                if (Math.abs(calculatedTotal - total) > 0.01) {
                    errors.add("Line Item ${itemNo}: Math mismatch! (${qty} x ${price} = ${calculatedTotal}, but Total is ${total})")
                }
            }
        }
    }

    // 3. Set Validation Status explicitly as String ("true" or "false")
    boolean validBool = errors.isEmpty()
    
    message.setProperty("isValid", validBool.toString())
    message.setProperty("validationErrors", errors.join(" | "))

    // Append validation results to JSON
    json.validation = [
        isValid: validBool,
        errors : errors
    ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
    message.setHeader("Content-Type", "application/json")

    return message
}

def double parseDouble(value) {
    if (value == null) return 0.0
    try {
        if (value instanceof Number) {
            return value.toDouble()
        }
        return value.toString().replaceAll("[^0-9.]", "").toDouble()
    } catch (Exception e) {
        return 0.0
    }
}