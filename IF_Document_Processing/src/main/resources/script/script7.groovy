import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

def Message processData(Message message) {

    def reader = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(reader)

    def header = json.header
    def lineItems = json.lineItems ?: []

    StringBuilder query = new StringBuilder()

    query.append("Purchase order:\n")
    query.append("PO Number: ${header.documentNumber}\n")
    query.append("PO Date: ${header.documentDate}\n")
    query.append("Delivery Date: ${header.deliveryDate}\n")
    query.append("Payment Terms: ${header.paymentTerms}\n")
    query.append("Currency: ${header.currency}\n")
    query.append("Shipping Terms: ${header.shippingTerms}\n")
    query.append("PO Net Amount: ${header.netAmount}\n")
    query.append("PO Gross Amount: ${header.grossAmount}\n")

    query.append("\nLine items:\n")

    lineItems.each { item ->
        query.append(
            "Item ${item.itemNumber}: " +
            "Description=${item.description}, " +
            "Quantity=${item.quantity}, " +
            "Unit Price=${item.unitPrice}, " +
            "Net Amount=${item.netAmount}\n"
        )
    }

    message.setBody(query.toString())
    message.setHeader("Content-Type", "text/plain")

    return message
}