package mega.privacy.android.app.upgradeAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.app.upgradeAccount.model.UpgradeAccountState
import mega.privacy.android.app.upgradeAccount.model.UpgradePayment
import mega.privacy.android.app.upgradeAccount.model.mapper.LocalisedSubscriptionMapper
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.livedata.SingleLiveEvent
import mega.privacy.android.domain.entity.AccountType
import mega.privacy.android.domain.usecase.billing.GetCurrentPaymentUseCase
import mega.privacy.android.domain.usecase.account.GetCurrentSubscriptionPlanUseCase
import mega.privacy.android.domain.usecase.billing.GetSubscriptionsUseCase
import mega.privacy.android.domain.usecase.billing.IsBillingAvailable
import javax.inject.Inject

/**
 * Upgrade account view model
 *
 * @param getCurrentSubscriptionPlanUseCase use case to get the current subscribed plan
 * @param getSubscriptionsUseCase use case to get the list of available subscriptions in the app
 * @param getCurrentPaymentUseCase use case to get the current payment option
 * @param isBillingAvailable use to check if billing is available
 * @param localisedSubscriptionMapper mapper to map Subscription class to LocalisedSubscription class
 *
 * @property state The current UI state
 */
@HiltViewModel
class UpgradeAccountViewModel @Inject constructor(
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val getCurrentSubscriptionPlanUseCase: GetCurrentSubscriptionPlanUseCase,
    private val getCurrentPaymentUseCase: GetCurrentPaymentUseCase,
    private val isBillingAvailable: IsBillingAvailable,
    private val localisedSubscriptionMapper: LocalisedSubscriptionMapper,
) : ViewModel() {
    private val _state = MutableStateFlow(
        UpgradeAccountState(
            listOf(),
            AccountType.FREE,
            showBillingWarning = false,
            currentPayment = UpgradePayment()
        )
    )
    val state: StateFlow<UpgradeAccountState> = _state

    private val upgradeClick = SingleLiveEvent<Int>()

    fun onUpgradeClick(): LiveData<Int> = upgradeClick

    init {
        viewModelScope.launch {
            val subscriptions = getSubscriptionsUseCase()
            val localisedSubscriptions =
                subscriptions.map { subscription -> localisedSubscriptionMapper(subscription) }
            _state.update { it.copy(subscriptionsList = localisedSubscriptions) }
        }
        viewModelScope.launch {
            val currentSubscriptionPlan = getCurrentSubscriptionPlanUseCase()
            _state.update { it.copy(currentSubscriptionPlan = currentSubscriptionPlan) }
        }
        viewModelScope.launch {
            val currentPayment = getCurrentPaymentUseCase()
            currentPayment?.let {
                _state.update {
                    it.copy(
                        showBuyNewSubscriptionDialog = false,
                        currentPayment = UpgradePayment(
                            upgradeType = Constants.INVALID_VALUE,
                            currentPayment = currentPayment
                        )
                    )
                }
            }
        }
    }

    /**
     * Check the current payment
     * @param upgradeType upgrade type
     */
    fun currentPaymentCheck(upgradeType: Int) {
        viewModelScope.launch {
            val currentPayment = getCurrentPaymentUseCase()
            currentPayment?.let {
                _state.update {
                    it.copy(
                        showBuyNewSubscriptionDialog = upgradeType != Constants.INVALID_VALUE,
                        currentPayment = UpgradePayment(
                            upgradeType = upgradeType,
                            currentPayment = currentPayment
                        )
                    )
                }
            } ?: run {
                upgradeClick.value = upgradeType
            }
        }
    }

    fun isBillingAvailable(): Boolean = isBillingAvailable.invoke()

    fun setBillingWarningVisibility(isVisible: Boolean) {
        _state.update { it.copy(showBillingWarning = isVisible) }
    }

    fun setShowBuyNewSubscriptionDialog(showBuyNewSubscriptionDialog: Boolean) {
        _state.update { it.copy(showBuyNewSubscriptionDialog = showBuyNewSubscriptionDialog) }
    }
}