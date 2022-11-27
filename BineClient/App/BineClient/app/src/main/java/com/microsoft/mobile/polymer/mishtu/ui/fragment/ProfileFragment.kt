 package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.microsoft.mobile.polymer.mishtu.BuildConfig
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentProfileBinding
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.TimestampUtils
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.storage.repositories.NotificationBadgeHelper
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.activity.*
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.*
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.NotificationViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ProfileViewModel
import com.microsoft.mobile.polymer.mishtu.utils.*
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

 @AndroidEntryPoint
class ProfileFragment : Fragment(),
    BottomSheetLanguageList.OnBottomSheetLanguageItemClickListener,
    BottomSheetOrdersList.OnBottomSheetOrdersItemClickListener {
    /* private lateinit var mViewModel: ProfilePageViewModel
     private lateinit var mProfileImageViewModel: ProfileImageViewModel*/
    //private val mCompositeSubscription = CompositeDisposable()

    private lateinit var binding: FragmentProfileBinding
    private  var currentUserName: String = ""
    //private var userId: String = ""
    //private lateinit var mProgressDialog: AlertDialog
    //private val EDIT_PROFILE_VALUES = 2

    private val viewModel by viewModels<ProfileViewModel>()
    private val notificationViewModel by activityViewModels<NotificationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile,
            container,
            false
        )

        /*mViewModel = ViewModelProviders.of(this).get(ProfilePageViewModel::class.java)
        mProfileImageViewModel = ViewModelProviders.of(this).get<ProfileImageViewModel>(ProfileImageViewModel::class.java)*/
        try {
            val pInfo: PackageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val version: String = pInfo.versionName
            if (!TextUtils.isEmpty(version)) {
                binding.lblVersionName.text = resources.getString(R.string.bn_version, version)
            }

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }


        try {
            val date = TimestampUtils.getDateFromUTC(BNConstants.CUT_OFF_DATE)
            val dateString = SimpleDateFormat(BNConstants.DATE_FORMAT_dd_MM_YY, Locale.getDefault()).format(date)
            binding.lblCutOffDate.text = String.format(getString(R.string.app_cutoff_date), dateString)
        }
        catch (e: Exception) {binding.lblCutOffDate.visibility = View.GONE}

        var count = 0
        binding.lblCutOffDate.setOnClickListener {
            count++
            if (count == 3) {
                val aboutUsBottomSheet = BottomSheetSettings()
                aboutUsBottomSheet.show(childFragmentManager, aboutUsBottomSheet.tag)
            }
            else if (count == 5) {
                Toast.makeText(
                    requireContext(),
                    AppSignatureHelper(requireContext()).appSignatures[0],
                    Toast.LENGTH_LONG
                ).show()
                count = 0
            }
        }

        /*if (!UserHelper.getSelfUserId(EndpointId.KAIZALA).isNullOrEmpty()) {
            userId = UserHelper.getSelfUserId(EndpointId.KAIZALA)
            mViewModel.initViewModel(userId, null, null, null, false, false)
        }*/

        setReferralCode()


//        binding.vm = mViewModel

        /*Glide.with(requireContext())
                .load(mViewModel.user.get()?.PictureServerUrl)
                .circleCrop()
                .into(binding.profileImage)

        binding.switchDarkMode.isChecked = CommonUtils.isDarkThemeEnabled()*/

        binding.saveUsername.visibility = View.INVISIBLE
        fetchSelectedLang()

        binding.profileImageBackground.setOnClickListener {
            showProfileImagePicker()
        }

        binding.userName.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                currentUserName = binding.userName.text.toString()
                binding.saveUsername.visibility = View.VISIBLE
            }
        }

        binding.switchDarkMode.setOnClickListener {
            //AppSettings.set(getString(R.string.settings_key_enable_dark_theme), binding.switchDarkMode.isChecked)
            /* val payload: MutableMap<String, String> = HashMap()
             payload[TelemetryConstants.RESULT] = binding.switchDarkMode.isChecked.toString()
             TelemetryWrapper.recordEvent(TelemetryWrapper.UI_MARKERS.DARK_THEME_ENABLED, payload)*/
            //Toast.makeText(this@BnProfileFragment.activity, getString(R.string.app_swipe_kill_message), Toast.LENGTH_SHORT).show()
            activity?.onBackPressed()
            activity?.startActivity(Intent(requireContext(), MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }

        binding.rateUs.setOnClickListener {
            BottomSheetRateOnPlayStore().show(childFragmentManager,"tag")
        }
        binding.aboutUs.setOnClickListener {
            aboutApp()
        }
        binding.accountSettings.setOnClickListener {
            accountSettingsBottomSheet()
        }

        binding.shareFriends.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${getString(R.string.share_text)}${BuildConfig.APPLICATION_ID}"
                )
            sendIntent.type = "text/plain"
            requireActivity().startActivity(Intent.createChooser(sendIntent, null))

            val params = java.util.HashMap<String, String>()
            params["ShareType"] = AnalyticsLogger.AppLink
            AnalyticsLogger.getInstance().logEvent(Event.SHARE, params)
        }

        binding.orderHistory.setOnClickListener {
            activity?.let {
                val bottomSheetFragment =
                    BottomSheetOrdersList()
                bottomSheetFragment.setListener(this)
                bottomSheetFragment.show(
                    it.supportFragmentManager,
                    bottomSheetFragment.tag
                )
            }
        }

        binding.helpAndSupport.setOnClickListener {
            /*activity?.let {
                val bottomSheetFragment =
                    BottomSheetHelpSupport()
                bottomSheetFragment.show(
                    it.supportFragmentManager,
                    bottomSheetFragment.tag
                )
            }*/
            showHelpAndSupportDialog()
        }


        binding.referralCode.setOnClickListener {
            if(SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_REFERRAL_CODE).isNullOrEmpty() ) {
                activity?.let {
                    val bottomSheetFragment =
                        BottomSheetReferralCodeHome("", object : OnDismissCallback {
                            override fun onDismiss() {
                                setReferralCode()
                            }
                        })
                    //bottomSheetFragment.setListener(this)
                    bottomSheetFragment.show(
                        it.supportFragmentManager,
                        bottomSheetFragment.tag
                    )
                }
            }
        }

        binding.lblLanguage.setOnClickListener {
            activity?.let {
                val bottomSheetFragment =
                    BottomSheetLanguageList()
                bottomSheetFragment.setListener(this)
                bottomSheetFragment.show(
                    it.supportFragmentManager,
                    bottomSheetFragment.tag
                )
            }
        }

        binding.subscription.setOnClickListener {
            val subscriptionActivity = Intent(requireContext(),
                SubscriptionActivity::class.java)
            startActivity(subscriptionActivity)
            notificationViewModel.clearNotification(NotificationBadgeHelper.BadgeType.PACK_EXPIRED)
        }

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }

        binding.editIcon.setOnClickListener {
            binding.userName.requestFocus()
            binding.userName.isFocusableInTouchMode = true
            showSoftKeyboard()
        }

        binding.saveUsername.setOnClickListener{
            validateAndSaveUserName()
        }

        binding.lblLocationRefresh.setOnClickListener {
            (requireActivity() as? HubScanBaseActivity)?.startHubScanWithCurrentLocation(false)
        }

        observeData()
        viewModel.getUserDetails()

        setProfileImage()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setBadge()
    }
    fun exportData(){
        observeDataForExportData()
        viewModel.downloadUserDataState("")
    }

    private fun aboutApp() {
         val aboutUsBottomSheet = BottomSheetAboutUs()
         aboutUsBottomSheet.show(childFragmentManager, aboutUsBottomSheet.tag)
    }

    private fun accountSettingsBottomSheet() {
         val accountSettingsBottomSheet = BottomSheetAccountSettings()
        accountSettingsBottomSheet.show(childFragmentManager, accountSettingsBottomSheet.tag)

        binding.accountSettingsBadge.visibility = View.GONE
        notificationViewModel.clearNotification(NotificationBadgeHelper.BadgeType.EXPORT_DATA_READY)
    }


    private fun showSoftKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.userName, InputMethodManager.SHOW_FORCED)
    }

    private fun validateAndSaveUserName()  {
        val newUserName = binding.userName.text.toString().trim()
        if(newUserName.isEmpty()){
            binding.userName.setText(currentUserName)
            Toast.makeText(activity, getString(R.string.userName_is_empty), Toast.LENGTH_SHORT ).show()
        }
        else if(newUserName == currentUserName){
            // do nothing
        }
        else{
            viewModel.updateUser(newUserName)
        }
        closeSoftKeyBoard()
    }

    private fun closeSoftKeyBoard() {
        binding.saveUsername.visibility = View.INVISIBLE
        binding.userName.clearFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }


    private fun observeData() {
        viewModel.userDetails.observe(viewLifecycleOwner,{
            binding.userName.setText(it.name)
            binding.userMobile.text = it.phoneNumber

            it.referralInfo?.let { referralInfo ->
                if(!referralInfo.retailerReferralCode.isNullOrEmpty()) {
                    SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_REFERRAL_CODE,
                        referralInfo.retailerReferralCode!!)
                    setReferralCode()
                }
            }
        })
        viewModel.error.observe(viewLifecycleOwner,{
            Log.d("datahere1", it.toString())
            // binding.userMobile.setText(it.toString())
        })

        viewModel.userUpdateResponse.observe(viewLifecycleOwner, {
            if(it.first){
                Toast.makeText(activity, getString(R.string.profile_name_updated),Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(activity, it.second.toString(),Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun observeDataForExportData(){
        viewModel.loading.observe(this, {
            if (it) (requireActivity() as BaseActivity).showProgress()
            else (requireActivity() as BaseActivity).hideProgress()
        })

        viewModel.exportDataNewRequest.observe(viewLifecycleOwner,{
            if(it) {
                downloadDataBottomSheet()
            }
        })
        viewModel.exportDataRequestSubmitted.observe(viewLifecycleOwner,{
            if(it){
                requestSubmittedBottomSheet()
            }
        })
        viewModel.exportDataReadyToDownload.observe(viewLifecycleOwner,{
            if(it.first != null && it.second != null) {
                Log.d("logggd",it.first+"____"+it.second)
                exportDataBottomSheet(it.first, it.second)
            }
        })

        viewModel.exportDataNewRequestResponse.observe(viewLifecycleOwner, {
            if (it) {
                requestSubmittedBottomSheet()
            }
        })
        viewModel.error.observe(viewLifecycleOwner,{
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
        })
    }

    private fun setReferralCode() {
        val referralCode = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_REFERRAL_CODE)
        if (!TextUtils.isEmpty(referralCode)) {
            binding.referralCode.isClickable = false
            binding.referralCode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bn_ic_referral_code, 0, 0, 0)
            binding.referralCode.setOnClickListener(null)

            //showing the used referral code
            binding.showReferralCode.text = referralCode
            binding.showReferralCode.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        }
    }

    private fun showLogoutDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    viewModel.logout(requireContext())

                    AnalyticsLogger.getInstance().logEvent(Event.LOGOUT_APP)
                    sendUserToLogin()
                    dialog.dismiss()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        builder.setMessage(resources.getString(R.string.bn_logout_title))
            .setPositiveButton(resources.getString(R.string.bn_logout), dialogClickListener)
            .setNegativeButton(resources.getString(R.string.bn_cancel), dialogClickListener).show()
    }

    private fun showHelpAndSupportDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
        val string = SpannableString(resources.getText(R.string.bn_help_support_message))
        Linkify.addLinks(string, Linkify.ALL)

        val alertDialog = builder.setTitle(resources.getString(R.string.bn_help_support_title))
            .setMessage(string)
            .setPositiveButton(R.string.cancel, null)
            .create()

        alertDialog.show()

        (alertDialog.findViewById(android.R.id.message) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
    }

    private fun sendUserToLogin() {
        val homeIntent = Intent(requireContext(), PhoneLoginActivity::class.java)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(homeIntent)
        activity?.finish()
    }

    private fun showProfileImagePicker() {
        val bottomSheetDialog = AvatarBottomSheetFragment(object : AvatarBottomSheetFragment.AvatarSelectListener{
            override fun onAvatarUpdated(index: Int) {
                setProfileImage(index)
                (requireActivity() as? MainActivity)?.setProfileTabIcon()
            }
        })
        bottomSheetDialog.show(childFragmentManager, "")
    }

    private fun fetchSelectedLang() {
        val languageEntryValue = AppUtils.getLanguageToShow()

        var langPosition = 0
        if (BNConstants.LanguagesCode.contains(languageEntryValue)) {
            langPosition = BNConstants.LanguagesCode.indexOf(languageEntryValue)
        }
        val lang = BNConstants.Languages[langPosition].split(":")
        binding.appLanguage.text = lang[0]
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }

    override fun onBottomSheetLanguageClicked(code: String) {
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_LANGUAGE, code)
        AnalyticsLogger.getInstance().logLanguageSelected()
        activity?.startActivity(Intent(requireContext(), MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

    }

    override fun onBottomSheetOrderItemClicked(order: com.msr.bine_sdk.cloud.models.Order) {
        activity?.let {
            val bottomSheetFragment =
                BottomSheetOrdersDetails()
            bottomSheetFragment.setOrder(order)
            bottomSheetFragment.show(
                it.supportFragmentManager,
                bottomSheetFragment.tag
            )
        }
    }

    private fun downloadDataBottomSheet() {
        BottomSheetGenericMessage(
            R.drawable.ic_download,
            getString(R.string.download_your_data),
            getString(R.string.download_dialog_text), true,
            getString(R.string.fw_download),
            null,
            { placeNewRequest() },
            null,null
        ).show(childFragmentManager,"tag")
    }

    private fun requestSubmittedBottomSheet() {
        BottomSheetGenericMessage(
            R.drawable.ic_download,
            getString(R.string.request_submitted),
            getString(R.string.download_data_response,21),
            true,
            getString(R.string.bn_okay),
            null,
            null,
            null,null
        ).show(childFragmentManager,"tag")
    }

    private fun exportDataBottomSheet(exportedDataUrl: String?, exportedDataValidity: String?) {
        BottomSheetGenericMessage(
            R.drawable.ic_download,
            getString(R.string.export_data_ready, exportedDataValidity),
            null, true,
            getString(R.string.download_data),
            getString(R.string.place_new_request),
            { downLoadData(exportedDataUrl) },
            { placeNewRequest() },null
        ).show(childFragmentManager, "tag")
    }

    private fun downLoadData(exportedDataUrl: String?) {
        Toast.makeText(requireContext(), "Downloading data..", Toast.LENGTH_SHORT).show()
        requireActivity().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(exportedDataUrl)))
    }

    private fun placeNewRequest() {
        viewModel.createDataExportRequest("")
    }

    private fun setBadge() {
        binding.subscriptionBadge.visibility = View.GONE
        binding.accountSettingsBadge.visibility = View.GONE
        notificationViewModel.getUnreadNotifications(NotificationBadgeHelper.BadgeType.PACK_EXPIRED)?.let {
            binding.subscriptionBadge.visibility = View.VISIBLE
        }
        notificationViewModel.getUnreadNotifications(NotificationBadgeHelper.BadgeType.EXPORT_DATA_READY)?.let {
            binding.accountSettingsBadge.visibility = View.VISIBLE
        }
    }

     private fun setProfileImage() {
         val indexString = SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_AVATAR_INDEX)
         if (!indexString.isNullOrEmpty()) {
             setProfileImage(indexString.toInt())
         }
     }

     private fun setProfileImage(index: Int) {
         val avatar = BNConstants.Avatars[index]
         val id: Int = requireContext().resources.getIdentifier(avatar, "drawable",
             requireContext().packageName)
         binding.profileImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), id))
     }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBadgeEventReceived(event: BadgeEvent) {
        if (event.eventType == NotificationBadgeHelper.BadgeType.EXPORT_DATA_READY) {
            binding.accountSettingsBadge.visibility = View.VISIBLE
        }
    }
}