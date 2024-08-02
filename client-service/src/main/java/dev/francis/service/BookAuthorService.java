package dev.francis.service;

import com.google.protobuf.Descriptors;
import dev.francis.TempDb;
import dev.francis.grpc.service.Author;
import dev.francis.grpc.service.Book;
import dev.francis.grpc.service.BookAuthorServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorService {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
            .usePlaintext()
            .build();
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient = BookAuthorServiceGrpc.newBlockingStub(channel);
    BookAuthorServiceGrpc.BookAuthorServiceStub asyncStub= BookAuthorServiceGrpc.newStub(channel);
    public Map<Descriptors.FieldDescriptor, Object> getAuthor(int authorId){

        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        Author authResponse = synchronousClient.getAuthor(authorRequest);
        return authResponse.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(int authorId) throws InterruptedException {
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        asyncStub.getBooksByAuthor(authorRequest, new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
       boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
       return await ? response : Collections.emptyList();
    }

    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        final Map<String, Map<Descriptors.FieldDescriptor, Object>> response = new HashMap<>();
        StreamObserver<Book> responseObserver = asyncStub.getExpensiveBook(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.put("ExpensiveBook",book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDb.getBooksFromTempDb().forEach(responseObserver::onNext);
        responseObserver.onCompleted();
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyMap();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorGender(String gender) throws InterruptedException {
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        List<Map<Descriptors.FieldDescriptor, Object>> response=new ArrayList<>();
        StreamObserver<Book> observer= asyncStub.getBooksByAuthorGender(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDb.getAuthorsFromTempDb()
                .stream()
                .filter(author -> author.getGender().equalsIgnoreCase(gender))
                .forEach(author -> observer.onNext(Book.newBuilder().setAuthorId(author.getAuthorId()).build()));
        observer.onCompleted();
        boolean await = countDownLatch.await(1,TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();

    }
}
