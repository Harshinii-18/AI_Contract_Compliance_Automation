import java.io.ByteArrayOutputStream
import java.util.UUID

def processData(message) {
    // 1. Get binary file content from message body
    byte[] fileBytes = message.getBody(byte[].class)
    
    // 2. Options JSON matching SAP_purchaseOrder_schema v1 exactly
    String optionsJson = '''{
        "clientId": "default",
        "documentType": "purchaseOrder",
        "schemaName": "SAP_purchaseOrder_schema",
        "schemaVersion": "1",
        "extractionHeaderFields": [
            "purchaseOrderNumber",
            "taxId",
            "subtotalAmount",
            "totalAmount",
            "currencyCode",
            "documentDate",
            "deliveryDate",
            "paymentTerms",
            "buyerBankAccount",
            "buyerAddress",
            "buyerName",
            "shipToAddress",
            "taxIdNumber",
            "supplierId",
            "shipToTerms",
            "quantity",
            "buyerId",
            "buyerStreet",
            "buyerCity",
            "buyerHouseNumber",
            "buyerPostalCode",
            "buyerCountryCode",
            "buyerPhone",
            "buyerFax",
            "buyerEmail",
            "buyerState",
            "buyerDistrict",
            "buyerExtraAddress",
            "shipToName",
            "shipToStreet",
            "shipToCity",
            "shipToHouseNumber",
            "shipToPostalCode",
            "shipToCountryCode",
            "shipToPhone",
            "shipToFax",
            "shipToEmail",
            "shipToState",
            "shipToDistrict",
            "shipToExtraAddress"
        ],
        "extractionLineItemFields": [
            "description",
            "netAmount",
            "quantity",
            "unitPrice",
            "deliveryDate",
            "itemNumber",
            "currencyCode",
            "supplierMaterialNumber",
            "customerMaterialNumber",
            "unitOfMeasure"
        ]
    }'''

    // 3. Dynamic Boundary using UUID
    String boundary = "---------------------------" + UUID.randomUUID().toString().replace("-", "")
    String lineEnd = "\r\n"
    String twoHyphens = "--"

    // 4. Retrieve filename
    def headers = message.getHeaders()
    String fileName = headers.get("CamelFileNameOnly") ?: headers.get("filename") ?: "purchase_order.pdf"

    // 5. Construct Multipart Payload
    ByteArrayOutputStream baos = new ByteArrayOutputStream()

    // Part 1: 'options' JSON
    String optionsHeader = twoHyphens + boundary + lineEnd +
        "Content-Disposition: form-data; name=\"options\"" + lineEnd +
        "Content-Type: application/json; charset=UTF-8" + lineEnd + lineEnd
    baos.write(optionsHeader.getBytes("UTF-8"))
    baos.write(optionsJson.getBytes("UTF-8"))
    baos.write(lineEnd.getBytes("UTF-8"))

    // Part 2: 'file' binary
    String fileHeader = twoHyphens + boundary + lineEnd +
        "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd +
        "Content-Type: application/pdf" + lineEnd + lineEnd
    baos.write(fileHeader.getBytes("UTF-8"))
    baos.write(fileBytes)
    baos.write(lineEnd.getBytes("UTF-8"))

    // End Boundary
    String endBoundary = twoHyphens + boundary + twoHyphens + lineEnd
    baos.write(endBoundary.getBytes("UTF-8"))

    // 6. Set message body and Content-Type header
    message.setBody(baos.toByteArray())
    message.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary)

    return message
}
