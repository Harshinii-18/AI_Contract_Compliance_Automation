import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

def Message processData(Message message) {

    def body = message.getBody(String)

    // Parse Groq response
    def response = new JsonSlurper().parseText(body)

    // Extract AI JSON from Groq response
    def aiContent = response.choices[0].message.content

    // Parse AI JSON
    def aiResponse = new JsonSlurper().parseText(aiContent)

    // Extract recommendation
    def recommendation = aiResponse.recommendation

    // Store recommendation as CPI property
    message.setProperty("aiRecommendation", recommendation)

    // Set clean AI JSON as message body
    message.setBody(aiContent)

    return message
}