package com.mindtrack.ai.repository;

import com.mindtrack.ai.model.Channel;
import com.mindtrack.ai.model.Conversation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for conversation persistence.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find all conversations for a user, ordered by most recent first.
     *
     * @param userId the user ID
     * @return list of conversations
     */
    List<Conversation> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * Find conversations for a user on a specific channel.
     *
     * @param userId the user ID
     * @param channel the communication channel
     * @return list of conversations
     */
    List<Conversation> findByUserIdAndChannelOrderByStartedAtDesc(Long userId, Channel channel);
}
