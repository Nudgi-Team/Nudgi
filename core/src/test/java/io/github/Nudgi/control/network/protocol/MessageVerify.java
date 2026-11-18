package io.github.Nudgi.control.network.protocol;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import io.github.Nudgi.control.network.protocol.Message.IllegalMessageException;
import io.github.Nudgi.control.network.protocol.config.Category;
import io.github.Nudgi.control.network.protocol.config.SourceContext;

public class MessageVerify {
    @BeforeAll
    static void setup() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = -1;
        new HeadlessApplication(new ApplicationAdapter() {}, config);

        Gdx.gl = mock();
        Gdx.gl20 = mock();
    } 

    private void checkCategoryExists(Set<String> initErrors) {
        for (Class<? extends Message> clazz: getConcreteSubclasses(initErrors)) {
            try { Category.find(clazz); }
            catch (IllegalMessageException e) {
                initErrors.add(e.getMessage());
            }
        }
    }

    private void checkCategoryConcreteOneToOne(Set<String> initErrors) {
        Set<Class<? extends Message>> findings = new HashSet<>();
        for (Category cat: Category.values()) {
            Class<? extends Message> type = cat.getAssociatedType();

            if (findings.contains(type)) {
                initErrors.add("error: multiple Category definition of " + type);
            }
            if (Modifier.isAbstract(type.getModifiers())) {
                initErrors.add("error: the abstract " + type + " may not have a Category");
            }
            findings.add(type);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<Class<? extends T>> typedPermittedSubclassesOf(Class<T> clazz) {
        return Arrays.asList((Class<? extends T>[])clazz.getPermittedSubclasses());
    }

    private List<Class<? extends Message>> getConcreteSubclasses(Set<String> initErrors) {
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

    private static Constructor<? extends Message> findConstructor(Class<? extends Message> clazz)
    throws NoSuchMethodException {
        return (clazz.getDeclaredConstructors().length == 1)
            ? clazz.getDeclaredConstructor()
            : clazz.getDeclaredConstructor(SourceContext.class, String[].class);
    }

    private void checkConstructor(Class<? extends Message> clazz, Set<String> initErrors) {
        try { findConstructor(clazz); }
        catch (NoSuchMethodException e) {
            initErrors.add("error: no matching constructor found for " + clazz
            + " (see Javadoc for constructor requirements)");
        }
    }

    private void checkErrors(Set<String> initErrors) {
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

    @Test 
    void verifyMessageInvariants() {
        final Set<String> initErrors = new HashSet<>();

        // category exists
        checkCategoryExists(initErrors);
        // category exists without duplicates nor abstract classes
        checkCategoryConcreteOneToOne(initErrors);
        // constructor signatures
        for (Class<? extends Message> clazz: getConcreteSubclasses(initErrors)) {
            checkConstructor(clazz, initErrors);
        }
        // final verification
        checkErrors(initErrors);
    }
}
