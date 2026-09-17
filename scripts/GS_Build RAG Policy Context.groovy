import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {

    def reader = message.getBody(java.io.Reader.class)
    def xml = new XmlSlurper().parse(reader)

    def policyRows = xml.select_response.row

    if (policyRows == null || policyRows.size() == 0) {
        throw new IllegalArgumentException(
                "No policy rows were returned from HANA RAG retrieval"
        )
    }

    /*
     * LinkedHashMap preserves insertion order and removes duplicates
     * based on POLICY_ID.
     */
    def policiesById = new LinkedHashMap<String, Map>()

    policyRows.each { row ->

        String policyId = row.POLICY_ID.text()?.trim()
        String category = row.CATEGORY.text()?.trim()
        String policyName = row.POLICY_NAME.text()?.trim()
        String policyText = row.POLICY_TEXT.text()?.trim()
        String similarityText = row.SIMILARITY.text()?.trim()

        if (!policyId) {
            return
        }

        BigDecimal similarity = null

        if (similarityText) {
            similarity = new BigDecimal(similarityText)
        }

        /*
         * Keep the highest similarity result if the same policy
         * is returned more than once.
         */
        if (!policiesById.containsKey(policyId)
                || similarity > policiesById[policyId].similarity) {

            policiesById[policyId] = [
                    policyId  : policyId,
                    category  : category,
                    policyName: policyName,
                    policyText: policyText,
                    similarity: similarity
            ]
        }
    }

    def policies = policiesById.values().toList()

    /*
     * Sort by category and then by similarity descending.
     */
    policies.sort { a, b ->

        int categoryComparison =
                a.category.toString() <=> b.category.toString()

        if (categoryComparison != 0) {
            return categoryComparison
        }

        return b.similarity <=> a.similarity
    }

    /*
     * Build a readable policy context for the LLM.
     */
    StringBuilder context = new StringBuilder()

    context.append("RETRIEVED PROCUREMENT POLICIES\n")
    context.append("================================\n\n")

    policies.eachWithIndex { policy, index ->

        context.append("Policy ${index + 1}\n")
        context.append("Policy ID: ${policy.policyId}\n")
        context.append("Category: ${policy.category}\n")
        context.append("Policy Name: ${policy.policyName}\n")
        context.append("Policy Text: ${policy.policyText}\n")

        if (policy.similarity != null) {
            context.append(
                    "Similarity Score: " +
                    policy.similarity.setScale(4, BigDecimal.ROUND_HALF_UP) +
                    "\n"
            )
        }

        context.append("\n")
    }

    String policyContext = context.toString()

    /*
     * Store structured policies for possible later use.
     */
    String policiesJson = JsonOutput.toJson(policies)

    message.setProperty("ragPolicyContext", policyContext)
    message.setProperty("ragPoliciesJson", policiesJson)
    message.setProperty("ragPolicyCount", policies.size().toString())

    /*
     * Set the body to the readable policy context for testing.
     * The next Content Modifier can use the ragPolicyContext property
     * when constructing the final Groq prompt.
     */
    message.setBody(policyContext)
    message.setHeader("Content-Type", "text/plain")

    return message
}