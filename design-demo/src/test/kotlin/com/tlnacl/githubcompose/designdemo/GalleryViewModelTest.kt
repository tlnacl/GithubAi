package com.tlnacl.githubcompose.designdemo

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {
    @BeforeEach fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test fun `selected theme is saved and restored`() {
        val handle = SavedStateHandle()
        val viewModel = GalleryViewModel(handle)
        viewModel.onAction(GalleryAction.SetDarkTheme(true))
        assertThat(viewModel.state.value.darkTheme).isTrue()
        assertThat(handle.get<Boolean>("darkTheme")).isEqualTo(true)
        val restored =
            GalleryViewModel(
                SavedStateHandle(mapOf("darkTheme" to handle.get<Boolean>("darkTheme"))),
            )
        assertThat(restored.state.value.darkTheme).isTrue()
    }

    @Test fun `sample action emits feedback without changing theme`() = runTest {
        val viewModel = GalleryViewModel(SavedStateHandle(mapOf("darkTheme" to true)))
        viewModel.events.test {
            viewModel.onAction(GalleryAction.ClickSample)
            assertThat(awaitItem()).isEqualTo(GalleryEvent.ShowFeedback)
            assertThat(viewModel.state.value.darkTheme).isTrue()
        }
    }
}
