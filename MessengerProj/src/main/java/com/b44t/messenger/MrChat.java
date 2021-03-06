/*******************************************************************************
 *
 *                              Delta Chat Android
 *                           (C) 2017 Björn Petersen
 *                    Contact: r10s@b44t.com, http://b44t.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see http://www.gnu.org/licenses/ .
 *
 *******************************************************************************
 *
 * File:    MrChat.java
 * Purpose: Wrap around mrchat_t
 *
 ******************************************************************************/

package com.b44t.messenger;

import android.text.TextUtils;

public class MrChat {

    public final static int      MR_CHAT_ID_DEADDROP        = 1;
    public final static int      MR_CHAT_ID_STARRED         = 5;
    public final static int      MR_CHAT_ID_ARCHIVED_LINK   = 6;
    public final static int      MR_CHAT_ID_LAST_SPECIAL    = 9;

    public MrChat(long hChat) {
        m_hChat = hChat;
    }

    @Override protected void finalize() throws Throwable {
        super.finalize();
        MrChatUnref(m_hChat);
        m_hChat = 0;
    }

    public native int    getId();
    public native boolean isGroup();
    public native int    getArchived();
    public native String getName();
    public native String getSubtitle();

    public native String getProfileImage();
    public native boolean isUnpromoted();
    public native boolean isSelfTalk();
    public native boolean isVerified();
    public native String getDraft();
    public native long getDraftTimestamp();

    private long                  m_hChat;  // must not be renamed as referenced by JNI under the name "m_hChat"
    private native static void    MrChatUnref                (long hChat);


    /* additional functions that are not 1:1 available in the backend
     **********************************************************************************************/

    public static TLRPC.Chat chatId2chat(int id)
    {
        TLRPC.Chat ret = new TLRPC.Chat();
        ret.id = id;
        return ret;
    }

    public long getCPtr() {
        return m_hChat;
    }

    public TLRPC.DraftMessage getDraftMessageObj() {
        if( getId() == 0 ) {
            return null;
        }
        TLRPC.DraftMessage ret = new TLRPC.DraftMessage();
        ret.message = getDraft();
        if( ret.message==null || ret.message.isEmpty() ) {
            return null;
        }
        ret.date = (int)getDraftTimestamp();
        ret.reply_to_msg_id = 0;
        return ret;
    }

    public void saveDraft(CharSequence message, TLRPC.Message replyToMessage) {
        if( message == null || TextUtils.isEmpty(message) ) {
            MrMailbox.setDraft(getId(), null);
        }
        else {
            MrMailbox.setDraft(getId(), message.toString());
        }
    }

    public void cleanDraft()
    {
        MrMailbox.setDraft(getId(), null);
    }

    public String getNameNAddr()
    {
        // returns name of group chats or name+email-address for normal chats
        String name = "ErrGrpNameNAddr";
        if( isGroup() ) {
            name = getName();
        }
        else {
            int contacts[] = MrMailbox.getChatContacts(getId());
            if( contacts.length==1 ) {
                name = MrMailbox.getContact(contacts[0]).getNameNAddr();
            }
        }
        return name;
    }
}
