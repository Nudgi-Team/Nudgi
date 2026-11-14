package io.github.Nudgi.network.protocol;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.Nudgi.network.protocol.config.Category;
import io.github.Nudgi.network.protocol.config.SourceContext;



/**
 * The root of all Messages.
 * <p>
 * # Overview
 * <p>
 * A Message consists of a header and a payload, where:
 * - The header consists of the {@link Category Message category} (determined
 * by the concrete class type) and its {@link SourceContext}.
 * - The payload consists of the data (fields) being sent as part of the Message.
 * <p>
 * A "data field" or just "field" need not correspond to a declared field in the subclass - it simply
 * represents a unit of data through which subclasses can act on when deserializing
 * a payload.
 * <p>
 * Said payload may be empty, particularly for notification-only messages that don't have
 * additional information to send. For these kinds of messages, consider extending {@link Notification}.
 * <p>
 * Some payloads may also be dependent on {@link SourceContext}, changing shape based on
 * who sent the message. This avoids asymmetric, bidirectional messages from having to
 * declare two separate classes with very similar categories, which would otherwise
 * overload the Category enum name's identifier. One Category thus always represents
 * one particular kind of event, regardless of who sent it.
 * <p>
 * # Subclassing Requirements
 * <p>
 * ## General
 * <p>
 * Inverse invariant: Serialization and deserialization by definition always represent inverse operations, i.e.
 * for any concrete subclass instance X, deserialize(serialized(X)) always contains
 * the exact same information as X.
 * <p>
 * <b> Your subclass must uphold this invariant. </b>
 * <p>
 * Your subclass must exist in either this or a descendant's {@code permits} clause.
 * <p>
 * All subclasses must be either
 * - {@code abstract} and {@code sealed}
 * <p>
 * or
 * - {@code final}
 * <p>
 * This restriction ensures that this class can properly detect the message type.
 * <i> It is an error for a subclass to be
 * {@code abstract} but not {@code sealed}, or concrete but not {@code final}.</i>
 * <p>
 * ## Concrete Implementations
 * <p>
 * ### Category Registration
 * <p>
 * All concrete subclasses must define their {@link Category}, by
 * registering their {@code Class} token within the enum for exactly one enum value.
 * Abstract subclasses may not register a Category.
 * <p>
 * <i>It is an error if any of the following occurs:</i>
 * - A concrete subclass does not map to any Category.
 * - A concrete subclass maps to multiple categories.
 * - An abstract subclass token appears in a Category definition.
 * <p>
 * ### Constructor Requirements
 * All concrete subclasses are required to implement a
 * Deserialization Constructor (any access modifier),
 * with a signature based on the existing number of constructors of the subclass:
 * - Exactly 1: That constructor must be a no-args constructor. Then, see Option 1 below.
 * - Otherwise (if 2 or more constructors are present, or if you have a parameterized constructor):
 * See Option 2 below.
 * <p>
 * <i>It is an error for any concrete subclass to not define a Deserialization Constructor, or 
 * to define only the constructor in Option 2, and no other constructors.</i>
 * <p>
 * #### Option 1. Deserialization Constructor()
 * <p>
 * For this kind of constructor, no arguments are passed. It is assumed that classes
 * using a no-args deserialization constructor are notification-only messages.
 * <p>
 * #### Option 2. Deserialization Constructor(SourceContext context, String[] args)
 * <p>
 * This type of constructor conforms to the following:
 * - The elements of {@code args} only include the string representations of each
 * data field specified in the corresponding subclass's serialized representation.
 * Furthermore, the {@code SourceContext} passed in can only correspond to 
 * any possible argument in super() calls within the subclass.
 *
 * - - In other words, {@code args} will always represent a plausible output
 * from a call to that Message's {@link Message#serialized() serialized()} method.
 *
 * - The constructor must pass a SourceContext value equal to the SourceContext received
 * in the first parameter, in its call to {@code super()}. For messages with
 * only one possible context, that context may be passed directly instead.
 * 
 * - It must reconstruct the subclass in a manner such that it conforms
 * to the Inverse Invariant, from the application's perspective.
 *
 * <p>
 * ### Contextual Values
 * If a value in your subclass is contextual (i.e. its presence depends on the SourceContext of the message),
 * you should emit a {@link Message.ContextMismatchException} whenever a getter is invoked
 * for a contextual value that is not present in the current context. See the {@link Message.Contextual Contextual API}
 * for an implementation of such.
 * <p>
 * By definition, only subclasses taking Option 2 above may have contextual values.
 * 
 * @see Message#Message(SourceContext)
 * @see Notification
 * @see Message#serialized()
 * @java 17
 */
public abstract sealed class Message
permits BoardMessage, PlayerMovedMessage, ShiftMessage,
        StepMessage, Notification, NewPlayerMessage,
        TurnMessage, WelcomeMessage, WinMessage {
    /**
     * If this character is present within the array result of {@link #serialized()},
     * a {@link BadMessageException} is thrown when {@link #toString()} is called.
     * <p>
     * Other delimiter characters remain permissible within data fields, but subclasses
     * will be responsible for any necessary separation.
     */
    protected static final char DELIMITER = '\t';

    private static final String CONTEXT_MISMATCH = "Attempted to access a field that is not available in this context";
    private static final String DELIMITER_STR = String.valueOf(DELIMITER);
    private static final Set<String> initErrors = new HashSet<>();

    private final Header header;
   
    // fail-fast setup: ExceptionInInitializer occurs here = something isn't right
    // verify part 1
    static {
        // category exists
        for (Class<? extends Message> clazz: getConcreteSubclasses()) {
            try { Category.find(clazz); }
            catch (IllegalMessageException e) {
                initErrors.add(e.getMessage());
            }
        }
        // category exists without duplicates nor abstract classes
        Map<Class<? extends Message>, Boolean> findings = new HashMap<>();
        for (Category cat: Category.values()) {
            Class<? extends Message> type = cat.getAssociatedType();
            if (findings.putIfAbsent(type, true) != null) {
                initErrors.add("error: multiple Category definition of " + type);
            }
            else if (Modifier.isAbstract(type.getModifiers())) {
                initErrors.add("error: the abstract " + type + " may not have a Category");
            }
        }
    }
    // verify part 2: constructors
    static {
        for (Class<? extends Message> clazz: getConcreteSubclasses()) {
            checkConstructor(clazz);
        }
    }
    // final checks
    static {
        if (!initErrors.isEmpty()) {
            String msg = "";
            for (String error: initErrors) {
                msg += error + '\n';
            }
            if (initErrors.size() > 1) {
                msg = "\n" + initErrors.size() + " errors\n" + msg;
            }
            // killer queen, daisan no bakudan: bites za dusto
            throw new IllegalMessageException(msg);
        }
    }

    /**
     * Constructs a new Message with the given context.
     * @param source the source context of this message. Passing ANY is only permissible
     * if the message is constructed with the exact same data fields, in the same way,
     * on both server and client.
     * Otherwise, use separate constructors passing in SERVER and CLIENT respectively if
     * a bidirectional, asymmetric message is needed.
     * @throws NullPointerException if source is {@code null}
     */
    protected Message(SourceContext source) {
        header = new Header(Category.find(getClass()), source);
    }


    @SuppressWarnings("unchecked")
    private static <T> List<Class<? extends T>> typedPermittedSubclassesOf(Class<T> clazz) {
        return Arrays.asList((Class<? extends T>[])clazz.getPermittedSubclasses());
    }


    private static List<Class<? extends Message>> getConcreteSubclasses() {
        final List<Class<? extends Message>> init = typedPermittedSubclassesOf(Message.class);

        Deque<Class<? extends Message>> processing = new ArrayDeque<>();
        List<Class<? extends Message>> current = new ArrayList<>();

        processing.addAll(init);
        while (!processing.isEmpty()) {
            Class<? extends Message> element = processing.pop();
            if (Modifier.isAbstract(element.getModifiers())) {
                if (!element.isSealed()) {
                    initErrors.add("error: expected 'sealed' in abstract subclass declaration for " + element);
                    continue;
                }
                processing.addAll(typedPermittedSubclassesOf(element));
            }
            else {
                if (!Modifier.isFinal(element.getModifiers())) {
                    initErrors.add("error: expected 'final' in concrete subclass declaration for " + element);
                }
                current.add(element);
            }
        }
        return current;
    }

    private static void checkConstructor(Class<? extends Message> clazz) {
        try { findConstructor(clazz); }
        catch (NoSuchMethodException e) {
            initErrors.add("error: no matching constructor found for " + clazz
            + " (see Javadoc for constructor requirements)");
        }
    }


    private static Constructor<? extends Message> findConstructor(Class<? extends Message> clazz)
    throws NoSuchMethodException {
        return (clazz.getDeclaredConstructors().length == 1)
            ? clazz.getDeclaredConstructor()
            : clazz.getDeclaredConstructor(SourceContext.class, String[].class);
    }

    private static String formatArgumentErrorDump(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("".equals(args[i])) {
                args[i] = "<empty>";
            }
            else {
                args[i] = String.format("\"%s\"", args[i]);
            }
        }
        return String.join(" ", args);
    }

    private static Message asDeserialized(Header header, String[] args)
    throws ExceptionInDeserializerException {
        try {
            Constructor<? extends Message> constructor = findConstructor(header.category().getAssociatedType());
            constructor.setAccessible(true);

            return (constructor.getParameterCount() == 2)
                ? constructor.newInstance(header.source(), args)
                : constructor.newInstance();
        }
        catch(InvocationTargetException e) {
            throw new ExceptionInDeserializerException(
                "Error during deserialization: " + e.getCause() + '\n'
                + "With arguments: " + formatArgumentErrorDump(args), e);
        }
        catch(NoSuchMethodException | IllegalAccessException | InstantiationException impossible) {
            throw new IllegalStateException(impossible);
        }
    }


    private static Header extractHeader(String input) {
        String[] fullArgs = input.split(DELIMITER_STR, -1);
        if (fullArgs.length < 2) {
            throw new IllegalArgumentException(input + " does not correspond to a valid Message");
        }
        return new Header(
            Category.valueOf(fullArgs[0]),
            SourceContext.valueOf(fullArgs[1])
        );
    }


    /**
     * Constructs a new Message based on the supplied serialized input, using
     * the corresponding Message's deserialization constructor.
     * @param input the serialized form of the complete Message.
     * @return the new Message, populated with any data the serialized input contained.
     * @throws NotAMessageException if the input does not correspond to a valid Message (i.e.
     * it does not have a valid Message header).
     * @throws ExceptionInDeserializerException if an exception is thrown in the Message's
     * deserialization constructor.
     * @throws IllegalStateException if an unexpected internal error occurs.
     */
    public static Message deserialize(String input) throws NotAMessageException, ExceptionInDeserializerException {
        String[] fullArgs = input.split(DELIMITER_STR, -1);
        Header header;
        try { 
            header = extractHeader(input);
        }
        catch (IllegalArgumentException e) {
            throw new NotAMessageException(e.getMessage(), e);
        }

        String[] args = new String[fullArgs.length-2];
        System.arraycopy(fullArgs, 2, args, 0, args.length);
   
        return asDeserialized(header, args);
    }


    public final Category getCategory() {
        return header.category();
    }


    public final SourceContext getSource() {
        return header.source();
    }

    /**
     * Returns an array of String representations of subclass data fields.
     * <p>
     * If the subclass contains no active fields
     * to serialize, an array of length 0 is returned.
     * <p>
     * It is an error for any element of the returned array to contain the {@link Message#DELIMITER}
     * character, or for any element of the returned array to be {@code null}, or for the 
     * array to be {@code null}.
     * <p>
     * Empty strings will be preserved.
     *
     * @return The array of String representations for this subclass's data fields.
     */
    protected abstract String[] serialized();


    private String buildPayload() {
        String[] args = serialized();
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null || args[i].contains(DELIMITER_STR)) {
                throw new BadMessageException(i, args[i] == null);
            }
        }
        return String.join(DELIMITER_STR, args);
    }


    /**
     * Generates a complete serialized string representation for this message, suitable
     * for network transfer.
     * @return The String representation of this Message.
     * @throws BadMessageException if the String representation of this Message is
     * erroneous
     * @implSpec This method appends the result of {@link #serialized()} to the
     * predefined header.
     */
    @Override
    public final String toString() {
        return header + buildPayload();
    }


    /**
     * Acts as a wrapper around a value, used in context-sensitive Messages
     * to allow for more readable semantics when handing contextual values,
     * while also enforcing
     * {@link ContextMismatchException} exceptions.
     * Many methods within this class change their behavior based on whether the
     * context is correct, defined as when the requiredContext within a Contextual
     * matches the SourceContext value of the enclosing Message.
     * @param <T> the type of the underlying value.
     * @apiNote Instances of this class are designed to be instantiated inline, e.g.
     * <pre>
     * {@code public final class MyMessage extends Message {
     *     private final Contextual<Integer> value = new Contextual<>(SourceContext.CLIENT);
     *     // ...
     * }}}
     * </pre>
     * Afterwards, you can insert values normally using its {@link #set(Object)} and
     * {@link #contextualSet(Supplier)} methods.
     */
    protected final class Contextual<T> {
        private final SourceContext context; 
        private T value = null;

        /**
         * Creates a new Contextual with the given Context representing
         * the context required for the value to be present.
         * @param requiredContext the context necessary for the value to be present; either CLIENT or SERVER.
         */
        public Contextual(SourceContext requiredContext) {
            if (requiredContext == SourceContext.ANY) {
                throw new IllegalArgumentException("Contextual may only contain CLIENT or SERVER values.");
            }
            this.context = Objects.requireNonNull(requiredContext);
        }

        /**
         * Unconditionally sets the value within this Contextual.
         * @param value the new value.
         */
        public void set(T value) {
            this.value = value;
        }

        /**
         * Gets the value within this Contextual only if the context is correct.
         * @return the value stored inside this Contextual.
         * @throws ContextMismatchException if the context of the enclosing Message,
         * and the required context within this Contextual, differ.
         */
        public T get() {
            if (!contextMatches()) {
                throw new ContextMismatchException();
            }
            return value;
        }

        /**
         * Sets the value within this Contextual to the result
         * of the supplying function, but only if the context is correct.
         * Otherwise, does nothing.
         * @param deserializer the method supplying the new value.
         * @apiNote This method is primarily for Deserialization Constructors,
         * as a means of lazy evaluation when the underlying value is not always present.
         */
        public void contextualSet(Supplier<T> deserializer) {
            if (contextMatches()) {
                value = deserializer.get();
            }
        }

        /**
         * Serializes the value within this Contextual, calling
         * the supplied function and returning its result
         * only if the context is correct. 
         * Otherwise, returns an empty String without calling the supplied function.
         * @param serializer the function to serialize a present value.
         * @return the result of serializer if the context is correct; otherwise an empty String.
         */
        public String serialize(Function<T, String> serializer) {
            return (contextMatches()) ? serializer.apply(value) : "";
        }

        private boolean contextMatches() {
            return context == Message.this.getSource();
        }
    }

    /**
     * Thrown when serialized forms of subclasses are erroneous.
     */
    public static class BadMessageException extends RuntimeException {
        public BadMessageException(int offendingIndex, boolean nullMode) {
            super((nullMode) 
                ? "error: Element at index " + offendingIndex + " is null"
                : "error: Element at index " + offendingIndex + " contains the prohibited delimiter character");
        }
    }

    /**
     * Thrown when a getter of a contextual value is accessed in the wrong context (e.g.
     * a field only present with a context of {@link SourceContext#CLIENT} 
     * being accessed in a Message with a context of {@link SourceContext#SERVER}).
     */
    public static class ContextMismatchException extends RuntimeException {
        public ContextMismatchException() {
            super(CONTEXT_MISMATCH);
        }
    }
    


    /**
     * Thrown when the configuration of a subclass is invalid.
     */
    public static class IllegalMessageException extends RuntimeException {
        public IllegalMessageException(String message) {
            super(message);
        }


        public IllegalMessageException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    /**
     * Catch all for general exceptions during deserialization.
     */
    public abstract static sealed class MessageDeserializationException extends Exception {
        public MessageDeserializationException(String message) {
            super(message);
        }


        public MessageDeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when an attempt is made to deserialize a string that cannot
     * be recognized as a Message.
     * @see MessageDeserializationException
     */
    public static final class NotAMessageException extends MessageDeserializationException {
        public NotAMessageException(String message) {
            super(message);
        }

        public NotAMessageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when an exception occurs in a Deserialization Constructor.
     * @see MessageDeserializationException
     */
    public static final class ExceptionInDeserializerException extends MessageDeserializationException {
        public ExceptionInDeserializerException(String message) {
            super(message);
        }

        public ExceptionInDeserializerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record Header(Category category, SourceContext source) {
        Header { Objects.requireNonNull(source); }

        @Override
        public String toString() {
            return category.name() + DELIMITER + source.name() + DELIMITER;  
        }
    }
}

