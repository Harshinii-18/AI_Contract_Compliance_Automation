import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

def Message processData(Message message) {
    // Stream the body using Reader as required by CPI guidelines
    def reader = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(reader)

    // Extract properties safely
    message.setProperty("documentId", json.id?.toString())
    message.setProperty("docStatus", json.status?.toString())

    return message
}