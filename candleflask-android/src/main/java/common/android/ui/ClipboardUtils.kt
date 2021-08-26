package common.android.ui

import android.content.ClipboardManager
import android.content.Context

object ClipboardUtils {
  @JvmStatic
  fun retrieveString(context: Context): String? {
    return (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let { clipboardManager ->
      return clipboardManager.primaryClip?.getItemAt(0)?.text?.trim()?.toString()
    }
  }
}