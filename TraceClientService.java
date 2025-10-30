import org.openjdk.btrace.core.annotations.*;
import static org.openjdk.btrace.core.BTraceUtils.*;

@BTrace
public class TraceClientService {
    @OnMethod(
            clazz = "com.increff.pos.dao.ProductDao",
            method = "findById",
            location = @Location(Kind.ENTRY)
    )
    public static void onEntry(Integer id) {
        println("Entered findById with id: " + id);
    }
}