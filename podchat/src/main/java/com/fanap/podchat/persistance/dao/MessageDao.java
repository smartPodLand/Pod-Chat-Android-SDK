package com.fanap.podchat.persistance.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.fanap.podchat.cachemodel.CacheForwardInfo;
import com.fanap.podchat.cachemodel.CacheLastMessageVO;
import com.fanap.podchat.cachemodel.CacheMessageVO;
import com.fanap.podchat.cachemodel.CacheParticipant;
import com.fanap.podchat.cachemodel.CacheReplyInfoVO;
import com.fanap.podchat.cachemodel.ThreadVo;
import com.fanap.podchat.mainmodel.Contact;
import com.fanap.podchat.mainmodel.Inviter;
import com.fanap.podchat.mainmodel.UserInfo;
import com.fanap.podchat.model.ConversationSummery;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MessageDao {

    //Cache contact
    @Insert(onConflict = REPLACE)
    void insertContacts(List<Contact> t);

    @Insert(onConflict = REPLACE)
    void insertContact(Contact contact);

    @Query("select * from Contact")
    List<Contact> getContact();

    //Cache thread history
    @Insert(onConflict = REPLACE)
    void insertMessage(CacheMessageVO messageVO);

    @Query("DELETE FROM CacheMessageVO WHERE id = :id ")
    void deleteMessage(long id);

    @Insert(onConflict = REPLACE)
    void insertHistories(List<CacheMessageVO> messageVOS);

    @Query("select * from CacheMessageVO where threadVoId = :threadVoId ORDER BY time ASC LIMIT :count OFFSET :offset ")
    List<CacheMessageVO> getHistoriesASC(long count, long offset, long threadVoId);

    @Query("select * from CacheMessageVO where threadVoId = :threadVoId AND id >= :messageId ORDER BY time ASC LIMIT :count OFFSET :offset ")
    List<CacheMessageVO> getHistoriesMessageIdASC(long count, long offset, long threadVoId, long messageId);

    @Query("select * from CacheMessageVO where threadVoId = :threadVoId AND id >= :messageId ORDER BY time DESC LIMIT :count OFFSET :offset ")
    List<CacheMessageVO> getHistoriesMessageIdDESC(long count, long offset, long threadVoId, long messageId);

    @Query("select * from CacheMessageVO where threadVoId = :threadVoId AND id BETWEEN :firstMessageId AND :lastMessageId ORDER BY time ASC LIMIT :count OFFSET :offset ")
    List<CacheMessageVO> getHistoriesFandLASC(long count, long offset, long threadVoId, long firstMessageId, long lastMessageId);

    @Query("select * from CacheMessageVO where threadVoId = :threadVoId AND id BETWEEN :firstMessageId AND :lastMessageId ORDER BY time DESC LIMIT :count OFFSET :offset ")
    List<CacheMessageVO> getHistoriesFandLDESC(long count, long offset, long threadVoId, long firstMessageId, long lastMessageId);

    @Query("select * from CacheMessageVO where threadVoId = :threadVoId ORDER BY time DESC LIMIT :count OFFSET :offset ")
    List<CacheMessageVO> getHistoriesDESC(long count, long offset, long threadVoId);

    @Query("DELETE FROM CacheMessageVo WHERE threadVoId = :threadVoId AND time IN (select time from CacheMessageVO ORDER BY time ASC LIMIT :count OFFSET :offset )")
    void deleteMessageAfterOffsetTime(long count, long offset, long threadVoId);

    @Query("DELETE FROM CacheMessageVO WHERE threadVoId = :threadVoId  AND time IN(SELECT time FROM CacheMessageVO WHERE id BETWEEN :firstMessageId AND :lastMessageId ORDER BY time ASC)")
    void deleteMessageBetweenLastAndFirstASC(long threadVoId, long firstMessageId, long lastMessageId);

    @Query("DELETE FROM CacheMessageVO WHERE threadVoId = :threadVoId  AND time IN(SELECT time FROM CacheMessageVO WHERE id BETWEEN :firstMessageId AND :lastMessageId ORDER BY time DESC)")
    void deleteMessageBetweenLastAndFirstDESC(long threadVoId, long firstMessageId, long lastMessageId);

    @Query("DELETE FROM cachemessagevo WHERE threadVoId = :threadVoId  AND time IN (SELECT time FROM CacheMessageVO WHERE id >= :firstMessageId ORDER BY time ASC LIMIT :count OFFSET :offset )")
    void deleteMessageWithFirstMessageIdASC(long count, long offset, long threadVoId, long firstMessageId);

    @Query("DELETE FROM cachemessagevo WHERE threadVoId = :threadVoId  AND time IN (SELECT time FROM CacheMessageVO WHERE id >=:firstMessageId ORDER BY time DESC LIMIT :count OFFSET :offset )")
    void deleteMessageWithFirstMessageIdDESC(long count, long offset, long threadVoId, long firstMessageId);

    @Query("SELECT COUNT(id) FROM CacheMessageVO")
    long getHistoryCount();

    //Cache userInfo
    @Insert(onConflict = REPLACE)
    void insertUserInfo(UserInfo userInfo);

    @Query("select * from UserInfo")
    UserInfo getUserInfo();

    //Cache thread

    @Query("select COUNT(id) FROM THREADVO")
    int getThreadCount();

    @Query("select * from ThreadVo LIMIT :count OFFSET :offset")
    List<ThreadVo> getThreads(long count, long offset);

    @Query("select  * from ThreadVo where id = :id")
    ThreadVo getThreadById(long id);

    @Query("select  * from ThreadVo where title = :title")
    ThreadVo getThreadByName(String title);

    @Insert(onConflict = REPLACE)
    void insertThreads(List<ThreadVo> ThreadVo);

    @Insert(onConflict = REPLACE)
    void insertThread(ThreadVo threadVo);

    //cache inviter

    @Query("select * from Inviter where id = :inviterId ")
    Inviter getInviter(long inviterId);

    @Insert(onConflict = REPLACE)
    void insertInviter(Inviter inviter);

    //cache LastMessageVO
    @Insert(onConflict = REPLACE)
    void insertLastMessageVO(CacheLastMessageVO lastMessageVO);

    @Query("select * from CacheLastMessageVO where id = :LastMessageVOId")
    CacheLastMessageVO getLastMessageVO(long LastMessageVOId);


    //cache participant
    @Insert(onConflict = REPLACE)
    void insertParticipant(CacheParticipant participant);

    @Query("select * from CacheParticipant where id = :participantId")
    CacheParticipant getParticipant(long participantId);

    @Query("select COUNT(id) FROM CacheParticipant WHERE threadId = :threadId")
    int getParticipantCount(long threadId);

    @Query("select * from CacheParticipant WHERE :threadId ORDER BY name LIMIT :count OFFSET :offset ")
    List<CacheParticipant> geParticipants(long offset, long count, long threadId);

    @Query("select * from CacheParticipant WHERE threadId = :threadId")
    List<CacheParticipant> geParticipantsWithThreadId(long threadId);

    //Search contact
    @Query("select * from Contact where id = :id")
    Contact getContactById(long id);

    @Query("select * from contact where firstName LIKE :firstName ")
    List<Contact> getContactsByFirst(String firstName);

    @Query("select * from contact where lastName LIKE :lastName ")
    List<Contact> getContactsByLast(String lastName);

    @Query("select * from contact where firstName LIKE :firstName AND lastName LIKE :lastName")
    List<Contact> getContactsByFirstAndLast(String firstName, String lastName);

    @Query("select * from Contact where cellphoneNumber LIKE :cellphoneNumber")
    List<Contact> getContactByCell(String cellphoneNumber);

    @Query("select * from contact where email LIKE :email ")
    List<Contact> getContactsByEmail(String email);

    //cache replyInfoVO
    @Insert(onConflict = REPLACE)
    void insertReplyInfoVO(CacheReplyInfoVO replyInfoVO);

    @Query("select * from CacheReplyInfoVO where id = :replyInfoVOId")
    CacheReplyInfoVO getReplyInfo(long replyInfoVOId);

    //Cache ForwardInfo
    @Insert(onConflict = REPLACE)
    void insertForwardInfo(CacheForwardInfo forwardInfo);

    @Query("select * from CacheForwardInfo where forwardInfo_Id = :forwardInfoId ")
    CacheForwardInfo getForwardInfo(long forwardInfoId);

    //Cache ConversationSummery
    @Query("select * from ConversationSummery where id = :id")
    ConversationSummery getConversationSummery(long id);

    @Insert(onConflict = REPLACE)
    void insertConversationSummery(ConversationSummery conversationSummery);
}
