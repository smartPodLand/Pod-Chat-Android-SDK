## Synopsis

**Fanap's POD** Chat service

# Changelog
All notable changes to this project will be documented here.

## Deprecation Methods
|Num|Deprecated Methods          | Description                                                            |
|:--|:------------------------------|:-----------------------------------------------------------------------------------|
|1|`createThread(int threadType, Invitee[] invitee, String threadTitle)` |  Create the threadVo.|
|2|`getHistory(int count, int offset, String order, long threadId)` | get the history of the specific threadVo|
|3|`getThreads(int count, int offset, ArrayList<Integer> threadIds, String threadName)`| gets the list of threadVo|
|4|`muteThread(int threadId)` | Mute the threadVo   |
|5|`unmuteThread(int threadId)` | Un Mute the threadVo  |
|6|`getThreadParticipants(int count, int offset, long threadId)`  | Gets the participant list      |
|7|`addParticipants(long threadId, List<Long> contactIds)`  |  adds participant of the group    |
|8|`removeParticipants(long threadId, List<Long> participantIds)` |  removes participant of the group    |
|9|`leaveThread(long threadId)` |  removes participant of the group  |
|10|`logOutSocket()`    | log out of the socket.  |
|11|`renameThread(long threadId, String title)` |  Rename the threadVo if you are the owner. |
|4|`muteThread(int threadId)`         | Mute the threadVo      |
|5|`unmuteThread(int threadId)`         | Un Mute the threadVo      |
|6|`getThreadParticipants(int count, int offset, long threadId)`         | Gets the participant list      |
|7|`addParticipants(long threadId, List<Long> contactIds)`         |  adds participant of the group    |
|8|`removeParticipants(long threadId, List<Long> participantIds)`         |  removes participant of the group    |
|9|`leaveThread(long threadId)`         | leave any threadVo you want     |
|11|`renameThread(long threadId, String title)`  |  Rename the threadVo if you are the owner. |
|12|`getUserInfo()`         | Get information about the current user        |
|7|`@deprecated uploadFile(Context context, Activity activity, String fileUri, Uri uri)`         | Upload file      |
|8|`@deprecated uploadImage(Context context, Activity activity, Uri fileUri)`         | Upload image      |
|5|`@Deprecated sendFileMessage(Context context, String description, long threadId, Uri fileUri, String metadata)`| Send file with message|
|2|`forwardMessage(long threadId, ArrayList<Long> messageIds)`                 | Forward the message or messages.        |
|3|`replyMessage(String messageContent, long threadId, long messageId)`         | Reply the message in the threadVo       |
|4|`editMessage(int messageId, String messageContent)`         | Edit the message      |
|6|`deleteMessage(long messageId, Boolean deleteForAll)`         | delete the message     |
|1|`@Deprecated  sendTextMessage(String textMessage, long threadId, String systemMetaData,SendTextMessageHandler handler)`| Send text message to threadVo.|
|1|`mapSearch(String searchTerm, Double latitude, Double longitude)`         | search in the map     |
|2|`mapRouting(String origin, String destination)`         | give you the direction     |



## Deprecation Methods
|Num|Deprecated Methods          | Description                                                            |
|:--|:------------------------------|:-----------------------------------------------------------------------------------|
|1|`createThread(int threadType, Invitee[] invitee, String threadTitle)` |  Create the threadVo.|
|2|`getHistory(int count, int offset, String order, long threadId)` | get the history of the specific threadVo|
|3|`getThreads(int count, int offset, ArrayList<Integer> threadIds, String threadName)`| gets the list of threadVo|
|4|`muteThread(int threadId)` | Mute the threadVo   |
|5|`unmuteThread(int threadId)` | Un Mute the threadVo  |
|6|`getThreadParticipants(int count, int offset, long threadId)`  | Gets the participant list      |
|7|`addParticipants(long threadId, List<Long> contactIds)`  |  adds participant of the group    |
|8|`removeParticipants(long threadId, List<Long> participantIds)` |  removes participant of the group    |
|9|`leaveThread(long threadId)` |  removes participant of the group  |
|10|`logOutSocket()`    | log out of the socket.  |
|11|`renameThread(long threadId, String title)` |  Rename the threadVo if you are the owner. |
|4|`muteThread(int threadId)`         | Mute the threadVo      |
|5|`unmuteThread(int threadId)`         | Un Mute the threadVo      |
|6|`getThreadParticipants(int count, int offset, long threadId)`         | Gets the participant list      |
|7|`addParticipants(long threadId, List<Long> contactIds)`         |  adds participant of the group    |
|8|`removeParticipants(long threadId, List<Long> participantIds)`         |  removes participant of the group    |
|9|`leaveThread(long threadId)`         | leave any threadVo you want     |
|11|`renameThread(long threadId, String title)`  |  Rename the threadVo if you are the owner. |
|12|`getUserInfo()`         | Get information about the current user        |
|7|`@deprecated uploadFile(Context context, Activity activity, String fileUri, Uri uri)`         | Upload file      |
|8|`@deprecated uploadImage(Context context, Activity activity, Uri fileUri)`         | Upload image      |
|5|`@Deprecated sendFileMessage(Context context, String description, long threadId, Uri fileUri, String metadata)`| Send file with message|
|2|`forwardMessage(long threadId, ArrayList<Long> messageIds)`                 | Forward the message or messages.        |
|3|`replyMessage(String messageContent, long threadId, long messageId)`         | Reply the message in the threadVo       |
|4|`editMessage(int messageId, String messageContent)`         | Edit the message      |
|6|`deleteMessage(long messageId, Boolean deleteForAll)`         | delete the message     |
|1|`@Deprecated  sendTextMessage(String textMessage, long threadId, String systemMetaData,SendTextMessageHandler handler)`| Send text message to threadVo.|
|1|`mapSearch(String searchTerm, Double latitude, Double longitude)`         | search in the map     |
|2|`mapRouting(String origin, String destination)`         | give you the direction     |


## Version [0.4.2.2] -2019-6-8
- [Added] Admin services cache (update cache on getAdminList, addAdminRoles and removeAdminRoles)
- [Added] startTyping() and stopTyping()
- [Added] getNotSeenDuration()
- [Added] clearHistory()
- [Added] onGetThreadAdmin(String, OutPutParticipant) in ChatListener
- [Added] delete multiple message ability to deleteMessage()
- Separate addAdminRoles() and removeAdminRoles()
- getAdminList() output resolved.
- searchContact() problem resolved.
- upload progress bug resolved. 


##  Version [0.4.2.1] -2019-3-5
- Added Queue Message
- Removed liveData library and Logger library in order to prevent some issue like size of the apk
- Added SetRole to Admin (Both of the Remove and Add rule to admin are have a same method )
- Added Get Admin List
- Added Signal Message like `Is_Typing` or `recording_voice`


##  Version [0.4.1.0] -2019-3-5
-  Added Message Queue to
  *  Reply message
   - send text message
   -  file message
   - reply file message
-  The methods of the UploadImageProgress's progress interface has been changed to ` default void`
- [Added]  ERROR_CODE_INVALID_FILE_URI = 6010
- [Removed] context attribute removed from `syncContact` method
- - gradle Updated
-[ Added] block method has 2 new attribute
 - [Added] unblock method has 2 new attribute , First attribute of unblock was changed to Long
 - [Deprecated]
  these fields are deprecated from `Thread` object
{
    private long lastSeenMessageId;
    private long partnerLastSeenMessageId;
    private long partnerLastDeliveredMessageId;
    private long partnerLastMessageId;
}
[Notice] You have to add `maven { url "https://s3.amazonaws.com/repo.commonsware.com" }`
to your app Module


- [Add] The responses of this listeners were changed 
onGetContacts
onGetHistory
onGetThread
onBlock
onUnBlock
onSeen
onDeliver
onSent
onNewMessage
onDeleteMessage
onThreadAddParticipant
onUpdateThreadInfo

##  Version [0.1.2.15] -2018-10-21
-   [Add]In order to receive a response by UpdateThreadInfo,
 you can use onUpdateThreadInfo listener. 
And beaware that you just see updated fields not the entire thread.
- [bug fix] `onNewmessage`
- [add] image to `inviter` model
- [add] `messageType1 to messageVo model
- Remove all `Serialized` from all of the models
- [bug fix] on `getForwardMessage`

##  Version [0.1.2.10] -2018-10-19

-   [Add] Now getHistory and getThreads have the same response model.
-   [Add] The `updateThreadInfo` function can accept title now.
-   [Add] We added `uniqueId` to [ErrorOutPut,]
-   [Add] We added instant `uniqueId` to `sendFileMessage,deleteMessage,replyMessage,forwardMessage
          seenMessage`.
-   [BugFix] Bug has been fixed in sendText message when put null in handler. 

##  Version [0.1.2.6] -2018-10-08
-   [Add] The project has been added to Maven
-   [Add] Unique id was added to Most of the functions as return. 
-   [Add] Unique id was added to Most responses.

##  Version [0.1.2.5] -09/26/2018
-   [BugFix] OnError Listener's bug has been fixed.
-   [BugFix] The bug of setting `count` parameter in `GetContact` function has been fixed.
-   [Add] Async has *ASYNC_READY* state now.
-   [Add] Add `lastMessageId` and `firstMessageId` to the *GetHistory*
-   [Replace] Replace most of the *integer* params to *long*.
-   [Removed] CHAT_READY state removed from Async and added to Chat and state changes 
     to *CHAT_READY* when response of the *getUserInfo* arrives.
-   [BugFix]  Some fields has been added to `createThread`'s response
-   [Add]  Now you can get instant unique id when you send text messages
-   [Add]  If you want to disable/hide logs for output set *isLoggable* false.
-   [Add]  If you want to disable *cache* set *cache* attribute as false.
-   [Deprecated] The `renameThread` is going to deprecated in the next version.

## Version [0.1.2.4] -2018-09-15
-   [BugFix]OnError Listener
-   [BugFix]Create ThreadVo 

## Version [0.1.2.3] -2018-09-03
-   [Add]Implement Cache for get Contact
-   [Add]Update ThreadVo Info
-   [Add]Get file
-   [Add]Get Image
-   [BugFix]You can get CHAT_READY on Live State

## Version [0.1.2.1] -2018-08-19
-   [Add]Map Routing
-   [Add]Map Search
-   [Add]Block
-   [Add]Unblock
-   [Add]GetBlockList
-   [Add]Search Contact
-   [Add]Search History

## Version [0.0.7.1] -2018-07-30

-   [Add]Delete Message
-   [Add]onThreadInfoUpdated listener
-   [Add]onLastSeenUpdated listener
-   [Add]Search in threadVos with name:
    We sdd a new param to the getThread so you can search through threadVos by their name. 
-   [Add]Remove participant
-   [Add]Add participant
-   [Add]Sync Contact listener
-   [Add]onChatState listener

## Version [0.0.7.0] -2018-07-22

-   Check Permission on SendFile and SyncContact 
-   UploadImage 
-   UploadFile 
-   Refactor SyncContact
-   Add Permission Class for request permission and check permission
-   Add FileServer param to Connect 

## Version [0.6.6.0] - 2018-07-18