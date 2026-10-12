package com.tored.bridgelauncher.ui2.shortcut

import android.app.Activity
import android.os.Bundle

/**
 * Declared only so that ShortcutManager.isRequestPinShortcutSupported() is true
 * for this launcher; Firefox's "Install" web app flow is a no-op otherwise.
 * Bridge does not render pinned shortcuts, so the request is simply cancelled.
 */
class PinShortcutActivity : Activity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        finish()
    }
}
