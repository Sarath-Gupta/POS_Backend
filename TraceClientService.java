import org.openjdk.btrace.core.annotations.*;
import static org.openjdk.btrace.core.BTraceUtils.*;
//import com.increff.pos.entity.Client; // Assuming Client is defined in this package

@BTrace
public class TraceClientService {
    @OnMethod(
            clazz="com.increff.pos.dao.ClientDao", // Use the exact fully qualified class name for testing!
            method="findByName"
    )
    public static void testInjection() {
        println("SUCCESSFUL INJECTION: Testing probe hit.");
    }
}
