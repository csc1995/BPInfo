/*
 * Copyright 2016 Olivér Falvai
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.ofalvai.bpinfo.ui.alertlist.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import com.ofalvai.bpinfo.BpInfoApplication
import com.ofalvai.bpinfo.R

class NoticeFragment : DialogFragment() {

    companion object {

        const val KEY_NOTICE_TEXT = "notice_text"

        @JvmStatic
        fun newInstance(noticeText: String): NoticeFragment {
            return NoticeFragment().apply {
                this.noticeText = noticeText
            }
        }
    }

    private lateinit var noticeText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.getString(KEY_NOTICE_TEXT) != null) {
                noticeText = savedInstanceState.getString(KEY_NOTICE_TEXT, "")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_notice_title)
                .setMessage(Html.fromHtml(noticeText))
                .setPositiveButton(R.string.dialog_notice_positive_button) { _, _ -> dismiss() }
                .create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_NOTICE_TEXT, noticeText)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BpInfoApplication.getRefWatcher(context).watch(this)
    }
}
