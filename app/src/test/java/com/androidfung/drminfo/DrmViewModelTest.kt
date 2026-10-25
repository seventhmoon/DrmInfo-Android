package com.androidfung.drminfo

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.androidfung.drminfo.data.DrmRepository
import com.androidfung.drminfo.model.DeviceInfo
import com.androidfung.drminfo.model.DisplayInfo
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.ui.DrmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DrmViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val fakeApplication = Application()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeDrmRepository(
        private val mockDeviceInfo: DeviceInfo = DeviceInfo(manufacturer = "Google", model = "Pixel"),
        private val mockSchemes: List<DrmSchemeState> = emptyList()
    ) : DrmRepository {
        var releaseAllCalled = false
            private set

        override suspend fun inspectAll(activeContext: Context?): Pair<DeviceInfo, List<DrmSchemeState>> {
            return mockDeviceInfo to mockSchemes
        }

        override fun readDisplayInfo(context: Context): DisplayInfo {
            return DisplayInfo(width = 1080, height = 2400)
        }

        override fun releaseAll() {
            releaseAllCalled = true
        }
    }

    @Test
    fun initialState_loadsFromRepository() = runTest {
        val fakeRepo = FakeDrmRepository(
            mockDeviceInfo = DeviceInfo(manufacturer = "Samsung", model = "Galaxy S24"),
            mockSchemes = emptyList()
        )

        val viewModel = DrmViewModel(fakeApplication, fakeRepo)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRefreshing)
        assertEquals("Samsung", viewModel.state.value.device.manufacturer)
        assertEquals("Galaxy S24", viewModel.state.value.device.model)
    }

    @Test
    fun refresh_updatesState() = runTest {
        val fakeRepo = FakeDrmRepository(
            mockDeviceInfo = DeviceInfo(manufacturer = "Sony", model = "Xperia")
        )

        val viewModel = DrmViewModel(fakeApplication, fakeRepo)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals("Sony", viewModel.state.value.device.manufacturer)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun updateDisplayInfo_updatesDeviceDisplayMetrics() = runTest {
        val fakeRepo = FakeDrmRepository(
            mockDeviceInfo = DeviceInfo(manufacturer = "Google", model = "Pixel 8")
        )

        val viewModel = DrmViewModel(fakeApplication, fakeRepo)
        advanceUntilIdle()

        viewModel.updateDisplayInfo(fakeApplication)
        advanceUntilIdle()

        assertEquals(1080, viewModel.state.value.device.displayWidth)
        assertEquals(2400, viewModel.state.value.device.displayHeight)
    }

    @Test
    fun testDefaultConstructorViaReflection() {
        val constructor = DrmViewModel::class.java.getConstructor(Application::class.java)
        org.junit.Assert.assertNotNull(constructor)
    }

    private class FakeAdsController(
        override var isInitialized: Boolean = false
    ) : com.androidfung.drminfo.ads.AdsController {
        var requestConsentCalled = false
        var preloadedAdUnitId: String? = null
        var trimmedLevel: Int? = null

        override fun requestConsentAndInitialize(
            activity: android.app.Activity,
            appId: String,
            adUnitId: String?
        ) {
            requestConsentCalled = true
        }

        override fun initializeIfConsentGiven(context: Context, appId: String, adUnitId: String?) {}

        override fun startPreload(adUnitId: String) {
            preloadedAdUnitId = adUnitId
        }

        override fun onTrimMemory(adUnitId: String, level: Int) {
            trimmedLevel = level
        }
    }

    @Test
    fun adsController_preloadAndStatusDelegation() = runTest {
        val fakeRepo = FakeDrmRepository()
        val fakeAds = FakeAdsController(isInitialized = true)

        val viewModel = DrmViewModel(fakeApplication, fakeRepo, fakeAds)
        advanceUntilIdle()

        org.junit.Assert.assertTrue(viewModel.isAdsInitialized())

        viewModel.preloadAd(fakeApplication)
        org.junit.Assert.assertNotNull(fakeAds.preloadedAdUnitId)
    }

    @Test
    fun provideFactory_createsViewModelWithCustomDependencies() {
        val fakeRepo = FakeDrmRepository()
        val fakeAds = FakeAdsController(isInitialized = true)
        val factory = DrmViewModel.provideFactory(fakeApplication, fakeRepo, fakeAds)
        val vm = factory.create(DrmViewModel::class.java)

        org.junit.Assert.assertNotNull(vm)
        org.junit.Assert.assertTrue(vm.isAdsInitialized())
    }
}
