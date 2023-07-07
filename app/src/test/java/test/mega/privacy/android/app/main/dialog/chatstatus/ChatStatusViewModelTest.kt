package test.mega.privacy.android.app.main.dialog.chatstatus

import app.cash.turbine.test
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mega.privacy.android.app.main.dialog.chatstatus.ChatStatusViewModel
import mega.privacy.android.domain.entity.contacts.UserStatus
import mega.privacy.android.domain.usecase.chat.GetCurrentUserStatusUseCase
import mega.privacy.android.domain.usecase.chat.SetCurrentUserStatusUseCase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ChatStatusViewModelTest {
    private lateinit var underTest: ChatStatusViewModel
    private val getCurrentUserStatusUseCase: GetCurrentUserStatusUseCase = mock()
    private val setCurrentUserStatusUseCase: SetCurrentUserStatusUseCase = mock()

    @BeforeAll
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterAll
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @BeforeEach
    fun resetMocks() {
        reset(
            getCurrentUserStatusUseCase,
            setCurrentUserStatusUseCase,
        )
    }

    private fun initTestClass() {
        underTest = ChatStatusViewModel(
            getCurrentUserStatusUseCase = getCurrentUserStatusUseCase,
            setCurrentUserStatusUseCase = setCurrentUserStatusUseCase
        )
    }

    @ParameterizedTest(name = "test that setUserStatus invoke {0} and shouldDismiss getting update")
    @EnumSource(UserStatus::class)
    fun `test that setUserStatus invoke correctly and shouldDismiss getting update`(status: UserStatus) =
        runTest {
            initTestClass()
            underTest.state.test {
                Truth.assertThat(awaitItem().shouldDismiss).isFalse()
            }
            underTest.setUserStatus(status)
            verify(setCurrentUserStatusUseCase).invoke(status)
            underTest.state.test {
                Truth.assertThat(awaitItem().shouldDismiss).isTrue()
            }
        }

    @ParameterizedTest(name = "test that status update correctly when getCurrentUserStatusUseCase returns {0}")
    @EnumSource(UserStatus::class)
    fun `test that status update correctly when getCurrentUserStatusUseCase returns`(status: UserStatus) =
        runTest {
            whenever(getCurrentUserStatusUseCase()).thenReturn(status)
            initTestClass()
            underTest.state.test {
                Truth.assertThat(awaitItem().status).isEqualTo(status)
            }
        }
}