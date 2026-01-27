# PlayMatch - Product UX Enhancement Roadmap

**Version:** 1.0
**Date:** January 23, 2026
**Status:** Strategic Planning
**Author:** Product Manager

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [UX Enhancement Priority Matrix](#ux-enhancement-priority-matrix)
4. [Phase 1: Critical UX Improvements](#phase-1-critical-ux-improvements)
5. [Phase 2: Enhanced Engagement Features](#phase-2-enhanced-engagement-features)
6. [Phase 3: Advanced Features](#phase-3-advanced-features)
7. [Backend Changes Required](#backend-changes-required)
8. [Frontend Implementation Guide](#frontend-implementation-guide)
9. [Success Metrics](#success-metrics)

---

## Executive Summary

PlayMatch is a cricket match coordination platform with strong foundational features. However, several UX gaps exist that could significantly impact user engagement, retention, and overall satisfaction. This document outlines a phased approach to enhance user experience through both frontend improvements and necessary backend changes.

### Key Insights

- **Current Strength:** Solid technical foundation with clean architecture
- **Main Gap:** Limited real-time feedback and communication features
- **User Pain Points:**
  - Lack of match notifications
  - No chat/communication channel
  - Limited visibility into match updates
  - Missing trust/reputation system
  - Poor onboarding experience

### Strategic Priorities

1. **Reduce Friction:** Simplify user journeys and reduce steps to key actions
2. **Increase Transparency:** Provide real-time updates and visibility
3. **Build Trust:** Implement reputation and verification systems
4. **Enable Communication:** Add in-app messaging and notifications
5. **Improve Retention:** Add gamification and community features

---

## Current State Analysis

### What We Have ✅

| Feature | Status | UX Impact |
|---------|--------|-----------|
| Phone-based OTP Auth | ✅ Implemented | Good - Simple, fast login |
| Match Creation | ✅ Implemented | Good - Comprehensive form |
| Invite Sharing | ✅ Implemented | Good - Shareable links |
| YES/NO Response | ✅ Implemented | Fair - Binary choice, no context |
| Payment Tracking | ✅ Implemented | Fair - Manual, captain-only |
| Emergency Player System | ✅ Implemented | Good - Unique feature |
| Backout Logging | ✅ Implemented | Fair - Backend only, no UI |
| My Games | ✅ Implemented | Good - User dashboard |

### What's Missing ❌

| Feature | User Impact | Business Impact |
|---------|-------------|-----------------|
| Push Notifications | High - Users miss updates | High - Lower engagement |
| In-app Chat | High - Poor coordination | High - Use WhatsApp instead |
| Real-time Updates | High - Stale data | Medium - Multiple API calls |
| Player Reputation | High - Trust issues | High - Quality concerns |
| Onboarding Tutorial | Medium - Learning curve | Medium - Drop-off |
| Match Reminders | High - Forgetfulness | High - Last-minute cancellations |
| Payment Receipts | Medium - No proof | Medium - Disputes |
| Photo Upload | Medium - Less engagement | Low - Visual appeal |
| Match Analytics | Low - No insights | Medium - Data-driven decisions |
| Social Features | Low - Siloed experience | Medium - Organic growth |

---

## UX Enhancement Priority Matrix

### Priority Framework

```
IMPACT (User Value)
  ↑
  │  P2: Consider      │  P1: Critical
  │  - Match Analytics │  - Push Notifications
  │  - Social Sharing  │  - In-app Chat
  │  - Photo Upload    │  - Real-time Updates
  │                    │  - Player Reputation
  ├─────────────────────┼──────────────────────
  │  P3: Nice-to-have  │  P2: Important
  │  - Gamification    │  - Onboarding Flow
  │  - Leaderboards    │  - Match Reminders
  │  - Achievements    │  - Payment QR Codes
  │                    │  - Edit Match Details
  └─────────────────────┴──────────────────────→
                    EFFORT (Implementation Cost)
```

---

## Phase 1: Critical UX Improvements

### 1.1 Push Notifications System 🔔

**Problem:** Users have no way to know about match updates without manually checking the app.

**User Impact:** HIGH - Users miss critical information (invites, responses, cancellations, match time changes)

**Solution:**
- Implement push notification system using FCM (Firebase Cloud Messaging)
- Backend sends notifications for key events
- Frontend handles notification delivery and deep linking

#### Backend Changes Required

```java
// New Entity: NotificationPreference
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private MvpUser user;

    private String fcmToken; // Device token
    private boolean matchInvites = true;
    private boolean matchReminders = true;
    private boolean participantUpdates = true;
    private boolean paymentReminders = true;
    private boolean chatMessages = true;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType; // ANDROID, IOS, WEB

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}

// New Service: NotificationService
@Service
public class NotificationService {

    // Register device token
    public void registerDevice(Long userId, String fcmToken, DeviceType deviceType) {
        // Save token to database
    }

    // Send notification
    public void sendNotification(Long userId, NotificationType type, Map<String, String> data) {
        // Get user's FCM tokens
        // Build notification payload
        // Send via FCM
        // Log notification history
    }

    // Trigger points
    public void notifyMatchInvite(Long matchId, Long userId) { }
    public void notifyPlayerResponse(Long matchId, Long captainId, String playerName) { }
    public void notifyMatchReminder(Long matchId, int hoursBeforeMatch) { }
    public void notifyPaymentDue(Long matchId, Long userId) { }
    public void notifyMatchCancelled(Long matchId, List<Long> participantIds) { }
    public void notifyBackout(Long matchId, Long captainId, String playerName) { }
    public void notifyEmergencyRequest(Long matchId, Long captainId) { }
}
```

#### New API Endpoints

```
POST /v2/mvp/notifications/register
- Register device token
- Body: { fcmToken, deviceType }

PUT /v2/mvp/notifications/preferences
- Update notification preferences
- Body: { matchInvites, matchReminders, ... }

GET /v2/mvp/notifications/preferences
- Get current preferences

GET /v2/mvp/notifications/history
- Get notification history (last 30 days)
```

#### Frontend Implementation

```typescript
// 1. Request notification permission on app start
const requestNotificationPermission = async () => {
  const permission = await Notification.requestPermission();
  if (permission === 'granted') {
    const fcmToken = await getFCMToken();
    await api.registerDevice(fcmToken, 'WEB');
  }
};

// 2. Handle incoming notifications
messaging.onMessage((payload) => {
  const { title, body, data } = payload;

  // Show in-app notification
  showToast(title, body);

  // Handle deep link
  if (data.matchId) {
    // Update local state or navigate
  }
});

// 3. Handle notification clicks (when app is in background)
messaging.onBackgroundMessage((payload) => {
  const { data } = payload;

  // Navigate to relevant screen
  if (data.matchId) {
    window.location.href = `/match/${data.matchId}`;
  }
});

// 4. Settings screen
const NotificationSettings = () => {
  const [prefs, setPrefs] = useState(null);

  useEffect(() => {
    api.getNotificationPreferences().then(setPrefs);
  }, []);

  const handleToggle = async (key: string, value: boolean) => {
    await api.updateNotificationPreferences({ [key]: value });
    setPrefs({ ...prefs, [key]: value });
  };

  return (
    <div>
      <h2>Notification Settings</h2>
      <Toggle
        label="Match Invites"
        checked={prefs?.matchInvites}
        onChange={(v) => handleToggle('matchInvites', v)}
      />
      <Toggle
        label="Match Reminders (2 hours before)"
        checked={prefs?.matchReminders}
        onChange={(v) => handleToggle('matchReminders', v)}
      />
      {/* ... other toggles */}
    </div>
  );
};
```

#### Notification Triggers

| Event | Recipient | Notification |
|-------|-----------|--------------|
| Match Created | None | (Captain shares invite manually) |
| Player Responds YES | Captain | "🏏 {Name} is in for {Match}" |
| Player Responds NO | Captain | "❌ {Name} can't make it for {Match}" |
| Player Backs Out | Captain | "⚠️ {Name} backed out of {Match}" |
| Match Reminder | All Participants | "⏰ Match starts in 2 hours! {Match}" |
| Payment Due | Unpaid Participants | "💰 Payment pending for {Match}" |
| Match Cancelled | All Participants | "🚫 {Match} has been cancelled" |
| Emergency Request | Captain | "🚨 {Name} requested emergency spot for {Match}" |
| Emergency Approved | Requester | "✅ Your emergency request approved for {Match}" |
| Match Full | Captain | "✅ {Match} is full! All slots filled" |

---

### 1.2 In-App Chat / Match Discussion 💬

**Problem:** Players have no way to communicate within the app, forcing them to use WhatsApp groups.

**User Impact:** HIGH - Critical for coordination (directions, equipment, timing changes)

**Solution:**
- Per-match chat room for all participants
- Real-time messaging using WebSockets or polling
- Support text messages, location sharing, and images

#### Backend Changes Required

```java
// New Entity: ChatMessage
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private MvpUser sender;

    @Column(columnDefinition = "TEXT")
    private String messageText;

    @Enumerated(EnumType.STRING)
    private MessageType messageType; // TEXT, IMAGE, LOCATION, SYSTEM

    private String imageUrl; // If type = IMAGE
    private Double latitude;  // If type = LOCATION
    private Double longitude; // If type = LOCATION

    private LocalDateTime sentAt;

    @ElementCollection
    private List<Long> readByUserIds = new ArrayList<>(); // For read receipts

    private boolean isEdited = false;
    private boolean isDeleted = false;
}

// New Service: ChatService
@Service
public class ChatService {

    public ChatMessage sendMessage(Long matchId, Long senderId, String text) {
        // Validate user is participant or captain
        // Create message
        // Notify other participants via push notification
        // Return message
    }

    public List<ChatMessage> getMessages(Long matchId, Long userId, int limit, Long beforeMessageId) {
        // Validate user access
        // Fetch messages with pagination
        // Mark as read
        // Return messages
    }

    public void markAsRead(Long matchId, Long userId, Long lastReadMessageId) {
        // Update read receipts
    }

    public void deleteMessage(Long messageId, Long userId) {
        // Validate ownership
        // Soft delete
    }
}
```

#### New API Endpoints

```
POST /v2/mvp/matches/{matchId}/chat/messages
- Send a message
- Body: { messageText, messageType, imageUrl?, latitude?, longitude? }
- Returns: ChatMessage

GET /v2/mvp/matches/{matchId}/chat/messages
- Get messages (paginated)
- Query: limit=50, beforeMessageId=123
- Returns: List<ChatMessage>

PUT /v2/mvp/matches/{matchId}/chat/read
- Mark messages as read
- Body: { lastReadMessageId }

DELETE /v2/mvp/matches/{matchId}/chat/messages/{messageId}
- Delete own message

GET /v2/mvp/matches/{matchId}/chat/unread-count
- Get unread message count
- Returns: { count: 5 }
```

#### Frontend Implementation

```typescript
// 1. Chat Component
const MatchChat = ({ matchId }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const { user } = useAuth();

  // Fetch messages on mount
  useEffect(() => {
    loadMessages();

    // Poll for new messages every 3 seconds
    const interval = setInterval(loadMessages, 3000);
    return () => clearInterval(interval);
  }, [matchId]);

  const loadMessages = async () => {
    const data = await api.getChatMessages(matchId, 50);
    setMessages(data);

    // Mark as read
    if (data.length > 0) {
      await api.markChatAsRead(matchId, data[0].id);
    }
  };

  const handleSend = async () => {
    if (!newMessage.trim()) return;

    await api.sendChatMessage(matchId, {
      messageText: newMessage,
      messageType: 'TEXT'
    });

    setNewMessage('');
    loadMessages();
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map(msg => (
          <ChatBubble
            key={msg.id}
            message={msg}
            isOwn={msg.senderId === user.userId}
          />
        ))}
      </div>

      <div className="input-box">
        <input
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSend()}
          placeholder="Type a message..."
        />
        <button onClick={handleSend}>Send</button>
      </div>
    </div>
  );
};

// 2. Chat Bubble Component
const ChatBubble = ({ message, isOwn }) => {
  const formatTime = (date) => {
    return new Date(date).toLocaleTimeString('en-IN', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className={`bubble ${isOwn ? 'own' : 'other'}`}>
      {!isOwn && <div className="sender">{message.senderName}</div>}

      {message.messageType === 'TEXT' && (
        <div className="text">{message.messageText}</div>
      )}

      {message.messageType === 'IMAGE' && (
        <img src={message.imageUrl} alt="shared" />
      )}

      {message.messageType === 'LOCATION' && (
        <a
          href={`https://maps.google.com/?q=${message.latitude},${message.longitude}`}
          target="_blank"
        >
          📍 View Location
        </a>
      )}

      <div className="time">{formatTime(message.sentAt)}</div>
    </div>
  );
};

// 3. Unread Badge on Match Card
const MatchCard = ({ game }) => {
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    api.getUnreadChatCount(game.matchId).then(data => {
      setUnreadCount(data.count);
    });
  }, [game.matchId]);

  return (
    <div className="match-card" onClick={() => navigate(`/match/${game.matchId}`)}>
      <h3>{game.teamName}</h3>
      {unreadCount > 0 && (
        <span className="unread-badge">{unreadCount} new messages</span>
      )}
    </div>
  );
};
```

#### UX Enhancements

1. **System Messages:** Auto-generate chat messages for key events
   - "🎯 {Captain} created this match"
   - "✅ {Player} is in!"
   - "❌ {Player} can't make it"
   - "⚠️ {Player} backed out"

2. **Quick Replies:** Pre-defined responses for common questions
   - "On my way"
   - "Running late"
   - "Where exactly?"
   - "What should I bring?"

3. **Rich Content:**
   - Share location from match details
   - Share payment QR code
   - Share contact info

---

### 1.3 Real-Time Match Updates ⚡

**Problem:** Match details (participant count, slots) become stale. Users need to refresh manually.

**User Impact:** HIGH - Confusion about current match state

**Solution:**
- Implement WebSocket connection for live updates
- Or use Server-Sent Events (SSE) for one-way updates
- Or implement smart polling with exponential backoff

#### Backend Changes Required

```java
// Option 1: WebSocket (Recommended)
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

@Service
public class MatchWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Send update to all subscribers of a match
    public void broadcastMatchUpdate(Long matchId, MatchUpdateEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/match/" + matchId,
            event
        );
    }

    // Trigger points
    public void notifyParticipantJoined(Long matchId, String playerName) {
        broadcastMatchUpdate(matchId, new MatchUpdateEvent(
            "PARTICIPANT_JOINED",
            Map.of("playerName", playerName)
        ));
    }

    public void notifyMatchUpdated(Long matchId, Match match) {
        broadcastMatchUpdate(matchId, new MatchUpdateEvent(
            "MATCH_UPDATED",
            Map.of("match", toPublicView(match))
        ));
    }
}

// DTO: MatchUpdateEvent
public class MatchUpdateEvent {
    private String eventType; // PARTICIPANT_JOINED, MATCH_UPDATED, CHAT_MESSAGE, etc.
    private Map<String, Object> data;
    private LocalDateTime timestamp;
}
```

#### Frontend Implementation

```typescript
// 1. WebSocket Hook
const useMatchUpdates = (matchId: string) => {
  const [match, setMatch] = useState(null);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  useEffect(() => {
    // Connect to WebSocket
    const client = new StompClient({
      brokerURL: 'ws://localhost:8080/ws',
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      // Subscribe to match updates
      client.subscribe(`/topic/match/${matchId}`, (message) => {
        const event = JSON.parse(message.body);
        handleMatchUpdate(event);
      });
    };

    client.activate();

    return () => client.deactivate();
  }, [matchId]);

  const handleMatchUpdate = (event: MatchUpdateEvent) => {
    switch (event.eventType) {
      case 'PARTICIPANT_JOINED':
        showToast(`${event.data.playerName} joined!`);
        setLastUpdate(new Date());
        break;

      case 'MATCH_UPDATED':
        setMatch(event.data.match);
        break;

      case 'CHAT_MESSAGE':
        // Trigger chat refresh
        break;
    }
  };

  return { match, lastUpdate };
};

// 2. Usage in Match Details Screen
const MatchDetailsScreen = ({ matchId }) => {
  const { match, lastUpdate } = useMatchUpdates(matchId);

  return (
    <div>
      <h1>{match?.teamName}</h1>

      {/* Live indicator */}
      <div className="live-badge">
        <span className="pulse"></span> Live
      </div>

      {/* Participant count updates in real-time */}
      <div className="slots">
        <span>{match?.teamCount}/{match?.requiredPlayers} Team</span>
        <span>{match?.backupCount}/{match?.backupSlots} Backup</span>
      </div>

      <small>Last updated: {formatRelativeTime(lastUpdate)}</small>
    </div>
  );
};
```

---

### 1.4 Player Reputation System ⭐

**Problem:** No way to know if a player is reliable or has a history of backing out.

**User Impact:** HIGH - Trust issues, captains hesitate to accept unknown players

**Solution:**
- Track player reliability metrics
- Display reputation score on profiles
- Show backout history
- Allow captains to rate players after match

#### Backend Changes Required

```java
// New Entity: PlayerReputation
@Entity
@Table(name = "player_reputation")
public class PlayerReputation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private MvpUser user;

    // Core Metrics
    private int totalMatches = 0;
    private int completedMatches = 0;
    private int backedOutMatches = 0;
    private int noShowMatches = 0;

    // Calculated Scores (0-100)
    private double reliabilityScore = 100.0; // Based on completion rate
    private double responseScore = 100.0;    // Based on response time
    private double paymentScore = 100.0;     // Based on payment timeliness

    // Overall Score (weighted average)
    private double overallScore = 100.0;

    // Additional Metrics
    private int averageResponseTimeMinutes = 0; // Avg time to respond to invites
    private int onTimePayments = 0;
    private int latePayments = 0;

    // Ratings from Captains (post-match)
    private double avgCaptainRating = 0.0; // 1-5 stars
    private int totalRatings = 0;

    private LocalDateTime lastUpdated;
}

// New Entity: PostMatchRating
@Entity
@Table(name = "post_match_ratings")
public class PostMatchRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "rated_user_id")
    private MvpUser ratedUser;

    @ManyToOne
    @JoinColumn(name = "rater_user_id")
    private MvpUser raterUser; // Captain

    private int rating; // 1-5 stars

    @Enumerated(EnumType.STRING)
    private RatingCategory category; // PUNCTUALITY, SKILL, ATTITUDE, PAYMENT

    private String comment; // Optional

    private LocalDateTime ratedAt;
}

// Service: ReputationService
@Service
public class ReputationService {

    public void recalculateReputation(Long userId) {
        PlayerReputation rep = getOrCreateReputation(userId);

        // Calculate reliability score
        double completionRate = (double) rep.getCompletedMatches() / rep.getTotalMatches();
        rep.setReliabilityScore(completionRate * 100);

        // Calculate response score (faster response = higher score)
        // If avg response < 30 min = 100, > 24 hours = 50
        double responseScore = calculateResponseScore(rep.getAverageResponseTimeMinutes());
        rep.setResponseScore(responseScore);

        // Calculate payment score
        double paymentRate = (double) rep.getOnTimePayments() /
                            (rep.getOnTimePayments() + rep.getLatePayments());
        rep.setPaymentScore(paymentRate * 100);

        // Calculate overall score (weighted average)
        double overallScore = (rep.getReliabilityScore() * 0.5) +
                             (rep.getResponseScore() * 0.2) +
                             (rep.getPaymentScore() * 0.2) +
                             (rep.getAvgCaptainRating() * 20 * 0.1); // Convert 5-star to 100
        rep.setOverallScore(overallScore);

        reputationRepository.save(rep);
    }

    public void recordMatchCompletion(Long userId, Long matchId) {
        // Update reputation metrics
        // Recalculate scores
    }

    public void recordBackout(Long userId, Long matchId, BackoutReason reason) {
        // Update reputation metrics
        // Apply penalty based on reason (GENUINE = small, NO_SHOW = large)
        // Recalculate scores
    }
}
```

#### New API Endpoints

```
GET /v2/mvp/players/{userId}/reputation
- Get player reputation
- Returns: PlayerReputationResponse

POST /v2/mvp/matches/{matchId}/rate-player
- Rate a player (captain-only, after match completion)
- Body: { ratedUserId, rating, category, comment }

GET /v2/mvp/players/{userId}/match-history
- Get player's match history (public view)
- Returns: List of matches with completion status
```

#### Frontend Implementation

```typescript
// 1. Reputation Badge Component
const ReputationBadge = ({ score }) => {
  const getLevel = (score: number) => {
    if (score >= 90) return { label: 'Excellent', color: 'green', icon: '🏆' };
    if (score >= 75) return { label: 'Good', color: 'blue', icon: '⭐' };
    if (score >= 60) return { label: 'Average', color: 'yellow', icon: '👍' };
    return { label: 'Poor', color: 'red', icon: '⚠️' };
  };

  const level = getLevel(score);

  return (
    <div className={`reputation-badge ${level.color}`}>
      <span>{level.icon}</span>
      <span>{level.label}</span>
      <span>{score.toFixed(0)}</span>
    </div>
  );
};

// 2. Player Profile with Reputation
const PlayerProfile = ({ userId }) => {
  const [reputation, setReputation] = useState(null);

  useEffect(() => {
    api.getPlayerReputation(userId).then(setReputation);
  }, [userId]);

  if (!reputation) return <Loading />;

  return (
    <div className="player-profile">
      <h2>{reputation.playerName}</h2>

      {/* Overall Score */}
      <div className="overall-score">
        <ReputationBadge score={reputation.overallScore} />
      </div>

      {/* Detailed Metrics */}
      <div className="metrics">
        <MetricCard
          title="Reliability"
          score={reputation.reliabilityScore}
          details={`${reputation.completedMatches}/${reputation.totalMatches} matches completed`}
        />

        <MetricCard
          title="Response Time"
          score={reputation.responseScore}
          details={`Avg ${reputation.averageResponseTimeMinutes} min to respond`}
        />

        <MetricCard
          title="Payment"
          score={reputation.paymentScore}
          details={`${reputation.onTimePayments} on-time payments`}
        />
      </div>

      {/* Captain Ratings */}
      {reputation.totalRatings > 0 && (
        <div className="ratings">
          <h3>Captain Ratings</h3>
          <div className="stars">
            {'⭐'.repeat(Math.round(reputation.avgCaptainRating))}
            {'☆'.repeat(5 - Math.round(reputation.avgCaptainRating))}
          </div>
          <span>({reputation.totalRatings} ratings)</span>
        </div>
      )}

      {/* Red Flags */}
      {reputation.backedOutMatches > 3 && (
        <div className="warning">
          ⚠️ {reputation.backedOutMatches} backouts in last 10 matches
        </div>
      )}
    </div>
  );
};

// 3. Rate Player Screen (Captain-only, post-match)
const RatePlayerScreen = ({ matchId }) => {
  const [participants, setParticipants] = useState([]);

  useEffect(() => {
    api.getMatch(matchId).then(data => {
      setParticipants(data.participants);
    });
  }, [matchId]);

  const handleRate = async (playerId: number, rating: number) => {
    await api.ratePlayer(matchId, {
      ratedUserId: playerId,
      rating,
      category: 'OVERALL',
      comment: ''
    });

    showToast('Rating submitted!');
  };

  return (
    <div className="rate-players">
      <h2>Rate Your Teammates</h2>
      <p>Your feedback helps build a trustworthy community</p>

      {participants.map(player => (
        <div key={player.userId} className="player-card">
          <div className="info">
            <strong>{player.name}</strong>
            <span>{player.phoneNumber}</span>
          </div>

          <div className="rating">
            {[1, 2, 3, 4, 5].map(star => (
              <button
                key={star}
                onClick={() => handleRate(player.userId, star)}
              >
                {star <= (player.rating || 0) ? '⭐' : '☆'}
              </button>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};

// 4. Show Reputation in Participant List
const ParticipantList = ({ participants }) => {
  return (
    <div className="participants">
      {participants.map(p => (
        <div key={p.userId} className="participant">
          <div className="name">{p.name}</div>
          <div className="reputation">
            <ReputationBadge score={p.reputationScore} />
          </div>
        </div>
      ))}
    </div>
  );
};
```

---

## Phase 2: Enhanced Engagement Features

### 2.1 Interactive Onboarding Flow 🎯

**Problem:** New users don't understand how the app works.

**User Impact:** MEDIUM - Initial confusion, potential drop-off

**Solution:**
- Step-by-step tutorial on first launch
- Interactive tooltips for key features
- Sample match walkthrough

#### Frontend Implementation (No Backend Changes)

```typescript
// 1. Onboarding Wizard
const OnboardingWizard = () => {
  const [step, setStep] = useState(0);

  const steps = [
    {
      title: "Welcome to PlayMatch! 🏏",
      description: "The easiest way to organize cricket matches",
      illustration: "/assets/onboarding-1.png"
    },
    {
      title: "Create a Match",
      description: "Set date, time, location, and player requirements",
      illustration: "/assets/onboarding-2.png"
    },
    {
      title: "Share Invite Link",
      description: "Share via WhatsApp, SMS, or any messaging app",
      illustration: "/assets/onboarding-3.png"
    },
    {
      title: "Track Responses",
      description: "See who's in, who's backup, and manage payments",
      illustration: "/assets/onboarding-4.png"
    },
    {
      title: "Emergency Players",
      description: "Last-minute cancellation? Request emergency players!",
      illustration: "/assets/onboarding-5.png"
    }
  ];

  const handleComplete = () => {
    localStorage.setItem('onboardingComplete', 'true');
    navigate('/home');
  };

  return (
    <div className="onboarding">
      <div className="step">
        <img src={steps[step].illustration} alt="" />
        <h2>{steps[step].title}</h2>
        <p>{steps[step].description}</p>
      </div>

      <div className="controls">
        {step > 0 && (
          <button onClick={() => setStep(step - 1)}>Back</button>
        )}

        {step < steps.length - 1 ? (
          <button onClick={() => setStep(step + 1)}>Next</button>
        ) : (
          <button onClick={handleComplete}>Get Started!</button>
        )}
      </div>

      <div className="progress">
        {steps.map((_, i) => (
          <div
            key={i}
            className={`dot ${i === step ? 'active' : ''}`}
            onClick={() => setStep(i)}
          />
        ))}
      </div>
    </div>
  );
};
```

---

### 2.2 Match Reminders ⏰

**Problem:** Players forget about matches they confirmed for.

**User Impact:** MEDIUM - Last-minute no-shows

**Solution:**
- Automated reminders 24 hours, 2 hours, and 30 minutes before match
- In-app alerts + push notifications
- Quick action buttons (confirm/cancel)

#### Backend Changes Required

```java
// Scheduled Task
@Component
public class MatchReminderScheduler {

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void sendMatchReminders() {
        LocalDateTime now = LocalDateTime.now();

        // Find matches starting in 24 hours
        List<Match> matches24h = matchRepository.findMatchesStartingBetween(
            now.plusHours(23).plusMinutes(55),
            now.plusHours(24).plusMinutes(5)
        );

        matches24h.forEach(match -> {
            List<Long> participantIds = getParticipantIds(match);
            participantIds.forEach(userId -> {
                notificationService.sendNotification(userId,
                    NotificationType.MATCH_REMINDER_24H,
                    Map.of("matchId", match.getId().toString())
                );
            });
        });

        // Repeat for 2 hours and 30 minutes
    }
}
```

#### Frontend Implementation

```typescript
// Reminder notification with quick actions
messaging.onMessage((payload) => {
  if (payload.data.type === 'MATCH_REMINDER') {
    showNotificationWithActions(
      payload.notification.title,
      payload.notification.body,
      [
        { label: 'I\'ll be there', action: 'confirm' },
        { label: 'Can\'t make it', action: 'cancel' }
      ],
      (action) => {
        if (action === 'cancel') {
          // Show backout form
          navigate(`/match/${payload.data.matchId}/backout`);
        } else {
          // Silently confirm
          showToast('Great! See you there!');
        }
      }
    );
  }
});
```

---

### 2.3 Payment QR Code & Receipts 💰

**Problem:** Manual payment tracking is cumbersome. No proof of payment.

**User Impact:** MEDIUM - Payment disputes, tracking overhead

**Solution:**
- Generate UPI QR code for captain's UPI ID
- Auto-generate payment receipts
- Payment reminders for unpaid players

#### Backend Changes Required

```java
// Extend Match entity
@Entity
public class Match {
    // ... existing fields

    private String captainUpiId; // e.g., captain@upi

    @Column(columnDefinition = "TEXT")
    private String paymentQrCodeUrl; // Generated QR code image
}

// Service: PaymentService
@Service
public class PaymentService {

    public String generatePaymentQrCode(String upiId, double amount, String note) {
        // Generate UPI URL: upi://pay?pa=captain@upi&pn=Name&am=200&cu=INR&tn=Match%20Fee
        String upiUrl = String.format(
            "upi://pay?pa=%s&am=%.2f&cu=INR&tn=%s",
            upiId, amount, URLEncoder.encode(note, StandardCharsets.UTF_8)
        );

        // Generate QR code image
        String qrCodeImageUrl = qrCodeGenerator.generate(upiUrl);

        return qrCodeImageUrl;
    }

    public PaymentReceipt generateReceipt(Long matchId, Long userId) {
        // Generate PDF receipt with match details, amount, timestamp
        // Upload to cloud storage
        // Return receipt URL
    }
}
```

#### New API Endpoints

```
POST /v2/mvp/matches/{matchId}/payment-qr
- Generate payment QR code
- Body: { captainUpiId }
- Returns: { qrCodeUrl }

GET /v2/mvp/matches/{matchId}/payment-receipt/{userId}
- Download payment receipt (after payment marked)
- Returns: PDF file
```

#### Frontend Implementation

```typescript
// Payment QR Code Screen
const PaymentScreen = ({ matchId }) => {
  const [qrCode, setQrCode] = useState(null);
  const [match, setMatch] = useState(null);

  useEffect(() => {
    api.getMatch(matchId).then(setMatch);

    if (match?.paymentQrCodeUrl) {
      setQrCode(match.paymentQrCodeUrl);
    }
  }, [matchId]);

  return (
    <div className="payment-screen">
      <h2>Payment Details</h2>

      <div className="amount">
        <strong>₹{match?.feePerPerson}</strong>
        <span>per person</span>
      </div>

      {/* UPI QR Code */}
      {qrCode && (
        <div className="qr-code">
          <h3>Scan to Pay</h3>
          <img src={qrCode} alt="UPI QR Code" />
          <p>Scan with any UPI app (Google Pay, PhonePe, Paytm)</p>
        </div>
      )}

      {/* Manual UPI ID */}
      <div className="upi-id">
        <strong>Or pay to UPI ID:</strong>
        <div className="copyable">
          <code>{match?.captainUpiId}</code>
          <button onClick={() => copyToClipboard(match?.captainUpiId)}>
            Copy
          </button>
        </div>
      </div>

      {/* Payment Status */}
      {match?.userPaymentStatus === 'PAID' ? (
        <div className="paid-status">
          ✅ Payment confirmed
          <button onClick={() => downloadReceipt(matchId)}>
            Download Receipt
          </button>
        </div>
      ) : (
        <div className="unpaid-status">
          ⏳ Waiting for captain to confirm payment
        </div>
      )}
    </div>
  );
};
```

---

### 2.4 Edit Match Details 📝

**Problem:** Captains cannot edit match details after creation (time, venue, overs, etc.).

**User Impact:** MEDIUM - Need to cancel and recreate match for small changes

**Solution:**
- Allow captains to edit match details before match starts
- Notify all participants about changes
- Track edit history

#### Backend Changes Required

```java
// New Entity: MatchEditHistory
@Entity
@Table(name = "match_edit_history")
public class MatchEditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "edited_by_user_id")
    private MvpUser editedBy;

    private String fieldName; // "startTime", "groundMapsUrl", etc.
    private String oldValue;
    private String newValue;

    private LocalDateTime editedAt;
}

// Service: MatchService (extend)
@Service
public class MatchService {

    public void updateMatch(Long matchId, Long captainId, MatchUpdateRequest request) {
        Match match = findById(matchId);

        // Validate captain ownership
        if (!match.getCaptain().getId().equals(captainId)) {
            throw new ForbiddenException("Only captain can edit match");
        }

        // Validate match hasn't started
        if (match.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot edit past matches");
        }

        // Track changes
        List<MatchEditHistory> edits = new ArrayList<>();

        if (request.getStartTime() != null) {
            edits.add(createEditRecord(match, "startTime",
                match.getStartTime().toString(),
                request.getStartTime().toString()));
            match.setStartTime(request.getStartTime());
        }

        if (request.getGroundMapsUrl() != null) {
            edits.add(createEditRecord(match, "groundMapsUrl",
                match.getGroundMapsUrl(),
                request.getGroundMapsUrl()));
            match.setGroundMapsUrl(request.getGroundMapsUrl());
        }

        // Save changes
        matchRepository.save(match);
        matchEditHistoryRepository.saveAll(edits);

        // Notify all participants
        notifyMatchUpdated(match, edits);
    }
}
```

#### New API Endpoints

```
PUT /v2/mvp/matches/{matchId}
- Update match details (captain-only)
- Body: MatchUpdateRequest (all fields optional)
- Returns: Updated match details

GET /v2/mvp/matches/{matchId}/edit-history
- Get edit history
- Returns: List<MatchEditHistory>
```

#### Frontend Implementation

```typescript
// Edit Match Screen
const EditMatchScreen = ({ matchId }) => {
  const [match, setMatch] = useState(null);
  const [form, setForm] = useState({});

  useEffect(() => {
    api.getMatch(matchId).then(data => {
      setMatch(data);
      setForm({
        startTime: data.startTime,
        groundMapsUrl: data.groundMapsUrl,
        overs: data.overs,
        feePerPerson: data.feePerPerson,
        // ... other fields
      });
    });
  }, [matchId]);

  const handleSave = async () => {
    await api.updateMatch(matchId, form);
    showToast('Match updated! All participants have been notified.');
    navigate(`/match/${matchId}`);
  };

  return (
    <div className="edit-match">
      <h2>Edit Match Details</h2>

      <div className="warning">
        ⚠️ All participants will be notified about changes
      </div>

      <form>
        <DateTimePicker
          label="Start Time"
          value={form.startTime}
          onChange={(v) => setForm({ ...form, startTime: v })}
        />

        <Input
          label="Google Maps URL"
          value={form.groundMapsUrl}
          onChange={(v) => setForm({ ...form, groundMapsUrl: v })}
        />

        <Input
          label="Overs"
          type="number"
          value={form.overs}
          onChange={(v) => setForm({ ...form, overs: v })}
        />

        <Input
          label="Fee per Person"
          type="number"
          value={form.feePerPerson}
          onChange={(v) => setForm({ ...form, feePerPerson: v })}
        />

        <div className="buttons">
          <button type="button" onClick={() => navigate(-1)}>Cancel</button>
          <button type="button" onClick={handleSave}>Save Changes</button>
        </div>
      </form>
    </div>
  );
};
```

---

## Phase 3: Advanced Features

### 3.1 Match Photos & Highlights 📸

**Problem:** No visual memory of matches played.

**User Impact:** LOW - Missed engagement opportunity

**Solution:**
- Allow participants to upload match photos
- Create match gallery
- Share highlights

#### Backend Changes Required

```java
// New Entity: MatchPhoto
@Entity
@Table(name = "match_photos")
public class MatchPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "uploaded_by_user_id")
    private MvpUser uploadedBy;

    private String photoUrl; // S3/Cloud storage URL
    private String thumbnailUrl;

    private String caption;

    private LocalDateTime uploadedAt;
}
```

---

### 3.2 Player Analytics Dashboard 📊

**Problem:** No insights into personal performance, trends.

**User Impact:** LOW - Missed retention opportunity

**Solution:**
- Personal stats dashboard
- Participation trends
- Payment history
- Favorite grounds

#### Frontend Implementation (No Backend Changes)

```typescript
const AnalyticsDashboard = () => {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    // Derive from existing /my-games API
    api.getMyGames().then(data => {
      const analytics = {
        totalMatches: data.totalCount,
        completedMatches: data.completedCount,
        completionRate: (data.completedCount / data.totalCount) * 100,
        totalSpent: calculateTotalSpent(data.games),
        favoriteGround: findMostFrequentGround(data.games),
        // ... more derived stats
      };
      setStats(analytics);
    });
  }, []);

  return (
    <div className="analytics">
      <StatCard title="Total Matches" value={stats.totalMatches} icon="🏏" />
      <StatCard title="Completion Rate" value={`${stats.completionRate}%`} icon="✅" />
      <StatCard title="Total Spent" value={`₹${stats.totalSpent}`} icon="💰" />
      <StatCard title="Favorite Ground" value={stats.favoriteGround} icon="📍" />
    </div>
  );
};
```

---

### 3.3 Social Features 🤝

**Problem:** Users operate in isolation, no community feel.

**User Impact:** LOW - Missed viral growth opportunity

**Solution:**
- Invite friends to app (referral system)
- Share match results on social media
- Public leaderboards

---

## Backend Changes Required - Summary

### Database Schema Changes

```sql
-- New Tables Required

-- 1. Notification System
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES mvp_users(id),
    fcm_token VARCHAR(255),
    device_type VARCHAR(20),
    match_invites BOOLEAN DEFAULT true,
    match_reminders BOOLEAN DEFAULT true,
    participant_updates BOOLEAN DEFAULT true,
    payment_reminders BOOLEAN DEFAULT true,
    chat_messages BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    last_updated TIMESTAMP
);

CREATE TABLE notification_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES mvp_users(id),
    notification_type VARCHAR(50),
    title VARCHAR(255),
    body TEXT,
    data JSONB,
    sent_at TIMESTAMP,
    read_at TIMESTAMP
);

-- 2. Chat System
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT REFERENCES matches(id),
    sender_id BIGINT REFERENCES mvp_users(id),
    message_text TEXT,
    message_type VARCHAR(20), -- TEXT, IMAGE, LOCATION, SYSTEM
    image_url VARCHAR(500),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    sent_at TIMESTAMP,
    is_edited BOOLEAN DEFAULT false,
    is_deleted BOOLEAN DEFAULT false
);

CREATE TABLE chat_read_receipts (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT REFERENCES chat_messages(id),
    user_id BIGINT REFERENCES mvp_users(id),
    read_at TIMESTAMP
);

-- 3. Reputation System
CREATE TABLE player_reputation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES mvp_users(id) UNIQUE,
    total_matches INT DEFAULT 0,
    completed_matches INT DEFAULT 0,
    backed_out_matches INT DEFAULT 0,
    no_show_matches INT DEFAULT 0,
    reliability_score DOUBLE PRECISION DEFAULT 100.0,
    response_score DOUBLE PRECISION DEFAULT 100.0,
    payment_score DOUBLE PRECISION DEFAULT 100.0,
    overall_score DOUBLE PRECISION DEFAULT 100.0,
    average_response_time_minutes INT DEFAULT 0,
    on_time_payments INT DEFAULT 0,
    late_payments INT DEFAULT 0,
    avg_captain_rating DOUBLE PRECISION DEFAULT 0.0,
    total_ratings INT DEFAULT 0,
    last_updated TIMESTAMP
);

CREATE TABLE post_match_ratings (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT REFERENCES matches(id),
    rated_user_id BIGINT REFERENCES mvp_users(id),
    rater_user_id BIGINT REFERENCES mvp_users(id),
    rating INT CHECK (rating >= 1 AND rating <= 5),
    category VARCHAR(50), -- PUNCTUALITY, SKILL, ATTITUDE, PAYMENT
    comment TEXT,
    rated_at TIMESTAMP,
    UNIQUE(match_id, rated_user_id, rater_user_id)
);

-- 4. Match Edit History
CREATE TABLE match_edit_history (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT REFERENCES matches(id),
    edited_by_user_id BIGINT REFERENCES mvp_users(id),
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    edited_at TIMESTAMP
);

-- 5. Match Photos
CREATE TABLE match_photos (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT REFERENCES matches(id),
    uploaded_by_user_id BIGINT REFERENCES mvp_users(id),
    photo_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    caption TEXT,
    uploaded_at TIMESTAMP
);

-- 6. Extend Existing Tables
ALTER TABLE matches ADD COLUMN captain_upi_id VARCHAR(100);
ALTER TABLE matches ADD COLUMN payment_qr_code_url TEXT;
ALTER TABLE matches ADD COLUMN last_edited_at TIMESTAMP;
```

### New API Endpoints Summary

| Feature | Endpoints | Count |
|---------|-----------|-------|
| **Notifications** | POST /register, PUT /preferences, GET /preferences, GET /history | 4 |
| **Chat** | POST /messages, GET /messages, PUT /read, DELETE /messages/{id}, GET /unread-count | 5 |
| **Reputation** | GET /reputation, POST /rate-player, GET /match-history | 3 |
| **Match Edit** | PUT /matches/{id}, GET /edit-history | 2 |
| **Payments** | POST /payment-qr, GET /receipt | 2 |
| **Photos** | POST /upload, GET /gallery, DELETE /photo/{id} | 3 |
| **TOTAL** | | **19 new endpoints** |

### Configuration Changes Required

```properties
# application.properties additions

# Firebase Cloud Messaging
fcm.api-key=${FCM_API_KEY}
fcm.sender-id=${FCM_SENDER_ID}

# WebSocket
spring.websocket.enabled=true

# File Upload (for photos)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Cloud Storage (AWS S3 or similar)
cloud.storage.bucket-name=${S3_BUCKET_NAME}
cloud.storage.region=${S3_REGION}
cloud.storage.access-key=${AWS_ACCESS_KEY}
cloud.storage.secret-key=${AWS_SECRET_KEY}

# Scheduled Tasks
spring.task.scheduling.pool.size=5
```

### External Dependencies

```xml
<!-- Add to pom.xml -->

<!-- Firebase Admin SDK for push notifications -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>

<!-- WebSocket support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- QR Code generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>

<!-- AWS S3 for file storage -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.529</version>
</dependency>
```

---

## Frontend Implementation Guide

### Tech Stack Recommendations

```json
{
  "core": {
    "framework": "React 18+ or Vue 3+",
    "language": "TypeScript",
    "build": "Vite",
    "routing": "React Router v6 / Vue Router v4"
  },
  "ui": {
    "styling": "Tailwind CSS / Material-UI",
    "components": "shadcn/ui / Vuetify",
    "animations": "Framer Motion / GSAP"
  },
  "state": {
    "local": "React Context / Pinia",
    "server": "TanStack Query (React Query)",
    "forms": "React Hook Form / VeeValidate"
  },
  "notifications": {
    "push": "Firebase Cloud Messaging SDK",
    "toast": "react-hot-toast / vue-toastification"
  },
  "realtime": {
    "websocket": "SockJS + STOMP / Socket.io",
    "polling": "TanStack Query with refetchInterval"
  },
  "maps": {
    "provider": "Google Maps JavaScript API",
    "wrapper": "@react-google-maps/api / vue-google-maps"
  },
  "misc": {
    "http": "Axios / Fetch API",
    "dates": "date-fns / Day.js",
    "validation": "Zod / Yup",
    "qr-scanner": "html5-qrcode"
  }
}
```

### Folder Structure

```
src/
├── api/
│   ├── client.ts          # Axios instance
│   ├── auth.ts            # Auth API calls
│   ├── matches.ts         # Match API calls
│   ├── chat.ts            # Chat API calls
│   ├── notifications.ts   # Notification API calls
│   └── reputation.ts      # Reputation API calls
│
├── components/
│   ├── common/
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Modal.tsx
│   │   └── Toast.tsx
│   ├── match/
│   │   ├── MatchCard.tsx
│   │   ├── ParticipantList.tsx
│   │   ├── MatchDetails.tsx
│   │   └── PaymentQRCode.tsx
│   ├── chat/
│   │   ├── ChatWindow.tsx
│   │   ├── ChatBubble.tsx
│   │   └── ChatInput.tsx
│   └── reputation/
│       ├── ReputationBadge.tsx
│       └── PlayerProfile.tsx
│
├── hooks/
│   ├── useAuth.ts
│   ├── useMatch.ts
│   ├── useChat.ts
│   ├── useNotifications.ts
│   └── useWebSocket.ts
│
├── contexts/
│   ├── AuthContext.tsx
│   └── NotificationContext.tsx
│
├── screens/
│   ├── auth/
│   │   ├── LoginScreen.tsx
│   │   └── ProfileScreen.tsx
│   ├── match/
│   │   ├── CreateMatchScreen.tsx
│   │   ├── MatchDetailsScreen.tsx
│   │   ├── EditMatchScreen.tsx
│   │   └── InviteScreen.tsx
│   ├── games/
│   │   └── MyGamesScreen.tsx
│   └── settings/
│       └── NotificationSettingsScreen.tsx
│
├── types/
│   ├── api.ts             # API response types
│   ├── match.ts
│   ├── user.ts
│   └── chat.ts
│
├── utils/
│   ├── dateHelpers.ts
│   ├── errorMessages.ts
│   ├── validators.ts
│   └── constants.ts
│
└── App.tsx
```

### Key Implementation Patterns

#### 1. API Client with Interceptors

```typescript
// api/client.ts
import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 10000,
});

// Request interceptor - add auth token
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle errors
client.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired, try refresh
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post('/v2/mvp/auth/refresh', { refreshToken });
          localStorage.setItem('accessToken', data.accessToken);
          // Retry original request
          return client(error.config);
        } catch {
          // Refresh failed, logout
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export default client;
```

#### 2. WebSocket Hook

```typescript
// hooks/useWebSocket.ts
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWebSocket = (matchId: string) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);

        // Subscribe to match updates
        client.subscribe(`/topic/match/${matchId}`, (message) => {
          const event = JSON.parse(message.body);
          handleEvent(event);
        });
      },
      onDisconnect: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => client.deactivate();
  }, [matchId]);

  const handleEvent = (event: any) => {
    // Handle different event types
    // Dispatch to relevant listeners
  };

  return { connected };
};
```

#### 3. Optimistic Updates

```typescript
// Example: Responding to match
const handleRespondYes = async () => {
  // Optimistically update UI
  setParticipantCount(prev => prev + 1);
  setUserResponse('YES');

  try {
    await api.respondToMatch(matchId, 'YES');
  } catch (error) {
    // Rollback on error
    setParticipantCount(prev => prev - 1);
    setUserResponse(null);
    showError(error.message);
  }
};
```

#### 4. Offline Support

```typescript
// Service Worker for offline support
// public/sw.js

const CACHE_NAME = 'playmatch-v1';
const OFFLINE_URL = '/offline.html';

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll([
        '/',
        '/offline.html',
        '/styles.css',
        '/app.js',
      ]);
    })
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request).catch(() => {
        return caches.match(OFFLINE_URL);
      })
    );
  }
});
```

---

## Success Metrics

### Phase 1 KPIs

| Metric | Current | Target (3 months) | Measurement |
|--------|---------|-------------------|-------------|
| Push Notification Opt-in Rate | 0% | 70% | % users who enable notifications |
| Chat Usage | 0% | 50% | % matches with ≥5 messages |
| Real-time Updates | N/A | <500ms | Avg latency for updates |
| Player Reputation Visibility | 0% | 90% | % users who view reputation before accepting |
| Backout Rate | Unknown | <10% | % players who back out after confirming |

### Phase 2 KPIs

| Metric | Target | Measurement |
|--------|--------|-------------|
| Onboarding Completion | 80% | % users who complete tutorial |
| Payment QR Code Usage | 60% | % matches using QR codes |
| Match Edit Usage | 20% | % matches edited post-creation |
| Reminder Effectiveness | 15% reduction | Drop in no-show rate |

### Phase 3 KPIs

| Metric | Target | Measurement |
|--------|--------|-------------|
| Photo Upload Rate | 30% | % matches with photos |
| Analytics Engagement | 40% | % users viewing dashboard weekly |
| Social Shares | 10% | % matches shared externally |
| Referral Rate | 25% | % users who invite friends |

---

## Implementation Timeline

### Phase 1: Critical Features (2-3 months)

**Month 1:**
- Week 1-2: Push Notifications (Backend + Frontend)
- Week 3-4: In-app Chat (Backend + Frontend)

**Month 2:**
- Week 1-2: Real-time Updates (WebSocket infrastructure)
- Week 3-4: Player Reputation System (Backend + Frontend)

**Month 3:**
- Testing, bug fixes, optimization
- User feedback collection
- Iteration

### Phase 2: Engagement Features (1-2 months)

**Month 4:**
- Onboarding Flow (Frontend only)
- Match Reminders (Backend scheduled tasks)
- Payment QR Codes (Backend + Frontend)

**Month 5:**
- Edit Match Details (Backend + Frontend)
- Testing and refinement

### Phase 3: Advanced Features (2-3 months)

**Month 6-8:**
- Match Photos (Backend + Storage + Frontend)
- Analytics Dashboard (Frontend only)
- Social Features (Backend + Frontend)

---

## Risk Mitigation

### Technical Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| WebSocket scaling issues | High | Start with polling, migrate gradually |
| Push notification delivery rate | High | Implement fallback SMS for critical notifications |
| Chat storage growth | Medium | Archive old messages, implement retention policy |
| Image storage costs | Medium | Compress images, limit uploads, use CDN |

### User Experience Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Notification fatigue | High | Granular preferences, smart batching |
| Complex UI | Medium | Extensive user testing, iterative design |
| Feature overload | Medium | Phased rollout, optional advanced features |
| Performance degradation | High | Lazy loading, code splitting, caching |

---

## Conclusion

This roadmap provides a comprehensive path to significantly enhance PlayMatch's user experience. By prioritizing critical features like push notifications, in-app chat, and reputation systems, we can address the most pressing user pain points while building toward a more engaging and trustworthy platform.

### Key Takeaways

1. **Backend Changes:** 19 new API endpoints, 6 new database tables, WebSocket infrastructure
2. **Frontend Work:** Complete rebuild with modern stack (React/Vue + TypeScript + TanStack Query)
3. **Timeline:** 6-8 months for full implementation across 3 phases
4. **Investment:** High initial effort, but significant long-term retention and engagement gains

### Next Steps

1. **Validate Assumptions:** Conduct user interviews to validate priority of features
2. **Technical Spike:** Prototype push notifications and WebSocket infrastructure
3. **Design Mockups:** Create high-fidelity designs for top 5 features
4. **Begin Phase 1:** Start with push notifications (highest impact)

---

**Document Version:** 1.0
**Last Updated:** January 23, 2026
**Status:** Ready for Review

For questions or feedback, please reach out to the product team.
