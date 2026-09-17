import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper

def Message processData(Message message) {

    def reader = message.getBody(java.io.Reader.class)
    def xml = new XmlSlurper().parse(reader)

    /*
     * Supports both possible structures:
     *
     * 1.
     * <queries>
     *     <queryId>PAYMENT</queryId>
     *     <query>...</query>
     * </queries>
     *
     * 2.
     * <root>
     *     <queries>
     *         <queryId>PAYMENT</queryId>
     *         <query>...</query>
     *     </queries>
     * </root>
     */

    def queryNode

    if (xml.name() == "queries") {
        queryNode = xml
    } else {
        queryNode = xml.queries[0]
    }

    if (queryNode == null || queryNode.size() == 0) {
        throw new IllegalArgumentException(
                "Could not find the queries element in the XML payload"
        )
    }

    String queryId = queryNode.queryId.text()?.trim()
    String ragQuery = queryNode.query.text()

    if (!queryId) {
        throw new IllegalArgumentException(
                "RAG query ID is empty. Received XML: ${xml}"
        )
    }

    if (!ragQuery || !ragQuery.trim()) {
        throw new IllegalArgumentException(
                "RAG query is empty for queryId: ${queryId}"
        )
    }

    String escapedQuery = ragQuery.replace("'", "''")

   String sql = """
    SELECT
        POLICY_ID,
        CATEGORY,
        POLICY_NAME,
        POLICY_TEXT,
        COSINE_SIMILARITY(
            VECTOR_EMBEDDING(
                '${escapedQuery}',
                'QUERY',
                'SAP_GXY.20250407'
            ),
            EMBEDDING
        ) AS SIMILARITY
    FROM PROCUREMENT_POLICIES
    WHERE EMBEDDING IS NOT NULL
      AND CATEGORY = '${queryId}'
      AND COSINE_SIMILARITY(
            VECTOR_EMBEDDING(
                '${escapedQuery}',
                'QUERY',
                'SAP_GXY.20250407'
            ),
            EMBEDDING
          ) >= 0.60
    ORDER BY SIMILARITY DESC
    """

    message.setProperty("ragQueryId", queryId)
    message.setProperty("ragSql", sql)

    message.setBody(sql)
    message.setHeader("Content-Type", "text/plain")

    return message
}