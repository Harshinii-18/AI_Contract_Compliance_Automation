import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {
    def reader = message.getBody(java.io.Reader.class)
    def rawJson = new JsonSlurper().parse(reader)
    
    def headerFields = rawJson?.extraction?.headerFields ?: []
    def rawLineItems = rawJson?.extraction?.lineItems ?: []
    
    // Helper to get header field value cleanly
    def getHeaderValue = { String name ->
        def field = headerFields.find { it?.name == name }
        return field?.value ?: null
    }

    // Helper to extract field value from a line item array
    def getItemValue = { itemArray, String name ->
        def field = itemArray.find { it?.name == name }
        return field?.value ?: null
    }

    // Map Header Safely
    def header = [
        documentNumber : getHeaderValue("documentNumber"),
        documentDate   : getHeaderValue("documentDate"),
        deliveryDate   : getHeaderValue("deliveryDate"),
        senderName     : getHeaderValue("senderName") ?: getHeaderValue("vendorName"),
        senderId       : getHeaderValue("senderId"),
        receiverId     : getHeaderValue("receiverId"),
        paymentTerms   : getHeaderValue("paymentTerms"),
        shippingTerms  : getHeaderValue("shippingTerms"),
        currency       : getHeaderValue("currencyCode") ?: getHeaderValue("currency"),
        netAmount      : getHeaderValue("netAmount"),
        grossAmount    : getHeaderValue("grossAmount"),
        shipTo         : [
            name       : getHeaderValue("shipToName"),
            address    : getHeaderValue("shipToAddress"),
            city       : getHeaderValue("shipToCity"),
            state      : getHeaderValue("shipToState"),
            postalCode : getHeaderValue("shipToPostalCode"),
            country    : getHeaderValue("shipToCountryCode"),
            phone      : getHeaderValue("shipToPhone"),
            email      : getHeaderValue("shipToEmail")
        ]
    ]

    // Map Line Items Safely
    def items = rawLineItems.collect { itemGroup ->
        [
            itemNumber             : getItemValue(itemGroup, "itemNumber"),
            description            : getItemValue(itemGroup, "description"),
            supplierMaterialNumber : getItemValue(itemGroup, "supplierMaterialNumber") ?: getItemValue(itemGroup, "materialNumber"),
            customerMaterialNumber : getItemValue(itemGroup, "customerMaterialNumber") ?: getItemValue(itemGroup, "senderMaterialNumber"),
            quantity               : getItemValue(itemGroup, "quantity"),
            unitOfMeasure          : getItemValue(itemGroup, "unitOfMeasure"),
            unitPrice              : getItemValue(itemGroup, "unitPrice"),
            netAmount              : getItemValue(itemGroup, "netAmount")
        ]
    }

    def normalizedPayload = [
        header   : header,
        lineItems: items
    ]

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(normalizedPayload)))
    message.setHeader("Content-Type", "application/json")

    return message
}