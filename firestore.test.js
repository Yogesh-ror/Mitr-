const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const MALLORY_UID = "mallory_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated rejection
test("Unauthenticated user cannot read or list posts, users, or conversations", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("conversations").get());
});

// 2. UserProfile creation, update & Shadow Update rejection
test("Authenticated user can create own profile and fails on shadow field injection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_lumina",
      displayName: "Alice Rivera",
      bio: "Digital creator",
      website: "https://lumina.example",
      avatarUrl: "",
      isPrivate: false,
      postCount: 0,
      followerCount: 0,
      followingCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Shadow update with ghost field 'isAdmin' must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Admin",
      isAdmin: true,
      updatedAt: new Date(),
    })
  );
});

// 3. PII & Private Settings Isolation
test("UserSettings are strictly isolated to the account owner", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("user_settings").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      isPrivateAccount: true,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("user_settings").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("user_settings").doc(ALICE_UID).get());
});

// 4. Post creation, identity spoofing prevention, and public list alignment
test("User can create own post, cannot spoof authorId, and can query PUBLIC posts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      authorId: ALICE_UID,
      authorUsername: "alice",
      authorDisplayName: "Alice",
      caption: "First Lumina moment #lumina",
      mediaUrls: ["https://images.unsplash.com/photo-1"],
      mediaType: "IMAGE",
      visibility: "PUBLIC",
      commentsAllowed: true,
      likeCount: 0,
      commentCount: 0,
      likedByUids: [],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot spoof Alice's authorId
  await assertFails(
    bobDb.collection("posts").doc("post_2").set({
      authorId: ALICE_UID,
      authorUsername: "alice",
      authorDisplayName: "Alice",
      caption: "Spoofed post",
      mediaUrls: ["https://images.unsplash.com/photo-1"],
      mediaType: "IMAGE",
      visibility: "PUBLIC",
      commentsAllowed: true,
      likeCount: 0,
      commentCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob can query PUBLIC posts
  await assertSucceeds(
    bobDb.collection("posts").where("visibility", "==", "PUBLIC").get()
  );

  // Bob cannot delete Alice's post
  await assertFails(bobDb.collection("posts").doc("post_1").delete());
});

// 5. Close-Friends Story Visibility Enforcement
test("Close-friends story is only readable by author and allowedViewerIds", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("stories").doc("story_cf_1").set({
      authorId: ALICE_UID,
      authorUsername: "alice",
      authorDisplayName: "Alice",
      mediaUrl: "https://images.unsplash.com/photo-2",
      mediaType: "IMAGE",
      caption: "Close friends only",
      audience: "CLOSE_FRIENDS",
      allowedViewerIds: [BOB_UID],
      viewerIds: [],
      createdAt: now,
      expiresAtMillis: Date.now() + 86400000,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const malloryDb = testEnv.authenticatedContext(MALLORY_UID).firestore();

  await assertSucceeds(bobDb.collection("stories").doc("story_cf_1").get());
  await assertFails(malloryDb.collection("stories").doc("story_cf_1").get());
});

// 6. Direct Messaging Participant Isolation
test("Direct messages are only accessible to conversation participants", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const malloryDb = testEnv.authenticatedContext(MALLORY_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_ab").set({
      participantIds: [ALICE_UID, BOB_UID],
      participantUsernames: ["alice", "bob"],
      initiatorId: ALICE_UID,
      lastMessageText: "Hello Bob!",
      lastSenderId: ALICE_UID,
      status: "ACTIVE",
      unreadCount: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_ab").collection("messages").doc("msg_1").set({
      conversationId: "conv_ab",
      participantIds: [ALICE_UID, BOB_UID],
      senderId: ALICE_UID,
      senderUsername: "alice",
      text: "Hello Bob!",
      mediaType: "NONE",
      deliveryStatus: "SENT",
      createdAt: now,
    })
  );

  // Bob can read the conversation and messages
  await assertSucceeds(bobDb.collection("conversations").doc("conv_ab").get());
  await assertSucceeds(
    bobDb
      .collection("conversations")
      .doc("conv_ab")
      .collection("messages")
      .where("participantIds", "array-contains", BOB_UID)
      .get()
  );

  // Mallory is rejected from reading conversation or messages
  await assertFails(malloryDb.collection("conversations").doc("conv_ab").get());
  await assertFails(
    malloryDb.collection("conversations").doc("conv_ab").collection("messages").doc("msg_1").get()
  );
});

// 7. Saved Posts & User Relations Cross-User Isolation
test("SavedPosts and UserRelations enforce strict owner isolation on list queries", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("saved_posts").doc("saved_1").set({
      userId: ALICE_UID,
      postId: "post_1",
      targetType: "POST",
      authorUsername: "alice",
      captionPreview: "Saved item",
      createdAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("saved_posts").where("userId", "==", ALICE_UID).get()
  );
  // Query without userId filter fails
  await assertFails(aliceDb.collection("saved_posts").get());
  // Bob querying Alice's saved posts fails
  await assertFails(
    bobDb.collection("saved_posts").where("userId", "==", ALICE_UID).get()
  );
});

// 8. Saved Music & Trimmed Music Metadata on Posts/Stories/Reels
test("SavedMusic enforces strict owner isolation and Posts/Stories/Reels accept trimmed music metadata", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("saved_music").doc("saved_music_1").set({
      userId: ALICE_UID,
      trackId: "itunes_998877",
      title: "Midnight Horizon",
      artist: "Aurora Synth",
      coverUrl: "https://is1-ssl.mzstatic.com/image/thumb/Music/cover.jpg",
      audioUrl: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview.m4a",
      durationMs: 30000,
      trimStartMs: 4000,
      trimEndMs: 19000,
      createdAt: now,
    })
  );

  // Alice can query her own saved music
  await assertSucceeds(
    aliceDb.collection("saved_music").where("userId", "==", ALICE_UID).get()
  );

  // Bob cannot read or query Alice's saved music
  await assertFails(bobDb.collection("saved_music").doc("saved_music_1").get());
  await assertFails(
    bobDb.collection("saved_music").where("userId", "==", ALICE_UID).get()
  );

  // Alice can publish a post with trimmed music metadata
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_with_music").set({
      authorId: ALICE_UID,
      authorUsername: "alice",
      authorDisplayName: "Alice",
      caption: "Sunset reel vibe #music",
      mediaUrls: ["content://com.example/cache/studio_capture_1.jpg"],
      mediaType: "IMAGE",
      visibility: "PUBLIC",
      commentsAllowed: true,
      likeCount: 0,
      commentCount: 0,
      likedByUids: [],
      musicTrackId: "itunes_998877",
      musicTitle: "Midnight Horizon",
      musicArtist: "Aurora Synth",
      musicCoverUrl: "https://is1-ssl.mzstatic.com/image/thumb/Music/cover.jpg",
      musicUrl: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview.m4a",
      musicStartMs: 4000,
      musicEndMs: 19000,
      createdAt: now,
      updatedAt: now,
    })
  );
});

