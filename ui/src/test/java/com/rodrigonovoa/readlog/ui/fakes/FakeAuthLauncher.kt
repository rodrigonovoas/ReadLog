package com.rodrigonovoa.readlog.ui.fakes

import com.rodrigonovoa.readlog.domain.auth.AuthLauncher

class FakeAuthLauncher : AuthLauncher {

    var launchResult: Result<String> = Result.success("fake_id_token")

    override suspend fun launchGoogleSignIn(): Result<String> {
        return launchResult
    }
}
