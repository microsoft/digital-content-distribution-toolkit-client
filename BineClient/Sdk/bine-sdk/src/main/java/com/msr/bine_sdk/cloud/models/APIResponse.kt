// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk.cloud.models

import com.msr.bine_sdk.models.Error

sealed class APIResponse<out T : Any> {

    data class Response(val result: String?, val code: Int?, val details:String?, val error: Error?)

    data class ContentResponse(val result: List<Content>?, val continuationToken: String?, val details:String?, val error: Error?)

    data class SubscriptionResponse(val result: List<SubscriptionPack>?, val details:String?, val error: Error?)

    data class ContentProviderResponse(val result: List<ContentProvider>?, val details:String?, val error: Error?)

    data class HubResponse(val result: HubListResponse?, val details:String?, val error: Error?)

    data class TokenResponse(val result: String?, val details:String?, val error: Error?)

    data class ReferralCodeResponse(val result: String?, val code: Int?, val details:String?, val error: Error?)

    data class CreateOrderResponse(val result: String?, val code: Int?, val details:String?, val error: Error?)

    data class GetOrderResponse(val result: List<Order>?, val code: Int?, val details:String?, val error: Error?)

    data class ActiveSubscriptionResponse(val result: List<Order.Subscription>?, val code: Int?, val details:String?, val error: Error?)

    data class AssetTokenResponse(val result: String?, val code: Int?, val details:String?, val error: Error?)

    data class CreateUserResponse(val result: String?, val code: Int?, val details:String?, val error: Error?)

    data class RetailerListResponse(val result: List<RetailerDistance>?, val code: Int?, val details:String?, val error: Error?)

    data class UserDetailResponse(val result : User?, val code : Int?, val details: String?, val error: Error?)

    data class UserUpdateResponse(val result : Boolean, val code : Int?, val details: String?, val error: Error?)

    data class IncentivePlanResponse(val result : IncentivePlan?, val code : Int?, val details: String?, val error: Error?)

    data class IncentiveEventResponse(val result : IncentiveEvent?, val code : Int?, val details: String?, val error: Error?)

    data class RecordEventResponse(val result : Boolean?, val code : Int?, val details: String?, val error: Error?)

    data class FolderPathResponse(val result: com.msr.bine_sdk.hub.model.FolderPathResponse?, val code: Int?, val details:String?, val error: Error?)

    data class DataExportResponse(val success : Boolean, val result : com.msr.bine_sdk.cloud.models.DataExportResponse?, val code : Int?, val details: String?, val error: Error?)

    data class DeviceContentResponse(val result : Array<String>?, val continuationToken: String?, val code : Int?, val details: String?, val error: Error?)

    data class ContentAvailabilityResponse(val result : Array<DeviceContentDetails>?, val continuationToken: String?, val code : Int?, val details: String?, val error: Error?)
}