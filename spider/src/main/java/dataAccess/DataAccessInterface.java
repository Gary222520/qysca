package dataAccess;

public interface DataAccessInterface<T>{
    void enqueue(T data);
    void flush();
}
