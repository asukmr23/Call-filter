# Call Filter

Silences the ringer for calls from numbers **not** in your Contacts, but still
logs the call and shows you a quiet, SMS-style notification so you can check
who called and call back if you want to.

## How it works

Android has a built-in system for exactly this: `CallScreeningService`.
Once you set this app as your phone's **Call Screening app**, Android hands
every incoming call to this app *before it rings* and asks "should this call
be allowed?" This app then:

1. Looks up the incoming number against your saved Contacts.
2. **Known contact** → allows the call through — it rings completely normally.
3. **Unknown number** → tells Android to silently decline the call (no ring,
   no popup), but keeps it in your normal call log as a missed/declined call,
   and posts its own low-key notification (message/SMS notification channel,
   default priority, short sound + short vibration — not the loud ringtone).
   Tapping that notification opens the dialer with the number ready to call
   back.

No calls are actually blocked from reaching your phone — this only controls
whether they *ring*. You'll never miss a genuine call from someone you know,
and you'll always have a record of who else tried to reach you.

## Project structure

```
app/src/main/java/com/example/callfilter/
  MainActivity.kt                 – permission + role setup screen
  CallScreeningServiceImpl.kt      – the core call-screening logic
  CallResponseBuilderCompat.kt     – builds the "allow" / "silently decline" response
  ContactUtils.kt                  – looks up a number against Contacts
  NotificationHelper.kt            – posts the SMS-style notification
```

## Building it

1. Open this folder in **Android Studio** (Giraffe/Koala or newer). It will
   sync Gradle automatically (internet required for the first sync).
2. Connect your Galaxy S23 (enable Developer Options → USB debugging) or use
   an emulator running Android 10+ for basic testing (note: call screening
   can only be truly tested on a real device receiving real calls).
3. Run the app.

## One-time setup on your phone (required — do this after installing)

1. Open the app and tap **"1. Grant permissions"** — allow Contacts, Call
   Log, Phone, and Notifications when prompted.
2. Tap **"2. Set as Call Screening app"**. This opens the system role picker;
   choose **Call Filter**.
   - On Samsung/One UI, this same setting also lives at:
     **Settings → Apps → Choose default apps → Caller ID & spam apps** (or
     inside the Phone app: **Phone app settings → Caller ID and spam
     protection → Call screening apps**).
   - If Samsung's own "Caller ID & spam protection" (Smart Call) is turned
     on for the built-in Phone app, you may want to leave it as-is for spam
     detection, but make sure **Call Filter** is selected as the app that
     handles call screening decisions, or the two features can compete.
3. That's it. From now on:
   - Calls from saved contacts ring as usual.
   - Calls from anyone else are silenced and show up as a notification like:
     *"Missed call from unknown number — 09xxxxxxxxx — tap to call back."*

## Notes & limitations

- This uses Android's official `CallScreeningService` API (available since
  Android 10). It does **not** require root and does not need to be your
  default Phone/dialer app.
- Numbers that match a contact via any saved phone number format (with or
  without country code, spacing, dashes, etc.) are treated as "known" —
  it uses the same lookup Android's own Phone app uses.
- If a number is completely blank/withheld, the app treats it as "known" by
  default (doesn't silence it) since there's nothing to match against — you
  can flip this behavior in `CallScreeningServiceImpl.kt` if you'd rather
  silence anonymous calls too.
- Samsung sometimes re-prompts you to reconfirm the default call-screening
  app after major One UI updates — if calls start ringing again after a
  system update, just repeat step 2 above.
