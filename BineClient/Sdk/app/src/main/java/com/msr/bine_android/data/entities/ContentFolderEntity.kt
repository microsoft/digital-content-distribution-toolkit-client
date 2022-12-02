// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ContentFolderEntity(@Embedded val content: ContentEntity,
                               @Relation(parentColumn = "id", entityColumn = "content_id")
                               val folders: List<FolderEntity>)
