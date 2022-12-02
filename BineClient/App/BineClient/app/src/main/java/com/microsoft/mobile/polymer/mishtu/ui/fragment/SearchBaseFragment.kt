// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.widget.ImageView
import androidx.fragment.app.Fragment

abstract class SearchBaseFragment: Fragment() {
    abstract fun getSearchMicView(): ImageView
}