package com.rodrigonovoa.readlog.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConnectivityRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var repository: ConnectivityRepositoryImpl

    @Before
    fun setup() {
        context = mockk()
        connectivityManager = mockk()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        repository = ConnectivityRepositoryImpl(context)
    }

    @Test
    fun `isOnline returns true when active network has internet capability`() {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        assertTrue(repository.isOnline())
    }

    @Test
    fun `isOnline returns false when there is no active network`() {
        every { connectivityManager.activeNetwork } returns null

        assertFalse(repository.isOnline())
    }

    @Test
    fun `isOnline returns false when active network has no capabilities info`() {
        val network = mockk<Network>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        assertFalse(repository.isOnline())
    }

    @Test
    fun `isOnline returns false when active network lacks internet capability`() {
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        assertFalse(repository.isOnline())
    }
}
