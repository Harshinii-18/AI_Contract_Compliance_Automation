import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.time.LocalDate
import java.time.temporal.ChronoUnit

def Message processData(Message message) {

    def reader = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(reader)

    def header = json.header ?: [:]
    def lineItems = json.lineItems ?: []

    def queries = []

    /*
     * ---------------------------------------------------------
     * 1. PAYMENT QUERY
     * ---------------------------------------------------------
     */

    String paymentTerms = header.paymentTerms ?: "Not available"
    String supplierId = header.senderId ?: "Not available"
    String supplierName = header.senderName ?: "Not available"

    String paymentQuery = """
Payment policy search.

Supplier name: ${supplierName}
Supplier ID: ${supplierId}
Payment terms: ${paymentTerms}
Advance payment or pre-payment: Not specified in the purchase order.

Find procurement policies related to:
- maximum payment terms
- newly onboarded suppliers
- advance payment approval
""".stripIndent().trim()

    queries.add([
        queryId : "PAYMENT",
        query  : paymentQuery
    ])

    /*
     * ---------------------------------------------------------
     * 2. DELIVERY QUERY
     * ---------------------------------------------------------
     */

    String poDate = header.documentDate ?: "Not available"
    String deliveryDate = header.deliveryDate ?: "Not available"

    String deliveryLeadTime = "Not calculable"

    try {
        if (header.documentDate && header.deliveryDate) {
            LocalDate startDate = LocalDate.parse(header.documentDate.toString())
            LocalDate endDate = LocalDate.parse(header.deliveryDate.toString())

            deliveryLeadTime =
                    ChronoUnit.DAYS.between(startDate, endDate).toString() +
                    " calendar days"
        }
    } catch (Exception ignored) {
        deliveryLeadTime = "Not calculable"
    }

    String deliveryQuery = """
Delivery policy search.

Purchase order date: ${poDate}
Requested delivery date: ${deliveryDate}
Calculated delivery lead time: ${deliveryLeadTime}
Delivery urgency: Not explicitly specified.

Find procurement policies related to:
- maximum delivery lead time
- expedited delivery
- delivery requests shorter than 7 calendar days
- delivery approval requirements
""".stripIndent().trim()

    queries.add([
        queryId : "DELIVERY",
        query  : deliveryQuery
    ])

    /*
     * ---------------------------------------------------------
     * 3. CURRENCY QUERY
     * ---------------------------------------------------------
     */

    String currency = header.currency ?: "Not available"
    String grossAmount = header.grossAmount != null
            ? header.grossAmount.toString()
            : "Not available"

    String currencyQuery = """
Currency policy search.

Purchase order currency: ${currency}
Purchase order gross amount: ${grossAmount}
Amount currency context: ${currency}

Find procurement policies related to:
- required purchase order currency
- non-USD purchase orders
- foreign currency thresholds
- Treasury hedging verification
""".stripIndent().trim()

    queries.add([
        queryId : "CURRENCY",
        query  : currencyQuery
    ])

    /*
     * ---------------------------------------------------------
     * 4. SHIPPING QUERY
     * ---------------------------------------------------------
     */

    String shippingTerms = header.shippingTerms ?: "Not available"

    def shipTo = header.shipTo ?: [:]

    String destinationCountry = shipTo.country ?: "Not available"
    String destinationAddress = shipTo.address ?: "Not available"

    String shippingQuery = """
Shipping policy search.

Shipping terms: ${shippingTerms}
Destination country: ${destinationCountry}
Destination address: ${destinationAddress}
Cross-border shipment: Cannot be determined from available data.

Find procurement policies related to:
- permitted shipping terms
- FOB Destination
- CIF
- DAP
- international or cross-border shipping
- customs clearance responsibility
""".stripIndent().trim()

    queries.add([
        queryId : "SHIPPING",
        query  : shippingQuery
    ])

    /*
     * ---------------------------------------------------------
     * 5. APPROVAL QUERY
     * ---------------------------------------------------------
     */

    String approvalQuery = """
Approval policy search.

Purchase order gross amount: ${grossAmount}
Purchase order currency: ${currency}

Find procurement policies related to:
- human review for purchase orders above USD 10,000
- Vice President approval for purchase orders above USD 50,000
- approval requirements based on purchase order value
""".stripIndent().trim()

    queries.add([
        queryId : "APPROVAL",
        query  : approvalQuery
    ])

    /*
     * ---------------------------------------------------------
     * 6. QUANTITY QUERY
     * ---------------------------------------------------------
     */

    StringBuilder quantityQuery = new StringBuilder()

    quantityQuery.append("""
Quantity policy search.

Review the following purchase order line items:
""".stripIndent().trim())

    quantityQuery.append("\n")

    lineItems.each { item ->

        quantityQuery.append(
                "Item number: ${item.itemNumber ?: 'Not available'}\n" +
                "Description: ${item.description ?: 'Not available'}\n" +
                "Quantity: ${item.quantity ?: 'Not available'}\n" +
                "Unit of measure: ${item.unitOfMeasure ?: 'Not available'}\n" +
                "Supplier material number: ${item.supplierMaterialNumber ?: 'Not available'}\n\n"
        )
    }

    quantityQuery.append("""
Find procurement policies related to:
- quantity greater than zero
- minimum order quantity
- Category A raw materials
- minimum quantity of 100 units
""".stripIndent().trim())

    queries.add([
        queryId : "QUANTITY",
        query  : quantityQuery.toString().trim()
    ])

    /*
     * ---------------------------------------------------------
     * 7. LINE AMOUNT QUERY
     * ---------------------------------------------------------
     */

    StringBuilder lineAmountQuery = new StringBuilder()

    lineAmountQuery.append("""
Line amount policy search.

Review the following line-item calculations:
""".stripIndent().trim())

    lineAmountQuery.append("\n")

    lineItems.each { item ->

        lineAmountQuery.append(
                "Item number: ${item.itemNumber ?: 'Not available'}, " +
                "Quantity: ${item.quantity ?: 'Not available'}, " +
                "Unit price: ${item.unitPrice ?: 'Not available'}, " +
                "Line net amount: ${item.netAmount ?: 'Not available'}\n"
        )
    }

    lineAmountQuery.append("""
Find procurement policies related to:
- quantity multiplied by unit price
- line-item net amount
- line amount tolerance of USD 0.01
""".stripIndent().trim())

    queries.add([
        queryId : "LINE_AMOUNT",
        query  : lineAmountQuery.toString().trim()
    ])

    /*
     * ---------------------------------------------------------
     * 8. PO TOTAL QUERY
     * ---------------------------------------------------------
     */

    StringBuilder poTotalQuery = new StringBuilder()

    poTotalQuery.append("""
Purchase order total policy search.

Purchase order net amount: ${header.netAmount ?: 'Not available'}
Purchase order gross amount: ${header.grossAmount ?: 'Not available'}
Currency: ${currency}

Line-item net amounts:
""".stripIndent().trim())

    poTotalQuery.append("\n")

    lineItems.each { item ->

        poTotalQuery.append(
                "Item ${item.itemNumber ?: 'Not available'} " +
                "net amount: ${item.netAmount ?: 'Not available'}\n"
        )
    }

    poTotalQuery.append("""
Find procurement policies related to:
- sum of line-item net amounts
- purchase order net amount
- PO total reconciliation
- tolerance of USD 0.01
""".stripIndent().trim())

    queries.add([
        queryId : "PO_TOTAL",
        query  : poTotalQuery.toString().trim()
    ])

    /*
     * Store the complete query array as a property.
     */
     def outputObject = [
        queries: queries
    ]
    
    String queriesJson = JsonOutput.toJson(outputObject)
    
    message.setProperty("ragQueries", queriesJson)
    message.setBody(queriesJson)
    message.setHeader("Content-Type", "application/json")
    
    return message
}