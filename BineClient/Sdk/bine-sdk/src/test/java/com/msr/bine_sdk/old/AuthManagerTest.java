package com.msr.bine_sdk.old;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AuthManagerTest {

    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void testLogin() throws InterruptedException {
        /*NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);

        LoginRequest loginRequest = new LoginRequest("",
                "/auth/login",
                "9000000000",
                "");
        new AuthManager().login(networkService, loginRequest, new AuthCallback() {
            @Override
            public void onResponse(LoginResponse response) {
                Assert.assertEquals(response.getToken(), "abcdefghi");
                latch.countDown();
            }
        });
        boolean waitExp = latch.await(ASYNC_WAIT, TimeUnit.SECONDS);
        Assert.assertTrue(waitExp);*/
    }

   /* @Test
    public void testLoginValidatePending() throws InterruptedException {
        NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);

        LoginRequest loginRequest = new LoginRequest("",
                "/auth/login",
                "9000000001",
                "");
        new AuthManager().login(networkService, loginRequest, new AuthCallback() {
            @Override
            public void onResponse(LoginResponse response) {
                Assert.assertEquals(response.getDetails(), "Validation pending");
                latch.countDown();
            }
        });
        boolean waitExp = latch.await(ASYNC_WAIT, TimeUnit.SECONDS);
        Assert.assertTrue(waitExp);
    }

    @Test
    public void testLoginProceedOTP() throws InterruptedException {
        NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);

        LoginRequest loginRequest = new LoginRequest("",
                "/auth/login",
                "9000000002",
                "");
        new AuthManager().login(networkService, loginRequest, new AuthCallback() {
            @Override
            public void onResponse(LoginResponse response) {
                Assert.assertEquals(response.getDetails(), "OTP sent. Please validate the same.");
                Assert.assertTrue(response.getStatus());
                latch.countDown();
            }
        });
        boolean waitExp = latch.await(ASYNC_WAIT, TimeUnit.SECONDS);
        Assert.assertTrue(waitExp);
    }

    @Test
    public void testOTPExpired() throws InterruptedException {
        NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);

        LoginRequest loginRequest = new LoginRequest("",
                "/validate",
                "9000000002",
                "123456");
        new AuthManager().verify(networkService, loginRequest, new AuthCallback() {
            @Override
            public void onResponse(LoginResponse response) {
                Assert.assertEquals(response.getDetails(), "OTP expired.Generated a new OTP");
                latch.countDown();
            }
        });
        boolean waitExp = latch.await(ASYNC_WAIT, TimeUnit.SECONDS);
        Assert.assertTrue(waitExp);
    }

    @Test
    public void testOTPInvalid() throws InterruptedException {
        NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);

        LoginRequest loginRequest = new LoginRequest("",
                "/validate",
                "9000000002",
                "123458");
        new AuthManager().verify(networkService, loginRequest, new AuthCallback() {
            @Override
            public void onResponse(LoginResponse response) {
                Assert.assertEquals(response.getDetails(), "Incorrect Credentials provided");
                latch.countDown();
            }
        });
        boolean waitExp = latch.await(ASYNC_WAIT, TimeUnit.SECONDS);
        Assert.assertTrue(waitExp);
    }

    @Test
    public void testOTPSuccess() throws InterruptedException {
        NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);

        LoginRequest loginRequest = new LoginRequest("",
                "/validate",
                "9000000002",
                "123457");
        new AuthManager().verify(networkService, loginRequest, new AuthCallback() {
            @Override
            public void onResponse(LoginResponse response) {
                Assert.assertEquals(response.getToken(), "abcdefghi");
                latch.countDown();
            }
        });
        boolean waitExp = latch.await(ASYNC_WAIT, TimeUnit.SECONDS);
        Assert.assertTrue(waitExp);
    }*/
}
