import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.ITApiFactory
import com.sap.it.api.securestore.SecureStoreService
import com.sap.it.api.securestore.UserCredential

def Message processData(Message message) {

    def secureStoreService =
        ITApiFactory.getService(SecureStoreService.class, null)

    UserCredential credential =
        secureStoreService.getUserCredential("GROQ_API_KEY")

    String apiKey = credential.getPassword().toString()

    message.setHeader("Authorization", "Bearer " + apiKey)

    return message
}