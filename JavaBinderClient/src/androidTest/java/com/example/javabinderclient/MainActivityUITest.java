package com.example.javabinderclient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

/**
 * UI tests for JavaBinderClient MainActivity
 * Tests the service binding and IPC communication flow
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private Context context;
    private TextChangeIdlingResource textIdlingResource;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        // Set longer timeout for service binding
        IdlingPolicies.setMasterPolicyTimeout(15, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(15, TimeUnit.SECONDS);

        // Create and register idling resource
        textIdlingResource = new TextChangeIdlingResource(10000); // 10 second timeout
        IdlingRegistry.getInstance().register(textIdlingResource);
    }

    @After
    public void tearDown() {
        if (textIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(textIdlingResource);
        }
    }

    @Test
    public void testServiceIsInstalled() {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("com.example.ndkbinderservice.MyService");
        intent.setPackage("com.example.ndkbinderservice");

        boolean serviceExists = !pm.queryIntentServices(intent, 0).isEmpty();
        assertTrue("NdkBinderService must be installed for this test", serviceExists);
    }

    @Test
    public void testInitialWaitingMessage() {
        // Verify the TextView is displayed
        onView(withId(R.id.sample_text))
                .check(matches(isDisplayed()));

        // Verify initial "Waiting" message appears
        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("Waiting to talk to IMyService"))));
    }

    @Test
    public void testServiceConnectionAndCommunication() {
        // Wait for service to connect and communication to complete
        textIdlingResource.waitForTextChange();

        // After service connection, verify the success message appears
        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("Talked to IMyService"))));

        // Verify the returned ComplexType is displayed
        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("ComplexType"))));
    }

    @Test
    public void testComplexTypeValuesInResponse() {
        // Wait for service communication to complete
        textIdlingResource.waitForTextChange();

        // Verify the ComplexType contains expected values
        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("int=2021"))));

        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("long=65535000"))));

        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("boolean=true"))));

        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("float=3.14"))));

        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("double=3.14159"))));

        onView(withId(R.id.sample_text))
                .check(matches(withText(containsString("string='Hello, World!'"))));
    }

    @Test
    public void testTextViewExistsAndVisible() {
        onView(withId(R.id.sample_text))
                .check(matches(isDisplayed()));
    }

    /**
     * Custom IdlingResource that waits for text to change from "Waiting" message
     */
    private static class TextChangeIdlingResource implements IdlingResource {
        private final long timeout;
        private final long startTime;
        private ResourceCallback callback;
        private final AtomicBoolean isIdle = new AtomicBoolean(false);

        TextChangeIdlingResource(long timeoutMillis) {
            this.timeout = timeoutMillis;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public String getName() {
            return TextChangeIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            if (isIdle.get()) {
                return true;
            }

            // Check if timeout has been reached
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeout) {
                isIdle.set(true);
                if (callback != null) {
                    callback.onTransitionToIdle();
                }
                return true;
            }

            return false;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.callback = callback;
        }

        void waitForTextChange() {
            // Give service time to connect (typically 1-3 seconds)
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            isIdle.set(true);
            if (callback != null) {
                callback.onTransitionToIdle();
            }
        }
    }
}
