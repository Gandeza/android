package mega.privacy.android.app.presentation.chat.groupInfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mega.privacy.android.app.components.ChatManagement
import mega.privacy.android.app.contacts.usecase.GetChatRoomUseCase
import mega.privacy.android.app.objects.PasscodeManagement
import mega.privacy.android.app.usecase.chat.SetChatVideoInDeviceUseCase
import mega.privacy.android.data.gateway.api.MegaChatApiGateway
import mega.privacy.android.domain.usecase.SetOpenInvite
import mega.privacy.android.domain.usecase.chat.BroadcastChatArchivedUseCase
import mega.privacy.android.domain.usecase.chat.BroadcastLeaveChatUseCase
import mega.privacy.android.domain.usecase.chat.EndCallUseCase
import mega.privacy.android.domain.usecase.meeting.SendStatisticsMeetingsUseCase
import mega.privacy.android.domain.usecase.meeting.StartChatCall
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.domain.usecase.setting.MonitorUpdatePushNotificationSettingsUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GroupChatInfoViewModelTest {

    private lateinit var underTest: GroupChatInfoViewModel

    private val setOpenInvite: SetOpenInvite = mock()
    private val monitorConnectivityUseCase: MonitorConnectivityUseCase = mock()
    private val startChatCall: StartChatCall = mock()
    private val getChatRoomUseCase: GetChatRoomUseCase = mock()
    private val passcodeManagement: PasscodeManagement = mock()
    private val chatApiGateway: MegaChatApiGateway = mock()
    private val setChatVideoInDeviceUseCase: SetChatVideoInDeviceUseCase = mock()
    private val chatManagement: ChatManagement = mock()
    private val endCallUseCase: EndCallUseCase = mock()
    private val sendStatisticsMeetingsUseCase: SendStatisticsMeetingsUseCase = mock()
    private val monitorUpdatePushNotificationSettingsUseCase: MonitorUpdatePushNotificationSettingsUseCase =
        mock()
    private val broadcastChatArchivedUseCase: BroadcastChatArchivedUseCase = mock()
    private val broadcastLeaveChatUseCase: BroadcastLeaveChatUseCase = mock()

    private val connectivityFlow = MutableSharedFlow<Boolean>()
    private val updatePushNotificationSettings = MutableSharedFlow<Boolean>()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        initializeStubbing()
        initializeViewModel()
    }

    private fun initializeStubbing() {
        whenever(monitorConnectivityUseCase()).thenReturn(connectivityFlow)
        whenever(monitorUpdatePushNotificationSettingsUseCase()).thenReturn(
            updatePushNotificationSettings
        )
    }

    private fun initializeViewModel() {
        underTest = GroupChatInfoViewModel(
            setOpenInvite = setOpenInvite,
            monitorConnectivityUseCase = monitorConnectivityUseCase,
            startChatCall = startChatCall,
            getChatRoomUseCase = getChatRoomUseCase,
            passcodeManagement = passcodeManagement,
            chatApiGateway = chatApiGateway,
            setChatVideoInDeviceUseCase = setChatVideoInDeviceUseCase,
            chatManagement = chatManagement,
            endCallUseCase = endCallUseCase,
            sendStatisticsMeetingsUseCase = sendStatisticsMeetingsUseCase,
            monitorUpdatePushNotificationSettingsUseCase = monitorUpdatePushNotificationSettingsUseCase,
            broadcastChatArchivedUseCase = broadcastChatArchivedUseCase,
            broadcastLeaveChatUseCase = broadcastLeaveChatUseCase
        )
    }

    @Test
    fun `test that the end call use case is executed, and the meeting's statistics are sent when the user ends the call for all`() =
        runTest {
            // When
            underTest.endCallForAll()

            // Then
            verify(endCallUseCase).invoke(underTest.state.value.chatId)
            verify(sendStatisticsMeetingsUseCase).invoke(any())
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        reset(
            setOpenInvite,
            monitorConnectivityUseCase,
            startChatCall,
            getChatRoomUseCase,
            passcodeManagement,
            chatApiGateway,
            setChatVideoInDeviceUseCase,
            chatManagement,
            endCallUseCase,
            sendStatisticsMeetingsUseCase,
            monitorUpdatePushNotificationSettingsUseCase,
            broadcastChatArchivedUseCase,
            broadcastLeaveChatUseCase
        )
    }
}
