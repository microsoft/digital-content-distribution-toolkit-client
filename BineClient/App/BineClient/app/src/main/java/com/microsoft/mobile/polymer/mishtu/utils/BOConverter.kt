// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.utils

import com.microsoft.mobile.polymer.mishtu.storage.entities.*
import com.msr.bine_sdk.hub.model.DownloadStatus
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BOConverter {

    enum class FormulaType(val value: String) {
        PLUS("PLUS"),
        PERCENTAGE("PERCENTAGE"),
        MULTIPLY("MULTIPLY")
    }

    enum class EventType(val string: String) {
        FIRST_SIGN_IN("CONSUMER_INCOME_FIRST_SIGNIN"),
        APP_ONCE_OPEN("CONSUMER_INCOME_APP_ONCE_OPEN"),
        CONTENT_STREAMED("CONSUMER_INCOME_STREAMED_CONTENT_ONCE_PER_CONTENTPROVIDER"),
        ON_BOARDING_RATING("CONSUMER_INCOME_ONBOARDING_RATING_SUBMITTED"),
        CONSUMER_EXPENSE_SUBSCRIPTION_REDEEM("CONSUMER_EXPENSE_SUBSCRIPTION_REDEEM"),
        CONSUMER_INCOME_ORDER_COMPLETED("CONSUMER_INCOME_ORDER_COMPLETED"),
        DOWNLOAD_COMPLETE("CONSUMER_INCOME_DOWNLOAD_MEDIA_COMPLETED");



        companion object {
            private val map = values().associateBy(EventType::string)
            fun fromValue(type: String) = map[type]
        }
    }

    companion object {
        private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        fun bnOrderToBO(order: com.msr.bine_sdk.cloud.models.Order): Order {
            return Order(order.id,
                order.userId,
                order.userName,
                order.retailerId,
                order.retailerName,
                order.orderStatus,
                order.orderCreatedDate,
                order.orderItems[0].subscription.contentProviderId,
                order.orderItems[0].subscription.id,
                order.orderItems[0].subscription.price,
                order.orderItems[0].subscription.durationDays
            )
        }

        /*fun bnOrderItemsToBOs(orderItems: List<Order.Subscription>): List<SubscriptionBO> {
            val subscriptions =  ArrayList<SubscriptionBO>()
            for (subscription in orderItems) {
                subscriptions.add(bnSubscriptionToBO(subscription))
            }
            return subscriptions
        }*/


        fun bnSubscriptionToBO(subscription: com.msr.bine_sdk.cloud.models.Order.Subscription): ActiveSubscription {
            return ActiveSubscription(
                subscription.subscription.contentProviderId,
                subscription.amountCollected,
                subscription.planStartDate,
                subscription.planEndDate,
                bnSubscriptionPackToBO(subscription.subscription))
        }

        fun bnSubscriptionPackToBO(subscription: com.msr.bine_sdk.cloud.models.SubscriptionPack): SubscriptionPack {
            return SubscriptionPack(subscription.id,
                subscription.contentProviderId,
                subscription.type,
                subscription.title ?: "",
                subscription.durationDays,
                subscription.price,
                subscription.startDate,
                subscription.endDate,
                subscription.subscriptionType,
                if(!subscription.contentIds.isNullOrEmpty()) ArrayList(subscription.contentIds) else null,
                subscription.isRedeemable,
                subscription.redemptionValue
                )
        }

         fun getContentBOFromBNContent(content: com.msr.bine_sdk.cloud.models.Content): Content {
            var name = ""
            var episode = ""
            var season = ""
            var isMovie = false
            content.hierarchy?.let {
                val items: List<String> = it.split("\\s*/\\s*".toRegex())
                if (items.isEmpty() || items.size <= 2) {
                    name = content.title
                    isMovie = true
                } else {
                    //Episode1/Season1/GameOfThrones/Eros
                    episode = items[0].replace("[^0-9]", "").lowercase()
                    season = items[1].replace("[^0-9]", "").lowercase()
                    name = items[2].lowercase()
                }
            } ?: if (content.hierarchy == null || content.hierarchy!!.isEmpty()) {
                name = content.title
                isMovie = true
            }

            var broadcastDate = Date()
            content.broadcastedBy?.broadcastRequest?.startDate?.let {
                broadcastDate = formatter.parse(it) ?: Date()
            }

            return Content(content.contentId,
                content.contentProviderId,
                content.title,
                content.shortDescription,
                content.longDescription,
                content.additionalDescription1,
                content.additionalDescription2,
                content.additionalTitle1,
                content.additionalTitle2,
                content.genre,
                content.yearOfRelease,
                content.language,
                content.durationInMts,
                "1.0",
                content.people,
                content.mediaFileName,
                content.dashUrl,
                    content.hierarchy,
                    content.isFreeContent,
                    content.isHeaderContent,
                    true,
                    content.attachments,
                    formatter.parse(content.createdDate) ?: Date(),
                    name,
                    season,
                    episode,
                    isMovie,
                    content.ageAppropriateness,
                    content.contentAdvisory,
                    content.videoTarFileSize,
                    content.audioTarFileSize,
                    null,
                    broadcastDate)
        }

        fun getContentProviderBOFromBN(contentProviderBNs: List<com.msr.bine_sdk.cloud.models.ContentProvider>): List<ContentProvider> {
            val contentProviders = arrayListOf<ContentProvider>()
            for(contentProvider in  contentProviderBNs) {
                contentProviders.add(ContentProvider(contentProvider.id,
                    contentProvider.name,
                    contentProvider.logoUrl,
                    contentProvider.isActive))
            }
            return contentProviders
        }

        fun getBNContentFromContent(content: Content): com.msr.bine_sdk.cloud.models.Content {
            return com.msr.bine_sdk.cloud.models.Content(content.contentId,
                "",
                content.contentProviderId,
                content.title,
                content.shortDescription ?: "",
                content.longDescription ?: "",
                content.additionalDescription1 ?: "",
                content.additionalDescription2 ?: "",
                content.additionalTitle1 ?: "",
                content.additionalTitle2 ?: "",
                content.genre,
                content.yearOfRelease ?: "",
                content.language,
                content.durationInMts,
                "1.0",
                content.mediaFilePath ?: "",
                content.dashUrl,
                content.free,
                content.isHeaderContent,
                true,
                content.artists,
                content.hierarchy,
                content.contentAttachments,
                formatter.format(content.createdDate).toString(),
                content.ageAppropriateness,
                content.contentAdvisory,
                content.videoTarFileSize ?: 0,
                content.audioTarFileSize ?: 0,
                null
            )
        }

        fun getContentFromContentDownload(content: ContentDownload): Content {
            return Content(content.contentId,
                content.contentProviderId,
                content.title,
                content.shortDescription ?: "",
                content.longDescription ?: "",
                content.additionalDescription1 ?: "",
                content.additionalDescription2 ?: "",
                content.additionalTitle1 ?: "",
                content.additionalTitle2 ?: "",
                content.genre,
                content.yearOfRelease,
                content.language,
                content.durationInMts,
                "1.0",
                content.artists,
                "",
                content.dashUrl,
                null,
                content.free,
                isHeaderContent = true,
                isExclusiveContent = true,
                contentAttachments = content.contentAttachments,
                createdDate = Date(),
                name = content.name,
                season = content.season,
                episode = content.episode,
                isMovie = content.isMovie,
                ageAppropriateness = content.ageAppropriateness,
                contentAdvisory = content.contentAdvisory,
                videoTarFileSize = content.videoTarFileSize ?: 0,
                audioTarFileSize = content.audioTarFileSize ?: 0,
                null,
                Date() //Adding default date as this content obj would only use display params
            )
        }

        fun getContentDownloadsFromContents(content: List<Content>): List<ContentDownload> {
            val contentDownloads = arrayListOf<ContentDownload>()
            for(con in content) {
                contentDownloads.add(getContentDownloadFromContent(con))
            }
            return contentDownloads
        }

        fun getContentDownloadFromContent(content: Content): ContentDownload {
            return ContentDownload(content.contentId,
                content.contentProviderId,
                content.title,
                null,
                DownloadStatus.NOT_DOWNLOADED.value,
                0,
                content.shortDescription,
                content.longDescription,
                content.additionalDescription1,
                content.additionalDescription2,
                content.additionalTitle1,
                content.additionalTitle2,
                content.genre,
                content.yearOfRelease,
                content.language,
                content.durationInMts,
                content.artists,
                content.dashUrl,
                content.free,
                content.contentAttachments,
                content.name,
                content.season,
                content.episode,
                content.isMovie,
                content.ageAppropriateness,
                content.contentAdvisory,
                content.videoTarFileSize ?: 0,
                content.audioTarFileSize ?: 0,
                0
            )
        }

        fun getContentDownloadFromContent(content: Content, downloads: Downloads): ContentDownload {
            return ContentDownload(content.contentId,
                content.contentProviderId,
                content.title,
                downloads.downloadUrl,
                downloads.downloadStatus,
                downloads.downloadProgress,
                content.shortDescription,
                content.longDescription,
                content.additionalDescription1,
                content.additionalDescription2,
                content.additionalTitle1,
                content.additionalTitle2,
                content.genre,
                content.yearOfRelease,
                content.language,
                content.durationInMts,
                content.artists,
                content.dashUrl,
                content.free,
                content.contentAttachments,
                content.name,
                content.season,
                content.episode,
                content.isMovie,
                content.ageAppropriateness,
                content.contentAdvisory,
                content.videoTarFileSize ?: 0,
                content.audioTarFileSize ?: 0,
                0
            )
        }

        /*fun getIncentiveEventBOFromBN(event: com.msr.bine_sdk.cloud.model.IncentiveEvent.Event, eventBOCopy: IncentiveEvent?): IncentiveEvent {
            eventBOCopy?.let {
                return IncentiveEvent(event.id,
                    event.aggregratedValue,
                    event.eventType,
                    event.eventSubType,
                    event.ruleType,
                    eventBOCopy.isScratched,
                    eventBOCopy.isSynced
                )
            }
            return IncentiveEvent(event.id,
                event.aggregratedValue,
                event.eventType,
                event.eventSubType,
                event.ruleType,
                isScratched = true,
                isSynced = true
            )
        }*/
    }
}