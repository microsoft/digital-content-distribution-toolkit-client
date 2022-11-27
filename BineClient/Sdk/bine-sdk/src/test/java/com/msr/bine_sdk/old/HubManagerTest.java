package com.msr.bine_sdk.old;

import android.content.Context;

import com.msr.bine_sdk.secure.BineSharedPreference;

import static org.mockito.Mockito.mock;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 */
//@RunWith(RobolectricTestRunner.class)
public class HubManagerTest {

    //private Context context = ApplicationProvider.getApplicationContext();
    static final int ASYNC_WAIT = 10;
    final BineSharedPreference sharedPrefs = mock(BineSharedPreference.class);

   /* @Test
    public void listRootData() throws InterruptedException {
//        NetworkService networkService = new MockNetworkService();
//        final CountDownLatch latch = new CountDownLatch(1);
        *//*ContentReceiveCallback callback = new ContentReceiveCallback() {
            @Override
            public void onReceiveMetadata(List<Folder> folders) {
                //TODO: Verify response
                assertEquals("Folders received One", folders.size(), 1);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception exception) {
                //TestCase.fail(exception.getLocalizedMessage());
                exception.printStackTrace();
                latch.countDown();
            }
        };
        ContentRequest request = new ContentRequest("", "", "MSR", "root");
        new HubManager(context).getContent(networkService, getDummyHub(),request ,callback);
        latch.await(ASYNC_WAIT, TimeUnit.SECONDS);*//*
    }
*/
    /*@Test
    public void listContentData() throws InterruptedException {
        /*NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);
        ContentReceiveCallback callback = new ContentReceiveCallback() {
            @Override
            public void onReceiveMetadata(List<Folder> folders) {
                assertEquals("Folders received for ars-talks", folders.size(), 1);
                latch.countDown();
                //TODO: Verify response
            }

            @Override
            public void onFailure(Exception exception) {
                latch.countDown();
            }
        };
        ContentRequest request = new ContentRequest("", "", "MSR", "root");
        new HubManager(context).getContent(networkService, getDummyHub(),request ,callback);
        latch.await(ASYNC_WAIT, TimeUnit.SECONDS);*/
    //}*/

    /*@Test
    public void listOneFolder() throws InterruptedException {
        /*NetworkService networkService = new MockNetworkService();
        final CountDownLatch latch = new CountDownLatch(1);
        ContentReceiveCallback callback = new ContentReceiveCallback() {
            @Override
            public void onReceiveMetadata(List<Folder> folders) {
                assertEquals("Folder received on list one", folders.size(), 1);
                assertEquals(folders.get(0).ID, "ars-talks-ARS 2020 Fairness Transparency and Privacy in Digital Systems");
                latch.countDown();
                //TODO: Verify response
            }

            @Override
            public void onFailure(Exception exception) {
                exception.printStackTrace();
                latch.countDown();
            }
        };
        new HubManager(context).listOne(networkService, getDummyHub(),"MSR", "ars-talks-ARS 2020 Fairness Transparency and Privacy in Digital Systems",callback);
        latch.await(ASYNC_WAIT, TimeUnit.SECONDS);*/
//    }*/
}