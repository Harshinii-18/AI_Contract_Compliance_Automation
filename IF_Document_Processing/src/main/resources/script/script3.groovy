import com.sap.gateway.ip.core.customdev.util.Message
import java.util.concurrent.TimeUnit

def Message processData(Message message) {
    // Wait for 3 seconds using standard Java Concurrency API
    TimeUnit.SECONDS.sleep(5)
    
    return message
}