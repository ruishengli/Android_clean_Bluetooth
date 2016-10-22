package com.inlook.android_clean_bluetooth.data.net.user;

import com.inlook.android_clean_bluetooth.data.entry.Account;
import com.inlook.android_clean_bluetooth.data.entry.UserEntry;

import rx.Observable;

/**
 * UserApi
 *
 * @author or
 * @since 2016/10/21.
 */

public interface UserApi {

     Observable<UserEntry> login(Account account) ;
}
