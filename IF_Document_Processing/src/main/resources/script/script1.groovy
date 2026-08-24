import com.sap.gateway.ip.core.customdev.util.Message
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.ContentType

def Message processData(Message message) {
    // 1. Get binary file payload
    byte[] fileBytes = message.getBody(byte[].class)
    
    // 2. Options JSON with verified Purchase Order schema fields
    String optionsJson = '''{
        "clientId": "default",
        "documentType": "purchaseOrder",
        "extractionHeaderFields": [
            "purchaseOrderNumber",
            "documentDate",
            "dueDate",
            "deliveryDate",
            "netAmount",
            "grossAmount",
            "taxAmount",
            "taxRate",
            "shippingAmount",
            "discount",
            "currencyCode",
            "paymentTerms",
            "deliveryNoteNumber",
            "barcode",
            "supplierName",
            "supplierAddress",
            "supplierTaxId",
            "supplierBankAccount",
            "supplierStreet",
            "supplierCity",
            "supplierHouseNumber",
            "supplierPostalCode",
            "supplierCountryCode",
            "supplierState",
            "supplierDistrict",
            "supplierExtraAddress",
            "buyerName",
            "buyerAddress",
            "buyerTaxId",
            "buyerStreet",
            "buyerCity",
            "buyerHouseNumber",
            "buyerPostalCode",
            "buyerCountryCode",
            "buyerState",
            "buyerDistrict",
            "buyerExtraAddress"
        ],
        "extractionLineItemFields": [
            "description",
            "amount",
            "quantity",
            "unitPrice",
            "materialNumber",
            "unitOfMeasure"
        ]
    }'''

    def headers = message.getHeaders()
    String fileName = headers.get("CamelFileNameOnly") ?: headers.get("filename") ?: "purchase_order.pdf"

    // 3. Construct Multipart Entity with explicit application/pdf content type
    MultipartEntityBuilder builder = MultipartEntityBuilder.create()
    builder.addPart("options", new StringBody(optionsJson, ContentType.APPLICATION_JSON))
    builder.addPart("file", new ByteArrayBody(fileBytes, ContentType.create("application/pdf"), fileName))

    def entity = builder.build()
    
    // 4. Output to Message Body & set Content-Type header
    ByteArrayOutputStream out = new ByteArrayOutputStream()
    entity.writeTo(out)
    
    message.setBody(out.toByteArray())
    message.setHeader("Content-Type", entity.getContentType().getValue())

    return message
}
