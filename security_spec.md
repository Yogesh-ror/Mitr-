# Lumina Social Network — Security Specification (Phase 0)

## 1. Data Invariants
1. **Authentication Gate**: All read (`get`, `list`) and write (`create`, `update`, `delete`) operations across every collection require `request.auth != null`.
2. **User Profile & Username Uniqueness**:
   - A user profile `/users/{userId}` can only be created or updated by `request.auth.uid == userId`.
   - `/usernames/{username}` enforces global username reservation and uniqueness where `userId == request.auth.uid`.
   - Private user settings `/user_settings/{userId}` are strictly readable and writable only by `request.auth.uid == userId` (PII & privacy isolation).
3. **Posts & Reels Visibility**:
   - Every `/posts/{postId}` and `/reels/{reelId}` document must have `authorId == request.auth.uid` on creation and immutable `authorId` and `createdAt` on update.
   - List queries must filter by `visibility == 'PUBLIC'` or `authorId == request.auth.uid`.
4. **Stories & 24h / Close-Friends Enforcement**:
   - `/stories/{storyId}` can only be created by `authorId == request.auth.uid`.
   - Stories with `audience == 'PUBLIC'` can be read by authenticated users; stories with `audience == 'CLOSE_FRIENDS'` require `request.auth.uid == resource.data.authorId` or `request.auth.uid in resource.data.allowedViewerIds`.
5. **Direct Messaging Membership Isolation**:
   - `/conversations/{conversationId}` and `/conversations/{conversationId}/messages/{messageId}` require `request.auth.uid in resource.data.participantIds` (for reads/updates) and `request.auth.uid in request.resource.data.participantIds` (for creates). Users outside `participantIds` can never read or modify messages or conversations.
6. **Interaction Collections (Follows, Likes, Saved, Activity, Reports, Close Friends, Blocked, Restricted)**:
   - Every interaction record requires the acting user field (`userId`, `followerId`, `ownerId`, `reporterId`) to equal `request.auth.uid`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Feed Scrape**: Calling `list` on `/posts` with `request.auth == null`. -> Rejected by `isSignedIn()`.
2. **Identity Spoofing on Post Creation**: Authenticated as `alice`, creating `/posts/post_1` with `authorId: "bob"`. -> Rejected by `incoming().authorId == request.auth.uid`.
3. **Shadow Update / Ghost Field Injection**: Updating `/users/alice` with `{"displayName": "Alice", "isVerifiedAdmin": true}`. -> Rejected by `affectedKeys().hasOnly(...)` and `isValidUserProfile()`.
4. **Cross-User Private Settings Read (PII Leak)**: `alice` attempting `get` on `/user_settings/bob`. -> Rejected by `request.auth.uid == userId`.
5. **Unauthorized Conversation Snooping**: `mallory` attempting `list` or `get` on `/conversations/conv_1/messages` where `participantIds` is `["alice", "bob"]`. -> Rejected by `request.auth.uid in resource.data.participantIds`.
6. **Close-Friends Story Bypass**: `mallory` reading a story where `audience == 'CLOSE_FRIENDS'` and `allowedViewerIds` does not include `mallory`. -> Rejected by audience check.
7. **Timestamp Forgery (Future Timestamp)**: Creating a post with `createdAt` set 10 hours in the future. -> Rejected by `incoming().createdAt <= request.time`.
8. **Immutable Field Mutation**: Updating a post's `authorId` or `createdAt` after creation. -> Rejected by `incoming().authorId == existing().authorId && incoming().createdAt == existing().createdAt`.
9. **Value Poisoning (Oversized Bio/Caption)**: Submitting a 50,000-character string in `caption` or `bio`. -> Rejected by `.size() <= 2200` / `.size() <= 300`.
10. **Path ID Poisoning**: Creating a document with an invalid or 500-character ID containing special characters. -> Rejected by `isValidId()`.
11. **Unfiltered Private Collection Query**: Calling `db.collection("saved_posts").get()` without `.where("userId", "==", uid)`. -> Rejected because `allow list` enforces `resource.data.userId == request.auth.uid`.
12. **Cross-User Post Deletion**: `bob` attempting to delete `/posts/alice_post`. -> Rejected by `resource.data.authorId == request.auth.uid`.
