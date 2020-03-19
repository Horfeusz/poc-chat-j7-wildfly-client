package be.chat.util;

/**
 * This is my consumer for java 1.7 and older
 *
 * @param <T>
 */
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);
}
