package com.fanap.podchat.persistance;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.commonsware.cwac.saferoom.SQLCipherUtils;
import com.commonsware.cwac.saferoom.SafeHelperFactory;
import com.fanap.podchat.cachemodel.CacheContact;
import com.fanap.podchat.cachemodel.CacheForwardInfo;
import com.fanap.podchat.cachemodel.CacheMessageVO;
import com.fanap.podchat.cachemodel.CacheParticipant;
import com.fanap.podchat.cachemodel.CacheParticipantRoles;
import com.fanap.podchat.cachemodel.CacheReplyInfoVO;
import com.fanap.podchat.cachemodel.CacheThreadParticipant;
import com.fanap.podchat.cachemodel.GapMessageVO;
import com.fanap.podchat.cachemodel.ThreadVo;
import com.fanap.podchat.cachemodel.queue.Failed;
import com.fanap.podchat.cachemodel.queue.Sending;
import com.fanap.podchat.cachemodel.queue.SendingQueueCache;
import com.fanap.podchat.cachemodel.queue.Uploading;
import com.fanap.podchat.cachemodel.queue.UploadingQueueCache;
import com.fanap.podchat.cachemodel.queue.WaitQueueCache;
import com.fanap.podchat.mainmodel.Contact;
import com.fanap.podchat.mainmodel.ForwardInfo;
import com.fanap.podchat.mainmodel.History;
import com.fanap.podchat.mainmodel.MessageVO;
import com.fanap.podchat.mainmodel.Participant;
import com.fanap.podchat.mainmodel.Thread;
import com.fanap.podchat.mainmodel.UserInfo;
import com.fanap.podchat.model.ConversationSummery;
import com.fanap.podchat.model.ReplyInfoVO;
import com.fanap.podchat.persistance.dao.MessageDao;
import com.fanap.podchat.persistance.dao.MessageQueueDao;
import com.fanap.podchat.util.Callback;
import com.fanap.podchat.util.OnWorkDone;
import com.fanap.podchat.util.Util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class MessageDatabaseHelper {

    private MessageDao messageDao;
    private MessageQueueDao messageQueueDao;
    private Context context;
    private AppDatabase appDatabase;

    @Inject
    public MessageDatabaseHelper(MessageDao messageDao, MessageQueueDao messageQueueDao, Context context, AppDatabase appDatabase) {
        this.messageQueueDao = messageQueueDao;
        this.messageDao = messageDao;
        this.context = context;
        this.appDatabase = appDatabase;
    }

    public void getDatabaseState(String dbName) {
        SQLCipherUtils.getDatabaseState(context, dbName);
    }

    public void decryptDatabase(@NonNull File file, char[] passphrase) throws IOException {
        SQLCipherUtils.decrypt(context, file, passphrase);
    }

    public void reEcryptKey(char[] passphrase) {
        SupportSQLiteOpenHelper supportSQLiteOH = appDatabase.getOpenHelper();
        supportSQLiteOH.getWritableDatabase();
        SupportSQLiteDatabase supportSQLiteDatabase = supportSQLiteOH.getReadableDatabase();
        SafeHelperFactory.rekey(supportSQLiteDatabase, passphrase);
    }

    public boolean isDbOpen() {
        return appDatabase.isOpen();
    }

    /**
     * Cache history
     */
    public void saveHistory(@NonNull List<CacheMessageVO> messageVOS, long threadId) {

        for (CacheMessageVO messageVO : messageVOS) {

            messageVO.setThreadVoId(threadId);

            long time = messageVO.getTime();
            long timeNanos = messageVO.getTimeNanos();
            long pow = (long) Math.pow(10, 9);
            long timestamp = ((time / 1000) * pow) + timeNanos;
            messageVO.setTimeStamp(timestamp);

            if (messageVO.getParticipant() != null) {
                messageVO.setParticipantId(messageVO.getParticipant().getId());
                messageDao.insertParticipant(messageVO.getParticipant());
            }

            if (messageVO.getConversation() != null) {
                messageVO.setConversationId(messageVO.getConversation().getId());
            }

            if (messageVO.getForwardInfo() != null) {
                messageVO.setForwardInfoId(messageVO.getForwardInfo().getId());
                messageDao.insertForwardInfo(messageVO.getForwardInfo());
                if (messageVO.getForwardInfo().getParticipant() != null) {
                    messageVO.getForwardInfo().setParticipantId(messageVO.getForwardInfo().getParticipant().getId());
                    messageDao.insertParticipant(messageVO.getForwardInfo().getParticipant());
                }
            }

            if (messageVO.getReplyInfoVO() != null) {
                messageVO.setReplyInfoVOId(messageVO.getReplyInfoVO().getId());
                messageDao.insertReplyInfoVO(messageVO.getReplyInfoVO());
                if (messageVO.getReplyInfoVO().getParticipant() != null) {
                    messageVO.getReplyInfoVO().setParticipantId(messageVO.getReplyInfoVO().getParticipant().getId());
                    messageDao.insertParticipant(messageVO.getReplyInfoVO().getParticipant());
                }
            }
        }
        messageDao.insertHistories(messageVOS);
    }

    public void saveMessage(@NonNull CacheMessageVO cacheMessageVO, long threadId) {

        cacheMessageVO.setThreadVoId(threadId);

        try {
            long time = cacheMessageVO.getTime();
            long timeNanos = cacheMessageVO.getTimeNanos();
            long pow = (long) Math.pow(10, 9);
            long timestamp = ((time / 1000) * pow) + timeNanos;

            cacheMessageVO.setTimeStamp(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CHAT_SDK", e.getMessage());
        }


        if (cacheMessageVO.getParticipant() != null) {
            cacheMessageVO.setParticipantId(cacheMessageVO.getParticipant().getId());
            messageDao.insertParticipant(cacheMessageVO.getParticipant());
        }

        if (cacheMessageVO.getForwardInfo() != null) {
            cacheMessageVO.setForwardInfoId(cacheMessageVO.getForwardInfo().getId());
            messageDao.insertForwardInfo(cacheMessageVO.getForwardInfo());
            if (cacheMessageVO.getForwardInfo().getParticipant() != null) {
                cacheMessageVO.getForwardInfo().setParticipantId(cacheMessageVO.getForwardInfo().getParticipant().getId());
                messageDao.insertParticipant(cacheMessageVO.getForwardInfo().getParticipant());
            }
        }

        if (cacheMessageVO.getReplyInfoVO() != null) {
            cacheMessageVO.setReplyInfoVOId(cacheMessageVO.getReplyInfoVO().getId());
            messageDao.insertReplyInfoVO(cacheMessageVO.getReplyInfoVO());
            if (cacheMessageVO.getReplyInfoVO().getParticipant() != null) {
                cacheMessageVO.getReplyInfoVO().setParticipantId(cacheMessageVO.getReplyInfoVO().getParticipant().getId());
                messageDao.insertParticipant(cacheMessageVO.getReplyInfoVO().getParticipant());
            }
        }

        messageDao.insertMessage(cacheMessageVO);
    }


    public void updateMessage(MessageVO lastMessage, long threadId) {

        CacheMessageVO cacheMessageVO = new CacheMessageVO(lastMessage);
        cacheMessageVO.setThreadVoId(threadId);


        if (cacheMessageVO.getParticipant() != null) {
            cacheMessageVO.setParticipantId(cacheMessageVO.getParticipant().getId());
            messageDao.insertParticipant(cacheMessageVO.getParticipant());
        }

        if (cacheMessageVO.getForwardInfo() != null) {
            cacheMessageVO.setForwardInfoId(cacheMessageVO.getForwardInfo().getId());
            messageDao.insertForwardInfo(cacheMessageVO.getForwardInfo());
            if (cacheMessageVO.getForwardInfo().getParticipant() != null) {
                cacheMessageVO.getForwardInfo().setParticipantId(cacheMessageVO.getForwardInfo().getParticipant().getId());
                messageDao.insertParticipant(cacheMessageVO.getForwardInfo().getParticipant());
            }
        }

        if (cacheMessageVO.getReplyInfoVO() != null) {
            cacheMessageVO.setReplyInfoVOId(cacheMessageVO.getReplyInfoVO().getId());
            messageDao.insertReplyInfoVO(cacheMessageVO.getReplyInfoVO());
            if (cacheMessageVO.getReplyInfoVO().getParticipant() != null) {
                cacheMessageVO.getReplyInfoVO().setParticipantId(cacheMessageVO.getReplyInfoVO().getParticipant().getId());
                messageDao.insertParticipant(cacheMessageVO.getReplyInfoVO().getParticipant());
            }
        }

        messageDao.updateMessage(cacheMessageVO);

    }

    /*
     *
     * Failed Queue
     *
     * */

    public void insertWaitMessageQueue(@NonNull SendingQueueCache sendingQueue) {
        WaitQueueCache waitMessageQueue = new WaitQueueCache();

        waitMessageQueue.setUniqueId(sendingQueue.getUniqueId());
        waitMessageQueue.setId(sendingQueue.getId());
        waitMessageQueue.setAsyncContent(sendingQueue.getAsyncContent());
        waitMessageQueue.setMessage(sendingQueue.getMessage());
        waitMessageQueue.setThreadId(sendingQueue.getThreadId());
        waitMessageQueue.setMessageType(sendingQueue.getMessageType());
        waitMessageQueue.setSystemMetadata(sendingQueue.getSystemMetadata());
        waitMessageQueue.setMetadata(sendingQueue.getMetadata());

        messageQueueDao.insertWaitMessageQueue(waitMessageQueue);
    }

    public void deleteWaitQueueMsgs(String uniqueId) {
        messageQueueDao.deleteWaitMessageQueue(uniqueId);
    }

    public List<WaitQueueCache> getAllWaitQueueMsg() {
        return messageQueueDao.getAllWaitQueueMsg();
    }

    @Nullable
    public SendingQueueCache getWaitMessageQueue(String uniqueId) {
        SendingQueueCache sendingQueueCache = new SendingQueueCache();

//        messageQueueDao.getWaitQueueMsgByUniqueId(uniqueId);
//
//        sendingQueueCache.
//
        return null;
    }

    @NonNull
    public List<Failed> getAllWaitQueueCacheByThreadId(long threadId) {
        List<Failed> listQueues = new ArrayList<>();
        List<WaitQueueCache> listCaches = messageQueueDao.getWaitQueueMsgByThreadId(threadId);
        for (WaitQueueCache queueCache : listCaches) {
            Failed failed = new Failed();

            MessageVO messageVO = new MessageVO();

            messageVO.setId(queueCache.getId());
            messageVO.setMessage(queueCache.getMessage());
            messageVO.setMessageType(queueCache.getMessageType());
            messageVO.setMetadata(queueCache.getMetadata());
            messageVO.setSystemMetadata(queueCache.getSystemMetadata());

            failed.setMessageVo(messageVO);
            failed.setThreadId(queueCache.getThreadId());
            failed.setUniqueId(queueCache.getUniqueId());

            listQueues.add(failed);
        }
        return listQueues;
    }

    public void getWaitQueueAsyncContent(String uniqueId) {
        messageQueueDao.getWaitQueueAsyncContent(uniqueId);
    }


    /*
     *
     * Sending Queue
     *
     * */
    public void insertSendingMessageQueue(SendingQueueCache sendingQueue) {
        messageQueueDao.insertSendingMessageQueue(sendingQueue);
    }

    public void deleteSendingMessageQueue(String uniqueId) {
        messageQueueDao.deleteSendingMessageQueue(uniqueId);
    }

    public SendingQueueCache getSendingQueueCache(String uniqueId) {
        return messageQueueDao.getSendingQueue(uniqueId);
    }

    public List<SendingQueueCache> getAllSendingQueue() {
        return messageQueueDao.getAllSendingQueue();
    }

    public List<WaitQueueCache> getListWaitQueueMsg(long threadId) {
        return messageQueueDao.getWaitQueueMsgByThreadId(threadId);
    }


    @NonNull
    public SendingQueueCache getWaitQueueMsgByUnique(String uniqueId) {
        SendingQueueCache sendingQueueCache = new SendingQueueCache();
        WaitQueueCache waitQueueCache = messageQueueDao.getWaitQueueMsgByUniqueId(uniqueId);

        sendingQueueCache.setAsyncContent(waitQueueCache.getAsyncContent());
        sendingQueueCache.setId(waitQueueCache.getId());
        sendingQueueCache.setMessage(waitQueueCache.getMessage());
        sendingQueueCache.setMetadata(waitQueueCache.getMetadata());
        sendingQueueCache.setThreadId(waitQueueCache.getThreadId());
        sendingQueueCache.setUniqueId(waitQueueCache.getUniqueId());
        sendingQueueCache.setSystemMetadata(waitQueueCache.getSystemMetadata());


        return sendingQueueCache;
    }


    public List<WaitQueueCache> getWaitQueueMsg(long threadId) {
        return messageQueueDao.getWaitQueueMsgByThreadId(threadId);
    }

    public List<SendingQueueCache> getAllSendingQueueCByThreadId(long threadId) {
        return messageQueueDao.getAllSendingQueue();
    }

    @NonNull
    public List<Sending> getAllSendingQueueByThreadId(long threadId) {

        List<Sending> listQueues = new ArrayList<>();
        List<SendingQueueCache> listCaches = messageQueueDao.getAllSendingQueue();

        for (SendingQueueCache queueCache : listCaches) {
            Sending sending = new Sending();
            sending.setThreadId(queueCache.getThreadId());

            MessageVO messageVO = new MessageVO();
            messageVO.setId(queueCache.getId());
            messageVO.setMessage(queueCache.getMessage());
            messageVO.setMessageType(queueCache.getMessageType());
            messageVO.setMetadata(queueCache.getMetadata());
            messageVO.setSystemMetadata(queueCache.getSystemMetadata());

            sending.setMessageVo(messageVO);

            sending.setUniqueId(queueCache.getUniqueId());

            listQueues.add(sending);
        }
        return listQueues;
    }

    /*
     * Uploading Queue
     * */

    public void insertUploadingQueue(UploadingQueueCache uploadingQueue) {
        messageQueueDao.insertUploadingQueue(uploadingQueue);
    }

    @NonNull
    public List<Uploading> getAllUploadingQueueByThreadId(long threadId) {
        List<Uploading> uploadingQueues = new ArrayList<>();
        List<UploadingQueueCache> uploadingQueueCaches = messageQueueDao.getAllUploadingQueueByThreadId(threadId);

        for (UploadingQueueCache queueCache : uploadingQueueCaches) {
            Uploading uploadingQueue = new Uploading();

            MessageVO messageVO = new MessageVO();

            messageVO.setId(queueCache.getId());
            messageVO.setMessage(queueCache.getMessage());
            messageVO.setMessageType(queueCache.getMessageType());
            messageVO.setMetadata(queueCache.getMetadata());
            messageVO.setSystemMetadata(queueCache.getSystemMetadata());

            uploadingQueue.setMessageVo(messageVO);
            uploadingQueue.setThreadId(queueCache.getThreadId());
            uploadingQueue.setUniqueId(queueCache.getUniqueId());
            uploadingQueues.add(uploadingQueue);
        }

        return uploadingQueues;
    }

    public void deleteUploadingQueue(String uniqueId) {
        messageQueueDao.deleteUploadingQueue(uniqueId);
    }

    public UploadingQueueCache getUploadingQ(String uniqueId) {
        return messageQueueDao.getUploadingQ(uniqueId);
    }

    /*
     * MessageVo Cache
     * */


    public void deleteMessage(long id) {
        messageDao.deleteMessage(id);
    }


    public void updateGetHistoryResponse(@NonNull Callback callback, @NonNull List<MessageVO> messageVOS, long threadId, @NonNull List<CacheMessageVO> cMessageVOS) {
        long count = callback.getCount();
        long offset = callback.getOffset();
        long firstMessageId = callback.getFirstMessageId();
        long lastMessageId = callback.getLastMessageId();
        long messageId = callback.getMessageId();
        long fromTime = callback.getFromTime();
        long fromTimeNanos = callback.getFromTimeNanos();
        long toTimeNanos = callback.getToTimeNanos();
        long toTime = callback.getToTime();
        String order = callback.getOrder();
        String query = callback.getQuery();

        History history = new History.Builder()
                .id(messageId)
                .count(count)
                .fromTime(fromTime)
                .fromTimeNanos(fromTimeNanos)
                .toTime(toTime)
                .toTimeNanos(toTimeNanos)
                .order(order)
                .offset(offset)
                .query(query)
                .build();

        if (!callback.isMetadataCriteria()) {
            if (order.equals("asc")) {
                whereClauseAsc(history, messageVOS, threadId, cMessageVOS);
            } else {
                whereClauseDesc(history, messageVOS, threadId, cMessageVOS);
            }
        }


    }

    private void whereClauseDesc(History history, @NonNull List<MessageVO> messageVOS, long threadId, @NonNull List<CacheMessageVO> cMessageVOS) {
        long messageId = history.getId();
        long fromTimeNanos = history.getFromTimeNanos();
        long fromTime = history.getFromTime() + fromTimeNanos;
        long toTimeNanos = history.getToTimeNanos();
        long toTime = history.getToTime() + toTimeNanos;
        long count = history.getCount();
        long offset = history.getOffset();
        String query = history.getQuery();
        String order = history.getOrder();


        if (!Util.isNullOrEmpty(messageId) && messageId > 0) {

            List<CacheMessageVO> messageById = getMessageById(messageId);
            if (Util.isNullOrEmpty(messageVOS) && !Util.isNullOrEmpty(messageById)) {
                messageDao.deleteMessage(messageId);
            }
            //TODO where should i put nano when there isn t any
        } else if (!Util.isNullOrEmpty(fromTime) && !Util.isNullOrEmpty(toTime)) {
            if (Util.isNullOrEmptyMessageVO(messageVOS)) {
                if (!Util.isNullOrEmptyMessageVO(getHistories(history, threadId))) {
                    messageDao.deleteMessageBetweenLastAndFirstDESC(threadId, fromTime, toTime);
                }

            } else if (messageVOS.size() == 1) {
                messageDao.deleteMessageBetweenLastAndFirstDESC(threadId, fromTime, toTime);
                saveMessage(cMessageVOS.get(0), threadId);

            } else if (messageVOS.size() > 1) {
                messageDao.deleteMessageBetweenLastAndFirstDESC(threadId, fromTime, toTime);
                saveHistory(cMessageVOS, threadId);
            }

            /*
             * From time conditions*/
        } else if (!Util.isNullOrEmpty(fromTime)) {
            if (Util.isNullOrEmptyMessageVO(messageVOS)) {
                if (!Util.isNullOrEmptyMessageVO(getHistories(history, threadId))) {
                    messageDao.deleteMessageWithFirstMessageIdDESC(count, offset, threadId, fromTime);
                }

            } else if (messageVOS.size() == 1) {
                if (getHistories(history, threadId).size() > 1) {
                    messageDao.deleteMessageWithFirstMessageIdDESC(count, offset, threadId, fromTime);
                    saveMessage(cMessageVOS.get(0), threadId);
                }

            } else if (messageVOS.size() > 1) {
                int size = getHistories(history, threadId).size();
                long firstMesssageId = getHistories(history, threadId).get(0).getId();
                long lastMesssageId = getHistories(history, threadId).get(size - 1).getId();
                messageDao.deleteMessageBetweenLastAndFirstDESC(threadId, firstMesssageId, lastMesssageId);
                saveHistory(cMessageVOS, threadId);
            }
            /*
             * To time conditions
             * */
        } else if (!Util.isNullOrEmpty(toTime)) {
            if (Util.isNullOrEmptyMessageVO(messageVOS)) {
                if (!Util.isNullOrEmptyMessageVO(getHistories(history, threadId))) {
                    messageDao.deleteMessageWithFirstMessageIdDESC(count, offset, threadId, toTime);
                }

            } else if (messageVOS.size() == 1) {
                if (getHistories(history, threadId).size() > 1) {
                    messageDao.deleteMessageWithFirstMessageIdDESC(count, offset, threadId, toTime);
                    saveMessage(cMessageVOS.get(0), threadId);
                }

            } else if (messageVOS.size() > 1) {
                int size = getHistories(history, threadId).size();
                long firstMesssageId = getHistories(history, threadId).get(0).getId();
                long lastMesssageId = getHistories(history, threadId).get(size - 1).getId();
                messageDao.deleteMessageBetweenLastAndFirstDESC(threadId, firstMesssageId, lastMesssageId);
                saveHistory(cMessageVOS, threadId);
            }
        } else if (Util.isNullOrEmpty(query)) {

            if (Util.isNullOrEmpty(messageVOS)) {
                if (!Util.isNullOrEmpty(getHistories(history, threadId))) {
                    deleteMessageWithQuery(order, threadId, count, offset, query);
                }
            } else if (messageVOS.size() == 1) {
                if (getHistories(history, threadId).size() > 1) {
                    deleteMessageWithQuery(order, threadId, count, offset, query);
                    saveMessage(cMessageVOS.get(0), threadId);
                }

            } else if (messageVOS.size() > 1) {
                int size = getHistories(history, threadId).size();
                long firstMsgId = getHistories(history, threadId).get(0).getId();
                long lastMsgId = getHistories(history, threadId).get(size - 1).getId();
                messageDao.deleteMessageBetweenLastAndFirstASC(threadId, firstMsgId, lastMsgId);
                saveHistory(cMessageVOS, threadId);
            }
        }
    }

    private void whereClauseAsc(History history, @NonNull List<MessageVO> messageVOS, long threadId, @NonNull List<CacheMessageVO> cMessageVOS) {

        long messageId = history.getId();
        long fromTimeNanos = history.getFromTimeNanos();
        long fromTime = history.getFromTime() + fromTimeNanos;
        long toTimeNanos = history.getToTimeNanos();
        long toTime = history.getToTime() + toTimeNanos;
        long count = history.getCount();
        long offset = history.getOffset();
        String query = history.getQuery();
        String order = history.getOrder();

        if (!Util.isNullOrEmpty(messageId) && messageId > 0) {

            List<CacheMessageVO> messageById = getMessageById(messageId);
            if (Util.isNullOrEmpty(messageVOS) && !Util.isNullOrEmpty(messageById)) {
                messageDao.deleteMessage(messageId);
            }

        } else if (!Util.isNullOrEmpty(fromTime) && !Util.isNullOrEmpty(toTime)) {
            if (Util.isNullOrEmptyMessageVO(messageVOS)) {
                if (!Util.isNullOrEmptyMessageVO(getHistories(history, threadId))) {
                    messageDao.deleteMessageBetweenLastAndFirstASC(threadId, fromTime, toTime);
                }

            } else if (messageVOS.size() == 1) {
                messageDao.deleteMessageBetweenLastAndFirstASC(threadId, fromTime, toTime);
                saveMessage(cMessageVOS.get(0), threadId);

            } else if (messageVOS.size() > 1) {
                messageDao.deleteMessageBetweenLastAndFirstASC(threadId, fromTime, toTime);
                saveHistory(cMessageVOS, threadId);
            }

            /*
             * From time conditions
             * */
        } else if (!Util.isNullOrEmpty(fromTime)) {
            if (Util.isNullOrEmptyMessageVO(messageVOS)) {
                if (!Util.isNullOrEmptyMessageVO(getHistories(history, threadId))) {
                    messageDao.deleteMessageWithFirstMessageIdASC(count, offset, threadId, fromTime);
                }

            } else if (messageVOS.size() == 1) {
                if (getHistories(history, threadId).size() > 1) {
                    messageDao.deleteMessageWithFirstMessageIdASC(count, offset, threadId, fromTime);
                    saveMessage(cMessageVOS.get(0), threadId);
                }

            } else if (messageVOS.size() > 1) {
                int size = getHistories(history, threadId).size();
                long firstMesssageId = getHistories(history, threadId).get(0).getId();
                long lastMesssageId = getHistories(history, threadId).get(size - 1).getId();
                messageDao.deleteMessageBetweenLastAndFirstASC(threadId, firstMesssageId, lastMesssageId);
                saveHistory(cMessageVOS, threadId);
            }
            /*
             * To time conditions
             * */
        } else if (!Util.isNullOrEmpty(toTime)) {
            if (Util.isNullOrEmptyMessageVO(messageVOS)) {
                if (!Util.isNullOrEmptyMessageVO(getHistories(history, threadId))) {
                    messageDao.deleteMessageWithFirstMessageIdASC(count, offset, threadId, fromTime);
                }

            } else if (messageVOS.size() == 1) {
                if (getHistories(history, threadId).size() > 1) {
                    messageDao.deleteMessageWithFirstMessageIdASC(count, offset, threadId, toTime);
                    saveMessage(cMessageVOS.get(0), threadId);
                }

            } else if (messageVOS.size() > 1) {
                int size = getHistories(history, threadId).size();
                long firstMesssageId = getHistories(history, threadId).get(0).getId();
                long lastMesssageId = getHistories(history, threadId).get(size - 1).getId();
                messageDao.deleteMessageBetweenLastAndFirstASC(threadId, firstMesssageId, lastMesssageId);
                saveHistory(cMessageVOS, threadId);
            }
            /*
             * Query conditions
             * */
        } else if (Util.isNullOrEmpty(query)) {

            if (Util.isNullOrEmpty(messageVOS)) {
                if (!Util.isNullOrEmpty(getHistories(history, threadId))) {
                    deleteMessageWithQuery(order, threadId, count, offset, query);
                }
            } else if (messageVOS.size() == 1) {
                if (getHistories(history, threadId).size() > 1) {
                    deleteMessageWithQuery(order, threadId, count, offset, query);
                    saveMessage(cMessageVOS.get(0), threadId);
                }

            } else if (messageVOS.size() > 1) {
                int size = getHistories(history, threadId).size();
                long firstMsgId = getHistories(history, threadId).get(0).getId();
                long lastMsgId = getHistories(history, threadId).get(size - 1).getId();
                messageDao.deleteMessageBetweenLastAndFirstASC(threadId, firstMsgId, lastMsgId);
                saveHistory(cMessageVOS, threadId);
            }
        }
    }

    private void deleteMessageWithQuery(String order, long threadId, long count, long offset, String query) {
        if (order.equals("asc")) {
            messageDao.deleteMessagesWithQueryAsc(threadId, count, offset, query);
        } else {
            messageDao.deleteMessagesWithQueryDesc(threadId, count, offset, query);
        }
    }

    private List<CacheMessageVO> getHistoriesFandLASC(String order, long count, long offset, long threadId, long firstMessageId, long lastMessageId) {
        if (order.equals("asc")) {
            return messageDao.getHistoriesFandLASC(count, offset, threadId, firstMessageId, lastMessageId);
        } else {
            return messageDao.getHistoriesFandLDESC(count, offset, threadId, firstMessageId, lastMessageId);
        }
    }

    private List<CacheMessageVO> getHistoriesMessageId(String order, long count, long offset, long threadId, long firstMessageId) {
        if (order.equals("asc")) {
            return messageDao.getHistoriesMessageIdASC(count, offset, threadId, firstMessageId);
        } else {
            return messageDao.getHistoriesMessageIdDESC(count, offset, threadId, firstMessageId);
        }
    }

    private List<CacheMessageVO> getQuery(String order, long count, long offset, long threadId, String query) {
        List<CacheMessageVO> vos;
        if (order.equals("asc")) {
            vos = messageDao.getQueryASC(count, offset, threadId, query);
            if (vos != null && vos.size() > 0) {
                return vos;
            }
        } else {
            vos = messageDao.getQueryDESC(count, offset, threadId, query);
            if (vos != null && vos.size() > 0) {
                return vos;
            }
        }
        return vos;
    }

    private List<CacheMessageVO> getHistoriesWithCountAndOffset(String order, long count, long offset, long threadId) {
        List<CacheMessageVO> vos;
        if (order.equals("asc")) {
            vos = messageDao.getHistoriesASC(count, offset, threadId);
            if (vos != null && vos.size() > 0) {
                return vos;
            }

        } else {
            vos = messageDao.getHistoriesDESC(count, offset, threadId);
            if (vos != null && vos.size() > 0) {
                return vos;
            }
        }
        return vos;
    }


    public void deleteMessages(@NonNull History history, long threadId) {

        long fromTime = history.getFromTime();
        long fromTimeNanos = history.getFromTimeNanos();
        long toTime = history.getToTime();
        long toTimeNanos = history.getToTimeNanos();
        long messageId = history.getId();
        long offset = history.getOffset();
        long count = history.getCount();
        String query = history.getQuery();
        String order = history.getOrder();

        offset = offset >= 0 ? offset : 0;
        count = count > 0 ? count : 50;

        if (Util.isNullOrEmpty(order)) {
            order = "desc";
        }

        //TODO
        String rawQuery = "DELETE FROM CacheMessageVO WHERE threadVoId =" + threadId + " ORDER BY timeStamp desc LIMIT 50 OFFSET 0";

//        rawQuery = rawQuery + " LIMIT " + count + " OFFSET " + offset;

        SupportSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(rawQuery);

        messageDao.deleteMessages(sqLiteQuery);

    }

    public void deleteMessages(List<CacheMessageVO> cacheMessages) {

        messageDao.deleteMessages(cacheMessages);

    }


    @NonNull
    public List<MessageVO> getHistories(@NonNull History history, long threadId) {


        List<MessageVO> messageVOS = new ArrayList<>();
        List<CacheMessageVO> cacheMessageVOS;
        long fromTime = history.getFromTime();
        long fromTimeNanos = history.getFromTimeNanos();
        long toTime = history.getToTime();
        long toTimeNanos = history.getToTimeNanos();
        long messageId = history.getId();
        long offset = history.getOffset();
        long count = history.getCount();
        String query = history.getQuery();
        String order = history.getOrder();
        offset = offset >= 0 ? offset : 0;
        count = count > 0 ? count : 50;
        if (Util.isNullOrEmpty(order)) {
            order = "desc";
        }

        String rawQuery = "SELECT * FROM CacheMessageVO WHERE threadVoId =" + threadId;

        rawQuery = addMessageIdIfExist(messageId, rawQuery);

        rawQuery = addFromTimeIfExist(fromTime, fromTimeNanos, rawQuery);

        rawQuery = addToTimeIfExist(toTime, toTimeNanos, rawQuery);

        rawQuery = addQueryIfExist(query, rawQuery);

        rawQuery = addOrderAndLimitAndOffset(offset, count, order, rawQuery);

        SupportSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(rawQuery);

        cacheMessageVOS = messageDao.getRawHistory(sqLiteQuery);


        Participant participant = null;
        ReplyInfoVO replyInfoVO = null;
        ForwardInfo forwardInfo = null;
        Thread thread = null;
        ConversationSummery conversationSummery = null;
        for (CacheMessageVO cacheMessageVO : cacheMessageVOS) {
            if (!Util.isNullOrEmpty(cacheMessageVO.getConversationId())) {
                cacheMessageVO.setConversation(messageDao.getThreadById(cacheMessageVO.getConversationId()));
                ThreadVo threadVo = cacheMessageVO.getConversation();
                thread = threadVoToThreadMapper(threadVo, null);
            }
            if (cacheMessageVO.getForwardInfoId() != null) {
                cacheMessageVO.setForwardInfo(messageDao.getForwardInfo(cacheMessageVO.getForwardInfoId()));
            }
            if (cacheMessageVO.getParticipantId() != null) {
                CacheParticipant cacheParticipant = messageDao.getParticipant(cacheMessageVO.getParticipantId());
                participant = cacheToParticipantMapper(cacheParticipant, null, null);

            }
            if (cacheMessageVO.getReplyInfoVOId() != null) {
                CacheReplyInfoVO cacheReplyInfoVO = messageDao.getReplyInfo(cacheMessageVO.getReplyInfoVOId());
                replyInfoVO = new ReplyInfoVO(
                        cacheReplyInfoVO.getRepliedToMessageId(),
                        cacheReplyInfoVO.getMessageType(),
                        cacheReplyInfoVO.isDeleted(),
                        cacheReplyInfoVO.getRepliedToMessage(),
                        cacheReplyInfoVO.getSystemMetadata(),
                        cacheReplyInfoVO.getMetadata(),
                        cacheReplyInfoVO.getMessage(),
                        cacheReplyInfoVO.getRepliedToMessageTime(),
                        cacheReplyInfoVO.getRepliedToMessageNanos()
                );
            }
            if (cacheMessageVO.getForwardInfo() != null) {
                CacheForwardInfo cacheForwardInfo = messageDao.getForwardInfo(cacheMessageVO.getForwardInfoId());
                if (cacheForwardInfo.getParticipantId() != null) {
                    CacheParticipant cacheParticipant = messageDao.getParticipant(cacheForwardInfo.getParticipantId());
                    participant = cacheToParticipantMapper(cacheParticipant, null, null);
                }
                if (Util.isNullOrEmpty(cacheForwardInfo.getConversationId())) {
                    conversationSummery = messageDao.getConversationSummery(cacheForwardInfo.getConversationId());
                }
                forwardInfo = new ForwardInfo(participant, conversationSummery);
            }

            MessageVO messageVO = cacheMessageVoToMessageVoMapper(participant, replyInfoVO, forwardInfo, thread, cacheMessageVO);

            messageVOS.add(messageVO);
        }
        return messageVOS;
    }

    private String addOrderAndLimitAndOffset(long offset, long count, String order, String rawQuery) {
        rawQuery = rawQuery + " ORDER BY timeStamp " + order + " LIMIT " + count + " OFFSET " + offset;
        return rawQuery;
    }

    private String addQueryIfExist(String query, String rawQuery) {
        if (!Util.isNullOrEmpty(query)) {
            rawQuery = rawQuery + " AND message LIKE '%" + query + "%'";
        }
        return rawQuery;
    }

    private String addToTimeIfExist(long toTime, long toTimeNanos, String rawQuery) {
        if (!Util.isNullOrEmpty(toTime)) {
            long pow = (long) Math.pow(10, 9);
            if (!Util.isNullOrEmpty(toTimeNanos)) {
                long timestamp = ((toTime / 1000) * pow) + toTimeNanos;
                rawQuery = rawQuery + " AND timeStamp <=" + timestamp;
            } else {
                long timestamp = ((toTime / 1000) * pow);
                rawQuery = rawQuery + " AND timeStamp <=" + timestamp;
            }
        }
        return rawQuery;
    }

    private String addFromTimeIfExist(long fromTime, long fromTimeNanos, String rawQuery) {
        if (!Util.isNullOrEmpty(fromTime)) {
            long pow = (long) Math.pow(10, 9);
            if (!Util.isNullOrEmpty(fromTimeNanos)) {
                long timestamp = ((fromTime / 1000) * pow) + fromTimeNanos;
                rawQuery = rawQuery + " AND timeStamp >=" + timestamp;
            } else {
                long timestamp = ((fromTime / 1000) * pow);
                rawQuery = rawQuery + " AND timeStamp >=" + timestamp;
            }
        }
        return rawQuery;
    }

    private String addMessageIdIfExist(long messageId, String rawQuery) {
        if (messageDao.getMessage(messageId) != null && messageId > 0) {
            rawQuery = rawQuery + " AND id=" + messageId;
        }
        return rawQuery;
    }

    private MessageVO cacheMessageVoToMessageVoMapper(Participant participant, ReplyInfoVO replyInfoVO, ForwardInfo forwardInfo, Thread thread, CacheMessageVO cacheMessageVO) {
        return new MessageVO(
                cacheMessageVO.getId(),
                cacheMessageVO.isEdited(),
                cacheMessageVO.isEditable(),
                cacheMessageVO.isDelivered(),
                cacheMessageVO.isSeen(),
                cacheMessageVO.isDeletable(),
                cacheMessageVO.getUniqueId(),
                cacheMessageVO.getMessageType(),
                cacheMessageVO.getPreviousId(),
                cacheMessageVO.getMessage(),
                participant,
                cacheMessageVO.getTime(),
                cacheMessageVO.getTimeNanos(),
                cacheMessageVO.getMetadata(),
                cacheMessageVO.getSystemMetadata(),
                thread,
                replyInfoVO,
                forwardInfo,
                false,
                cacheMessageVO.hasGap()
        );
    }

    public List<CacheMessageVO> getCacheHistories(@NonNull History history, long threadId) {
        List<CacheMessageVO> cacheMessageVOS;

        long fromTime = history.getFromTime();
        long fromTimeNanos = history.getFromTimeNanos();
        long toTime = history.getToTime();
        long toTimeNanos = history.getToTimeNanos();
        long messageId = history.getId();
        long offset = history.getOffset();
        long count = history.getCount();
        String query = history.getQuery();
        String order = history.getOrder();

        offset = offset >= 0 ? offset : 0;
        count = count > 0 ? count : 50;

        if (Util.isNullOrEmpty(order)) {
            order = "desc";
        }

        String rawQuery = "SELECT * FROM CacheMessageVO WHERE threadVoId =" + threadId;

        rawQuery = addMessageIdIfExist(messageId, rawQuery);

        rawQuery = addFromTimeIfExist(fromTime,fromTimeNanos,rawQuery);

        rawQuery = addToTimeIfExist(toTime, toTimeNanos, rawQuery);

        rawQuery = addQueryIfExist(query, rawQuery);

        rawQuery = addOrderAndLimitAndOffset(offset, count, order, rawQuery);

        SupportSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(rawQuery);

        cacheMessageVOS = messageDao.getRawHistory(sqLiteQuery);

        return cacheMessageVOS;
    }


    public List<CacheMessageVO> getMessageById(long messageId) {
        return messageDao.getMessage(messageId);
    }

    public long getHistoryContentCount(long threadVoId) {
        return messageDao.getHistoryCount(threadVoId);
    }

    /**
     * Cache contact
     */
    @NonNull
    public List<Contact> getContacts(Integer count, Long offset) {

        List<Contact> contacts = new ArrayList<>();

        List<CacheContact> cacheContacts = messageDao.getContacts(count, offset);


        if (cacheContacts != null && cacheContacts.size() > 0) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            Date nowDate = c.getTime();

            for (CacheContact cacheContact : cacheContacts) {
                try {
                    Date expireDate = format.parse(cacheContact.getExpireDate());
                    if (expireDate.compareTo(nowDate) < 0) {
                        deleteContact(cacheContact);
                    } else {
                        Contact contact = new Contact(
                                cacheContact.getId(),
                                cacheContact.getFirstName(),
                                cacheContact.getUserId(),
                                cacheContact.getLastName(),
                                cacheContact.getBlocked(),
                                cacheContact.getCreationDate(),
                                cacheContact.getLinkedUser(),
                                cacheContact.getCellphoneNumber(),
                                cacheContact.getEmail(),
                                cacheContact.getUniqueId(),
                                cacheContact.getNotSeenDuration(),
                                cacheContact.isHasUser()
                        );
                        contacts.add(contact);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return contacts;
    }

    public int getContactCount() {
        return messageDao.getContactCount();
    }

    public void saveContacts(@NonNull List<Contact> contacts, int expireAmount) {
        List<CacheContact> cacheContacts = new ArrayList<>();
        for (Contact contact : contacts) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.SECOND, expireAmount);
            String expireDate = format.format(c.getTime());

            CacheContact cacheContact = new CacheContact(
                    expireDate,
                    contact.getId(),
                    contact.getFirstName(),
                    contact.getUserId(),
                    contact.getLastName(),
                    contact.getBlocked(),
                    contact.getCreationDate(),
                    contact.getLinkedUser(),
                    contact.getCellphoneNumber(),
                    contact.getEmail(),
                    contact.getUniqueId(),
                    contact.getNotSeenDuration(),
                    contact.isHasUser()
            );
            cacheContacts.add(cacheContact);
        }
        messageDao.insertContacts(cacheContacts);
    }

    public void saveContact(@NonNull Contact contact, int expireSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, expireSecond);

        String expireDate = dateFormat.format(c.getTime());
        CacheContact cacheContact = new CacheContact(
                expireDate,
                contact.getId(),
                contact.getFirstName(),
                contact.getUserId(),
                contact.getLastName(),
                contact.getBlocked(),
                contact.getCreationDate(),
                contact.getLinkedUser(),
                contact.getCellphoneNumber(),
                contact.getEmail(),
                contact.getUniqueId(),
                contact.getNotSeenDuration(),
                contact.isHasUser()
        );
        messageDao.insertContact(cacheContact);
    }

    private void deleteContact(CacheContact cacheContact) {
        messageDao.deleteContact(cacheContact);
    }

    public void deleteContactById(long id) {
        messageDao.deleteContactById(id);
    }

    /**
     * Cache UserInfo
     */
    public void saveUserInfo(UserInfo userInfo) {
        messageDao.insertUserInfo(userInfo);
    }

    public UserInfo getUserInfo() {

        UserInfo userInfo = new UserInfo();
        if (messageDao.getUserInfo() != null) {
            userInfo = messageDao.getUserInfo();
        }
        return userInfo;
    }

    public int getThreadCount() {
        return messageDao.getThreadCount();
    }

    //TODO test
    @NonNull
    public List<Thread> getThreadRaw(Integer count, Long offset, @Nullable ArrayList<Integer> threadIds, @Nullable String threadName) {

        String sQuery;

        sQuery = "select  * from ThreadVo ORDER BY id DESC LIMIT " + count + " OFFSET " + offset;
        if (threadName != null) {
            sQuery = "select  * from ThreadVo where title LIKE  '%" + threadName + "%' ORDER BY id DESC LIMIT " + count + " OFFSET " + offset;
        }
        if (threadIds != null && threadIds.size() > 0) {

            StringBuilder stringBuilder = new StringBuilder();
            for (int id : threadIds) {
                stringBuilder.append(id).append(",");
            }

            String stringIds = stringBuilder.toString();
            String lastString = stringIds.replaceAll(",$", "");

            if (threadName != null) {
                sQuery = "select  * from ThreadVo where id IN " + "(" + lastString + ")" + "AND title LIKE  '%" + threadName + "%' ORDER BY id DESC LIMIT " + count + " OFFSET " + offset;
            } else {
                sQuery = "select  * from ThreadVo where id IN " + "(" + lastString + ")" + " ORDER BY id DESC LIMIT " + count + " OFFSET " + offset;
            }
        }

        SimpleSQLiteQuery query = new SimpleSQLiteQuery(sQuery);
        List<ThreadVo> threadVos = messageDao.getThreadRaw(query);

        List<Thread> threads = new ArrayList<>();
        if (threadVos != null) {

            CacheParticipant cacheParticipant;
            CacheReplyInfoVO cacheReplyInfoVO;
            Participant participant = null;
            ReplyInfoVO replyInfoVO = null;
            for (ThreadVo threadVo : threadVos) {
                MessageVO lastMessageVO = null;
                if (threadVo.getInviterId() != null) {
                    threadVo.setInviter(messageDao.getInviter(threadVo.getInviterId()));
                }
                if (threadVo.getLastMessageVOId() != null) {
                    threadVo.setLastMessageVO(messageDao.getLastMessageVO(threadVo.getLastMessageVOId()));
                    CacheMessageVO cacheLastMessageVO = threadVo.getLastMessageVO();
                    if (cacheLastMessageVO.getParticipantId() != null) {
                        cacheParticipant = messageDao.getParticipant(cacheLastMessageVO.getParticipantId());
                        if (cacheParticipant != null) {
                            participant = cacheToParticipantMapper(cacheParticipant, null, null);
                        }

                    }
                    if (cacheLastMessageVO.getReplyInfoVOId() != null) {
                        cacheReplyInfoVO = messageDao.getReplyInfo(cacheLastMessageVO.getReplyInfoVOId());
                        replyInfoVO = new ReplyInfoVO(
                                cacheReplyInfoVO.getRepliedToMessageId(),
                                cacheReplyInfoVO.getMessageType(),
                                cacheReplyInfoVO.isDeleted(),
                                cacheReplyInfoVO.getRepliedToMessage(),
                                cacheReplyInfoVO.getSystemMetadata(),
                                cacheReplyInfoVO.getMetadata(),
                                cacheReplyInfoVO.getMessage(),
                                cacheReplyInfoVO.getRepliedToMessageTime(),
                                cacheReplyInfoVO.getRepliedToMessageNanos()
                        );
                    }
                    lastMessageVO = cacheMessageVoToMessageVoMapper(participant, replyInfoVO, null, null, cacheLastMessageVO);
                }

                Thread thread = threadVoToThreadMapper(threadVo, lastMessageVO);
                threads.add(thread);
            }
        }
        return threads;
    }

    private Thread threadVoToThreadMapper(ThreadVo threadVo, MessageVO lastMessageVO) {
        return new Thread(
                threadVo.getId(),
                threadVo.getJoinDate(),
                threadVo.getInviter(),
                lastMessageVO,
                threadVo.getTitle(),
                null,
                threadVo.getTime(),
                threadVo.getLastMessage(),
                threadVo.getLastParticipantName(),
                threadVo.getLastParticipantImage(),
                threadVo.isGroup(),
                threadVo.getPartner(),
                threadVo.getImage(),
                threadVo.getDescription(),
                threadVo.getUnreadCount(),
                threadVo.getLastSeenMessageId(),
                threadVo.getPartnerLastSeenMessageId(),
                threadVo.getPartnerLastDeliveredMessageId(),
                threadVo.getLastSeenMessageNanos(),
                threadVo.getLastSeenMessageTime(),
                threadVo.getPartnerLastSeenMessageTime(),
                threadVo.getPartnerLastSeenMessageNanos(),
                threadVo.getPartnerLastDeliveredMessageTime(),
                threadVo.getPartnerLastDeliveredMessageNanos(),
                threadVo.getType(),
                threadVo.isMute(),
                threadVo.getMetadata(),
                threadVo.isCanEditInfo(),
                threadVo.getParticipantCount(),
                threadVo.isCanSpam(),
                threadVo.isAdmin(),
                threadVo.isPin(),
                threadVo.isMentioned()
        );
    }

    @NonNull
    private Participant cacheToParticipantMapper(CacheParticipant cacheParticipant, Boolean getAdmin, List<String> roles) {


        return new Participant(
                cacheParticipant.getId(),
                cacheParticipant.getName(),
                cacheParticipant.getFirstName(),
                cacheParticipant.getLastName(),
                cacheParticipant.getImage(),
                cacheParticipant.getNotSeenDuration(),
                cacheParticipant.getContactId(),
                cacheParticipant.getCoreUserId(),
                cacheParticipant.getContactName(),
                cacheParticipant.getContactFirstName(),
                cacheParticipant.getContactLastName(),
                cacheParticipant.getSendEnable(),
                cacheParticipant.getReceiveEnable(),
                cacheParticipant.getCellphoneNumber(),
                cacheParticipant.getEmail(),
                cacheParticipant.getMyFriend(),
                cacheParticipant.getOnline(),
                cacheParticipant.getBlocked(),
                cacheParticipant.getAdmin(),
                cacheParticipant.isAuditor(),
                getAdmin != null ? getAdmin ? roles : null : null,
                cacheParticipant.getKeyId(),
                cacheParticipant.getUsername()
        );
    }

    public List<Thread> getThreads(long count, long offset) {
        List<Thread> threads;
        if (messageDao.getThreads(count, offset) != null) {
            List<ThreadVo> threadVos = messageDao.getThreads(count, offset);
            threads = new ArrayList<>();
            CacheParticipant cacheParticipant;
            CacheReplyInfoVO cacheReplyInfoVO;
            Participant participant = null;
            ReplyInfoVO replyInfoVO = null;
            for (ThreadVo threadVo : threadVos) {
                MessageVO lastMessageVO = null;
                if (threadVo.getInviterId() != null) {
                    threadVo.setInviter(messageDao.getInviter(threadVo.getInviterId()));
                }
                if (threadVo.getLastMessageVOId() != null) {
                    threadVo.setLastMessageVO(messageDao.getLastMessageVO(threadVo.getLastMessageVOId()));
                    CacheMessageVO cacheLastMessageVO = threadVo.getLastMessageVO();
                    if (cacheLastMessageVO.getParticipantId() != null) {
                        cacheParticipant = messageDao.getParticipant(cacheLastMessageVO.getParticipantId());
                        if (cacheParticipant != null) {
                            participant = cacheToParticipantMapper(cacheParticipant, null, null);
                        }

                    }
                    if (cacheLastMessageVO.getReplyInfoVOId() != null) {
                        cacheReplyInfoVO = messageDao.getReplyInfo(cacheLastMessageVO.getReplyInfoVOId());
                        replyInfoVO = new ReplyInfoVO(
                                cacheReplyInfoVO.getRepliedToMessageId(),
                                cacheReplyInfoVO.getMessageType(),
                                cacheReplyInfoVO.isDeleted(),
                                cacheReplyInfoVO.getRepliedToMessage(),
                                cacheReplyInfoVO.getSystemMetadata(),
                                cacheReplyInfoVO.getMetadata(),
                                cacheReplyInfoVO.getMessage(),
                                cacheReplyInfoVO.getRepliedToMessageTime(),
                                cacheReplyInfoVO.getRepliedToMessageNanos()
                        );
                    }
                    lastMessageVO = cacheMessageVoToMessageVoMapper(participant, replyInfoVO, null, null, cacheLastMessageVO);

                }


                Thread thread = threadVoToThreadMapper(threadVo, lastMessageVO);
                threads.add(thread);
            }
            return threads;
        } else {
            threads = new ArrayList<>();
        }

        return threads;
    }

    @NonNull
    public void getThreadList(OnWorkDone listener) {

        new java.lang.Thread(() -> {

            String rawQuery = "select * from ThreadVo";

            SupportSQLiteQuery query = new SimpleSQLiteQuery(rawQuery);

            List<Thread> threads = new ArrayList<>();

            List<ThreadVo> tvo = messageDao.getThreadRaw(query);

            for (ThreadVo threadInCache :
                    tvo) {

                threads.add(threadVoToThreadMapper(threadInCache, null));

            }


            listener.onWorkDone(threads);

        }).start();


    }

    @NonNull


    public void getThreadIdsList(OnWorkDone listener) {

        new java.lang.Thread(() -> {

            String rawQuery = "select id from ThreadVo";

            SupportSQLiteQuery query = new SimpleSQLiteQuery(rawQuery);

            List<Long> tvo = messageDao.getThreadIds(query);

            listener.onWorkDone(tvo);

        }).start();


    }

    public List<Thread> getThreadsByThreadName(String threadName) {
        List<Thread> threads;
        List<ThreadVo> listThreadVo = messageDao.getThreadByName(50, 0, threadName);
        if (listThreadVo != null) {
            threads = new ArrayList<>();
            for (ThreadVo threadVo : listThreadVo) {
                CacheParticipant cacheParticipant;
                CacheReplyInfoVO cacheReplyInfoVO;
                Participant participant = null;
                ReplyInfoVO replyInfoVO = null;
                MessageVO lastMessageVO = null;
                if (threadVo.getInviterId() != null) {
                    threadVo.setInviter(messageDao.getInviter(threadVo.getInviterId()));
                }
                if (threadVo.getLastMessageVOId() != null) {
                    threadVo.setLastMessageVO(messageDao.getLastMessageVO(threadVo.getLastMessageVOId()));
                    CacheMessageVO cacheLastMessageVO = threadVo.getLastMessageVO();
                    if (cacheLastMessageVO.getParticipantId() != null) {
                        cacheParticipant = messageDao.getParticipant(cacheLastMessageVO.getParticipantId());
                        participant = cacheToParticipantMapper(cacheParticipant, null, null);
                    }
                    if (cacheLastMessageVO.getReplyInfoVOId() != null) {
                        cacheReplyInfoVO = messageDao.getReplyInfo(cacheLastMessageVO.getReplyInfoVOId());
                        replyInfoVO = new ReplyInfoVO(
                                cacheReplyInfoVO.getRepliedToMessageId(),
                                cacheReplyInfoVO.getMessageType(),
                                cacheReplyInfoVO.isDeleted(),
                                cacheReplyInfoVO.getRepliedToMessage(),
                                cacheReplyInfoVO.getSystemMetadata(),
                                cacheReplyInfoVO.getMetadata(),
                                cacheReplyInfoVO.getMessage(),
                                cacheReplyInfoVO.getRepliedToMessageTime(),
                                cacheReplyInfoVO.getRepliedToMessageNanos()
                        );
                    }

                    lastMessageVO = cacheMessageVoToMessageVoMapper(participant, replyInfoVO, null, null, cacheLastMessageVO);

                }

                Thread thread = threadVoToThreadMapper(threadVo, lastMessageVO);
                threads.add(thread);
            }


        } else {
            return new ArrayList<>();
        }

        return threads;
    }

    @NonNull
    public List<Thread> getThreadsByThreadIds(@NonNull ArrayList<Integer> threadIds) {
        List<Thread> threads = new ArrayList<>();

        for (int id : threadIds) {
            if (messageDao.getThreadById(id) != null) {
                ThreadVo threadVo = messageDao.getThreadById(id);
                CacheParticipant cacheParticipant;
                CacheReplyInfoVO cacheReplyInfoVO;
                Participant participant = null;
                ReplyInfoVO replyInfoVO = null;
                MessageVO lastMessageVO = null;
                if (threadVo.getInviterId() != null) {
                    threadVo.setInviter(messageDao.getInviter(threadVo.getInviterId()));
                }
                if (threadVo.getLastMessageVOId() != null) {
                    threadVo.setLastMessageVO(messageDao.getLastMessageVO(threadVo.getLastMessageVOId()));
                    CacheMessageVO cacheLastMessageVO = threadVo.getLastMessageVO();
                    if (cacheLastMessageVO.getParticipantId() != null) {
                        cacheParticipant = messageDao.getParticipant(cacheLastMessageVO.getParticipantId());
                        participant = cacheToParticipantMapper(cacheParticipant, null, null);
                    }
                    if (cacheLastMessageVO.getReplyInfoVOId() != null) {
                        cacheReplyInfoVO = messageDao.getReplyInfo(cacheLastMessageVO.getReplyInfoVOId());
                        replyInfoVO = new ReplyInfoVO(
                                cacheReplyInfoVO.getRepliedToMessageId(),
                                cacheReplyInfoVO.getMessageType(),
                                cacheReplyInfoVO.isDeleted(),
                                cacheReplyInfoVO.getRepliedToMessage(),
                                cacheReplyInfoVO.getSystemMetadata(),
                                cacheReplyInfoVO.getMetadata(),
                                cacheReplyInfoVO.getMessage(),
                                cacheReplyInfoVO.getRepliedToMessageTime(),
                                cacheReplyInfoVO.getRepliedToMessageNanos()
                        );
                    }
                    lastMessageVO = cacheMessageVoToMessageVoMapper(participant, replyInfoVO, null, null, cacheLastMessageVO);

                }


                Thread thread = threadVoToThreadMapper(threadVo, lastMessageVO);
                threads.add(thread);
            }
        }

        return threads;
    }

    public void saveThreads(@NonNull List<ThreadVo> threadVos) {


        worker(() -> {

            CacheMessageVO cacheMessageVO;
            CacheReplyInfoVO cacheReplyInfoVO;
            CacheForwardInfo cacheForwardInfo;

            for (ThreadVo threadVo : threadVos) {
                if (threadVo.getInviter() != null) {

                    threadVo.setInviterId(threadVo.getInviter().getId());
                    messageDao.insertInviter(threadVo.getInviter());
                }
                if (threadVo.getLastMessageVO() != null) {

                    threadVo.setLastMessageVOId(threadVo.getLastMessageVO().getId());
                    cacheMessageVO = threadVo.getLastMessageVO();
                    messageDao.insertLastMessageVO(cacheMessageVO);
                    if (threadVo.getLastMessageVO().getParticipant() != null) {

                        cacheMessageVO.setParticipantId(threadVo.getLastMessageVO().getParticipant().getId());
                        messageDao.insertLastMessageVO(cacheMessageVO);
                        CacheParticipant cacheParticipantLastMessageVO = threadVo.getLastMessageVO().getParticipant();
                        cacheParticipantLastMessageVO.setThreadId(threadVo.getId());
                        messageDao.insertParticipant(threadVo.getLastMessageVO().getParticipant());
                    }
                    if (threadVo.getLastMessageVO().getReplyInfoVO() != null) {
                        cacheReplyInfoVO = threadVo.getLastMessageVO().getReplyInfoVO();
                        cacheMessageVO.setReplyInfoVOId(threadVo.getLastMessageVO().getReplyInfoVO().getId());
                        messageDao.insertLastMessageVO(cacheMessageVO);
                        messageDao.insertReplyInfoVO(cacheReplyInfoVO);

                        if (threadVo.getLastMessageVO().getReplyInfoVO().getParticipant() != null) {

                            cacheReplyInfoVO.setParticipantId(threadVo.getLastMessageVO().getReplyInfoVO().getParticipant().getId());
                            messageDao.insertReplyInfoVO(cacheReplyInfoVO);
                            messageDao.insertParticipant(threadVo.getLastMessageVO().getReplyInfoVO().getParticipant());
                        }
                    }
                    if (threadVo.getLastMessageVO().getForwardInfo() != null) {

                        cacheForwardInfo = threadVo.getLastMessageVO().getForwardInfo();
                        cacheForwardInfo.setId(threadVo.getLastMessageVO().getId());
                        messageDao.insertForwardInfo(cacheForwardInfo);
                        cacheMessageVO.setForwardInfoId(threadVo.getLastMessageVO().getId());
                        messageDao.insertLastMessageVO(cacheMessageVO);
                        if (threadVo.getLastMessageVO().getForwardInfo().getParticipant() != null) {

                            cacheForwardInfo.setParticipantId(threadVo.getLastMessageVO().getForwardInfo().getParticipant().getId());
                            messageDao.insertParticipant(threadVo.getLastMessageVO().getForwardInfo().getParticipant());
                        }
                        if (threadVo.getLastMessageVO().getForwardInfo().getConversation() != null) {
                            cacheForwardInfo.setConversationId(threadVo.getLastMessageVO().getForwardInfo().getConversation().getId());
                            messageDao.insertConversationSummery(threadVo.getLastMessageVO().getForwardInfo().getConversation());
                        }
                    }
                }
                messageDao.insertThread(threadVo);
            }


        });

    }

    public void leaveThread(long threadId) {
        deleteLastMessageVo(threadId);
        messageDao.deleteThread(threadId);
        messageDao.deleteAllThreadParticipant(threadId);
        messageDao.deleteAllMessageByThread(threadId);
    }

    private void deleteLastMessageVo(long threadId) {
        long lastMsgId = messageDao.getLastMessageId(threadId);
        messageDao.deleteLastMessage(lastMsgId);
    }

    /**
     * Cache participant
     */

    public void saveParticipants(@NonNull List<CacheParticipant> participants, long threadId, int expireSecond) {

        for (CacheParticipant participant : participants) {

            participant.setThreadId(threadId);

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.SECOND, expireSecond);
            String expireDate = format.format(c.getTime());

            messageDao.insertParticipant(participant);

            CacheThreadParticipant ctp = new CacheThreadParticipant();
            ctp.setExpireDate(expireDate);
            ctp.setParticipantId(participant.getId());
            ctp.setThreadId(threadId);

            messageDao.insertThreadParticipant(ctp);

            if (!Util.isNullOrEmpty(participant.getRoles())) {

                CacheParticipantRoles cpr = new CacheParticipantRoles();

                cpr.setId(participant.getId());

                cpr.setThreadId(threadId);

                cpr.setRoles(participant.getRoles());

                Log.d("MTAG", "SAVE CPR: " + cpr);

                messageDao.insertRoles(cpr);

            }
        }
    }

    public void deleteParticipant(long threadId, long id) {
        messageDao.deleteParticipant(threadId, id);
    }

    /**
     * Participants have expire date after expTime that you can set it manually
     */


    public void updateParticipantRoles(long participantId, long threadId, List<String> roles) {

        new java.lang.Thread(new Runnable() {
            @Override
            public void run() {
                CacheParticipantRoles cpr = new CacheParticipantRoles();

                cpr.setId(participantId);

                cpr.setThreadId(threadId);

                cpr.setRoles(roles);

                messageDao.insertRoles(cpr);
            }
        }).start();

    }


    @NonNull
    public List<Participant> getThreadParticipant(long offset, long count, long threadId, @Nullable Boolean getAdmin) {

        getAdmin = getAdmin != null ? getAdmin : false;


        List<Participant> participants = new ArrayList<>();

        List<CacheThreadParticipant> listCtp = messageDao.getAllThreadParticipants(offset, count, threadId);
        if (listCtp == null) {
            return participants;
        } else {


            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            Date nowDate = c.getTime();

            for (CacheThreadParticipant threadParticipant : listCtp) {
                try {
                    Date expireDate = format.parse(threadParticipant.getExpireDate());
                    long participantId = threadParticipant.getParticipantId();

                    if (expireDate.compareTo(nowDate) < 0) {
                        messageDao.deleteCacheThreadParticipant(participantId);
                    } else {

                        CacheParticipant cParticipant = messageDao.getParticipant(participantId);

//                        if (getAdmin && !cParticipant.getAdmin()) {
//
//                            return participants;
//                        }

                        List<String> roles = new ArrayList<>();

                        if (getAdmin) {

                            CacheParticipantRoles cpr = messageDao.getParticipantRoles(participantId, threadId);

                            Log.d("MTAG", "CPR: " + cpr);


                            if (cpr != null) {


                                if (cpr.getRoles().size() > 0)
                                    roles = cpr.getRoles();

                            }


                        }


                        if (getAdmin) {

                            if (roles.size() > 0) {


                                Participant participant = cacheToParticipantMapper(cParticipant, true, roles);
                                participants.add(participant);

                            }

                        } else {

                            Participant participant = cacheToParticipantMapper(cParticipant, getAdmin, roles);
                            participants.add(participant);
                        }


                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return participants;
    }

    private Date createNewDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.getTime();
    }

    public long getParticipantCount(long threadId) {
        return messageDao.getParticipantCount(threadId);
    }

    //Cache contact
    @NonNull
    public Contact getContactById(long id) {
        CacheContact cacheContact = messageDao.getContactById(id);
        return new Contact(
                cacheContact.getId(),
                cacheContact.getFirstName(),
                cacheContact.getUserId(),
                cacheContact.getLastName(),
                cacheContact.getBlocked(),
                cacheContact.getCreationDate(),
                cacheContact.getLinkedUser(),
                cacheContact.getCellphoneNumber(),
                cacheContact.getEmail(),
                cacheContact.getUniqueId(),
                cacheContact.getNotSeenDuration(),
                cacheContact.isHasUser()
        );
    }

    @NonNull
    public List<Contact> getContactsByFirst(String firstName) {
        List<Contact> contacts = new ArrayList<>();
        List<CacheContact> cacheContacts = messageDao.getContactsByFirst(firstName);
        if (cacheContacts != null) {
            for (CacheContact cacheContact : cacheContacts) {
                Contact contact = new Contact(
                        cacheContact.getId(),
                        cacheContact.getFirstName(),
                        cacheContact.getUserId(),
                        cacheContact.getLastName(),
                        cacheContact.getBlocked(),
                        cacheContact.getCreationDate(),
                        cacheContact.getLinkedUser(),
                        cacheContact.getCellphoneNumber(),
                        cacheContact.getEmail(),
                        cacheContact.getUniqueId(),
                        cacheContact.getNotSeenDuration(),
                        cacheContact.isHasUser()
                );
                contacts.add(contact);
            }
        }
        return contacts;
    }

    @NonNull
    public List<Contact> getContactsByLast(String lastName) {
        List<Contact> contacts = new ArrayList<>();
        List<CacheContact> cacheContacts = messageDao.getContactsByLast(lastName);
        if (cacheContacts != null) {
            for (CacheContact cacheContact : cacheContacts) {
                Contact contact = new Contact(
                        cacheContact.getId(),
                        cacheContact.getFirstName(),
                        cacheContact.getUserId(),
                        cacheContact.getLastName(),
                        cacheContact.getBlocked(),
                        cacheContact.getCreationDate(),
                        cacheContact.getLinkedUser(),
                        cacheContact.getCellphoneNumber(),
                        cacheContact.getEmail(),
                        cacheContact.getUniqueId(),
                        cacheContact.getNotSeenDuration(),
                        cacheContact.isHasUser()
                );
                contacts.add(contact);
            }
        }
        return contacts;
    }

    @NonNull
    public List<Contact> getContactsByFirstAndLast(String firstName, String lastName) {
        List<Contact> contacts = new ArrayList<>();
        List<CacheContact> cacheContacts = messageDao.getContactsByFirstAndLast(firstName, lastName);
        if (cacheContacts != null) {
            for (CacheContact cacheContact : cacheContacts) {
                Contact contact = new Contact(
                        cacheContact.getId(),
                        cacheContact.getFirstName(),
                        cacheContact.getUserId(),
                        cacheContact.getLastName(),
                        cacheContact.getBlocked(),
                        cacheContact.getCreationDate(),
                        cacheContact.getLinkedUser(),
                        cacheContact.getCellphoneNumber(),
                        cacheContact.getEmail(),
                        cacheContact.getUniqueId(),
                        cacheContact.getNotSeenDuration(),
                        cacheContact.isHasUser()
                );
                contacts.add(contact);
            }
        }

        return contacts;
    }

    @NonNull
    public List<Contact> getContactByCell(String cellphoneNumber) {
        List<Contact> contacts = new ArrayList<>();
        List<CacheContact> cacheContacts = messageDao.getContactByCell(cellphoneNumber);
        if (cacheContacts != null) {
            for (CacheContact cacheContact : cacheContacts) {
                Contact contact = new Contact(
                        cacheContact.getId(),
                        cacheContact.getFirstName(),
                        cacheContact.getUserId(),
                        cacheContact.getLastName(),
                        cacheContact.getBlocked(),
                        cacheContact.getCreationDate(),
                        cacheContact.getLinkedUser(),
                        cacheContact.getCellphoneNumber(),
                        cacheContact.getEmail(),
                        cacheContact.getUniqueId(),
                        cacheContact.getNotSeenDuration(),
                        cacheContact.isHasUser()
                );
                contacts.add(contact);
            }
        }
        return contacts;
    }

    @NonNull
    public List<Contact> getContactsByEmail(String email) {
        List<Contact> contacts = new ArrayList<>();
        List<CacheContact> cacheContacts = messageDao.getContactsByEmail(email);

        if (cacheContacts != null) {
            for (CacheContact cacheContact : cacheContacts) {
                Contact contact = new Contact(
                        cacheContact.getId(),
                        cacheContact.getFirstName(),
                        cacheContact.getUserId(),
                        cacheContact.getLastName(),
                        cacheContact.getBlocked(),
                        cacheContact.getCreationDate(),
                        cacheContact.getLinkedUser(),
                        cacheContact.getCellphoneNumber(),
                        cacheContact.getEmail(),
                        cacheContact.getUniqueId(),
                        cacheContact.getNotSeenDuration(),
                        cacheContact.isHasUser()
                );
                contacts.add(contact);
            }
        }
        return contacts;
    }


    public void insertGap(GapMessageVO gapMessageVO) {


        worker(() -> messageDao.insertGap(gapMessageVO));


    }

    public void getAllGaps(long threadId, OnWorkDone listener) {


        worker(() -> {


            List<GapMessageVO> data = messageDao.getGapMessages(threadId);

            listener.onWorkDone(data);


        });


    }

    public List<GapMessageVO> getAllGaps(long threadId) {


        return messageDao.getGapMessages(threadId);


    }

    public void deleteAllGapsFrom(long threadId) {

        worker(() -> messageDao.deleteAllGapMessagesFrom(threadId));

    }


    private void worker(Runnable work) {
        new java.lang.Thread(work).start();
    }

    public void getGap(long id, OnWorkDone listener) {


        worker(() -> listener.onWorkDone(messageDao.getGap(id)));


    }

    public void deleteGapForMessageId(Long msgId) {


        worker(() -> messageDao.deleteGap(msgId));


    }

    public void updateMessageGapState(Long msgId, boolean hasGap) {

        worker(() -> {

            List<CacheMessageVO> cacheMessageVO = messageDao.getMessage(msgId);

            if (!Util.isNullOrEmpty(cacheMessageVO)) {

                for (CacheMessageVO msg :
                        cacheMessageVO) {
                    msg.setHasGap(hasGap);

                    messageDao.updateMessage(msg);
                }

            }

        });


    }

    public void deleteThread(long threadId) {

        worker(() -> messageDao.deleteThread(threadId));

    }

    public void deleteThreads(ArrayList<Long> deletedThreadsIds) {

        worker(() -> {

            for (Long id :
                    deletedThreadsIds) {

                messageDao.deleteThread(id);


            }

        });

    }


}
