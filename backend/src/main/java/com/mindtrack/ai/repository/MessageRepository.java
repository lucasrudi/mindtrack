package com.mindtrack.ai.repository;

import com.mindtrack.ai.model.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for message persistence.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages in a conversation, ordered chronologically.
     *
     * @param conversationId the conversation ID
     * @return list of messages
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
