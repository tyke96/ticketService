import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

public class TestUtils {
    public static String extractBodyContent(HttpRequest.BodyPublisher bodyPublisher) throws IOException, InterruptedException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Flow.Subscriber<ByteBuffer> subscriber = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                outputStream.write(item.array(), 0, item.remaining());
            }

            @Override
            public void onError(Throwable throwable) {
                throw new RuntimeException(throwable);
            }

            @Override
            public void onComplete() {
            }
        };
        bodyPublisher.subscribe(subscriber);
        return outputStream.toString();
    }
}
