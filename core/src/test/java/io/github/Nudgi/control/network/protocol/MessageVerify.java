package io.github.Nudgi.control.network.protocol;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
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

public class MessageVerify {
    @BeforeAll
    static void setup() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = -1;
        new HeadlessApplication(new ApplicationAdapter() {}, config);

        Gdx.gl = mock();
        Gdx.gl20 = mock();
    } 

    @SuppressWarnings("unchecked")
    private <T> List<Class<? extends T>> typedPermittedSubclassesOf(Class<T> clazz) {
        return Arrays.asList((Class<? extends T>[])clazz.getPermittedSubclasses());
    }

    // inheritance hierarchy checks for final/sealed modifiers
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
            fail(msg);
        }
    } 

    @Test 
    void verifyMessageInvariants() throws Exception {
        final Set<String> initErrors = new HashSet<>();
        for (Class<? extends Message> clazz: getConcreteSubclasses(initErrors)) {
            checkConstructor(clazz, initErrors);
        }
        checkErrors(initErrors);
    }
}
