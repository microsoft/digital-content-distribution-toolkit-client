package com.msr.bine_sdk

import com.msr.bine_sdk.base.BaseTest
import com.msr.bine_sdk.cloud.CloudManager
import com.msr.bine_sdk.cloud.models.ContentListResponse
import com.msr.bine_sdk.cloud.models.CouponResponse
import com.msr.bine_sdk.cloud.models.HubListResponse
import com.msr.bine_sdk.cloud.models.ReferralCodeVerifyResponse
import com.msr.bine_sdk.models.old.Folder
import com.msr.bine_sdk.cloud.models.Token
import com.msr.bine_sdk.network.NetworkResponse
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
class CloudManagerTest : BaseTest() {

    @Inject
    lateinit var cloudManager: CloudManager
    lateinit var unknownError: NetworkResponse.UnknownError
    lateinit var networkError: NetworkResponse.NetworkError

    @Before
    fun setUpTest() {
        super.setUp()
        unknownError = NetworkResponse.UnknownError(null)
        networkError = NetworkResponse.NetworkError(IOException())
    }

    @After
    fun setTearDown() = super.tearDown()

    fun setCloudContent() {
        val list: List<Folder> = listOf()
        val conteResponse = ContentListResponse(list, "", null)
        val networkResponse = NetworkResponse.Success(conteResponse, Headers.headersOf())
        runBlocking {
            whenever(cloudService.getContentList(Constants.TEST_MEDIA_HOUSE))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setCloudContentFailureNetworkError() {
        runBlocking {
            whenever(cloudService.getContentList(Constants.TEST_MEDIA_HOUSE))
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setCloudContentFailureUnknownError() {
        runBlocking {
            whenever(cloudService.getContentList(Constants.TEST_MEDIA_HOUSE))
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setCloudContentApiErrorFailure() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(cloudService.getContentList(Constants.TEST_MEDIA_HOUSE))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setNearestHubList() {
        val hubListResponse = HubListResponse()
        val networkResponse = NetworkResponse.Success(hubListResponse, Headers.headersOf())
        runBlocking {
            whenever(cloudService.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setNearestHubListError() {
        runBlocking {
            whenever(cloudService.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG))
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setNearestHubListNetWorkError() {
        runBlocking {
            whenever(cloudService.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG))
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setNearestHubListAPIError() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(cloudService.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setAssetToken() {
        val token = Token("yyyy", "yyyy")
        val networkResponse = NetworkResponse.Success(token, Headers.headersOf())
        runBlocking {
            whenever(cloudService.getAssetToken())
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setAssetTokenError() {
        runBlocking {
            whenever(cloudService.getAssetToken())
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setAssetTokenNetworkError() {
        runBlocking {
            whenever(cloudService.getAssetToken())
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setAssetTokenAPIError() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(cloudService.getAssetToken())
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setRefreshToken() {
        val token = Token("yyyy", "yyyy")
        val networkResponse = NetworkResponse.Success(token, Headers.headersOf())
        runBlocking {
            whenever(cloudService.refreshAssetToken())
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setRefreshTokenUnknownError() {
        runBlocking {
            whenever(cloudService.refreshAssetToken())
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setRefreshTokenNetworkError() {
        runBlocking {
            whenever(cloudService.refreshAssetToken())
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setRefreshTokenApiError() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(cloudService.refreshAssetToken())
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setGetVocher() {
        val couponResponse = CouponResponse(Constants.TEST_VOUCHER)
        val networkResponse = NetworkResponse.Success(couponResponse, Headers.headersOf())
        runBlocking {
            whenever(hubManagementService.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setGetVocherError() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(hubManagementService.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setGetVocherNetworkError() {
        runBlocking {
            whenever(hubManagementService.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun setGetVocherUnknownError() {
        runBlocking {
            whenever(hubManagementService.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun sendDownloadStats() {
        val networkResponse = NetworkResponse.Success("success", Headers.headersOf())
        runBlocking {
            whenever(hubManagementService.sendDownloadStat(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun sendDownloadStatsApiError() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(hubManagementService.sendDownloadStat(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun sendDownloadStatsNetworkError() {
        runBlocking {
            whenever(hubManagementService.sendDownloadStat(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun sendDownloadStatsUnknownError() {
        runBlocking {
            whenever(hubManagementService.sendDownloadStat(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun verifyReferralCode() {
        val referralCodeVerifyResponse = ReferralCodeVerifyResponse()
        val networkResponse = NetworkResponse.Success(referralCodeVerifyResponse, Headers.headersOf())
        runBlocking {
            whenever(hubManagementService.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }

    fun verifyReferralCodeError() {
        val networkResponse = NetworkResponse.ApiError(com.msr.bine_sdk.network.Error(false, "error failure", 2), 403)
        runBlocking {
            whenever(hubManagementService.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkResponse)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }
    fun verifyReferralCodeUnknownError() {
        runBlocking {
            whenever(hubManagementService.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(unknownError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }
    fun verifyReferralCodeNetworkError() {
        runBlocking {
            whenever(hubManagementService.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME))
                    .thenReturn(networkError)
        }
        cloudManager = CloudManager(cloudService, hubManagementService, sharedPreferences)
    }


    @Test
    fun `test fetching cloud content`() =
            runBlocking {
                setCloudContent()
                val response = cloudManager.getContent(Constants.TEST_MEDIA_HOUSE, "")
                assert(response.error == null)
            }

    @Test
    fun `test fetching cloud content Network failure`() =
            runBlocking {
                setCloudContentFailureNetworkError()
                val response = cloudManager.getContent(Constants.TEST_MEDIA_HOUSE, "")
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test fetching cloud content Api failure`() =
            runBlocking {
                setCloudContentApiErrorFailure()
                val response = cloudManager.getContent(Constants.TEST_MEDIA_HOUSE, "")
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test fetching cloud content UNKOWN failure`() =
            runBlocking {
                setCloudContentFailureUnknownError()
                val response = cloudManager.getContent(Constants.TEST_MEDIA_HOUSE, "")
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test finding hubs API success`() =
            runBlocking {
                setNearestHubList()
                val response = cloudManager.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG)
                assert(response.error == null)
            }

    @Test
    fun `test finding hubs UnKnown  failure`() =
            runBlocking {
                setNearestHubListError()
                val response = cloudManager.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test finding hubs NetWork failure`() =
            runBlocking {
                setNearestHubListNetWorkError()
                val response = cloudManager.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG)
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test finding hubs API failure`() =
            runBlocking {
                setNearestHubListAPIError()
                val response = cloudManager.getNearestHubs(Constants.TEST_RADIUS, Constants.TEST_LAT, Constants.TEST_LONG)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test get asset API success`() =
            runBlocking {
                setAssetToken()
                val response = cloudManager.getAssetToken()
                assert(response.error == null)
            }

    @Test
    fun `test get asset API failure`() =
            runBlocking {
                setAssetTokenError()
                val response = cloudManager.getAssetToken()
                assert(response.error != null)
            }

    @Test
    fun `test get asset Network failure`() =
            runBlocking {
                setAssetTokenNetworkError()
                val response = cloudManager.getAssetToken()
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test get asset Unknown error API failure`() =
            runBlocking {
                setAssetTokenAPIError()
                val response = cloudManager.getAssetToken()
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test get refresh token API success`() =
            runBlocking {
                setRefreshToken()
                val response = cloudManager.refreshAssetToken()
                assert(response.error == null)
            }

    @Test
    fun `test get refresh token API failure`() =
            runBlocking {
                setRefreshTokenUnknownError()
                val response = cloudManager.refreshAssetToken()
                assert(response.error != null)
            }

    @Test
    fun `test get refresh token Unknown failure`() =
            runBlocking {
                setRefreshTokenUnknownError()
                val response = cloudManager.refreshAssetToken()
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test get refresh token Network API failure`() =
            runBlocking {
                setRefreshTokenNetworkError()
                val response = cloudManager.refreshAssetToken()
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test get voucher API success`() =
            runBlocking {
                setGetVocher()
                val response = cloudManager.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error == null)
            }

    @Test
    fun `test get voucher  API failure`() =
            runBlocking {
                setGetVocherError()
                val response = cloudManager.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test get voucher  Network failure`() =
            runBlocking {
                setGetVocherNetworkError()
                val response = cloudManager.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error == Error.NETWORK_ERROR)
            }

    @Test
    fun `test get voucher  Unknown failure`() =
            runBlocking {
                setGetVocherUnknownError()
                val response = cloudManager.getVoucher(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error == Error.UNKNOWN_ERROR)
            }

    @Test
    fun `test sendDownloadStats API success`() =
            runBlocking {
                sendDownloadStats()
                val response = cloudManager.sendDownloadStats(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error == null)
            }

    @Test
    fun `test sendDownloadStats  API failure`() =
            runBlocking {
                sendDownloadStatsApiError()
                val response = cloudManager.sendDownloadStats(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error != null)
            }

    @Test
    fun `test sendDownloadStats Network API failure`() =
            runBlocking {
                sendDownloadStatsNetworkError()
                val response = cloudManager.sendDownloadStats(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error != null)
            }

    @Test
    fun `test sendDownloadStats  Unknown error failure`() =
            runBlocking {
                sendDownloadStatsUnknownError()
                val response = cloudManager.sendDownloadStats(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error != null)
            }

    @Test
    fun `test verifyReferralCode API success`() =
            runBlocking {
                verifyReferralCode()
                val response = cloudManager.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error == null)
            }

    @Test
    fun `test verifyReferralCode  API failure`() =
            runBlocking {
                verifyReferralCodeError()
                val response = cloudManager.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error != null)
            }

    @Test
    fun `test verifyReferralCode Network API failure`() =
            runBlocking {
                verifyReferralCodeUnknownError()
                val response = cloudManager.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error != null)
            }

    @Test
    fun `test verifyReferralCode Unknown API failure`() =
            runBlocking {
                verifyReferralCodeNetworkError()
                val response = cloudManager.verifyReferralCode(Constants.TEST_HUB_ID, Constants.TEST_HUB_NAME)
                assert(response.error != null)
            }
}